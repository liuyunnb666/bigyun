package com.bigyun.provider.core;

import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.provider.core.runtime.ProviderExecutedEvent;
import com.bigyun.provider.domain.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Provider 运行时执行入口。
 * <p>
 * 业务模块优先按 `capabilityCode` 调用；迁移期仍兼容 `configType/providerCode/operation` 旧链路。
 */
public interface ProviderRuntimeExecutor
{
    /**
     * 按业务能力编码执行当前默认 Provider 绑定。
     */
    GenericResponse executeByCapability(String capabilityCode, ProviderPayload payload);

    /**
     * 按指定 Provider 配置执行，用于兼容旧调用链和明确指定厂商的场景。
     */
    GenericResponse execute(String configType, String providerCode, String operation, ProviderPayload payload);

    /**
     * 发布 Provider 执行事件，供 config-service 等模块的 EventListener 记录调用日志。
     * <p>
     * 日志写入失败不影响主业务流程。
     */
    default void publishExecutionEvent(ApplicationEventPublisher publisher, String capabilityCode,
                                      String configType, String providerCode, String operation,
                                      String modelCode, String runtimeSource, boolean fallback,
                                      boolean success, String errorMessage, long durationMs)
    {
        if (publisher == null)
        {
            return;
        }
        try
        {
            publisher.publishEvent(new ProviderExecutedEvent(this, capabilityCode, configType,
                    providerCode, operation, modelCode, runtimeSource, fallback,
                    success, errorMessage, durationMs));
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(ProviderRuntimeExecutor.class)
                    .warn("Provider execution event publish failed: {}", e.getMessage());
        }
    }
}
