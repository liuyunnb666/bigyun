package com.bigyun.provider.service.handler.ocr;

import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.provider.domain.GenericRequest;
import com.bigyun.provider.domain.GenericResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aliyun OCR handler.
 */
@Component
public class AliyunOcrHandler implements IProviderHandler<GenericRequest, GenericResponse>
{
    private static final Logger log = LoggerFactory.getLogger(AliyunOcrHandler.class);

    private static final String CONFIG_TYPE = "ocr";

    private static final String PROVIDER_CODE = "aliyun-ocr";

    private static final String DEFAULT_ENDPOINT = "ocr-api.cn-hangzhou.aliyuncs.com";

    private static final String VERSION = "2021-07-07";

    private static final String DEFAULT_ACTION = "RecognizeGeneral";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProviderResponse<GenericResponse> execute(ProviderConfigDTO config, ProviderRequest<GenericRequest> request)
    {
        try
        {
            GenericRequest genericRequest = request.getData();
            Map<String, Object> params = genericRequest == null ? new HashMap<>() : genericRequest.toContextMap();
            String action = resolveAction(genericRequest, params);
            String imageUrl = firstText(params, "imageUrl", "Url", "url");

            String rawResponse;
            if (StringUtils.isNotBlank(imageUrl))
            {
                rawResponse = doGet(config, action, imageUrl, params);
            }
            else
            {
                byte[] imageBytes = resolveImageBytes(params);
                rawResponse = doPost(config, action, imageBytes, params);
            }

            GenericResponse response = new GenericResponse(parseResponse(rawResponse));
            response.setRawResponse(rawResponse);
            return ProviderResponse.success(response);
        }
        catch (Exception e)
        {
            log.error("Aliyun OCR call failed", e);
            return ProviderResponse.fail("ALIYUN_OCR_ERROR", e.getMessage());
        }
    }

    private String doGet(ProviderConfigDTO config, String action, String imageUrl, Map<String, Object> params)
            throws Exception
    {
        Map<String, String> queryParams = commonParams(config, action);
        queryParams.put("Url", imageUrl);
        putIfNotBlank(queryParams, "Type", firstText(params, "Type", "type"));

        String url = buildSignedUrl(config, "GET", queryParams);
        return request("GET", url, null);
    }

    private String doPost(ProviderConfigDTO config, String action, byte[] imageBytes, Map<String, Object> params)
            throws Exception
    {
        Map<String, String> queryParams = commonParams(config, action);
        putIfNotBlank(queryParams, "Type", firstText(params, "Type", "type"));

        String url = buildSignedUrl(config, "POST", queryParams);
        return request("POST", url, imageBytes);
    }

    private String request(String method, String url, byte[] body) throws IOException
    {
        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod(method);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            if (body != null)
            {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/octet-stream");
                connection.setRequestProperty("Content-Length", String.valueOf(body.length));
                try (OutputStream outputStream = connection.getOutputStream())
                {
                    outputStream.write(body);
                }
            }

            int status = connection.getResponseCode();
            String responseBody = read(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status < 200 || status >= 300)
            {
                throw new IOException("Aliyun OCR HTTP " + status + ": " + responseBody);
            }
            return responseBody;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    private Map<String, Object> parseResponse(String rawResponse) throws IOException
    {
        Map<String, Object> result = objectMapper.readValue(rawResponse, new TypeReference<Map<String, Object>>() {});
        Object data = result.get("Data");
        if (data instanceof String && StringUtils.isNotBlank((String) data))
        {
            try
            {
                Map<String, Object> dataMap = objectMapper.readValue((String) data,
                        new TypeReference<Map<String, Object>>() {});
                result.put("parsedData", dataMap);
                Object content = dataMap.get("content");
                if (content != null)
                {
                    result.put("text", content);
                    result.put("ocrText", content);
                }
                Object wordsInfo = dataMap.get("prism_wordsInfo");
                if (wordsInfo != null)
                {
                    result.put("words", wordsInfo);
                }
            }
            catch (Exception ignored)
            {
                result.put("text", data);
                result.put("ocrText", data);
            }
        }
        return result;
    }

    private Map<String, String> commonParams(ProviderConfigDTO config, String action)
    {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("Action", action);
        params.put("Version", VERSION);
        params.put("Format", "JSON");
        params.put("AccessKeyId", config.getAccessKey());
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("Timestamp", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureVersion", "1.0");
        return params;
    }

    private String buildSignedUrl(ProviderConfigDTO config, String method, Map<String, String> params) throws Exception
    {
        String host = resolveHost(config);
        String canonicalQuery = canonicalQuery(params);
        String stringToSign = method + "&" + percentEncode("/") + "&" + percentEncode(canonicalQuery);
        String signature = hmacSha1(stringToSign, config.getSecretKey() + "&");
        return "https://" + host + "/?" + canonicalQuery + "&Signature=" + percentEncode(signature);
    }

    private String canonicalQuery(Map<String, String> params)
    {
        return params.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> percentEncode(entry.getKey()) + "=" + percentEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String hmacSha1(String data, String secret) throws Exception
    {
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String percentEncode(String value)
    {
        try
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        }
        catch (Exception e)
        {
            return value;
        }
    }

    private byte[] resolveImageBytes(Map<String, Object> params) throws IOException
    {
        String imageBase64 = firstText(params, "imageBase64", "imageData", "image", "body");
        if (StringUtils.isBlank(imageBase64))
        {
            throw new IllegalArgumentException("imageBase64 must not be blank when imageUrl is not provided.");
        }
        int commaIndex = imageBase64.indexOf(',');
        if (commaIndex >= 0)
        {
            imageBase64 = imageBase64.substring(commaIndex + 1);
        }
        return Base64.getDecoder().decode(imageBase64);
    }

    private String resolveAction(GenericRequest request, Map<String, Object> params)
    {
        String operation = request == null ? null : request.getOperation();
        if (StringUtils.isNotBlank(operation) && operation.startsWith("Recognize"))
        {
            return operation;
        }
        String action = firstText(params, "Action", "action", "ocrType", "ocrScene");
        return StringUtils.isNotBlank(action) ? action : DEFAULT_ACTION;
    }

    private String resolveHost(ProviderConfigDTO config)
    {
        String endpoint = StringUtils.isNotBlank(config.getEndpoint()) ? config.getEndpoint() : DEFAULT_ENDPOINT;
        String normalized = endpoint.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://"))
        {
            normalized = URI.create(normalized).getHost();
        }
        int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0)
        {
            normalized = normalized.substring(0, slashIndex);
        }
        return StringUtils.isNotBlank(normalized) ? normalized : DEFAULT_ENDPOINT;
    }

    private String read(InputStream inputStream) throws IOException
    {
        if (inputStream == null)
        {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String firstText(Map<String, Object> params, String... keys)
    {
        if (params == null)
        {
            return null;
        }
        for (String key : keys)
        {
            Object value = params.get(key);
            if (value != null && StringUtils.isNotBlank(value.toString()))
            {
                return value.toString().trim();
            }
        }
        return null;
    }

    private void putIfNotBlank(Map<String, String> target, String key, String value)
    {
        if (StringUtils.isNotBlank(value))
        {
            target.put(key, value);
        }
    }

    @Override
    public String getSupportedConfigType()
    {
        return CONFIG_TYPE;
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
}
