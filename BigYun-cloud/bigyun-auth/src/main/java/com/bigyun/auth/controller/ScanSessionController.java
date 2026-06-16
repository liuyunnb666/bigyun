package com.bigyun.auth.controller;

import com.bigyun.auth.constant.ScanLoginConstants;
import com.bigyun.auth.domain.ScanGrantPayload;
import com.bigyun.auth.domain.ScanSessionCache;
import com.bigyun.auth.domain.vo.ScanConfirmResponse;
import com.bigyun.auth.domain.vo.ScanCreateResponse;
import com.bigyun.auth.domain.vo.ScanResolveResponse;
import com.bigyun.auth.domain.vo.ScanStatusResponse;
import com.bigyun.auth.form.ScanConfirmRequest;
import com.bigyun.auth.form.ScanResolveRequest;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.uuid.IdUtils;
import com.bigyun.common.redis.service.RedisService;
import com.bigyun.common.security.annotation.RequiresLogin;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.system.domain.model.LoginUser;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web 扫码登录会话控制器。
 *
 * <p>create/status 由未登录的 Web 登录页调用；resolve/confirm 必须由已登录的移动端或门户端调用，
 * 确认人身份只能从当前 token 获取，不能信任前端请求体传入的用户信息。</p>
 */
@RestController
@RequestMapping("/scan")
public class ScanSessionController
{
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private RedisService redisService;

    /**
     * 创建 Web 登录页二维码会话。
     */
    @PostMapping("/create")
    public R<ScanCreateResponse> create()
    {
        String sid = IdUtils.fastSimpleUUID();
        String loginCode = generateLoginCode();
        long nowMillis = Instant.now().toEpochMilli();
        long expireAtMillis = nowMillis + ScanLoginConstants.SESSION_TTL_SECONDS * 1000;

        ScanSessionCache session = new ScanSessionCache();
        session.setSid(sid);
        session.setLoginCode(loginCode);
        session.setStatus(ScanLoginConstants.STATUS_CREATED);
        session.setCreatedAt(nowMillis);
        session.setExpireAt(expireAtMillis);

        redisService.setCacheObject(buildSessionKey(sid), session,
                ScanLoginConstants.SESSION_TTL_SECONDS, TimeUnit.SECONDS);
        redisService.setCacheObject(buildCodeKey(loginCode), sid,
                ScanLoginConstants.SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        ScanCreateResponse response = new ScanCreateResponse();
        response.setSid(sid);
        response.setLoginCode(loginCode);
        response.setStatus(ScanLoginConstants.STATUS_CREATED);
        response.setExpiresIn(ScanLoginConstants.SESSION_TTL_SECONDS);
        response.setExpireAt(expireAtMillis);
        return R.ok(response);
    }

    /**
     * Web 登录页轮询二维码会话状态。
     */
    @GetMapping("/status")
    public R<ScanStatusResponse> status(@RequestParam("sid") String sid)
    {
        if (StringUtils.isEmpty(sid))
        {
            throw new ServiceException("sid 不能为空");
        }
        ScanSessionCache session = getSessionOrThrow(sid);

        ScanStatusResponse response = new ScanStatusResponse();
        response.setSid(sid);
        response.setStatus(session.getStatus());
        response.setGrantCode(session.getGrantCode());
        response.setExpiresIn(Math.max(redisService.getExpire(buildSessionKey(sid)), 0L));
        response.setExpireAt(session.getExpireAt());
        return R.ok(response);
    }

    /**
     * 已登录端扫描二维码后解析会话，将状态从 CREATED 推进到 RESOLVED。
     */
    @PostMapping("/resolve")
    @RequiresLogin
    public R<ScanResolveResponse> resolve(@Valid @RequestBody ScanResolveRequest req)
    {
        ensureAuthenticatedUser();
        String loginCode = req == null ? null : req.getLoginCode();
        if (StringUtils.isEmpty(loginCode))
        {
            throw new ServiceException("loginCode 不能为空");
        }

        String sid = redisService.getCacheObject(buildCodeKey(loginCode));
        if (StringUtils.isEmpty(sid))
        {
            throw new ServiceException("扫码会话不存在或已过期");
        }

        if (StringUtils.isNotEmpty(req.getSid()) && !StringUtils.equals(req.getSid(), sid))
        {
            throw new ServiceException("会话与 loginCode 不匹配");
        }

        ScanSessionCache session = getSessionOrThrow(sid);
        ensureState(session.getStatus(), ScanLoginConstants.STATUS_CREATED,
                ScanLoginConstants.STATUS_RESOLVED, ScanLoginConstants.STATUS_CONFIRMED);

        if (Objects.equals(session.getStatus(), ScanLoginConstants.STATUS_CREATED))
        {
            session.setStatus(ScanLoginConstants.STATUS_RESOLVED);
            refreshSession(sid, session);
        }

        ScanResolveResponse response = new ScanResolveResponse();
        response.setSid(sid);
        response.setStatus(session.getStatus());
        response.setExpireAt(session.getExpireAt());
        return R.ok(response);
    }

    /**
     * 已登录端确认 Web 扫码登录，生成一次性 grantCode。
     */
    @PostMapping("/confirm")
    @RequiresLogin
    public R<ScanConfirmResponse> confirm(@Valid @RequestBody ScanConfirmRequest req)
    {
        String sid = req == null ? null : req.getSid();
        String loginCode = req == null ? null : req.getLoginCode();
        Long currentUserId = ensureAuthenticatedUser();

        if (StringUtils.isAnyBlank(sid, loginCode))
        {
            throw new ServiceException("sid/loginCode 不能为空");
        }

        String cacheSid = redisService.getCacheObject(buildCodeKey(loginCode));
        if (!StringUtils.equals(sid, cacheSid))
        {
            throw new ServiceException("会话与 loginCode 不匹配");
        }

        String lockKey = buildLockKey(sid);
        boolean locked = tryAcquireLock(lockKey);
        if (!locked)
        {
            throw new ServiceException("会话确认中，请稍后重试");
        }

        try
        {
            ScanSessionCache session = getSessionOrThrow(sid);
            ensureState(session.getStatus(), ScanLoginConstants.STATUS_RESOLVED);

            String grantCode = IdUtils.fastSimpleUUID();
            session.setStatus(ScanLoginConstants.STATUS_CONFIRMED);
            session.setConfirmedUserId(currentUserId);
            session.setGrantCode(grantCode);
            session.setConfirmedAt(Instant.now().toEpochMilli());
            refreshSession(sid, session);

            ScanGrantPayload grantPayload = new ScanGrantPayload();
            grantPayload.setSid(sid);
            grantPayload.setUserId(currentUserId);
            grantPayload.setGrantCode(grantCode);
            redisService.setCacheObject(buildGrantKey(grantCode), grantPayload,
                    ScanLoginConstants.GRANT_TTL_SECONDS, TimeUnit.SECONDS);
            redisService.deleteObject(buildCodeKey(loginCode));

            ScanConfirmResponse response = new ScanConfirmResponse();
            response.setSid(sid);
            response.setStatus(ScanLoginConstants.STATUS_CONFIRMED);
            response.setGrantCode(grantCode);
            return R.ok(response);
        }
        finally
        {
            redisService.deleteObject(lockKey);
        }
    }

    private String generateLoginCode()
    {
        for (int i = 0; i < 5; i++)
        {
            String loginCode = String.format("%06d", RANDOM.nextInt(1_000_000));
            if (Boolean.FALSE.equals(redisService.hasKey(buildCodeKey(loginCode))))
            {
                return loginCode;
            }
        }
        throw new ServiceException("生成登录码失败，请重试");
    }

    private boolean tryAcquireLock(String lockKey)
    {
        @SuppressWarnings("unchecked")
        Boolean success = redisService.redisTemplate.opsForValue().setIfAbsent(lockKey, "1",
                ScanLoginConstants.LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 获取当前扫码确认人的登录用户 ID。
     */
    private Long ensureAuthenticatedUser()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null)
            {
                userId = loginUser.getUserid();
                if (userId == null && loginUser.getSysUser() != null)
                {
                    userId = loginUser.getSysUser().getUserId();
                }
            }
        }
        if (userId == null)
        {
            throw new ServiceException("请先登录后再扫码确认");
        }
        return userId;
    }

    private ScanSessionCache getSessionOrThrow(String sid)
    {
        ScanSessionCache session = redisService.getCacheObject(buildSessionKey(sid));
        if (session == null)
        {
            throw new ServiceException("扫码会话不存在或已过期");
        }
        return session;
    }

    private void refreshSession(String sid, ScanSessionCache session)
    {
        long ttlSeconds = Math.max(redisService.getExpire(buildSessionKey(sid)), 1L);
        redisService.setCacheObject(buildSessionKey(sid), session, ttlSeconds, TimeUnit.SECONDS);
    }

    private void ensureState(String current, String... allowed)
    {
        for (String candidate : allowed)
        {
            if (StringUtils.equals(current, candidate))
            {
                return;
            }
        }
        throw new ServiceException("非法状态流转: " + current);
    }

    private String buildSessionKey(String sid)
    {
        return ScanLoginConstants.SCAN_SESSION_KEY_PREFIX + sid;
    }

    private String buildCodeKey(String loginCode)
    {
        return ScanLoginConstants.SCAN_CODE_KEY_PREFIX + loginCode;
    }

    private String buildLockKey(String sid)
    {
        return ScanLoginConstants.SCAN_LOCK_KEY_PREFIX + sid;
    }

    private String buildGrantKey(String grantCode)
    {
        return ScanLoginConstants.SCAN_GRANT_KEY_PREFIX + grantCode;
    }
}
