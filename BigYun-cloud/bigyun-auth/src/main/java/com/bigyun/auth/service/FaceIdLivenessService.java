package com.bigyun.auth.service;

import com.bigyun.auth.constant.FaceLoginConstants;
import com.bigyun.auth.domain.FaceIdProviderSettings;
import com.bigyun.auth.domain.FaceIdResultResponse;
import com.bigyun.auth.domain.FaceIdTokenResponse;
import com.bigyun.auth.domain.FaceLivenessSessionCache;
import com.bigyun.auth.domain.FaceLivenessSessionRequest;
import com.bigyun.auth.domain.FaceLivenessSessionResponse;
import com.bigyun.auth.domain.FaceLivenessStatusResponse;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.uuid.IdUtils;
import com.bigyun.common.redis.service.RedisService;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.remote.RemoteUserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * FaceID H5 活体会话服务。
 *
 * <p>会话状态只写 Redis，auth 服务不引入数据库依赖；活体通过后再用 FaceID 返回的
 * image_best 临时做 Face++ detect/compare，最终只保存 face_token。</p>
 */
@Service
public class FaceIdLivenessService
{
    @Autowired
    private RedisService redisService;

    @Autowired
    private FaceProviderService faceProviderService;

    @Autowired
    private IUserFaceService userFaceService;

    @Autowired
    private RemoteUserService remoteUserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public FaceLivenessSessionResponse createEnrollSession(Long userId, FaceLivenessSessionRequest request)
    {
        if (userId == null)
        {
            throw new ServiceException("未获取到当前登录用户");
        }
        FaceLivenessSessionCache session = createBaseSession(FaceLoginConstants.LIVENESS_SCENE_ENROLL, request);
        session.setUserId(userId);
        return createFaceIdSession(session);
    }

    public FaceLivenessSessionResponse createLoginSession(FaceLivenessSessionRequest request)
    {
        validateAccountSelector(request);
        LoginUser loginUser = resolveLoginUser(request);
        Long userId = loginUser.getSysUser().getUserId();
        userFaceService.checkLocked(userId);
        if (StringUtils.isBlank(userFaceService.getFaceTokenByUserId(userId)))
        {
            throw new ServiceException("用户未录入人脸信息，请先登录后录入人脸");
        }

        FaceLivenessSessionCache session = createBaseSession(FaceLoginConstants.LIVENESS_SCENE_LOGIN, request);
        session.setUserId(userId);
        session.setUserName(trim(request.getUserName()));
        session.setPhone(trim(request.getPhone()));
        session.setEmail(trim(request.getEmail()));
        return createFaceIdSession(session);
    }

    public FaceLivenessStatusResponse getStatus(String sessionId, String nonce)
    {
        FaceLivenessSessionCache session = getSessionOrThrow(sessionId, nonce);
        refreshResultIfPossible(session, false);
        return toStatusResponse(session);
    }

    public String completeAndGetBestImage(String sessionId, String nonce, String expectedScene, Long expectedUserId)
    {
        FaceLivenessSessionCache session = getSessionOrThrow(sessionId, nonce);
        if (!StringUtils.equals(expectedScene, session.getScene()))
        {
            throw new ServiceException("活体会话场景不匹配");
        }
        if (expectedUserId != null && !expectedUserId.equals(session.getUserId()))
        {
            throw new ServiceException("活体会话不属于当前登录用户");
        }

        FaceIdResultResponse result = fetchLatestResult(session, true);
        applyResult(session, result);
        if (!isPass(session))
        {
            throw new ServiceException(StringUtils.defaultIfBlank(session.getErrorMessage(), "活体检测未通过"));
        }
        if (StringUtils.isBlank(result.getImageBest()))
        {
            throw new ServiceException("FaceID 未返回可用于比对的人脸图片");
        }
        return result.getImageBest();
    }

    public FaceLivenessSessionCache getVerifiedSession(String sessionId, String nonce, String expectedScene)
    {
        FaceLivenessSessionCache session = getSessionOrThrow(sessionId, nonce);
        if (!StringUtils.equals(expectedScene, session.getScene()))
        {
            throw new ServiceException("活体会话场景不匹配");
        }
        return session;
    }

    public void markConsumed(String sessionId, String nonce)
    {
        FaceLivenessSessionCache session = getSessionOrThrow(sessionId, nonce);
        session.setStatus(FaceLoginConstants.LIVENESS_STATUS_CONSUMED);
        refreshSession(session);
    }

    public void handleNotify(String data, String sign)
    {
        if (StringUtils.isAnyBlank(data, sign))
        {
            throw new ServiceException("FaceID 回调缺少 data 或 sign");
        }
        String expected = sha1(faceProviderService.getFaceIdSecretKey() + data);
        if (!StringUtils.equalsIgnoreCase(expected, sign))
        {
            throw new ServiceException("FaceID 回调签名校验失败");
        }

        String sessionId = parseCallbackValue(data, "biz_no");
        String bizId = parseCallbackValue(data, "biz_id");
        if (StringUtils.isBlank(sessionId) && StringUtils.isNotBlank(bizId))
        {
            sessionId = redisService.getCacheObject(buildBizKey(bizId));
        }
        if (StringUtils.isBlank(sessionId))
        {
            throw new ServiceException("FaceID 回调未匹配到本地会话");
        }

        FaceLivenessSessionCache session = redisService.getCacheObject(buildSessionKey(sessionId));
        if (session == null)
        {
            throw new ServiceException("FaceID 活体会话不存在或已过期");
        }
        refreshResultIfPossible(session, true);
    }

    public String buildReturnRedirect(String sessionId, String nonce, String bizId, String result)
    {
        FaceLivenessSessionCache session = getSessionOrThrow(sessionId, nonce);
        String target = StringUtils.defaultIfBlank(session.getClientReturnUrl(), faceProviderService.getFaceIdSettings().getReturnUrl());
        if (StringUtils.isBlank(target))
        {
            return null;
        }
        return appendQuery(target, "sessionId", session.getSessionId(), "nonce", session.getNonce(),
                "scene", session.getScene(), "bizId", StringUtils.defaultIfBlank(bizId, session.getFaceIdBizId()),
                "result", StringUtils.defaultIfBlank(result, session.getStatus()));
    }

    private FaceLivenessSessionCache createBaseSession(String scene, FaceLivenessSessionRequest request)
    {
        long now = Instant.now().toEpochMilli();
        FaceLivenessSessionCache session = new FaceLivenessSessionCache();
        session.setSessionId(IdUtils.fastSimpleUUID());
        session.setNonce(IdUtils.fastSimpleUUID());
        session.setScene(scene);
        session.setStatus(FaceLoginConstants.LIVENESS_STATUS_CREATED);
        session.setClientType(request == null ? null : trim(request.getClientType()));
        session.setFaceLoginMode(request == null ? null : trim(request.getFaceLoginMode()));
        session.setClientReturnUrl(request == null ? null : trim(request.getReturnUrl()));
        session.setCreatedAt(now);
        session.setExpireAt(now + FaceLoginConstants.LIVENESS_SESSION_TTL_SECONDS * 1000);
        return session;
    }

    private FaceLivenessSessionResponse createFaceIdSession(FaceLivenessSessionCache session)
    {
        FaceIdProviderSettings settings = faceProviderService.getFaceIdSettings();
        String returnUrl = buildClientReturnUrl(session, settings);
        FaceIdTokenResponse tokenResponse = faceProviderService.createFaceIdH5Token(
                session.getSessionId(), session.getNonce(), returnUrl);

        session.setFaceIdToken(tokenResponse.getToken());
        session.setFaceIdBizId(tokenResponse.getBizId());
        session.setVerifyUrl(tokenResponse.getVerifyUrl());
        session.setStatus(FaceLoginConstants.LIVENESS_STATUS_PROCESSING);
        saveSession(session);
        if (StringUtils.isNotBlank(session.getFaceIdBizId()))
        {
            redisService.setCacheObject(buildBizKey(session.getFaceIdBizId()), session.getSessionId(),
                    FaceLoginConstants.LIVENESS_SESSION_TTL_SECONDS, TimeUnit.SECONDS);
        }

        FaceLivenessSessionResponse response = new FaceLivenessSessionResponse();
        response.setSessionId(session.getSessionId());
        response.setNonce(session.getNonce());
        response.setScene(session.getScene());
        response.setStatus(session.getStatus());
        response.setBizId(session.getFaceIdBizId());
        response.setVerifyUrl(session.getVerifyUrl());
        response.setExpiresIn(FaceLoginConstants.LIVENESS_SESSION_TTL_SECONDS);
        return response;
    }

    private String buildClientReturnUrl(FaceLivenessSessionCache session, FaceIdProviderSettings settings)
    {
        String base = StringUtils.defaultIfBlank(session.getClientReturnUrl(), settings.getReturnUrl());
        if (StringUtils.isBlank(base))
        {
            throw new ServiceException("请配置 FaceID H5 returnUrl，或由前端传入 returnUrl");
        }
        return appendQuery(base, "sessionId", session.getSessionId(), "nonce", session.getNonce(),
                "scene", session.getScene());
    }

    private void refreshResultIfPossible(FaceLivenessSessionCache session, boolean force)
    {
        if (session == null || StringUtils.isBlank(session.getFaceIdBizId()))
        {
            return;
        }
        if (!force && (isPass(session) || FaceLoginConstants.LIVENESS_STATUS_FAIL.equals(session.getStatus())
                || FaceLoginConstants.LIVENESS_STATUS_CONSUMED.equals(session.getStatus())))
        {
            return;
        }
        try
        {
            applyResult(session, fetchLatestResult(session, force));
        }
        catch (ServiceException e)
        {
            // 轮询阶段 FaceID 可能尚未生成结果，这里保留处理中状态，不影响普通登录链路。
            if (force)
            {
                throw e;
            }
            session.setErrorMessage("活体结果尚未返回，请稍后刷新");
            refreshSession(session);
        }
    }

    private FaceIdResultResponse fetchLatestResult(FaceLivenessSessionCache session, boolean required)
    {
        if (StringUtils.isBlank(session.getFaceIdBizId()))
        {
            throw new ServiceException("FaceID bizId 为空，无法查询活体结果");
        }
        try
        {
            return faceProviderService.getFaceIdH5Result(session.getFaceIdBizId());
        }
        catch (ServiceException e)
        {
            if (required)
            {
                throw e;
            }
            throw e;
        }
    }

    private void applyResult(FaceLivenessSessionCache session, FaceIdResultResponse result)
    {
        if (result == null)
        {
            return;
        }
        String faceIdStatus = result.getResult();
        String livenessResult = result.getLivenessResult();
        if (StringUtils.isBlank(faceIdStatus) && StringUtils.isBlank(livenessResult))
        {
            session.setStatus(FaceLoginConstants.LIVENESS_STATUS_PROCESSING);
            refreshSession(session);
            return;
        }

        // FaceID 顶层 status=OK 只代表结果可查询，必须同时要求 liveness_result.result=PASS 才算活体通过。
        if (StringUtils.isNotBlank(faceIdStatus) && !isPassText(faceIdStatus))
        {
            session.setStatus(FaceLoginConstants.LIVENESS_STATUS_FAIL);
            session.setErrorMessage(StringUtils.defaultIfBlank(result.getErrorMessage(), "FaceID 活体认证未通过"));
            session.setCompletedAt(Instant.now().toEpochMilli());
            refreshSession(session);
            return;
        }

        if (StringUtils.isBlank(livenessResult))
        {
            session.setStatus(FaceLoginConstants.LIVENESS_STATUS_PROCESSING);
            session.setErrorMessage("FaceID 已返回结果，但尚未返回活体 PASS 信息");
            refreshSession(session);
            return;
        }

        session.setLivenessResult(livenessResult);
        if (isPassText(livenessResult))
        {
            session.setStatus(FaceLoginConstants.LIVENESS_STATUS_PASS);
            session.setErrorMessage(null);
            session.setCompletedAt(Instant.now().toEpochMilli());
        }
        else if (isFailText(livenessResult))
        {
            session.setStatus(FaceLoginConstants.LIVENESS_STATUS_FAIL);
            session.setErrorMessage(StringUtils.defaultIfBlank(result.getErrorMessage(), "活体检测未通过"));
            session.setCompletedAt(Instant.now().toEpochMilli());
        }
        else
        {
            session.setStatus(FaceLoginConstants.LIVENESS_STATUS_PROCESSING);
        }
        refreshSession(session);
    }

    private FaceLivenessSessionCache getSessionOrThrow(String sessionId, String nonce)
    {
        if (StringUtils.isAnyBlank(sessionId, nonce))
        {
            throw new ServiceException("活体会话参数不能为空");
        }
        FaceLivenessSessionCache session = redisService.getCacheObject(buildSessionKey(sessionId));
        if (session == null)
        {
            throw new ServiceException("活体会话不存在或已过期");
        }
        if (!StringUtils.equals(session.getNonce(), nonce))
        {
            throw new ServiceException("活体会话校验失败");
        }
        return session;
    }

    private FaceLivenessStatusResponse toStatusResponse(FaceLivenessSessionCache session)
    {
        FaceLivenessStatusResponse response = new FaceLivenessStatusResponse();
        response.setSessionId(session.getSessionId());
        response.setScene(session.getScene());
        response.setStatus(session.getStatus());
        response.setLivenessResult(session.getLivenessResult());
        response.setBizId(session.getFaceIdBizId());
        response.setMessage(session.getErrorMessage());
        response.setCanComplete(isPass(session));
        response.setExpiresIn(Math.max(redisService.getExpire(buildSessionKey(session.getSessionId())), 0L));
        return response;
    }

    private void validateAccountSelector(FaceLivenessSessionRequest request)
    {
        if (request == null)
        {
            throw new ServiceException("活体登录请求不能为空");
        }
        int count = 0;
        if (StringUtils.isNotBlank(request.getUserName()))
        {
            count++;
        }
        if (StringUtils.isNotBlank(request.getPhone()))
        {
            count++;
        }
        if (StringUtils.isNotBlank(request.getEmail()))
        {
            count++;
        }
        if (count != 1)
        {
            throw new ServiceException("账号、手机号、邮箱必须填写且只能填写一个");
        }
    }

    private LoginUser resolveLoginUser(FaceLivenessSessionRequest request)
    {
        R<LoginUser> userResult;
        if (StringUtils.isNotBlank(request.getUserName()))
        {
            userResult = remoteUserService.getUserInfo(request.getUserName().trim(), SecurityConstants.INNER);
        }
        else if (StringUtils.isNotBlank(request.getPhone()))
        {
            userResult = remoteUserService.getUserInfoByPhone(request.getPhone().trim(), SecurityConstants.INNER);
        }
        else
        {
            userResult = remoteUserService.getUserInfoByEmail(request.getEmail().trim(), SecurityConstants.INNER);
        }
        if (userResult == null || R.isError(userResult) || userResult.getData() == null
                || userResult.getData().getSysUser() == null || userResult.getData().getSysUser().getUserId() == null)
        {
            throw new ServiceException(userResult == null ? "用户查询失败" : userResult.getMsg());
        }
        return userResult.getData();
    }

    private void saveSession(FaceLivenessSessionCache session)
    {
        redisService.setCacheObject(buildSessionKey(session.getSessionId()), session,
                FaceLoginConstants.LIVENESS_SESSION_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void refreshSession(FaceLivenessSessionCache session)
    {
        long ttlSeconds = Math.max(redisService.getExpire(buildSessionKey(session.getSessionId())), 1L);
        redisService.setCacheObject(buildSessionKey(session.getSessionId()), session, ttlSeconds, TimeUnit.SECONDS);
    }

    private boolean isPass(FaceLivenessSessionCache session)
    {
        return session != null && FaceLoginConstants.LIVENESS_STATUS_PASS.equals(session.getStatus());
    }

    private boolean isPassText(String value)
    {
        String text = normalizeResult(value);
        return "PASS".equals(text) || "OK".equals(text) || "SUCCESS".equals(text);
    }

    private boolean isFailText(String value)
    {
        String text = normalizeResult(value);
        return "FAIL".equals(text) || "FAILED".equals(text) || "ERROR".equals(text)
                || "TIMEOUT".equals(text) || "CANCEL".equals(text);
    }

    private String normalizeResult(String value)
    {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String parseCallbackValue(String data, String fieldName)
    {
        try
        {
            JsonNode root = objectMapper.readTree(data);
            JsonNode direct = root.get(fieldName);
            if (direct != null && !direct.isNull())
            {
                return direct.asText();
            }
            JsonNode bizInfo = root.get("biz_info");
            if (bizInfo != null && bizInfo.get(fieldName) != null)
            {
                return bizInfo.get(fieldName).asText();
            }
        }
        catch (IOException e)
        {
            throw new ServiceException("FaceID 回调 data 不是合法 JSON");
        }
        return null;
    }

    private String sha1(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes)
            {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            throw new ServiceException("FaceID 回调签名计算失败");
        }
    }

    private String appendQuery(String baseUrl, String... pairs)
    {
        StringBuilder builder = new StringBuilder(baseUrl);
        builder.append(baseUrl.contains("?") ? "&" : "?");
        for (int i = 0; i + 1 < pairs.length; i += 2)
        {
            if (i > 0)
            {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(pairs[i], StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(StringUtils.defaultString(pairs[i + 1]), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private String buildSessionKey(String sessionId)
    {
        return FaceLoginConstants.FACE_LIVENESS_SESSION_KEY_PREFIX + sessionId;
    }

    private String buildBizKey(String bizId)
    {
        return FaceLoginConstants.FACE_LIVENESS_BIZ_KEY_PREFIX + bizId;
    }

    private String trim(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String firstNotBlank(String first, String second)
    {
        return StringUtils.isNotBlank(first) ? first : second;
    }
}
