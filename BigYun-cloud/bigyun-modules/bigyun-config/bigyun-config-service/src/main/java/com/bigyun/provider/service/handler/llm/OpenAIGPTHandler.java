package com.bigyun.provider.service.handler.llm;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.config.service.handler.llm.LLMRequest;
import com.bigyun.config.service.handler.llm.LLMResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenAI GPT服务处理器
 */
@Component
public class OpenAIGPTHandler implements IProviderHandler<LLMRequest, LLMResponse> {

    private static final Logger log = LoggerFactory.getLogger(OpenAIGPTHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProviderResponse<LLMResponse> execute(ProviderConfigDTO config, ProviderRequest<LLMRequest> request) {
        try {
            // 解析扩展参数
            Map<String, Object> extParams = parseExtParams(config.getExtParamsJson());
            String apiKey = resolveApiKey(config, extParams);
            String endpoint = (String) extParams.getOrDefault("endpoint", "https://api.openai.com/v1");
            String model = (String) extParams.getOrDefault("model", "gpt-4");

            LLMRequest llmRequest = request.getData();

            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", llmRequest.getMessages());

            if (extParams.containsKey("maxTokens")) {
                requestBody.put("max_tokens", extParams.get("maxTokens"));
            }
            if (extParams.containsKey("temperature")) {
                requestBody.put("temperature", extParams.get("temperature"));
            }

            // TODO: 实际调用OpenAI API
            // 这里需要使用HTTP客户端调用OpenAI API
            log.info("调用OpenAI API: endpoint={}, model={}", endpoint, model);

            // 模拟响应（实际项目中需要真实调用）
            LLMResponse llmResponse = new LLMResponse();
            llmResponse.setContent("这是模拟的GPT响应");
            llmResponse.setModel(model);
            llmResponse.setUsage(new LLMResponse.Usage(100, 50, 150));

            return ProviderResponse.success(llmResponse);

        } catch (Exception e) {
            log.error("调用OpenAI API失败", e);
            return ProviderResponse.fail("OPENAI_ERROR", e.getMessage());
        }
    }

    @Override
    public String getSupportedConfigType() {
        return "llm";
    }

    @Override
    public String getSupportedProviderCode() {
        return "openai-gpt";
    }

    @Override
    public boolean validateConfig(ProviderConfigDTO config) {
        try {
            Map<String, Object> extParams = parseExtParams(config.getExtParamsJson());
            return resolveApiKey(config, extParams) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 LLM API Key。
     * <p>
     * 历史实现把 apiKey 放在 extParamsJson 中，新配置页会把密钥统一保存到 ProviderConfig.accessKey。
     * 为了兼容旧数据和新数据，这里优先读 extParamsJson.apiKey，没有时回退到 accessKey。
     * </p>
     *
     * @param config Provider配置实体
     * @param extParams 扩展参数JSON解析后的Map
     * @return 可用于调用模型接口的API Key；如果未配置则返回null
     */
    private String resolveApiKey(ProviderConfigDTO config, Map<String, Object> extParams) {
        Object apiKey = extParams.get("apiKey");
        if (apiKey != null && !apiKey.toString().trim().isEmpty()) {
            return apiKey.toString();
        }
        if (config.getAccessKey() != null && !config.getAccessKey().trim().isEmpty()) {
            return config.getAccessKey();
        }
        return null;
    }

    private Map<String, Object> parseExtParams(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, HashMap.class);
        } catch (Exception e) {
            log.error("解析扩展参数失败", e);
            return new HashMap<>();
        }
    }
}
