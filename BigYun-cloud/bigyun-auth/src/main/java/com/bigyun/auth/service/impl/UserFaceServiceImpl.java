package com.bigyun.auth.service.impl;

import com.bigyun.auth.constant.FaceLoginConstants;
import com.bigyun.auth.domain.FaceStatusResponse;
import com.bigyun.auth.service.IUserFaceService;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.constant.HttpStatus;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.redis.service.RedisService;
import com.bigyun.system.domain.SysUserFace;
import com.bigyun.system.remote.RemoteUserService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFaceServiceImpl implements IUserFaceService
{
    @Autowired
    private RedisService redisService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Override
    public String getFaceTokenByUserId(Long userId)
    {
        String cachedToken = redisService.getCacheObject(faceTokenKey(userId));
        if (StringUtils.isNotBlank(cachedToken))
        {
            return cachedToken;
        }

        SysUserFace userFace = getRemoteUserFace(userId);
        if (userFace == null || StringUtils.isBlank(userFace.getFaceToken()))
        {
            return null;
        }
        redisService.setCacheObject(faceTokenKey(userId), userFace.getFaceToken(),
                FaceLoginConstants.FACE_TOKEN_TTL_DAYS, TimeUnit.DAYS);
        return userFace.getFaceToken();
    }

    @Override
    public SysUserFace getUserFaceByUserId(Long userId)
    {
        return getRemoteUserFace(userId);
    }

    @Override
    public void saveFaceToken(Long userId, String faceToken)
    {
        saveFaceToken(userId, faceToken, FaceLoginConstants.LIVENESS_MODE_IMAGE_COMPARE, null,
                FaceLoginConstants.LIVENESS_STATUS_PASS);
    }

    @Override
    public void saveFaceToken(Long userId, String faceToken, String livenessMode, String livenessBizId,
            String livenessStatus)
    {
        saveFaceCredential(userId, FaceLoginConstants.DEFAULT_PROVIDER_CODE, faceToken, livenessMode, livenessBizId,
                livenessStatus, null);
    }

    @Override
    public void saveFaceCredential(Long userId, String providerCode, String faceToken, String livenessMode,
            String livenessBizId, String livenessStatus, String remark)
    {
        SysUserFace userFace = new SysUserFace();
        userFace.setUserId(userId);
        userFace.setProviderCode(StringUtils.defaultIfBlank(providerCode, FaceLoginConstants.DEFAULT_PROVIDER_CODE));
        userFace.setFaceToken(faceToken);
        userFace.setLivenessMode(livenessMode);
        userFace.setLastLivenessBizId(livenessBizId);
        userFace.setLastLivenessStatus(livenessStatus);
        userFace.setRemark(remark);
        R<Boolean> result = remoteUserService.saveUserFace(userFace, SecurityConstants.INNER);
        if (result == null || R.isError(result) || !Boolean.TRUE.equals(result.getData()))
        {
            throw new ServiceException(result == null ? "保存人脸信息失败" : result.getMsg());
        }
        redisService.setCacheObject(faceTokenKey(userId), faceToken,
                FaceLoginConstants.FACE_TOKEN_TTL_DAYS, TimeUnit.DAYS);
    }

    @Override
    public FaceStatusResponse getFaceStatus(Long userId)
    {
        SysUserFace userFace = getRemoteUserFace(userId);
        FaceStatusResponse response = new FaceStatusResponse();
        response.setHasFace(userFace != null && StringUtils.isNotBlank(userFace.getFaceToken()));
        if (userFace != null)
        {
            response.setProviderCode(userFace.getProviderCode());
            response.setLastEnrollTime(userFace.getLastEnrollTime());
            response.setLastLoginTime(userFace.getLastLoginTime());
            response.setLivenessMode(userFace.getLivenessMode());
            response.setLastLivenessTime(userFace.getLastLivenessTime());
            response.setLastLivenessBizId(userFace.getLastLivenessBizId());
            response.setLastLivenessStatus(userFace.getLastLivenessStatus());
        }
        response.setStatusLabel(buildStatusLabel(response));
        return response;
    }

    @Override
    public void deleteFaceToken(Long userId)
    {
        R<Boolean> result = remoteUserService.deleteUserFaceByUserId(userId, SecurityConstants.INNER);
        if (result == null || R.isError(result))
        {
            throw new ServiceException(result == null ? "解绑人脸信息失败" : result.getMsg());
        }
        String key = faceTokenKey(userId);
        if (redisService.hasKey(key))
        {
            redisService.deleteObject(key);
        }
    }

    @Override
    public void recordLoginSuccess(Long userId)
    {
        remoteUserService.updateUserFaceLoginTime(userId, SecurityConstants.INNER);
    }

    @Override
    public void checkLocked(Long userId)
    {
        Integer failCount = redisService.getCacheObject(failCountKey(userId));
        if (failCount != null && failCount >= FaceLoginConstants.MAX_FAIL_COUNT)
        {
            throw new ServiceException("操作频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Override
    public void recordFailure(Long userId)
    {
        Integer failCount = redisService.getCacheObject(failCountKey(userId));
        failCount = failCount == null ? 1 : failCount + 1;
        redisService.setCacheObject(failCountKey(userId), failCount,
                FaceLoginConstants.FAIL_LOCK_TTL_MINUTES, TimeUnit.MINUTES);
        if (failCount >= FaceLoginConstants.MAX_FAIL_COUNT)
        {
            throw new ServiceException("操作频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Override
    public void clearFailure(Long userId)
    {
        String key = failCountKey(userId);
        if (redisService.hasKey(key))
        {
            redisService.deleteObject(key);
        }
    }

    private String faceTokenKey(Long userId)
    {
        return FaceLoginConstants.FACE_TOKEN_KEY_PREFIX + userId;
    }

    private String failCountKey(Long userId)
    {
        return FaceLoginConstants.FACE_FAIL_COUNT_KEY_PREFIX + userId;
    }

    private SysUserFace getRemoteUserFace(Long userId)
    {
        R<SysUserFace> result = remoteUserService.getUserFaceByUserId(userId, SecurityConstants.INNER);
        if (result == null || R.isError(result))
        {
            throw new ServiceException(result == null ? "查询人脸信息失败" : result.getMsg());
        }
        return result.getData();
    }

    private String buildStatusLabel(FaceStatusResponse response)
    {
        if (response == null || !Boolean.TRUE.equals(response.getHasFace()))
        {
            return "未录入";
        }
        if (FaceLoginConstants.LIVENESS_MODE_FACEID_H5.equals(response.getLivenessMode()))
        {
            return "已活体录入";
        }
        if (FaceLoginConstants.LIVENESS_MODE_ALIYUN_STATIC.equals(response.getLivenessMode()))
        {
            return "已通过活体录入";
        }
        if (FaceLoginConstants.LIVENESS_MODE_ALIYUN_VIDEO.equals(response.getLivenessMode()))
        {
            return "已通过视频活体录入";
        }
        if (FaceLoginConstants.LIVENESS_MODE_TEST_IMAGE.equals(response.getLivenessMode()))
        {
            return "仅图片测试录入";
        }
        if (FaceLoginConstants.LIVENESS_MODE_IMAGE_COMPARE.equals(response.getLivenessMode()))
        {
            return "已图片比对录入";
        }
        return "已录入";
    }
}
