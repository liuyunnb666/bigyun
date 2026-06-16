package com.bigyun.auth.service;

import com.bigyun.common.core.constant.Constants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.ip.IpUtils;
import com.bigyun.common.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthRateLimitService
{
    private static final Logger LOG = LoggerFactory.getLogger(AuthRateLimitService.class);
    private static final String MESSAGE_TOO_FREQUENT = "操作过于频繁，请稍后再试";
    private static final String KEY_PREFIX = "auth:rl:";
    private static final int LOGIN_LIMIT = 10;
    private static final long LOGIN_WINDOW_SECONDS = 5 * 60L;
    private static final int ACCOUNT_LOGIN_LIMIT = 30;
    private static final long ACCOUNT_LOGIN_WINDOW_SECONDS = 30 * 60L;
    private static final int REGISTER_LIMIT = 3;
    private static final long REGISTER_WINDOW_SECONDS = 10 * 60L;
    private static final int FACE_LOGIN_LIMIT = 5;
    private static final long FACE_LOGIN_WINDOW_SECONDS = 5 * 60L;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysRecordLogService recordLogService;

    public void checkLogin(String account)
    {
        enforce("login", account, LOGIN_LIMIT, LOGIN_WINDOW_SECONDS, false, true);
        enforce("login-account", account, ACCOUNT_LOGIN_LIMIT, ACCOUNT_LOGIN_WINDOW_SECONDS, false, false);
    }

    public void checkRegister(String account)
    {
        enforce("register", account, REGISTER_LIMIT, REGISTER_WINDOW_SECONDS, true, true);
    }

    public void checkFaceLogin(String account, String scene)
    {
        String safeScene = normalize(scene);
        enforce("face:" + safeScene, account, FACE_LOGIN_LIMIT, FACE_LOGIN_WINDOW_SECONDS, true, true);
    }

    private void enforce(String action, String account, int limit, long windowSeconds, boolean failClosed, boolean includeIp)
    {
        String safeAccount = normalize(account);
        String ip = normalize(IpUtils.getIpAddr());
        String key = KEY_PREFIX + action + ":" + (includeIp ? ip + ":" : "") + safeAccount;
        try
        {
            Long count = redisService.redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L)
            {
                redisService.expire(key, windowSeconds, TimeUnit.SECONDS);
            }
            if (count != null && count > limit)
            {
                recordLimited(action, safeAccount);
                throw new ServiceException(MESSAGE_TOO_FREQUENT);
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            LOG.warn("Auth rate limit failed, action={}, account={}", action, safeAccount, e);
            if (failClosed)
            {
                throw new ServiceException(MESSAGE_TOO_FREQUENT);
            }
        }
    }

    private void recordLimited(String action, String account)
    {
        try
        {
            recordLogService.recordLogininfor(account, Constants.LOGIN_FAIL, "rate limited: " + action);
        }
        catch (RuntimeException e)
        {
            LOG.warn("Record auth rate limit log failed, action={}, account={}", action, account, e);
        }
    }

    private String normalize(String value)
    {
        String safeValue = StringUtils.defaultIfBlank(value, "unknown").trim().toLowerCase();
        return safeValue.length() > 128 ? safeValue.substring(0, 128) : safeValue;
    }
}
