package com.bigyun.auth.controller;

import com.bigyun.auth.constant.FaceLoginConstants;
import com.bigyun.auth.domain.FaceEnrollRequest;
import com.bigyun.auth.domain.FaceLivenessSessionRequest;
import com.bigyun.auth.domain.FaceLivenessSessionResponse;
import com.bigyun.auth.domain.FaceLivenessStatusResponse;
import com.bigyun.auth.domain.FaceStatusResponse;
import com.bigyun.auth.domain.FaceVideoLivenessUploadResponse;
import com.bigyun.auth.service.FaceIdLivenessService;
import com.bigyun.auth.service.FaceMediaStorageService;
import com.bigyun.auth.service.FaceProviderService;
import com.bigyun.auth.service.IUserFaceService;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.security.annotation.RequiresLogin;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.file.domain.SysFile;
import com.bigyun.file.remote.RemoteFileService;
import com.bigyun.system.domain.model.LoginUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 登录后人脸信息和 FaceID H5 活体会话控制器。
 */
@RestController
@RequestMapping("/face")
public class FaceAuthController
{
    private static final String TEST_IMAGE_WARNING = "当前使用上传图片测试兜底，未走正式摄像头活体检测，仅用于本地联调";

    @Autowired
    private IUserFaceService userFaceService;

    @Autowired
    private FaceProviderService faceProviderService;

    @Autowired
    private FaceIdLivenessService faceIdLivenessService;

    @Autowired
    private FaceMediaStorageService faceMediaStorageService;

    @Autowired
    private RemoteFileService remoteFileService;

    /**
     * 查询当前登录用户的人脸录入状态，用于“我的/资料”页展示。
     */
    @RequiresLogin
    @GetMapping("/status")
    public R<FaceStatusResponse> status()
    {
        return R.ok(userFaceService.getFaceStatus(currentUserId()));
    }

    /**
     * 创建当前登录用户的 FaceID H5 活体录入会话。
     */
    @RequiresLogin
    @PostMapping("/liveness/enroll/session")
    public R<FaceLivenessSessionResponse> createEnrollLivenessSession(@Valid @RequestBody FaceLivenessSessionRequest request)
    {
        return R.ok(faceIdLivenessService.createEnrollSession(currentUserId(), request));
    }

    /**
     * 创建人脸登录活体会话。该接口未登录可用，但必须先提交账号、手机号或邮箱定位用户。
     */
    @PostMapping("/liveness/login/session")
    public R<FaceLivenessSessionResponse> createLoginLivenessSession(@Valid @RequestBody FaceLivenessSessionRequest request)
    {
        return R.ok(faceIdLivenessService.createLoginSession(request));
    }

    /**
     * FaceID 服务端回调。签名由 sign = sha1(API_SECRET + data) 校验。
     */
    @PostMapping("/liveness/notify")
    public R<?> livenessNotify(@RequestParam(value = "data", required = false) String data,
            @RequestParam(value = "sign", required = false) String sign)
    {
        faceIdLivenessService.handleNotify(data, sign);
        return R.ok();
    }

    /**
     * FaceID 浏览器回跳入口。优先重定向到创建会话时前端传入的 H5 结果页。
     */
    @GetMapping("/liveness/return")
    public void livenessReturn(@RequestParam("sessionId") String sessionId,
            @RequestParam("nonce") String nonce,
            @RequestParam(value = "biz_id", required = false) String bizId,
            @RequestParam(value = "result", required = false) String result,
            HttpServletResponse response) throws IOException
    {
        String redirectUrl = faceIdLivenessService.buildReturnRedirect(sessionId, nonce, bizId, result);
        if (StringUtils.isBlank(redirectUrl))
        {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("活体认证已返回，请回到 H5 页面查看结果");
            return;
        }
        response.sendRedirect(redirectUrl);
    }

    /**
     * 前端结果页按 sessionId + nonce 轮询活体状态。
     */
    @GetMapping("/liveness/status")
    public R<FaceLivenessStatusResponse> livenessStatus(@RequestParam("sessionId") String sessionId,
            @RequestParam("nonce") String nonce)
    {
        return R.ok(faceIdLivenessService.getStatus(sessionId, nonce));
    }

    /**
     * 人脸登录前上传浏览器录制的活体视频，先放到 OSS，再把可访问地址交给阿里云视频活体检测。
     */
    @PostMapping(value = "/video-liveness/login/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FaceVideoLivenessUploadResponse> uploadLoginVideoLiveness(@RequestPart("file") MultipartFile file)
    {
        return R.ok(uploadVideoToOss(file));
    }

    /**
     * 登录用户录入人脸时上传活体视频，必须携带登录态，避免匿名写入录入材料。
     */
    @RequiresLogin
    @PostMapping(value = "/video-liveness/enroll/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FaceVideoLivenessUploadResponse> uploadEnrollVideoLiveness(@RequestPart("file") MultipartFile file)
    {
        currentUserId();
        return R.ok(uploadVideoToOss(file));
    }

    /**
     * 当前登录用户录入或重新录入人脸。
     *
     * <p>安全路径必须先完成 FaceID H5 活体，然后使用活体结果 image_best 做 Face++ detect。
     * 后台可选图片 1:1 体验链路；上传图片兜底只在测试开关允许且请求明确标记 testImageFallback=true 时可用。</p>
     */
    @RequiresLogin
    @PostMapping("/enroll")
    public R<FaceStatusResponse> enroll(@Valid @RequestBody FaceEnrollRequest request)
    {
        Long userId = currentUserId();
        if (request != null && StringUtils.isNotBlank(request.getFaceLivenessSessionId()))
        {
            String imageBest = faceIdLivenessService.completeAndGetBestImage(
                    request.getFaceLivenessSessionId(), request.getFaceLivenessNonce(),
                    FaceLoginConstants.LIVENESS_SCENE_ENROLL, userId);
            String faceToken = faceProviderService.detectFace(imageBest);
            userFaceService.saveFaceToken(userId, faceToken, FaceLoginConstants.LIVENESS_MODE_FACEID_H5,
                    faceIdLivenessService.getVerifiedSession(request.getFaceLivenessSessionId(),
                            request.getFaceLivenessNonce(), FaceLoginConstants.LIVENESS_SCENE_ENROLL).getFaceIdBizId(),
                    FaceLoginConstants.LIVENESS_STATUS_PASS);
            faceIdLivenessService.markConsumed(request.getFaceLivenessSessionId(), request.getFaceLivenessNonce());
            return R.ok(userFaceService.getFaceStatus(userId));
        }

        if (isAdminImageCompare(request))
        {
            if (!faceProviderService.isAdminImageLoginAllowed())
            {
                throw new ServiceException("后台图片人脸录入未启用，请联系管理员");
            }
            String faceToken = faceProviderService.detectFace(request.getImageBase64());
            userFaceService.saveFaceToken(userId, faceToken, FaceLoginConstants.LIVENESS_MODE_IMAGE_COMPARE, null,
                    FaceLoginConstants.LIVENESS_STATUS_PASS);
            return R.ok(userFaceService.getFaceStatus(userId));
        }

        if (isPortalAliyunStaticLiveness(request))
        {
            faceProviderService.validateStaticLiveness(request.getImageBase64());
            String faceToken = faceProviderService.detectFace(request.getImageBase64());
            userFaceService.saveFaceToken(userId, faceToken, FaceLoginConstants.LIVENESS_MODE_ALIYUN_STATIC, null,
                    FaceLoginConstants.LIVENESS_STATUS_PASS);
            return R.ok(userFaceService.getFaceStatus(userId));
        }

        if (isPortalAliyunVideoLiveness(request))
        {
            faceProviderService.validateVideoLiveness(request.getVideoUrl());
            SysFile faceImage = faceMediaStorageService.uploadAliyunFaceImage(request.getImageBase64(),
                    "portal-face-enroll");
            boolean saved = false;
            try
            {
                userFaceService.saveFaceCredential(userId, FaceLoginConstants.PROVIDER_ALIYUN_FACEBODY,
                        faceImage.getObjectKey(), FaceLoginConstants.LIVENESS_MODE_ALIYUN_VIDEO,
                        request.getVideoObjectKey(), FaceLoginConstants.LIVENESS_STATUS_PASS,
                        StringUtils.defaultIfBlank(faceImage.getUrl(), faceImage.getObjectKey()));
                saved = true;
            }
            finally
            {
                if (!saved)
                {
                    faceMediaStorageService.deleteQuietly(StringUtils.defaultIfBlank(faceImage.getUrl(),
                            faceImage.getObjectKey()));
                }
            }
            return R.ok(userFaceService.getFaceStatus(userId));
        }

        if (!Boolean.TRUE.equals(request == null ? null : request.getTestImageFallback())
                || !faceProviderService.isTestImageFallbackAllowed())
        {
            throw new ServiceException("请先完成人脸活体检测或使用后台图片录入");
        }
        String faceToken = faceProviderService.detectFace(request.getImageBase64());
        userFaceService.saveFaceToken(userId, faceToken, FaceLoginConstants.LIVENESS_MODE_TEST_IMAGE, null,
                FaceLoginConstants.LIVENESS_STATUS_PASS);
        FaceStatusResponse response = userFaceService.getFaceStatus(userId);
        response.setWarning(TEST_IMAGE_WARNING);
        return R.ok(response);
    }

    private boolean isAdminImageCompare(FaceEnrollRequest request)
    {
        return request != null
                && FaceLoginConstants.CLIENT_TYPE_ADMIN.equalsIgnoreCase(request.getClientType())
                && FaceLoginConstants.FACE_LOGIN_MODE_IMAGE_COMPARE.equalsIgnoreCase(request.getFaceLoginMode());
    }

    private boolean isPortalAliyunStaticLiveness(FaceEnrollRequest request)
    {
        return request != null
                && FaceLoginConstants.CLIENT_TYPE_PORTAL.equalsIgnoreCase(request.getClientType())
                && !Boolean.TRUE.equals(request.getTestImageFallback())
                && FaceLoginConstants.FACE_LOGIN_MODE_ALIYUN_STATIC.equalsIgnoreCase(request.getFaceLoginMode());
    }

    private boolean isPortalAliyunVideoLiveness(FaceEnrollRequest request)
    {
        return request != null
                && FaceLoginConstants.CLIENT_TYPE_PORTAL.equalsIgnoreCase(request.getClientType())
                && !Boolean.TRUE.equals(request.getTestImageFallback())
                && FaceLoginConstants.FACE_LOGIN_MODE_ALIYUN_VIDEO.equalsIgnoreCase(request.getFaceLoginMode());
    }

    private FaceVideoLivenessUploadResponse uploadVideoToOss(MultipartFile file)
    {
        validateVideoFile(file);
        // 视频活体材料显式使用阿里云 OSS，不改变头像、附件等普通文件的默认存储策略。
        R<SysFile> fileResult = remoteFileService.uploadByProvider(file, ProviderConfigConstants.PROVIDER_ALIYUN_OSS);
        if (fileResult == null || R.isError(fileResult) || fileResult.getData() == null)
        {
            throw new ServiceException(fileResult == null ? "视频上传 OSS 失败" : fileResult.getMsg());
        }
        SysFile sysFile = fileResult.getData();
        String videoUrl = StringUtils.isNotBlank(sysFile.getTemporaryUrl()) ? sysFile.getTemporaryUrl() : sysFile.getUrl();
        if (StringUtils.isBlank(videoUrl))
        {
            throw new ServiceException("视频上传成功但未返回可访问地址");
        }

        FaceVideoLivenessUploadResponse response = new FaceVideoLivenessUploadResponse();
        response.setVideoUrl(videoUrl);
        response.setFileUrl(sysFile.getUrl());
        response.setObjectKey(sysFile.getObjectKey());
        response.setProvider(sysFile.getProvider());
        return response;
    }

    private void validateVideoFile(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("活体视频不能为空");
        }
        if (file.getSize() > 30 * 1024 * 1024L)
        {
            throw new ServiceException("活体视频不能超过 30MB");
        }
        String name = StringUtils.defaultString(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String contentType = StringUtils.defaultString(file.getContentType()).toLowerCase(Locale.ROOT);
        // 浏览器录制可能返回 WebM，阿里云接口优先推荐 MP4/AVI；这里先允许上传，再由活体接口给出最终结果。
        boolean validExtension = name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".webm");
        boolean validContentType = contentType.startsWith("video/");
        if (!validExtension && !validContentType)
        {
            throw new ServiceException("请上传 MP4、AVI 或 WebM 活体视频");
        }
    }

    /**
     * 解绑当前登录用户的人脸凭据，解绑后不能再使用扫脸登录。
     */
    @RequiresLogin
    @DeleteMapping
    public R<?> unbind()
    {
        userFaceService.deleteFaceToken(currentUserId());
        return R.ok();
    }

    private Long currentUserId()
    {
        Long userId = SecurityUtils.getUserId();
        // 网关未注入 user_id 头时，@RequiresLogin 已经校验 token，这里从登录缓存兜底取用户。
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
            throw new ServiceException("未获取到当前登录用户");
        }
        return userId;
    }
}
