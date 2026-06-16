package com.bigyun.auth.service.strategy;

import com.bigyun.auth.constant.FaceLoginConstants;
import com.bigyun.auth.domain.FaceLivenessSessionCache;
import com.bigyun.auth.service.FaceIdLivenessService;
import com.bigyun.auth.service.FaceMediaStorageService;
import com.bigyun.auth.service.FaceProviderService;
import com.bigyun.auth.service.IUserFaceService;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.file.domain.SysFile;
import com.bigyun.system.domain.SysUserFace;
import com.bigyun.system.domain.form.LoginReq;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.enums.LoginTypeEnum;
import com.bigyun.system.remote.RemoteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaceLoginStrategy implements LoginStrategy
{
    @Autowired
    private IUserFaceService userFaceService;

    @Autowired
    private FaceProviderService faceProviderService;

    @Autowired
    private FaceIdLivenessService faceIdLivenessService;

    @Autowired
    private FaceMediaStorageService faceMediaStorageService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Override
    public String getType()
    {
        return LoginTypeEnum.FACE.getCode();
    }

    @Override
    public LoginUser login(LoginReq req)
    {
        validateAccountSelector(req);
        LoginUser loginUser = resolveLoginUser(req);
        Long userId = loginUser.getSysUser().getUserId();
        userFaceService.checkLocked(userId);

        try
        {
            if (isPortalAliyunVideoLiveness(req))
            {
                validatePortalAliyunVideoLogin(req, userId);
            }
            else
            {
                String currentFaceToken = detectCurrentFace(req, userId);
                faceProviderService.validateFaceMatch(currentFaceToken, userFaceService.getFaceTokenByUserId(userId));
            }
            userFaceService.clearFailure(userId);
            userFaceService.recordLoginSuccess(userId);
            return loginUser;
        }
        catch (ServiceException e)
        {
            userFaceService.recordFailure(userId);
            throw e;
        }
        catch (RuntimeException e)
        {
            userFaceService.recordFailure(userId);
            throw e;
        }
    }

    private void validatePortalAliyunVideoLogin(LoginReq req, Long userId)
    {
        faceProviderService.validateVideoLiveness(req.getVideoUrl());
        SysUserFace userFace = userFaceService.getUserFaceByUserId(userId);
        if (userFace == null || StringUtils.isBlank(userFace.getFaceToken()))
        {
            throw new ServiceException("用户未录入视频人脸，请先登录后录入");
        }
        if (!FaceLoginConstants.PROVIDER_ALIYUN_FACEBODY.equalsIgnoreCase(userFace.getProviderCode())
                || !FaceLoginConstants.LIVENESS_MODE_ALIYUN_VIDEO.equalsIgnoreCase(userFace.getLivenessMode()))
        {
            throw new ServiceException("请先重新录入视频人脸");
        }

        SysFile currentImage = faceMediaStorageService.uploadAliyunFaceImage(req.getImageBase64(),
                "portal-face-login");
        try
        {
            String currentImageRef = StringUtils.defaultIfBlank(currentImage.getTemporaryUrl(),
                    StringUtils.defaultIfBlank(currentImage.getUrl(), currentImage.getObjectKey()));
            String currentImageUrl = faceMediaStorageService.getAccessibleUrl(currentImageRef);
            String enrolledImageRef = StringUtils.defaultIfBlank(userFace.getRemark(), userFace.getFaceToken());
            String enrolledImageUrl = faceMediaStorageService.getAccessibleUrl(enrolledImageRef);
            faceProviderService.validateAliyunFaceMatch(currentImageUrl, enrolledImageUrl);
        }
        finally
        {
            faceMediaStorageService.deleteQuietly(StringUtils.defaultIfBlank(currentImage.getUrl(),
                    currentImage.getObjectKey()));
        }
    }

    private String detectCurrentFace(LoginReq req, Long userId)
    {
        if (StringUtils.isNotBlank(req.getFaceLivenessSessionId()))
        {
            FaceLivenessSessionCache session = faceIdLivenessService.getVerifiedSession(
                    req.getFaceLivenessSessionId(), req.getFaceLivenessNonce(),
                    FaceLoginConstants.LIVENESS_SCENE_LOGIN);
            if (!userId.equals(session.getUserId()))
            {
                throw new ServiceException("活体会话不属于当前登录账号");
            }
            // 活体通过后只临时使用 image_best 做 detect，不保存原图或视频。
            String imageBest = faceIdLivenessService.completeAndGetBestImage(
                    req.getFaceLivenessSessionId(), req.getFaceLivenessNonce(),
                    FaceLoginConstants.LIVENESS_SCENE_LOGIN, userId);
            String faceToken = faceProviderService.detectFace(imageBest);
            faceIdLivenessService.markConsumed(req.getFaceLivenessSessionId(), req.getFaceLivenessNonce());
            return faceToken;
        }

        if (isAdminImageCompare(req))
        {
            if (!faceProviderService.isAdminImageLoginAllowed())
            {
                throw new ServiceException("后台图片人脸登录未启用，请联系管理员");
            }
            return detectImageBase64(req.getImageBase64());
        }

        if (isPortalAliyunStaticLiveness(req))
        {
            faceProviderService.validateStaticLiveness(req.getImageBase64());
            return detectImageBase64(req.getImageBase64());
        }

        if (Boolean.TRUE.equals(req.getTestImageFallback()) && faceProviderService.isTestImageFallbackAllowed())
        {
            return detectImageBase64(req.getImageBase64());
        }

        throw new ServiceException("请先完成人脸活体检测后再登录");
    }

    private String detectImageBase64(String imageBase64)
    {
        if (StringUtils.isBlank(imageBase64))
        {
            throw new ServiceException("人脸图像不能为空");
        }
        return faceProviderService.detectFace(imageBase64);
    }

    private boolean isAdminImageCompare(LoginReq req)
    {
        return req != null
                && FaceLoginConstants.CLIENT_TYPE_ADMIN.equalsIgnoreCase(req.getClientType())
                && FaceLoginConstants.FACE_LOGIN_MODE_IMAGE_COMPARE.equalsIgnoreCase(req.getFaceLoginMode());
    }

    private boolean isPortalAliyunStaticLiveness(LoginReq req)
    {
        return req != null
                && FaceLoginConstants.CLIENT_TYPE_PORTAL.equalsIgnoreCase(req.getClientType())
                && !Boolean.TRUE.equals(req.getTestImageFallback())
                && FaceLoginConstants.FACE_LOGIN_MODE_ALIYUN_STATIC.equalsIgnoreCase(req.getFaceLoginMode());
    }

    private boolean isPortalAliyunVideoLiveness(LoginReq req)
    {
        return req != null
                && FaceLoginConstants.CLIENT_TYPE_PORTAL.equalsIgnoreCase(req.getClientType())
                && !Boolean.TRUE.equals(req.getTestImageFallback())
                && FaceLoginConstants.FACE_LOGIN_MODE_ALIYUN_VIDEO.equalsIgnoreCase(req.getFaceLoginMode());
    }

    private void validateAccountSelector(LoginReq req)
    {
        if (req == null)
        {
            throw new ServiceException("登录请求参数不能为空");
        }
        int accountCount = 0;
        if (StringUtils.isNotBlank(req.getUserName()))
        {
            accountCount++;
        }
        if (StringUtils.isNotBlank(req.getPhone()))
        {
            accountCount++;
        }
        if (StringUtils.isNotBlank(req.getEmail()))
        {
            accountCount++;
        }
        if (accountCount != 1)
        {
            throw new ServiceException("账号、手机号、邮箱必须填写且只能填写一个");
        }
    }

    private LoginUser resolveLoginUser(LoginReq req)
    {
        R<LoginUser> userResult;
        if (StringUtils.isNotBlank(req.getUserName()))
        {
            userResult = remoteUserService.getUserInfo(req.getUserName().trim(), SecurityConstants.INNER);
        }
        else if (StringUtils.isNotBlank(req.getPhone()))
        {
            userResult = remoteUserService.getUserInfoByPhone(req.getPhone().trim(), SecurityConstants.INNER);
        }
        else
        {
            userResult = remoteUserService.getUserInfoByEmail(req.getEmail().trim(), SecurityConstants.INNER);
        }
        if (userResult == null || R.isError(userResult) || userResult.getData() == null
                || userResult.getData().getSysUser() == null
                || userResult.getData().getSysUser().getUserId() == null)
        {
            throw new ServiceException(userResult == null ? "用户查询失败" : userResult.getMsg());
        }
        return userResult.getData();
    }
}
