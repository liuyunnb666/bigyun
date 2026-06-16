package com.bigyun.provider.core.runtime;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.ProviderPayloadConverter;
import com.bigyun.provider.core.ProviderRuntimeInvoker;
import com.bigyun.provider.core.domain.ProviderApiTemplateRuntime;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.domain.GenericResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 配置驱动 Provider 本地运行时。
 * <p>
 * 业务服务引入 provider-core 后，可以直接读取已发布的 Provider 配置和 API 模板完成调用，
 * 不再把 config-service 作为唯一运行时依赖。
 */
public class ConfigDrivenProviderRuntimeInvoker implements ProviderRuntimeInvoker
{
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final TypeReference<Map<String, Object>> OBJECT_MAP_TYPE = new TypeReference<>()
    {
    };
    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE = new TypeReference<>()
    {
    };
    private static final String CONTENT_KEY = "content";
    private static final String[] CONTENT_FALLBACK_PATHS = {
            "$.content",
            "$.answer",
            "$.text",
            "$.data.content",
            "$.data.answer",
            "$.data.text",
            "$.output.text",
            "$.choices[0].message.content",
            "$.choices[0].text",
            "$.choices[0].answer_list[0].content",
            "$.choices[0].answer_list[0].answer",
            "$.choices[0].answer_list[0].text"
    };
    private static final Set<String> OPTIONAL_RESPONSE_KEYS = Set.of(
            "answer",
            "usage",
            "model",
            "modelcode",
            "modelname",
            "prompttokens",
            "completiontokens",
            "totaltokens",
            "reasoningcontent",
            "reasoning_content"
    );

    /** Face++ 配置校验和 detect 模板的默认图片，避免保存配置前必须额外传图片。 */
    private static final String FACEPLUS_VALIDATE_IMAGE_BASE64 =
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABgAGADASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/AP/EABQQAQAAAAAAAAAAAAAAAAAAAGD/2gAIAQEAAQUCf//EABQRAQAAAAAAAAAAAAAAAAAAAGD/2gAIAQMBAT8BP//EABQRAQAAAAAAAAAAAAAAAAAAAGD/2gAIAQIBAT8BP//EABQQAQAAAAAAAAAAAAAAAAAAAGD/2gAIAQEABj8Cf//EABQQAQAAAAAAAAAAAAAAAAAAAGD/2gAIAQEAAT8hP//aAAwDAQACAAMAAAAQ/wD/xAAUEQEAAAAAAAAAAAAAAAAAAABg/9oACAEDAQE/ED//xAAUEQEAAAAAAAAAAAAAAAAAAABg/9oACAECAQE/ED//xAAUEAEAAAAAAAAAAAAAAAAAAABg/9oACAEBAAE/ED//2Q==";

    private final ProviderApiTemplateResolver templateResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<ProviderRuntimeAdapter> runtimeAdapters;

    /**
     * 仅消费 Redis 快照的模块可使用无 Mapper 构造器。若快照没有携带 API 模板，会抛出异常并交给远程 fallback。
     */
    public ConfigDrivenProviderRuntimeInvoker()
    {
        this(null, defaultAdapters());
    }

    public ConfigDrivenProviderRuntimeInvoker(ProviderApiTemplateResolver templateResolver)
    {
        this(templateResolver, defaultAdapters());
    }

    public ConfigDrivenProviderRuntimeInvoker(ProviderApiTemplateResolver templateResolver,
                                              List<ProviderRuntimeAdapter> runtimeAdapters)
    {
        this.templateResolver = templateResolver;
        this.runtimeAdapters = runtimeAdapters == null ? Collections.emptyList() : runtimeAdapters;
    }

    @Override
    public GenericResponse invoke(ProviderConfigDTO config, String operation, ProviderPayload payload)
    {
        if (config == null)
        {
            throw new ServiceException("Provider config is null.");
        }
        GenericResponse adapted = invokeAdapterIfSupported(config, operation, payload);
        if (adapted != null)
        {
            return adapted;
        }
        ProviderApiTemplateRuntime template = selectTemplate(config, operation);
        return invoke(config, operation, payload, template);
    }

    @Override
    public GenericResponse invoke(ProviderRuntimeSnapshot snapshot, ProviderPayload payload)
    {
        if (snapshot == null || snapshot.getProviderConfig() == null)
        {
            throw new ServiceException("Provider runtime snapshot is invalid.");
        }
        GenericResponse adapted = invokeAdapterIfSupported(snapshot.getProviderConfig(), snapshot.getOperation(), payload);
        if (adapted != null)
        {
            return adapted;
        }
        ProviderApiTemplateRuntime template = snapshot.getApiTemplate();
        if (template == null)
        {
            return invoke(snapshot.getProviderConfig(), snapshot.getOperation(), payload);
        }
        return invoke(snapshot.getProviderConfig(), snapshot.getOperation(), payload, template);
    }

    private GenericResponse invoke(ProviderConfigDTO config, String operation,
                                   ProviderPayload payload, ProviderApiTemplateRuntime template)
    {
        Map<String, Object> context = buildContext(config, operation, payload);
        String url = render(template.getUrlTemplate(), context);
        HttpHeaders headers = buildHeaders(template.getHeadersJson(), context);
        addAuthentication(headers, template.getAuthType(), template.getAuthConfigJson(), context);
        Object body = buildBody(template.getBodyType(), template.getBodyTemplate(), context);
        ensureBodyContentType(headers, template.getBodyType());
        String responseBody = executeHttp(template, url, headers, body);
        Map<String, Object> parsedData = parseResponse(responseBody, template.getResponseType(),
                template.getResponseMapping(), context);

        GenericResponse response = new GenericResponse(parsedData);
        response.setRawResponse(ProviderRuntimeSecretUtils.mask(responseBody));
        response.setStatusCode(200);
        return response;
    }

    private ProviderApiTemplateRuntime selectTemplate(ProviderConfigDTO config, String operation)
    {
        if (templateResolver == null)
        {
            throw new ServiceException("Provider api template resolver is not available for local runtime.");
        }
        ProviderApiTemplateRuntime template = templateResolver.resolve(config, operation);
        if (template == null)
        {
            throw new ServiceException("No enabled provider api template found: "
                    + config.getConfigType() + "/" + config.getProviderCode() + "/" + operation);
        }
        return template;
    }

    private Map<String, Object> buildContext(ProviderConfigDTO config, String operation, ProviderPayload payload)
    {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("endpoint", config.getEndpoint());
        context.put("baseUrl", config.getEndpoint());
        context.put("region", config.getRegion());
        context.put("bucketName", config.getBucketName());
        context.put("accessKey", config.getAccessKey());
        context.put("apiKey", config.getAccessKey());
        context.put("secretKey", config.getSecretKey());
        context.put("domain", config.getDomain());
        context.put("basePath", config.getBasePath());

        if (StringUtils.isNotBlank(config.getExtParamsJson()))
        {
            try
            {
                context.putAll(objectMapper.readValue(config.getExtParamsJson(), OBJECT_MAP_TYPE));
            }
            catch (Exception e)
            {
                throw new ServiceException("Provider ext params parse failed: " + e.getMessage());
            }
        }

        context.putAll(ProviderPayloadConverter.toContextMap(payload, null));
        syncCommonAliases(context);
        if ("face".equalsIgnoreCase(config.getConfigType()) && "faceplus".equalsIgnoreCase(config.getProviderCode()))
        {
            context.putIfAbsent("returnAttributes", "gender,age");
            context.putIfAbsent("validationImageBase64", FACEPLUS_VALIDATE_IMAGE_BASE64);
        }
        Object fileData = context.get("fileData");
        if (fileData instanceof byte[])
        {
            context.put("fileSize", ((byte[]) fileData).length);
        }
        context.put("operation", operation);
        context.put("timestamp", System.currentTimeMillis());
        context.put("timestampSeconds", System.currentTimeMillis() / 1000);
        context.put("date", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).format(new Date()));
        context.put("dateISO", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date()));
        return context;
    }

    private void syncCommonAliases(Map<String, Object> context)
    {
        String endpoint = text(context.get("endpoint"));
        String baseUrl = text(context.get("baseUrl"));
        if (StringUtils.isBlank(endpoint) && StringUtils.isNotBlank(baseUrl))
        {
            context.put("endpoint", baseUrl);
            endpoint = baseUrl;
        }
        if (StringUtils.isBlank(baseUrl) && StringUtils.isNotBlank(endpoint))
        {
            context.put("baseUrl", endpoint);
            baseUrl = endpoint;
        }

        String accessKey = text(context.get("accessKey"));
        String apiKey = text(context.get("apiKey"));
        if (StringUtils.isBlank(apiKey) && StringUtils.isNotBlank(accessKey))
        {
            context.put("apiKey", accessKey);
        }
        if (StringUtils.isBlank(accessKey) && StringUtils.isNotBlank(apiKey))
        {
            context.put("accessKey", apiKey);
        }

        String chatCompletionsUrl = buildChatCompletionsUrl(firstNonBlank(baseUrl, endpoint));
        if (StringUtils.isNotBlank(chatCompletionsUrl))
        {
            context.put("chatCompletionsUrl", chatCompletionsUrl);
        }
    }

    private String buildChatCompletionsUrl(String baseUrl)
    {
        if (StringUtils.isBlank(baseUrl))
        {
            return null;
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/"))
        {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.endsWith("/chat/completions") ? normalized : normalized + "/chat/completions";
    }

    private HttpHeaders buildHeaders(String headersJson, Map<String, Object> context)
    {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isBlank(headersJson))
        {
            return headers;
        }
        try
        {
            Map<String, String> headerMap = objectMapper.readValue(headersJson, STRING_MAP_TYPE);
            for (Map.Entry<String, String> entry : headerMap.entrySet())
            {
                headers.add(entry.getKey(), render(entry.getValue(), context));
            }
            return headers;
        }
        catch (Exception e)
        {
            throw new ServiceException("Provider headers template parse failed: " + e.getMessage());
        }
    }

    private void addAuthentication(HttpHeaders headers, String authType,
                                   String authConfigJson, Map<String, Object> context)
    {
        if (StringUtils.isBlank(authType) || "none".equalsIgnoreCase(authType))
        {
            return;
        }
        try
        {
            Map<String, String> authConfig = StringUtils.isBlank(authConfigJson)
                    ? new HashMap<>() : objectMapper.readValue(authConfigJson, STRING_MAP_TYPE);
            if ("basic".equalsIgnoreCase(authType))
            {
                String username = renderConfigValue(authConfig.get("username"), context);
                String password = renderConfigValue(authConfig.get("password"), context);
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
                {
                    throw new ServiceException("Basic auth username or password is blank.");
                }
                String credentials = username + ":" + password;
                headers.set("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
                return;
            }
            if ("bearer".equalsIgnoreCase(authType))
            {
                String token = renderConfigValue(firstNonBlank(authConfig.get("token"), authConfig.get("tokenField")),
                        context);
                if (StringUtils.isBlank(token))
                {
                    throw new ServiceException("Bearer token is blank.");
                }
                headers.set("Authorization", "Bearer " + token);
                return;
            }
            if ("apikey".equalsIgnoreCase(authType))
            {
                String headerName = firstNonBlank(authConfig.get("header"), "X-API-Key");
                String apiKey = renderConfigValue(authConfig.get("value"), context);
                if (StringUtils.isBlank(apiKey))
                {
                    throw new ServiceException("Api key is blank.");
                }
                headers.set(headerName, apiKey);
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("Provider auth config parse failed: " + e.getMessage());
        }
    }

    private Object buildBody(String bodyType, String bodyTemplate, Map<String, Object> context)
    {
        if (StringUtils.isBlank(bodyTemplate))
        {
            return null;
        }
        String effectiveBodyType = StringUtils.isBlank(bodyType) ? "json" : bodyType.trim().toLowerCase(Locale.ROOT);
        String rendered = render(bodyTemplate, context);
        try
        {
            if ("json".equals(effectiveBodyType))
            {
                objectMapper.readTree(rendered);
                return rendered;
            }
            if ("form".equals(effectiveBodyType))
            {
                Map<String, Object> formMap = objectMapper.readValue(rendered, OBJECT_MAP_TYPE);
                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                for (Map.Entry<String, Object> entry : formMap.entrySet())
                {
                    if (entry.getValue() != null)
                    {
                        formData.add(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
                return formData;
            }
            if ("multipart".equals(effectiveBodyType))
            {
                Map<String, Object> multipartMap = objectMapper.readValue(rendered, OBJECT_MAP_TYPE);
                MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
                for (Map.Entry<String, Object> entry : multipartMap.entrySet())
                {
                    Object value = resolveMultipartValue(entry.getKey(), entry.getValue(), context);
                    if (value != null)
                    {
                        multipartData.add(entry.getKey(), value);
                    }
                }
                return multipartData;
            }
            return rendered;
        }
        catch (Exception e)
        {
            throw new ServiceException("Provider body template parse failed: " + e.getMessage());
        }
    }

    private Object resolveMultipartValue(String key, Object value, Map<String, Object> context)
    {
        if ("file".equalsIgnoreCase(key) && context.get("fileData") instanceof byte[])
        {
            byte[] fileData = (byte[]) context.get("fileData");
            return new ByteArrayResource(fileData)
            {
                @Override
                public String getFilename()
                {
                    return firstNonBlank(text(context.get("fileName")), "provider-upload.bin");
                }
            };
        }
        if (value instanceof String)
        {
            return render((String) value, context);
        }
        return value;
    }

    private String executeHttp(ProviderApiTemplateRuntime template, String url, HttpHeaders headers, Object body)
    {
        int retryTimes = template.getRetryTimes() == null ? 0 : Math.max(template.getRetryTimes(), 0);
        Exception lastFailure = null;
        for (int attempt = 0; attempt <= retryTimes; attempt++)
        {
            try
            {
                RestTemplate restTemplate = buildRestTemplate(template.getTimeout());
                HttpMethod method = HttpMethod.valueOf(defaultHttpMethod(template.getHttpMethod()));
                HttpEntity<?> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
                return response.getBody();
            }
            catch (HttpStatusCodeException e)
            {
                String bodyText = e.getResponseBodyAsString();
                throw new ServiceException("Provider call failed with status "
                        + e.getStatusCode().value() + ": "
                        + ProviderRuntimeSecretUtils.mask(abbreviate(bodyText, 500)));
            }
            catch (Exception e)
            {
                lastFailure = e;
            }
        }
        throw new ServiceException("Provider call failed: "
                + (lastFailure == null ? "unknown error"
                : ProviderRuntimeSecretUtils.mask(lastFailure.getMessage())));
    }

    private RestTemplate buildRestTemplate(Integer timeout)
    {
        int effectiveTimeout = timeout == null || timeout <= 0 ? (int) Duration.ofSeconds(30).toMillis() : timeout;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(effectiveTimeout);
        factory.setReadTimeout(effectiveTimeout);
        return new RestTemplate(factory);
    }

    private void ensureBodyContentType(HttpHeaders headers, String bodyType)
    {
        if (headers.getContentType() != null)
        {
            return;
        }
        String effectiveBodyType = StringUtils.isBlank(bodyType) ? "json" : bodyType.trim().toLowerCase(Locale.ROOT);
        if ("json".equals(effectiveBodyType))
        {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
    }

    private Map<String, Object> parseResponse(String responseBody, String responseType,
                                              String responseMapping, Map<String, Object> context)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (StringUtils.isBlank(responseBody))
        {
            result.put("raw", null);
            return result;
        }
        String effectiveType = StringUtils.isBlank(responseType) ? "json" : responseType.trim().toLowerCase(Locale.ROOT);
        if (!"json".equals(effectiveType))
        {
            result.put("content", responseBody);
            result.put("raw", responseBody);
            return result;
        }
        try
        {
            Object jsonObject = objectMapper.readValue(responseBody, Object.class);
            assertNoProviderError(jsonObject);
            result.put("raw", jsonObject);
            if (StringUtils.isBlank(responseMapping))
            {
                if (jsonObject instanceof Map<?, ?> map)
                {
                    for (Map.Entry<?, ?> entry : map.entrySet())
                    {
                        if (entry.getKey() != null)
                        {
                            result.put(String.valueOf(entry.getKey()), entry.getValue());
                        }
                    }
                }
                return result;
            }

            Map<String, String> mapping = objectMapper.readValue(responseMapping, STRING_MAP_TYPE);
            for (Map.Entry<String, String> entry : mapping.entrySet())
            {
                String expression = render(entry.getValue(), context);
                try
                {
                    Object value = readExpression(jsonObject, expression);
                    if (isContentKey(entry.getKey()) && value == null)
                    {
                        value = tryReadFirst(jsonObject, CONTENT_FALLBACK_PATHS);
                    }
                    result.put(entry.getKey(), value);
                }
                catch (Exception e)
                {
                    if (isContentKey(entry.getKey()))
                    {
                        Object content = tryReadFirst(jsonObject, CONTENT_FALLBACK_PATHS);
                        result.put(entry.getKey(), content);
                        continue;
                    }
                    if (isOptionalResponseKey(entry.getKey()))
                    {
                        result.put(entry.getKey(), null);
                        continue;
                    }
                    throw e;
                }
            }
            if (!result.containsKey("content"))
            {
                Object content = tryReadFirst(jsonObject, CONTENT_FALLBACK_PATHS);
                if (content != null)
                {
                    result.put("content", content);
                }
            }
            return result;
        }
        catch (Exception e)
        {
            throw new ServiceException("Provider response parse failed: " + e.getMessage());
        }
    }

    private Object readExpression(Object jsonObject, String expression)
    {
        if ("$".equals(expression))
        {
            return jsonObject;
        }
        return JsonPath.read(jsonObject, expression);
    }

    private void assertNoProviderError(Object jsonObject)
    {
        if (!(jsonObject instanceof Map<?, ?> map) || !map.containsKey("code"))
        {
            return;
        }
        String code = text(map.get("code"));
        if (StringUtils.isBlank(code) || "0".equals(code) || "200".equals(code)
                || "ok".equalsIgnoreCase(code) || "success".equalsIgnoreCase(code))
        {
            return;
        }
        String message = firstNonBlank(text(map.get("message")), text(map.get("error")),
                text(map.get("errorMessage")), "unknown provider error");
        throw new ServiceException("Provider returned error: " + abbreviate(message, 300));
    }

    private boolean isContentKey(String key)
    {
        return CONTENT_KEY.equalsIgnoreCase(key);
    }

    private boolean isOptionalResponseKey(String key)
    {
        return key != null && OPTIONAL_RESPONSE_KEYS.contains(key.toLowerCase(Locale.ROOT));
    }

    private Object tryReadFirst(Object jsonObject, String... paths)
    {
        for (String path : paths)
        {
            try
            {
                Object value = JsonPath.read(jsonObject, path);
                if (value != null)
                {
                    return value;
                }
            }
            catch (Exception ignored)
            {
                // Ignore and try next path.
            }
        }
        return null;
    }

    private String defaultHttpMethod(String method)
    {
        return StringUtils.isBlank(method) ? "POST" : method.trim().toUpperCase(Locale.ROOT);
    }

    private String renderConfigValue(String template, Map<String, Object> context)
    {
        String value = render(template, context);
        return StringUtils.isBlank(value) ? null : value;
    }

    private String render(String template, Map<String, Object> context)
    {
        if (template == null)
        {
            return null;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find())
        {
            String key = matcher.group(1);
            Object value = context.get(key);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String text(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private String abbreviate(String text, int maxLength)
    {
        if (text == null || text.length() <= maxLength)
        {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private GenericResponse invokeAdapterIfSupported(ProviderConfigDTO config, String operation, ProviderPayload payload)
    {
        for (ProviderRuntimeAdapter adapter : runtimeAdapters)
        {
            if (adapter.supports(config, operation))
            {
                return adapter.invoke(config, operation, payload);
            }
        }
        return null;
    }

    private static List<ProviderRuntimeAdapter> defaultAdapters()
    {
        return Collections.singletonList(new AliyunFacebodyRuntimeAdapter());
    }
}
