package com.bigyun.provider.db.runtime;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.ProviderPayloadConverter;
import com.bigyun.config.reader.ConfigReader;
import com.bigyun.provider.core.ProviderRuntimeExecutor;
import com.bigyun.provider.core.ProviderRuntimeInvoker;
import com.bigyun.provider.core.runtime.ProviderRuntimeMetadata;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import com.bigyun.provider.db.domain.ProviderCapabilityEntity;
import com.bigyun.provider.db.mapper.ProviderCapabilityReadMapper;
import com.bigyun.provider.domain.GenericResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB-capable Provider executor.
 * <p>
 * Runtime order: Redis snapshot first, then Provider tables in the config database.
 * Business modules without database access should use SnapshotOnlyProviderRuntimeExecutor.
 * </p>
 */
public class DbProviderRuntimeExecutor implements ProviderRuntimeExecutor
{
    private static final Logger log = LoggerFactory.getLogger(DbProviderRuntimeExecutor.class);

    private final ProviderCapabilityReadMapper capabilityReadMapper;

    private final ConfigReader configReader;

    private final ProviderRuntimeInvoker runtimeInvoker;

    private ProviderRuntimeSnapshotStore snapshotStore;

    private ApplicationEventPublisher eventPublisher;

    public DbProviderRuntimeExecutor(ProviderCapabilityReadMapper capabilityReadMapper,
                                     ConfigReader configReader,
                                     ProviderRuntimeInvoker runtimeInvoker)
    {
        this.capabilityReadMapper = capabilityReadMapper;
        this.configReader = configReader;
        this.runtimeInvoker = runtimeInvoker;
    }

    @Autowired(required = false)
    public void setSnapshotStore(ProviderRuntimeSnapshotStore snapshotStore)
    {
        this.snapshotStore = snapshotStore;
    }

    @Autowired(required = false)
    public void setEventPublisher(ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GenericResponse executeByCapability(String capabilityCode, ProviderPayload payload)
    {
        Optional<ProviderRuntimeSnapshot> snapshot = readCapabilitySnapshot(capabilityCode);
        if (snapshot.isPresent())
        {
            ProviderRuntimeSnapshot value = snapshot.get();
            log.info("Provider runtime snapshot hit: capabilityCode={}, providerCode={}, operation={}, modelCode={}, version={}",
                    capabilityCode, value.getProviderCode(), value.getOperation(), value.getModelCode(),
                    value.getVersion());
            ProviderPayload effectivePayload = ProviderPayloadConverter.withDefaultModel(payload, value.getModelCode());
            long start = System.currentTimeMillis();
            try
            {
                GenericResponse response = runtimeInvoker.invoke(value, effectivePayload);
                GenericResponse enriched = ProviderRuntimeMetadata.enrich(response,
                        value, "redis-runtime", false, start);
                publishEvent(capabilityCode, value.getConfigType(), value.getProviderCode(),
                        value.getOperation(), value.getModelCode(), "redis-runtime", false,
                        true, null, System.currentTimeMillis() - start);
                return enriched;
            }
            catch (Exception e)
            {
                log.warn("Provider runtime snapshot execution failed: capabilityCode={}, providerCode={}, operation={}, modelCode={}, version={}, message={}",
                        capabilityCode, value.getProviderCode(), value.getOperation(), value.getModelCode(),
                        value.getVersion(), e.getMessage());
                publishEvent(capabilityCode, value.getConfigType(), value.getProviderCode(),
                        value.getOperation(), value.getModelCode(), "redis-runtime", false,
                        false, e.getMessage(), System.currentTimeMillis() - start);
                throw e;
            }
        }

        log.info("Provider runtime snapshot miss, fallback to DB: capabilityCode={}", capabilityCode);
        ProviderCapabilityEntity capability = selectDefaultCapability(capabilityCode);
        if (capability == null)
        {
            throw new ServiceException("No enabled provider capability found: " + capabilityCode);
        }
        ProviderPayload effectivePayload = ProviderPayloadConverter.withDefaultModel(payload, capability.getModelCode());
        ProviderConfigDTO config = selectConfig(capability.getConfigType(), capability.getProviderCode());
        long start = System.currentTimeMillis();
        try
        {
            GenericResponse response = runtimeInvoker.invoke(config, capability.getOperation(), effectivePayload);
            GenericResponse enriched = ProviderRuntimeMetadata.enrich(response,
                    capability.getCapabilityCode(), capability.getConfigType(), capability.getProviderCode(),
                    capability.getOperation(), capability.getModelCode(), "db-runtime", false, start);
            publishEvent(capabilityCode, capability.getConfigType(), capability.getProviderCode(),
                    capability.getOperation(), capability.getModelCode(), "db-runtime", false,
                    true, null, System.currentTimeMillis() - start);
            return enriched;
        }
        catch (Exception e)
        {
            publishEvent(capabilityCode, capability.getConfigType(), capability.getProviderCode(),
                    capability.getOperation(), capability.getModelCode(), "db-runtime", false,
                    false, e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    @Override
    public GenericResponse execute(String configType, String providerCode, String operation, ProviderPayload payload)
    {
        Optional<ProviderRuntimeSnapshot> snapshot = readProviderSnapshot(configType, providerCode, operation);
        if (snapshot.isPresent())
        {
            ProviderRuntimeSnapshot value = snapshot.get();
            log.info("Provider runtime snapshot hit: configType={}, providerCode={}, operation={}, modelCode={}, version={}",
                    configType, value.getProviderCode(), value.getOperation(), value.getModelCode(),
                    value.getVersion());
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
                log.warn("Provider runtime snapshot execution failed: configType={}, providerCode={}, operation={}, modelCode={}, version={}, message={}",
                        configType, value.getProviderCode(), value.getOperation(), value.getModelCode(),
                        value.getVersion(), e.getMessage());
                publishEvent(null, value.getConfigType(), value.getProviderCode(),
                        value.getOperation(), value.getModelCode(), "redis-runtime", false,
                        false, e.getMessage(), System.currentTimeMillis() - start);
                throw e;
            }
        }

        log.info("Provider runtime snapshot miss, fallback to DB: configType={}, providerCode={}, operation={}",
                configType, providerCode, operation);
        ProviderConfigDTO config = StringUtils.isBlank(providerCode)
                ? configReader.getDefaultConfig(configType)
                : selectConfig(configType, providerCode);
        long start = System.currentTimeMillis();
        try
        {
            GenericResponse response = runtimeInvoker.invoke(config, operation, payload);
            GenericResponse enriched = ProviderRuntimeMetadata.enrich(response,
                    null, config.getConfigType(), config.getProviderCode(), operation,
                    null, "db-runtime", false, start);
            publishEvent(null, config.getConfigType(), config.getProviderCode(),
                    operation, null, "db-runtime", false,
                    true, null, System.currentTimeMillis() - start);
            return enriched;
        }
        catch (Exception e)
        {
            publishEvent(null, config.getConfigType(), config.getProviderCode(),
                    operation, null, "db-runtime", false,
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

    private Optional<ProviderRuntimeSnapshot> readCapabilitySnapshot(String capabilityCode)
    {
        return snapshotStore == null ? Optional.empty() : snapshotStore.getByCapability(capabilityCode);
    }

    private Optional<ProviderRuntimeSnapshot> readProviderSnapshot(String configType, String providerCode, String operation)
    {
        return snapshotStore == null ? Optional.empty()
                : snapshotStore.getByProvider(configType, providerCode, operation);
    }

    private ProviderCapabilityEntity selectDefaultCapability(String capabilityCode)
    {
        if (StringUtils.isBlank(capabilityCode))
        {
            return null;
        }
        return ProviderRuntimeDataSource.readFromSlave(() -> {
            QueryWrapper<ProviderCapabilityEntity> wrapper = new QueryWrapper<>();
            wrapper.select("*")
                    .eq("capability_code", capabilityCode.trim().toLowerCase())
                    .eq("status", UserConstants.NORMAL)
                    .orderByDesc("is_default")
                    .orderByAsc("priority")
                    .orderByDesc("update_time")
                    .last("limit 1");
            return capabilityReadMapper.selectOne(wrapper);
        });
    }

    private ProviderConfigDTO selectConfig(String configType, String providerCode)
    {
        return configReader.listEnabledConfigs(configType).stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getProviderCode(), providerCode))
                .findFirst()
                .orElseThrow(() -> new ServiceException("No enabled provider config found: "
                        + configType + "/" + providerCode));
    }
}
