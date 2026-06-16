package com.bigyun.provider.core.runtime;

import org.springframework.context.ApplicationEvent;

/**
 * Provider 能力执行事件。
 * <p>
 * 在 {@link com.bigyun.provider.core.ProviderRuntimeExecutor} 的每次执行后发布，
 * 由 config-service 等模块的EventListener消费并写入 sys_provider_capability_log。
 * 事件仅在同 JVM 内传播，跨服务场景由各模块独立实现日志写入。
 */
public class ProviderExecutedEvent extends ApplicationEvent
{
    /** 能力编码，例如 ai-guidance-llm。按 configType/providerCode/operation 执行时可能为空。 */
    private final String capabilityCode;

    /** 配置类型，例如 llm、ocr、face。 */
    private final String configType;

    /** Provider 编码，例如 deepseek、faceplus。 */
    private final String providerCode;

    /** 操作编码，例如 chat、detect。 */
    private final String operation;

    /** 模型编码，例如 deepseek-chat。 */
    private final String modelCode;

    /** 运行时来源，例如 redis-runtime、db-runtime。 */
    private final String runtimeSource;

    /** 是否为 fallback 调用。 */
    private final boolean fallback;

    /** 执行是否成功。 */
    private final boolean success;

    /** 错误信息，成功时为空。 */
    private final String errorMessage;

    /** 执行耗时（毫秒）。 */
    private final long durationMs;

    public ProviderExecutedEvent(Object source, String capabilityCode, String configType,
                                 String providerCode, String operation, String modelCode,
                                 String runtimeSource, boolean fallback,
                                 boolean success, String errorMessage, long durationMs)
    {
        super(source);
        this.capabilityCode = capabilityCode;
        this.configType = configType;
        this.providerCode = providerCode;
        this.operation = operation;
        this.modelCode = modelCode;
        this.runtimeSource = runtimeSource;
        this.fallback = fallback;
        this.success = success;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
    }

    public String getCapabilityCode()
    {
        return capabilityCode;
    }

    public String getConfigType()
    {
        return configType;
    }

    public String getProviderCode()
    {
        return providerCode;
    }

    public String getOperation()
    {
        return operation;
    }

    public String getModelCode()
    {
        return modelCode;
    }

    public String getRuntimeSource()
    {
        return runtimeSource;
    }

    public boolean isFallback()
    {
        return fallback;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public long getDurationMs()
    {
        return durationMs;
    }
}
