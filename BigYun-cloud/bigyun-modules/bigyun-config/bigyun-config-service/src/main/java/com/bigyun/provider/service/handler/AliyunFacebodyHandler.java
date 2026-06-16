package com.bigyun.provider.service.handler;

import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.provider.core.runtime.ProviderRuntimeSecretUtils;
import com.bigyun.provider.domain.GenericRequest;
import com.bigyun.provider.domain.GenericResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 阿里云人脸人体 Provider。
 *
 * <p>阿里云视觉智能开放平台使用 RPC 签名，不能按普通 api_key 表单调用。
 * 该处理器只做签名和调用编排，业务侧仍只拿标准化后的活体结果。</p>
 */
@Component
public class AliyunFacebodyHandler implements IProviderHandler<GenericRequest, GenericResponse>
{
    private static final Logger log = LoggerFactory.getLogger(AliyunFacebodyHandler.class);

    private static final String CONFIG_TYPE_FACE = "face";

    private static final String PROVIDER_CODE = "aliyun-facebody";

    private static final String OPERATION_DETECT_LIVING_FACE = "detect_living_face";

    private static final String OPERATION_DETECT_VIDEO_LIVING_FACE = "detect_video_living_face";

    private static final String OPERATION_COMPARE_FACE = "compare_face";

    private static final String ACTION_DETECT_LIVING_FACE = "DetectLivingFace";

    private static final String ACTION_DETECT_VIDEO_LIVING_FACE = "DetectVideoLivingFace";

    private static final String ACTION_COMPARE_FACE = "CompareFace";

    private static final String DEFAULT_ENDPOINT = "https://facebody.cn-shanghai.aliyuncs.com";

    private static final String DEFAULT_REGION = "cn-shanghai";

    private static final String VERSION = "2019-12-30";

    private static final String SIGNATURE_METHOD = "HMAC-SHA1";

    private static final String SIGNATURE_VERSION = "1.0";

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProviderResponse<GenericResponse> execute(ProviderConfigDTO config, ProviderRequest<GenericRequest> request)
    {
        try
        {
            GenericRequest data = request == null ? null : request.getData();
            String operation = data == null ? null : data.getOperation();
            if (isDetectVideoLivingFace(operation))
            {
                // 视频活体只传 VideoUrl，视频文件本身由业务侧先上传到 OSS 并生成可访问地址。
                String videoUrl = readParam(data, "videoUrl");
                if (StringUtils.isBlank(videoUrl))
                {
                    return ProviderResponse.fail("MISSING_VIDEO_URL", "视频活体检测地址不能为空");
                }
                String responseBody = callDetectVideoLivingFace(config, videoUrl.trim());
                GenericResponse response = parseDetectVideoLivingFaceResponse(responseBody);
                response.setRawResponse(ProviderRuntimeSecretUtils.mask(responseBody));
                return ProviderResponse.success(response);
            }

            if (isCompareFace(operation))
            {
                String imageUrlA = readParam(data, "imageUrlA");
                String imageUrlB = readParam(data, "imageUrlB");
                String imageBase64A = readParam(data, "imageBase64A");
                String imageBase64B = readParam(data, "imageBase64B");
                if ((StringUtils.isBlank(imageUrlA) && StringUtils.isBlank(imageBase64A))
                        || (StringUtils.isBlank(imageUrlB) && StringUtils.isBlank(imageBase64B)))
                {
                    return ProviderResponse.fail("MISSING_COMPARE_IMAGE", "人脸比对图片不能为空");
                }
                String responseBody = callCompareFace(config, imageUrlA, imageUrlB, imageBase64A, imageBase64B);
                GenericResponse response = parseCompareFaceResponse(responseBody);
                response.setRawResponse(ProviderRuntimeSecretUtils.mask(responseBody));
                return ProviderResponse.success(response);
            }

            if (!isDetectLivingFace(operation))
            {
                return ProviderResponse.fail("UNSUPPORTED_OPERATION", "阿里云人脸人体暂不支持操作: " + operation);
            }

            String imageBase64 = readParam(data, "imageBase64");
            if (StringUtils.isBlank(imageBase64))
            {
                return ProviderResponse.fail("MISSING_IMAGE", "活体检测图片不能为空");
            }

            String responseBody = callDetectLivingFace(config, normalizeImageBase64(imageBase64));
            GenericResponse response = parseDetectLivingFaceResponse(responseBody);
            response.setRawResponse(ProviderRuntimeSecretUtils.mask(responseBody));
            return ProviderResponse.success(response);
        }
        catch (HttpStatusCodeException e)
        {
            String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            String message = ProviderRuntimeSecretUtils.mask(StringUtils.defaultIfBlank(body, e.getMessage()));
            log.warn("阿里云活体检测调用失败: status={}, body={}", e.getStatusCode(), message);
            return ProviderResponse.fail("ALIYUN_FACEBODY_ERROR", message);
        }
        catch (Exception e)
        {
            log.error("阿里云活体检测调用异常", e);
            return ProviderResponse.fail("ALIYUN_FACEBODY_ERROR", ProviderRuntimeSecretUtils.mask(e.getMessage()));
        }
    }

    @Override
    public String getSupportedConfigType()
    {
        return CONFIG_TYPE_FACE;
    }

    @Override
    public String getSupportedProviderCode()
    {
        return PROVIDER_CODE;
    }

    @Override
    public boolean validateConfig(ProviderConfigDTO config)
    {
        return config != null
                && StringUtils.isNotBlank(config.getAccessKey())
                && StringUtils.isNotBlank(config.getSecretKey());
    }

    private String callDetectLivingFace(ProviderConfigDTO config, String imageBase64) throws Exception
    {
        TreeMap<String, String> params = buildCommonParams(config);
        params.put("Action", ACTION_DETECT_LIVING_FACE);
        params.put("Tasks.1.ImageData", imageBase64);

        String canonical = canonicalize(params);
        String signature = sign(canonical, config.getSecretKey());
        String body = canonical + "&Signature=" + percentEncode(signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint(config), HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String callDetectVideoLivingFace(ProviderConfigDTO config, String videoUrl) throws Exception
    {
        TreeMap<String, String> params = buildCommonParams(config);
        params.put("Action", ACTION_DETECT_VIDEO_LIVING_FACE);
        params.put("VideoUrl", videoUrl);

        // 阿里云视觉智能 RPC 接口使用表单签名提交，不能按普通 JSON 接口调用。
        String canonical = canonicalize(params);
        String signature = sign(canonical, config.getSecretKey());
        String body = canonical + "&Signature=" + percentEncode(signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint(config), HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String callCompareFace(ProviderConfigDTO config, String imageUrlA, String imageUrlB,
            String imageBase64A, String imageBase64B) throws Exception
    {
        TreeMap<String, String> params = buildCommonParams(config);
        params.put("Action", ACTION_COMPARE_FACE);
        putImageParam(params, "A", imageUrlA, imageBase64A);
        putImageParam(params, "B", imageUrlB, imageBase64B);

        String canonical = canonicalize(params);
        String signature = sign(canonical, config.getSecretKey());
        String body = canonical + "&Signature=" + percentEncode(signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint(config), HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private void putImageParam(TreeMap<String, String> params, String suffix, String imageUrl, String imageBase64)
    {
        if (StringUtils.isNotBlank(imageUrl))
        {
            params.put("ImageURL" + suffix, imageUrl.trim());
            return;
        }
        params.put("ImageData" + suffix, normalizeImageBase64(imageBase64));
    }

    private TreeMap<String, String> buildCommonParams(ProviderConfigDTO config)
    {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("Version", VERSION);
        params.put("Format", "JSON");
        params.put("AccessKeyId", config.getAccessKey());
        params.put("SignatureMethod", SIGNATURE_METHOD);
        params.put("SignatureVersion", SIGNATURE_VERSION);
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("Timestamp", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()));
        params.put("RegionId", StringUtils.defaultIfBlank(config.getRegion(), DEFAULT_REGION));
        return params;
    }

    private GenericResponse parseDetectLivingFaceResponse(String responseBody) throws Exception
    {
        JsonNode root = objectMapper.readTree(responseBody);
        Map<String, Object> data = new TreeMap<>();
        data.put("requestId", text(root, "RequestId"));
        data.put("label", text(root, "Label"));
        data.put("suggestion", text(root, "Suggestion"));
        data.put("message", text(root, "Message"));

        Double rate = number(root, "Rate");
        data.put("rate", rate);

        String suggestion = text(root, "Suggestion");
        String label = text(root, "Label");
        boolean pass = "pass".equalsIgnoreCase(suggestion)
                || ("normal".equalsIgnoreCase(label) && rate != null && rate >= 80D);
        data.put("pass", pass);

        return new GenericResponse(data);
    }

    private GenericResponse parseDetectVideoLivingFaceResponse(String responseBody) throws Exception
    {
        JsonNode root = objectMapper.readTree(responseBody);
        Map<String, Object> data = new TreeMap<>();
        data.put("requestId", text(root, "RequestId"));
        data.put("message", text(root, "Message"));

        Double liveConfidence = number(root, "LiveConfidence");
        Double faceConfidence = number(root, "FaceConfidence");
        data.put("liveConfidence", liveConfidence);
        data.put("faceConfidence", faceConfidence);
        // 当前阈值与业务侧 FaceProviderService 保持一致，后续可下沉到能力配置表调整。
        data.put("pass", liveConfidence != null && liveConfidence >= 0.80D);

        JsonNode rect = find(root, "Rect");
        if (rect != null && rect.isArray())
        {
            data.put("rect", objectMapper.convertValue(rect, java.util.List.class));
        }

        return new GenericResponse(data);
    }

    private GenericResponse parseCompareFaceResponse(String responseBody) throws Exception
    {
        JsonNode root = objectMapper.readTree(responseBody);
        Map<String, Object> data = new TreeMap<>();
        data.put("requestId", text(root, "RequestId"));
        data.put("message", text(root, "Message"));

        Double confidence = number(root, "Confidence");
        data.put("confidence", confidence);
        data.put("pass", confidence != null && confidence >= 80D);

        JsonNode thresholds = find(root, "Thresholds");
        if (thresholds != null && thresholds.isArray())
        {
            data.put("thresholds", objectMapper.convertValue(thresholds, java.util.List.class));
        }

        return new GenericResponse(data);
    }

    private boolean isDetectLivingFace(String operation)
    {
        return OPERATION_DETECT_LIVING_FACE.equalsIgnoreCase(operation)
                || ACTION_DETECT_LIVING_FACE.equalsIgnoreCase(operation);
    }

    private boolean isDetectVideoLivingFace(String operation)
    {
        return OPERATION_DETECT_VIDEO_LIVING_FACE.equalsIgnoreCase(operation)
                || ACTION_DETECT_VIDEO_LIVING_FACE.equalsIgnoreCase(operation);
    }

    private boolean isCompareFace(String operation)
    {
        return OPERATION_COMPARE_FACE.equalsIgnoreCase(operation)
                || ACTION_COMPARE_FACE.equalsIgnoreCase(operation);
    }

    private String canonicalize(TreeMap<String, String> params)
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (builder.length() > 0)
            {
                builder.append('&');
            }
            builder.append(percentEncode(entry.getKey()))
                    .append('=')
                    .append(percentEncode(entry.getValue()));
        }
        return builder.toString();
    }

    private String sign(String canonical, String secretKey) throws Exception
    {
        String stringToSign = "POST&%2F&" + percentEncode(canonical);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((secretKey + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return java.util.Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
    }

    private String endpoint(ProviderConfigDTO config)
    {
        return StringUtils.defaultIfBlank(config.getEndpoint(), DEFAULT_ENDPOINT).replaceAll("/+$", "") + "/";
    }

    private String normalizeImageBase64(String imageBase64)
    {
        String value = imageBase64.trim();
        int commaIndex = value.indexOf(',');
        if (value.startsWith("data:") && commaIndex >= 0)
        {
            value = value.substring(commaIndex + 1);
        }
        return value.replaceAll("\\s+", "");
    }

    private String readParam(GenericRequest request, String key)
    {
        Map<String, Object> context = request == null ? null : request.toContextMap();
        if (context == null || context.get(key) == null)
        {
            return null;
        }
        return String.valueOf(context.get(key));
    }

    private String percentEncode(String value)
    {
        try
        {
            return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8.name())
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException("UTF-8 charset is not supported", e);
        }
    }

    private String text(JsonNode root, String fieldName)
    {
        JsonNode node = find(root, fieldName);
        return node == null || node.isNull() ? null : node.asText();
    }

    private Double number(JsonNode root, String fieldName)
    {
        JsonNode node = find(root, fieldName);
        return node == null || !node.isNumber() ? null : node.asDouble();
    }

    private JsonNode find(JsonNode node, String fieldName)
    {
        if (node == null)
        {
            return null;
        }
        JsonNode direct = node.get(fieldName);
        if (direct != null)
        {
            return direct;
        }
        if (node.isObject())
        {
            java.util.Iterator<JsonNode> values = node.elements();
            while (values.hasNext())
            {
                JsonNode found = find(values.next(), fieldName);
                if (found != null)
                {
                    return found;
                }
            }
        }
        if (node.isArray())
        {
            for (JsonNode item : node)
            {
                JsonNode found = find(item, fieldName);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }
}
