package com.bigyun.provider.core;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.ProviderPayloadConverter;
import com.bigyun.provider.core.runtime.ProviderRuntimeMetadata;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import com.bigyun.provider.domain.GenericResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 只消费 Redis 运行时快照的 Provider 执行器。
 * <p>
 * 适用于暂时不接入配置库数据源的业务模块，例如后续的人脸链路。它只负责
 * “Redis 快照 -> core HTTP 执行”，快照缺失或执行失败时由业务侧继续走远程 config-service fallback。
 * </p>
 */
public class SnapshotOnlyProviderRuntimeExecutor implements ProviderRuntimeExecutor
{
    private final ProviderRuntimeSnapshotStore snapshotStore;

    private final ProviderRuntimeInvoker runtimeInvoker;

    private ApplicationEventPublisher eventPublisher;

    public SnapshotOnlyProviderRuntimeExecutor(ProviderRuntimeSnapshotStore snapshotStore,
                                               ProviderRuntimeInvoker runtimeInvoker)
    {
        this.snapshotStore = snapshotStore;
        this.runtimeInvoker = runtimeInvoker;
    }

    @Autowired(required = false)
    public void setEventPublisher(ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GenericResponse executeByCapability(String capabilityCode, ProviderPayload payload)
    {
        ProviderRuntimeSnapshot snapshot = snapshotStore.getByCapability(capabilityCode)
                .orElseThrow(() -> new ServiceException("Provider runtime snapshot not found: " + capabilityCode));
        ProviderPayload effectivePayload = ProviderPayloadConverter.withDefaultModel(payload, snapshot.getModelCode());
        long start = System.currentTimeMillis();
        try
        {
            GenericResponse response = runtimeInvoker.invoke(snapshot, effectivePayload);
            GenericResponse enriched = ProviderRuntimeMetadata.enrich(response,
                    snapshot, "redis-runtime", false, start);
            publishEvent(capabilityCode, snapshot.getConfigType(), snapshot.getProviderCode(),
                    snapshot.getOperation(), snapshot.getModelCode(), "redis-runtime", false,
                    true, null, System.currentTimeMillis() - start);
            return enriched;
        }
        catch (Exception e)
        {
            publishEvent(capabilityCode, snapshot.getConfigType(), snapshot.getProviderCode(),
                    snapshot.getOperation(), snapshot.getModelCode(), "redis-runtime", false,
                    false, e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    @Override
    public GenericResponse execute(String configType, String providerCode, String operation, ProviderPayload payload)
    {
        Optional<ProviderRuntimeSnapshot> snapshot = snapshotStore.getByProvider(configType, providerCode, operation);
        if (!snapshot.isPresent())
        {
            throw new ServiceException("Provider runtime snapshot not found: "
                    + configType + "/" + providerCode + "/" + operation);
        }
        ProviderRuntimeSnapshot value = snapshot.get();
        long start = System.currentTimeMillis();
        try
        {
            GenericResponse response = runtimeInvoker.invoke(value, payload);
            GenericResponse enriched = ProviderRuntimeMetadata.enrich(response,
                    value, "redis-runtime", false, start);
            publishEvent(null, value.getConfigType(), value.getProviderCode(),
                    value.getOperation(), value.getModelCode(), "redis-runtime", false,
                    true, null, System.currentTimeMillis() - start);
            return enriched;
        }
        catch (Exception e)
        {
            publishEvent(null, value.getConfigType(), value.getProviderCode(),
                    value.getOperation(), value.getModelCode(), "redis-runtime", false,
                    false, e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    private void publishEvent(String capabilityCode, String configType, String providerCode,
                              String operation, String modelCode, String runtimeSource,
                              boolean fallback, boolean success, String errorMessage, long durationMs)
    {
        publishExecutionEvent(eventPublisher, capabilityCode, configType, providerCode,
                operation, modelCode, runtimeSource, fallback, success, errorMessage, durationMs);
    }
}
