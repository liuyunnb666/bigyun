package com.bigyun.provider.listener;

import com.bigyun.provider.core.runtime.ProviderExecutedEvent;
import com.bigyun.provider.domain.ProviderCapabilityLog;
import com.bigyun.provider.mapper.ProviderCapabilityLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Provider 执行事件监听器。
 * <p>
 * 消费 {@link ProviderExecutedEvent}，将每次 Provider 能力调用记录写入
 * sys_provider_capability_log 表。日志写入失败不影响主业务流程。
 * <p>
 * 注意：AI 模块和 Auth 模块在各自 JVM 中发布事件，但它们没有 log mapper 依赖，
 * 事件只在 config-service 的 JVM 内被消费（同模块内的 executeInternal 路径）。
 * AI/Auth 模块的调用日志需要后续通过异步方式（MQ 或 Feign 回调）补充。
 */
@Component
public class ProviderCapabilityLogEventListener
{
    private static final Logger log = LoggerFactory.getLogger(ProviderCapabilityLogEventListener.class);

    private final ProviderCapabilityLogMapper capabilityLogMapper;

    public ProviderCapabilityLogEventListener(ProviderCapabilityLogMapper capabilityLogMapper)
    {
        this.capabilityLogMapper = capabilityLogMapper;
    }

    /**
     * 处理 Provider 执行事件，写入调用日志。
     *
     * @param event Provider 执行事件
     */
    @EventListener
    public void onProviderExecuted(ProviderExecutedEvent event)
    {
        ProviderCapabilityLog entry = new ProviderCapabilityLog();
        entry.setCapabilityCode(event.getCapabilityCode());
        entry.setConfigType(event.getConfigType());
        entry.setProviderCode(event.getProviderCode());
        entry.setOperation(event.getOperation());
        entry.setModelCode(event.getModelCode());
        entry.setRuntimeSource(event.getRuntimeSource());
        entry.setFallback(String.valueOf(event.isFallback()));
        entry.setDurationMs(event.getDurationMs());
        entry.setStatus(event.isSuccess() ? "success" : "fail");
        entry.setErrorMessage(truncate(event.getErrorMessage(), 500));

        try
        {
            capabilityLogMapper.insert(entry);
        }
        catch (Exception e)
        {
            log.warn("Provider capability log save failed: capabilityCode={}, providerCode={}, error={}",
                    event.getCapabilityCode(), event.getProviderCode(), e.getMessage());
        }
    }

    private String truncate(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
