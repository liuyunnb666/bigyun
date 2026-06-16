package com.bigyun.config.enums;

import java.util.*;

/**
 * Provider 静态字段兜底定义。
 * <p>
 * 该类只保留不依赖 Spring 容器和数据库的静态字段描述，
 * 用于动态字段元数据不可用时提供最基础的表单兜底能力。
 * 真正的数据库动态字段读取能力由 config-service 内部的运行时组件负责。
 * </p>
 */
public class ProviderFieldConfig {

    /**
     * 字段定义
     */
    public static class Field {
        private String key;           // 字段key
        private String label;         // 字段标签
        private String type;          // 字段类型: text/password/number/select/textarea
        private boolean required;     // 是否必填
        private String placeholder;   // 占位符
        private String defaultValue;  // 默认值
        private List<Option> options; // 下拉选项（type=select时使用）
        private String optionsJson;   // 数据库候选项原始JSON，由接口层解析为options
        private String helpText;      // 帮助文本

        public Field(String key, String label, String type, boolean required) {
            this.key = key;
            this.label = label;
            this.type = type;
            this.required = required;
        }

        public Field placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Field defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Field options(List<Option> options) {
            this.options = options;
            return this;
        }

        public Field optionsJson(String optionsJson) {
            this.optionsJson = optionsJson;
            return this;
        }

        public Field helpText(String helpText) {
            this.helpText = helpText;
            return this;
        }

        // Getters
        public String getKey() { return key; }
        public String getLabel() { return label; }
        public String getType() { return type; }
        public boolean isRequired() { return required; }
        public String getPlaceholder() { return placeholder; }
        public String getDefaultValue() { return defaultValue; }
        public List<Option> getOptions() { return options; }
        public String getOptionsJson() { return optionsJson; }
        public String getHelpText() { return helpText; }
    }

    /**
     * 下拉选项
     */
    public static class Option {
        private String label;
        private String value;

        public Option(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public String getValue() { return value; }
    }

    /**
     * 获取指定服务商的静态兜底字段。
     * <p>
     * 这里只返回代码内置的备用字段定义，不再尝试访问 Spring Bean 或数据库。
     * </p>
     *
     * @param providerCode 服务商编码
     * @return 该服务商的静态兜底字段列表
     */
    public static List<Field> getFieldsByProvider(String providerCode) {
        switch (providerCode) {
            // ========== 存储类 ==========
            case "local":
                return Arrays.asList(
                    new Field("basePath", "存储路径", "text", true)
                        .placeholder("/data/upload")
                        .helpText("文件存储的本地路径"),
                    new Field("domain", "访问域名", "text", false)
                        .placeholder("https://example.com")
                );

            case "minio":
                return Arrays.asList(
                    new Field("endpoint", "服务地址", "text", true)
                        .placeholder("http://localhost:9000"),
                    new Field("accessKey", "Access Key", "text", true),
                    new Field("secretKey", "Secret Key", "password", true),
                    new Field("bucketName", "存储桶名称", "text", true),
                    new Field("domain", "访问域名", "text", false)
                );

            case "aliyun-oss":
            case "tencent-cos":
                return Arrays.asList(
                    new Field("endpoint", "服务地址", "text", true)
                        .placeholder("oss-cn-hangzhou.aliyuncs.com"),
                    new Field("region", "区域", "text", true)
                        .placeholder("cn-hangzhou"),
                    new Field("accessKey", "Access Key", "text", true),
                    new Field("secretKey", "Secret Key", "password", true),
                    new Field("bucketName", "存储桶名称", "text", true),
                    new Field("domain", "自定义域名", "text", false)
                );

            // ========== LLM大语言模型 ==========
            case "openai-gpt":
                return Arrays.asList(
                    new Field("apiKey", "API Key", "password", true)
                        .placeholder("sk-..."),
                    new Field("endpoint", "API地址", "text", false)
                        .placeholder("https://api.openai.com/v1")
                        .defaultValue("https://api.openai.com/v1")
                        .helpText("可选，使用代理或第三方API时填写"),
                    new Field("model", "默认模型", "select", true)
                        .defaultValue("gpt-4")
                        .options(Arrays.asList(
                            new Option("GPT-4", "gpt-4"),
                            new Option("GPT-4 Turbo", "gpt-4-turbo"),
                            new Option("GPT-3.5 Turbo", "gpt-3.5-turbo")
                        )),
                    new Field("maxTokens", "最大Token数", "number", false)
                        .defaultValue("2000")
                        .placeholder("2000"),
                    new Field("temperature", "温度参数", "number", false)
                        .defaultValue("0.7")
                        .placeholder("0.0-2.0")
                );

            case "anthropic-claude":
                return Arrays.asList(
                    new Field("apiKey", "API Key", "password", true)
                        .placeholder("sk-ant-..."),
                    new Field("endpoint", "API地址", "text", false)
                        .placeholder("https://api.anthropic.com")
                        .defaultValue("https://api.anthropic.com"),
                    new Field("model", "默认模型", "select", true)
                        .defaultValue("claude-3-opus-20240229")
                        .options(Arrays.asList(
                            new Option("Claude 3 Opus", "claude-3-opus-20240229"),
                            new Option("Claude 3 Sonnet", "claude-3-sonnet-20240229"),
                            new Option("Claude 3 Haiku", "claude-3-haiku-20240307")
                        )),
                    new Field("maxTokens", "最大Token数", "number", false)
                        .defaultValue("4096")
                );

            case "aliyun-qwen":
                return Arrays.asList(
                    new Field("apiKey", "API Key", "password", true),
                    new Field("endpoint", "API地址", "text", false)
                        .defaultValue("https://dashscope.aliyuncs.com/api/v1"),
                    new Field("model", "默认模型", "select", true)
                        .defaultValue("qwen-max")
                        .options(Arrays.asList(
                            new Option("通义千问Max", "qwen-max"),
                            new Option("通义千问Plus", "qwen-plus"),
                            new Option("通义千问Turbo", "qwen-turbo")
                        ))
                );

            case "baidu-wenxin":
                return Arrays.asList(
                    new Field("apiKey", "API Key", "text", true),
                    new Field("secretKey", "Secret Key", "password", true),
                    new Field("model", "默认模型", "select", true)
                        .defaultValue("ERNIE-Bot-4")
                        .options(Arrays.asList(
                            new Option("文心一言4.0", "ERNIE-Bot-4"),
                            new Option("文心一言3.5", "ERNIE-Bot"),
                            new Option("文心一言Turbo", "ERNIE-Bot-turbo")
                        ))
                );

            case "zhipu-glm":
                return Arrays.asList(
                    new Field("apiKey", "API Key", "password", true),
                    new Field("model", "默认模型", "select", true)
                        .defaultValue("glm-4")
                        .options(Arrays.asList(
                            new Option("GLM-4", "glm-4"),
                            new Option("GLM-3-Turbo", "glm-3-turbo")
                        ))
                );

            // ========== TTS文本转语音 ==========
            case "aliyun-tts":
                return Arrays.asList(
                    new Field("accessKey", "Access Key", "text", true),
                    new Field("secretKey", "Secret Key", "password", true),
                    new Field("appKey", "App Key", "text", true),
                    new Field("voice", "默认音色", "select", false)
                        .defaultValue("xiaoyun")
                        .options(Arrays.asList(
                            new Option("小云（女声）", "xiaoyun"),
                            new Option("小刚（男声）", "xiaogang"),
                            new Option("小美（女声）", "xiaomei")
                        ))
                );

            case "xunfei-tts":
                return Arrays.asList(
                    new Field("appId", "App ID", "text", true),
                    new Field("apiKey", "API Key", "text", true),
                    new Field("apiSecret", "API Secret", "password", true),
                    new Field("voice", "默认音色", "text", false)
                        .defaultValue("xiaoyan")
                );

            // ========== STT语音转文本 ==========
            case "aliyun-asr":
                return Arrays.asList(
                    new Field("accessKey", "Access Key", "text", true),
                    new Field("secretKey", "Secret Key", "password", true),
                    new Field("appKey", "App Key", "text", true)
                );

            case "xunfei-asr":
                return Arrays.asList(
                    new Field("appId", "App ID", "text", true),
                    new Field("apiKey", "API Key", "text", true),
                    new Field("apiSecret", "API Secret", "password", true)
                );

            // ========== 短信服务 ==========
            case "aliyun-sms":
                return Arrays.asList(
                    new Field("accessKey", "Access Key", "text", true),
                    new Field("secretKey", "Secret Key", "password", true),
                    new Field("signName", "短信签名", "text", true)
                        .placeholder("阿里云"),
                    new Field("templateCode", "默认模板", "text", false)
                        .placeholder("SMS_123456789")
                );

            default:
                // 通用字段
                return Arrays.asList(
                    new Field("endpoint", "服务地址", "text", false),
                    new Field("accessKey", "Access Key", "text", false),
                    new Field("secretKey", "Secret Key", "password", false),
                    new Field("apiKey", "API Key", "password", false)
                );
        }
    }

    /**
     * 获取所有支持的服务商列表（按类型分组）
     */
    public static Map<String, List<Map<String, String>>> getAllProviders() {
        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();

        for (ProviderCodeEnum provider : ProviderCodeEnum.values()) {
            String typeCode = provider.getType().getCode();
            result.computeIfAbsent(typeCode, k -> new ArrayList<>());

            Map<String, String> providerInfo = new HashMap<>();
            providerInfo.put("code", provider.getCode());
            providerInfo.put("name", provider.getName());
            providerInfo.put("type", typeCode);

            result.get(typeCode).add(providerInfo);
        }

        return result;
    }
}
