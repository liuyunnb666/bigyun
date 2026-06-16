package com.bigyun.auth.service;

import com.bigyun.auth.domain.AliyunStaticLivenessResult;
import com.bigyun.auth.domain.AliyunVideoLivenessResult;
import com.bigyun.auth.domain.FaceIdProviderSettings;
import com.bigyun.auth.domain.FaceIdResultResponse;
import com.bigyun.auth.domain.FaceIdTokenResponse;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderCapabilityDTO;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.ProviderExecuteRequest;
import com.bigyun.config.domain.payload.AliyunFaceComparePayload;
import com.bigyun.config.domain.payload.FaceComparePayload;
import com.bigyun.config.domain.payload.FaceDetectPayload;
import com.bigyun.config.domain.payload.FaceIdResultPayload;
import com.bigyun.config.domain.payload.FaceIdTokenPayload;
import com.bigyun.config.domain.payload.FaceLivenessPayload;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.remote.RemoteProviderConfigService;
import com.bigyun.provider.core.ProviderRuntimeExecutor;
import com.bigyun.provider.core.runtime.ProviderRuntimeMetadata;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import com.bigyun.provider.domain.GenericResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 人脸识别 Provider 调用封装。
 *
 * <p>Face++ detect/compare、FaceID H5 活体和阿里云活体能力均通过 Provider 配置中心执行。
 * auth 服务只负责编排登录/录入流程，不在代码中保存第三方密钥。</p>
 */
@Service
public class FaceProviderService
{
    private static final Logger log = LoggerFactory.getLogger(FaceProviderService.class);

    private static final String CONFIG_TYPE_FACE = "face";

    private static final String PROVIDER_FACEPLUS = "faceplus";

    private static final String PROVIDER_ALIYUN_FACEBODY = "aliyun-facebody";

    private static final String CAPABILITY_FACE_DETECT = "face_login_detect";

    private static final String CAPABILITY_FACE_COMPARE = "face_login_compare";

    private static final String CAPABILITY_STATIC_LIVENESS = "face_login_static_liveness";

    private static final String CAPABILITY_VIDEO_LIVENESS = "face_login_video_liveness";

    private static final String CAPABILITY_ALIYUN_FACE_COMPARE = "face_login_aliyun_compare";

    private static final String CAPABILITY_FACEID_H5_GET_TOKEN = "faceid_h5_get_token";

    private static final String CAPABILITY_FACEID_H5_GET_RESULT = "faceid_h5_get_result";

    private static final String OPERATION_STATIC_LIVENESS = "detect_living_face";

    private static final String OPERATION_VIDEO_LIVENESS = "detect_video_living_face";

    private static final String OPERATION_ALIYUN_FACE_COMPARE = "compare_face";

    private static final String DEFAULT_FACEID_API_BASE = "https://api.megvii.com";

    private static final String DEFAULT_FACEID_DO_URL = "https://api.megvii.com/faceid/lite/do";

    private static final String DEFAULT_FACEID_SCENE_ID = "face_login_h5";

    private static final String DEFAULT_FACEID_PROCEDURE_TYPE = "meglive_flash";

    private static final String DEFAULT_FACEID_COMPARISON_TYPE = "-1";

    private static final String DEFAULT_FACEID_ACTION_HTTP_METHOD = "GET";

    private static final boolean DEFAULT_ALLOW_ADMIN_IMAGE_LOGIN = true;

    private static final int MAX_BASE64_LENGTH = 5 * 1024 * 1024;

    private static final double CONFIDENCE_THRESHOLD = 80.0D;

    private static final double STATIC_LIVENESS_RATE_THRESHOLD = 80.0D;

    private static final double VIDEO_LIVENESS_CONFIDENCE_THRESHOLD = 0.80D;

    private static final double ALIYUN_FACE_CONFIDENCE_THRESHOLD = 80.0D;

    @Autowired
    private RemoteProviderConfigService remoteProviderConfigService;

    @Autowired(required = false)
    private ProviderRuntimeExecutor providerRuntimeExecutor;

    @Autowired(required = false)
    private ProviderRuntimeSnapshotStore snapshotStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 Face++ detect，把前端采集的人脸图片或 FaceID image_best 转换为 face_token。
     */
    public String detectFace(String imageBase64)
    {
        String normalizedImage = normalizeImageBase64(imageBase64);
        ensureFaceConfig();

        GenericResponse response = executeByCapability(CAPABILITY_FACE_DETECT, "detect",
                FaceDetectPayload.of(normalizedImage));
        Map<String, Object> data = response.getData();
        String faceToken = data == null ? null : toStringValue(data.get("face_token"));
        if (StringUtils.isEmpty(faceToken))
        {
            throw new ServiceException("未检测到人脸");
        }
        return faceToken;
    }

    /**
     * 使用 Face++ compare 做 1:1 人脸比对。
     */
    public void validateFaceMatch(String faceToken, String storedFaceToken)
    {
        if (StringUtils.isEmpty(storedFaceToken))
        {
            throw new ServiceException("用户未录入人脸信息，请先登录后录入人脸");
        }

        GenericResponse response = executeByCapability(CAPABILITY_FACE_COMPARE, "compare",
                FaceComparePayload.of(faceToken, storedFaceToken));
        Double confidence = toDouble(response.getData() == null ? null : response.getData().get("confidence"));
        if (confidence == null || confidence < CONFIDENCE_THRESHOLD)
        {
            log.warn("Face compare failed: confidence={}, threshold={}", confidence, CONFIDENCE_THRESHOLD);
            throw new ServiceException("人脸不匹配");
        }
    }

    public void validateAliyunFaceMatch(String currentFaceImageUrl, String enrolledFaceImageUrl)
    {
        if (StringUtils.isBlank(currentFaceImageUrl) || StringUtils.isBlank(enrolledFaceImageUrl))
        {
            throw new ServiceException("人脸比对图片地址不能为空");
        }

        GenericResponse response = executeByCapability(CAPABILITY_ALIYUN_FACE_COMPARE, OPERATION_ALIYUN_FACE_COMPARE,
                PROVIDER_ALIYUN_FACEBODY, AliyunFaceComparePayload.urls(currentFaceImageUrl, enrolledFaceImageUrl));
        Double confidence = toDouble(response.getData() == null ? null : response.getData().get("confidence"));
        if (confidence == null || confidence < ALIYUN_FACE_CONFIDENCE_THRESHOLD)
        {
            log.warn("Aliyun face compare failed: confidence={}, threshold={}", confidence,
                    ALIYUN_FACE_CONFIDENCE_THRESHOLD);
            throw new ServiceException("人脸不匹配");
        }
    }

    /**
     * 调用阿里云 DetectLivingFace 做静默活体检测。
     */
    public AliyunStaticLivenessResult validateStaticLiveness(String imageBase64)
    {
        String normalizedImage = normalizeImageBase64(imageBase64);
        GenericResponse response = executeByCapability(CAPABILITY_STATIC_LIVENESS, OPERATION_STATIC_LIVENESS,
                PROVIDER_ALIYUN_FACEBODY, FaceLivenessPayload.staticImage(normalizedImage));
        Map<String, Object> data = response.getData();

        AliyunStaticLivenessResult result = new AliyunStaticLivenessResult();
        result.setPass(toBoolean(data == null ? null : data.get("pass")));
        result.setLabel(toStringValue(data == null ? null : data.get("label")));
        result.setSuggestion(toStringValue(data == null ? null : data.get("suggestion")));
        result.setRate(toDouble(data == null ? null : data.get("rate")));
        result.setRequestId(toStringValue(data == null ? null : data.get("requestId")));
        result.setMessage(toStringValue(data == null ? null : data.get("message")));

        boolean pass = result.isPass()
                || "pass".equalsIgnoreCase(result.getSuggestion())
                || ("normal".equalsIgnoreCase(result.getLabel())
                && result.getRate() != null
                && result.getRate() >= STATIC_LIVENESS_RATE_THRESHOLD);
        result.setPass(pass);
        if (!pass)
        {
            log.warn("Aliyun static liveness failed: label={}, suggestion={}, rate={}, requestId={}",
                    result.getLabel(), result.getSuggestion(), result.getRate(), result.getRequestId());
            throw new ServiceException("活体检测未通过，请重新拍摄本人正脸照片");
        }
        return result;
    }

    public AliyunVideoLivenessResult validateVideoLiveness(String videoUrl)
    {
        if (StringUtils.isBlank(videoUrl))
        {
            throw new ServiceException("活体检测视频地址不能为空");
        }
        GenericResponse response = executeByCapability(CAPABILITY_VIDEO_LIVENESS, OPERATION_VIDEO_LIVENESS,
                PROVIDER_ALIYUN_FACEBODY, FaceLivenessPayload.video(videoUrl.trim()));
        Map<String, Object> data = response.getData();

        AliyunVideoLivenessResult result = new AliyunVideoLivenessResult();
        result.setPass(toBoolean(data == null ? null : data.get("pass")));
        result.setLiveConfidence(toDouble(data == null ? null : data.get("liveConfidence")));
        result.setFaceConfidence(toDouble(data == null ? null : data.get("faceConfidence")));
        result.setRequestId(toStringValue(data == null ? null : data.get("requestId")));
        result.setMessage(toStringValue(data == null ? null : data.get("message")));

        boolean pass = result.isPass()
                || (result.getLiveConfidence() != null
                && result.getLiveConfidence() >= VIDEO_LIVENESS_CONFIDENCE_THRESHOLD);
        result.setPass(pass);
        if (!pass)
        {
            log.warn("Aliyun video liveness failed: liveConfidence={}, faceConfidence={}, requestId={}",
                    result.getLiveConfidence(), result.getFaceConfidence(), result.getRequestId());
            throw new ServiceException("视频活体检测未通过，请重新录制本人正脸视频");
        }
        return result;
    }

    /**
     * 创建 FaceID H5 活体 token，前端通过 verifyUrl 跳转到 FaceID 页面完成动作活体。
     */
    public FaceIdTokenResponse createFaceIdH5Token(String sessionId, String nonce, String returnUrl)
    {
        FaceIdProviderSettings settings = getFaceIdSettings();
        if (StringUtils.isBlank(returnUrl))
        {
            throw new ServiceException("FaceID H5 returnUrl 未配置，无法创建活体会话");
        }

        FaceIdTokenPayload payload = new FaceIdTokenPayload();
        payload.setFaceIdApiBase(settings.getApiBase());
        payload.setUuid(nonce);
        payload.setBizNo(sessionId);
        payload.setReturnUrl(returnUrl);
        payload.setNotifyUrl(StringUtils.defaultString(settings.getNotifyUrl()));
        payload.setSceneId(settings.getSceneId());
        payload.setProcedureType(settings.getProcedureType());
        payload.setComparisonType(settings.getComparisonType());
        payload.setActionHttpMethod(settings.getActionHttpMethod());

        GenericResponse response = executeByCapability(CAPABILITY_FACEID_H5_GET_TOKEN, "faceid_h5_get_token", payload);
        Map<String, Object> data = response.getData();
        String token = firstNotBlank(data, "token", "faceIdToken");
        String bizId = firstNotBlank(data, "bizId", "biz_id");
        if (StringUtils.isBlank(token))
        {
            throw new ServiceException("FaceID H5 未返回 token，请检查活体 Provider 模板");
        }

        FaceIdTokenResponse result = new FaceIdTokenResponse();
        result.setToken(token);
        result.setBizId(bizId);
        result.setVerifyUrl(buildDoVerificationUrl(settings.getDoUrl(), token));
        return result;
    }

    /**
     * 查询 FaceID H5 活体结果，imageBest 只在内存/Redis 会话中临时使用，不落库。
     */
    public FaceIdResultResponse getFaceIdH5Result(String bizId)
    {
        if (StringUtils.isBlank(bizId))
        {
            throw new ServiceException("FaceID bizId 不能为空");
        }
        FaceIdProviderSettings settings = getFaceIdSettings();
        GenericResponse response = executeByCapability(CAPABILITY_FACEID_H5_GET_RESULT, "faceid_h5_get_result",
                FaceIdResultPayload.of(settings.getApiBase(), bizId));
        Map<String, Object> data = response.getData();

        FaceIdResultResponse result = new FaceIdResultResponse();
        result.setBizId(firstNotBlank(data, "bizId", "biz_id"));
        result.setBizNo(firstNotBlank(data, "bizNo", "biz_no"));
        result.setResult(firstNotBlank(data, "result", "status"));
        result.setLivenessResult(firstNotBlank(data, "livenessResult", "liveness_result"));
        result.setImageBest(firstNotBlank(data, "imageBest", "image_best"));
        result.setErrorMessage(firstNotBlank(data, "errorMessage", "error_message"));
        return result;
    }

    public FaceIdProviderSettings getFaceIdSettings()
    {
        ProviderConfigDTO config = getEnabledFaceplusConfig();
        Map<String, Object> ext = parseExtParams(config.getExtParamsJson());

        FaceIdProviderSettings settings = new FaceIdProviderSettings();
        settings.setApiBase(stringValue(ext.get("faceIdApiBase"), DEFAULT_FACEID_API_BASE));
        settings.setDoUrl(stringValue(ext.get("faceIdDoUrl"), DEFAULT_FACEID_DO_URL));
        settings.setNotifyUrl(stringValue(ext.get("faceIdNotifyUrl"), null));
        settings.setReturnUrl(stringValue(ext.get("faceIdReturnUrl"), null));
        settings.setSceneId(stringValue(ext.get("sceneId"), DEFAULT_FACEID_SCENE_ID));
        settings.setProcedureType(stringValue(ext.get("procedureType"), DEFAULT_FACEID_PROCEDURE_TYPE));
        settings.setComparisonType(stringValue(ext.get("comparisonType"), DEFAULT_FACEID_COMPARISON_TYPE));
        settings.setActionHttpMethod(stringValue(ext.get("actionHttpMethod"), DEFAULT_FACEID_ACTION_HTTP_METHOD));
        settings.setAllowTestImageFallback(toBoolean(ext.get("allowTestImageFallback")));
        settings.setAllowAdminImageLogin(toBoolean(ext.get("allowAdminImageLogin"), DEFAULT_ALLOW_ADMIN_IMAGE_LOGIN));
        return settings;
    }

    public boolean isTestImageFallbackAllowed()
    {
        return getFaceIdSettings().isAllowTestImageFallback();
    }

    public boolean isAdminImageLoginAllowed()
    {
        return getFaceIdSettings().isAllowAdminImageLogin();
    }

    public String getFaceIdSecretKey()
    {
        return getEnabledFaceplusConfig().getSecretKey();
    }

    private String normalizeImageBase64(String imageBase64)
    {
        if (StringUtils.isBlank(imageBase64))
        {
            throw new ServiceException("人脸图像不能为空");
        }
        String value = imageBase64.trim();
        int commaIndex = value.indexOf(',');
        if (value.startsWith("data:") && commaIndex >= 0)
        {
            value = value.substring(commaIndex + 1);
        }
        value = value.replaceAll("\\s+", "");
        if (value.length() > MAX_BASE64_LENGTH)
        {
            throw new ServiceException("人脸图像过大，请重新拍摄后上传");
        }
        return value;
    }

    private ProviderConfigDTO getEnabledFaceplusConfig()
    {
        ProviderConfigDTO snapshotConfig = getFaceplusConfigFromSnapshot();
        if (snapshotConfig != null)
        {
            return snapshotConfig;
        }
        R<List<ProviderConfigDTO>> configResult = remoteProviderConfigService.listEnabledConfigs(CONFIG_TYPE_FACE);
        if (configResult == null || R.isError(configResult) || configResult.getData() == null)
        {
            throw new ServiceException("请先配置并启用 Face++ Provider");
        }
        return configResult.getData().stream()
                .filter(this::isFaceplusConfig)
                .findFirst()
                .orElseThrow(() -> new ServiceException("请先配置并启用 Face++ Provider"));
    }

    private void ensureFaceConfig()
    {
        getEnabledFaceplusConfig();
    }

    private boolean isFaceplusConfig(ProviderConfigDTO config)
    {
        return config != null && PROVIDER_FACEPLUS.equalsIgnoreCase(config.getProviderCode());
    }

    private GenericResponse execute(String operation, ProviderPayload payload)
    {
        return execute(PROVIDER_FACEPLUS, operation, payload);
    }

    private GenericResponse executeByCapability(String capabilityCode, String fallbackOperation, ProviderPayload payload)
    {
        return executeByCapability(capabilityCode, fallbackOperation, PROVIDER_FACEPLUS, payload);
    }

    private GenericResponse executeByCapability(String capabilityCode, String fallbackOperation, String fallbackProviderCode,
            ProviderPayload payload)
    {
        GenericResponse localResponse = executeLocalByCapability(capabilityCode, payload);
        if (localResponse != null)
        {
            return localResponse;
        }
        R<ProviderCapabilityDTO> capabilityResult = remoteProviderConfigService.getDefaultCapability(capabilityCode);
        if (capabilityResult != null && !R.isError(capabilityResult) && capabilityResult.getData() != null)
        {
            ProviderCapabilityDTO capability = capabilityResult.getData();
            ProviderExecuteRequest request = ProviderExecuteRequest.of(capability.getConfigType(),
                    capability.getProviderCode(), capability.getOperation(), payload);
            return executeRemote(request, capabilityCode, capability.getModelCode());
        }
        return execute(fallbackProviderCode, fallbackOperation, payload);
    }

    private GenericResponse execute(String providerCode, String operation, ProviderPayload payload)
    {
        GenericResponse localResponse = executeLocalProvider(providerCode, operation, payload);
        if (localResponse != null)
        {
            return localResponse;
        }
        ProviderExecuteRequest request = ProviderExecuteRequest.of(CONFIG_TYPE_FACE, providerCode, operation, payload);
        return executeRemote(request, null, null);
    }

    private GenericResponse executeLocalByCapability(String capabilityCode, ProviderPayload payload)
    {
        if (providerRuntimeExecutor == null)
        {
            return null;
        }
        try
        {
            return providerRuntimeExecutor.executeByCapability(capabilityCode, payload);
        }
        catch (Exception e)
        {
            log.warn("本地人脸 Provider 运行时执行失败，准备走远程兼容链路 capabilityCode={}, message={}",
                    capabilityCode, e.getMessage());
            return null;
        }
    }

    private GenericResponse executeLocalProvider(String providerCode, String operation, ProviderPayload payload)
    {
        if (providerRuntimeExecutor == null)
        {
            return null;
        }
        try
        {
            return providerRuntimeExecutor.execute(CONFIG_TYPE_FACE, providerCode, operation, payload);
        }
        catch (Exception e)
        {
            log.warn("本地人脸 Provider 运行时执行失败，准备走远程兼容链路 providerCode={}, operation={}, message={}",
                    providerCode, operation, e.getMessage());
            return null;
        }
    }

    private GenericResponse executeRemote(ProviderExecuteRequest request, String capabilityCode, String modelCode)
    {
        long start = System.currentTimeMillis();
        R<GenericResponse> result = remoteProviderConfigService.execute(request);
        if (result == null || R.isError(result) || result.getData() == null)
        {
            throw new ServiceException(result == null ? "人脸服务调用失败" : result.getMsg());
        }
        return ProviderRuntimeMetadata.enrich(result.getData(), capabilityCode, request.getConfigType(),
                request.getProviderCode(), request.getOperation(), modelCode,
                "remote-config-service", true, start);
    }

    private ProviderConfigDTO getFaceplusConfigFromSnapshot()
    {
        if (snapshotStore == null)
        {
            return null;
        }
        String[] capabilityCodes = {
                CAPABILITY_FACEID_H5_GET_TOKEN,
                CAPABILITY_FACEID_H5_GET_RESULT,
                CAPABILITY_FACE_DETECT,
                CAPABILITY_FACE_COMPARE
        };
        for (String capabilityCode : capabilityCodes)
        {
            Optional<ProviderRuntimeSnapshot> snapshot = snapshotStore.getByCapability(capabilityCode);
            ProviderConfigDTO config = snapshot.map(ProviderRuntimeSnapshot::getProviderConfig).orElse(null);
            if (isFaceplusConfig(config))
            {
                return config;
            }
        }
        String[] operations = {"faceid_h5_get_token", "faceid_h5_get_result", "detect", "compare"};
        for (String operation : operations)
        {
            Optional<ProviderRuntimeSnapshot> snapshot =
                    snapshotStore.getByProvider(CONFIG_TYPE_FACE, PROVIDER_FACEPLUS, operation);
            ProviderConfigDTO config = snapshot.map(ProviderRuntimeSnapshot::getProviderConfig).orElse(null);
            if (isFaceplusConfig(config))
            {
                return config;
            }
        }
        return null;
    }

    private Map<String, Object> parseExtParams(String extParamsJson)
    {
        if (StringUtils.isBlank(extParamsJson))
        {
            return new HashMap<>();
        }
        try
        {
            return objectMapper.readValue(extParamsJson, new TypeReference<Map<String, Object>>()
            {
            });
        }
        catch (Exception e)
        {
            throw new ServiceException("Face++ Provider 扩展参数不是合法 JSON");
        }
    }

    private String buildDoVerificationUrl(String doUrl, String token)
    {
        String separator = doUrl.contains("?") ? "&" : "?";
        return doUrl + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String firstNotBlank(Map<String, Object> data, String... keys)
    {
        if (data == null)
        {
            return null;
        }
        for (String key : keys)
        {
            String value = toStringValue(data.get(key));
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value, String defaultValue)
    {
        String text = toStringValue(value);
        return StringUtils.isBlank(text) ? defaultValue : text.trim();
    }

    private String toStringValue(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private Boolean toBoolean(Object value)
    {
        return toBoolean(value, false);
    }

    private Boolean toBoolean(Object value, boolean defaultValue)
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        if (value == null)
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }

    private Double toDouble(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        if (value == null)
        {
            return null;
        }
        try
        {
            return Double.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
