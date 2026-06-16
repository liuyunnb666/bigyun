package com.bigyun.provider.db.snapshot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.ProviderConfigEntity;
import com.bigyun.config.mapper.ProviderConfigReadMapper;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import com.bigyun.provider.db.domain.ProviderApiTemplateEntity;
import com.bigyun.provider.db.domain.ProviderCapabilityEntity;
import com.bigyun.provider.db.mapper.ProviderApiTemplateReadMapper;
import com.bigyun.provider.db.mapper.ProviderCapabilityReadMapper;
import com.bigyun.provider.db.runtime.ProviderRuntimeDataSource;
import com.bigyun.provider.db.runtime.ProviderRuntimeDbConverter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * Publishes Provider runtime snapshots from DB truth into Redis.
 */
public class ProviderRuntimeSnapshotPublisher
{
    private static final Logger log = LoggerFactory.getLogger(ProviderRuntimeSnapshotPublisher.class);

    private final ProviderRuntimeSnapshotStore snapshotStore;

    private final ProviderCapabilityReadMapper capabilityReadMapper;

    private final ProviderConfigReadMapper configReadMapper;

    private final ProviderApiTemplateReadMapper templateReadMapper;

    public ProviderRuntimeSnapshotPublisher(ProviderRuntimeSnapshotStore snapshotStore,
                                            ProviderCapabilityReadMapper capabilityReadMapper,
                                            ProviderConfigReadMapper configReadMapper,
                                            ProviderApiTemplateReadMapper templateReadMapper)
    {
        this.snapshotStore = snapshotStore;
        this.capabilityReadMapper = capabilityReadMapper;
        this.configReadMapper = configReadMapper;
        this.templateReadMapper = templateReadMapper;
    }

    public boolean publishCapability(String capabilityCode)
    {
        if (StringUtils.isBlank(capabilityCode))
        {
            return false;
        }
        return ProviderRuntimeDataSource.readFromMaster(() -> publishCapabilityOnCurrentDataSource(capabilityCode));
    }

    public int publishConfigType(String configType)
    {
        if (StringUtils.isBlank(configType))
        {
            return publishAll();
        }
        return ProviderRuntimeDataSource.readFromMaster(() -> {
            int count = 0;
            for (String capabilityCode : selectCapabilityCodes(configType))
            {
                if (publishCapabilityOnCurrentDataSource(capabilityCode))
                {
                    count++;
                }
            }
            count += publishProviderSnapshots(configType);
            log.info("Provider runtime snapshots published by configType: configType={}, count={}", configType, count);
            return count;
        });
    }

    public int publishAll()
    {
        return ProviderRuntimeDataSource.readFromMaster(() -> {
            int count = 0;
            for (String capabilityCode : selectCapabilityCodes(null))
            {
                if (publishCapabilityOnCurrentDataSource(capabilityCode))
                {
                    count++;
                }
            }
            count += publishProviderSnapshots(null);
            log.info("Provider runtime snapshots published all: count={}", count);
            return count;
        });
    }

    private boolean publishCapabilityOnCurrentDataSource(String capabilityCode)
    {
        ProviderCapabilityEntity capability = selectDefaultCapability(capabilityCode);
        if (capability == null)
        {
            snapshotStore.deleteCapabilitySnapshot(capabilityCode);
            return false;
        }
        ProviderConfigDTO config = selectConfig(capability.getConfigType(), capability.getProviderCode());
        ProviderApiTemplateEntity template = selectTemplate(capability.getConfigType(),
                capability.getProviderCode(), capability.getOperation());
        if (config == null || template == null)
        {
            snapshotStore.deleteCapabilitySnapshot(capabilityCode);
            return false;
        }
        return snapshotStore.publishCapabilitySnapshot(buildSnapshot(capability, config, template));
    }

    private int publishProviderSnapshots(String configType)
    {
        List<ProviderApiTemplateEntity> templates = templateReadMapper.selectList(
                new LambdaQueryWrapper<ProviderApiTemplateEntity>()
                        .eq(StringUtils.isNotBlank(configType),
                                ProviderApiTemplateEntity::getConfigType, normalize(configType))
                        .eq(ProviderApiTemplateEntity::getIsEnabled, "1"));
        int count = 0;
        for (ProviderApiTemplateEntity template : templates)
        {
            ProviderConfigDTO config = selectConfig(template.getConfigType(), template.getProviderCode());
            if (config == null)
            {
                snapshotStore.deleteProviderSnapshot(template.getConfigType(), template.getProviderCode(),
                        template.getOperation());
                continue;
            }
            ProviderRuntimeSnapshot snapshot = buildSnapshot(null, config, template);
            if (snapshotStore.publishProviderSnapshot(snapshot))
            {
                count++;
            }
        }
        return count;
    }

    private Set<String> selectCapabilityCodes(String configType)
    {
        List<ProviderCapabilityEntity> capabilities = capabilityReadMapper.selectList(
                new QueryWrapper<ProviderCapabilityEntity>()
                        .select("capability_code")
                        .eq("status", UserConstants.NORMAL)
                        .eq(StringUtils.isNotBlank(configType), "config_type", normalize(configType)));
        Set<String> result = new LinkedHashSet<>();
        for (ProviderCapabilityEntity capability : capabilities)
        {
            if (StringUtils.isNotBlank(capability.getCapabilityCode()))
            {
                result.add(capability.getCapabilityCode());
            }
        }
        return result;
    }

    private ProviderCapabilityEntity selectDefaultCapability(String capabilityCode)
    {
        QueryWrapper<ProviderCapabilityEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*")
                .eq("capability_code", normalize(capabilityCode))
                .eq("status", UserConstants.NORMAL)
                .orderByDesc("is_default")
                .orderByAsc("priority")
                .orderByDesc("update_time")
                .last("limit 1");
        return capabilityReadMapper.selectOne(wrapper);
    }

    private ProviderConfigDTO selectConfig(String configType, String providerCode)
    {
        ProviderConfigEntity entity = configReadMapper.selectOne(
                new LambdaQueryWrapper<ProviderConfigEntity>()
                        .eq(ProviderConfigEntity::getConfigType, normalize(configType))
                        .eq(ProviderConfigEntity::getProviderCode, normalize(providerCode))
                        .eq(ProviderConfigEntity::getStatus, UserConstants.NORMAL)
                        .last("limit 1"));
        if (entity == null)
        {
            return null;
        }
        ProviderConfigDTO dto = new ProviderConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private ProviderApiTemplateEntity selectTemplate(String configType, String providerCode, String operation)
    {
        return templateReadMapper.selectOne(new LambdaQueryWrapper<ProviderApiTemplateEntity>()
                .eq(ProviderApiTemplateEntity::getConfigType, normalize(configType))
                .eq(ProviderApiTemplateEntity::getProviderCode, normalize(providerCode))
                .eq(ProviderApiTemplateEntity::getOperation, normalize(operation))
                .eq(ProviderApiTemplateEntity::getIsEnabled, "1")
                .last("limit 1"));
    }

    private ProviderRuntimeSnapshot buildSnapshot(ProviderCapabilityEntity capability,
                                                  ProviderConfigDTO config,
                                                  ProviderApiTemplateEntity template)
    {
        ProviderRuntimeSnapshot snapshot = new ProviderRuntimeSnapshot();
        if (capability != null)
        {
            snapshot.setCapabilityCode(capability.getCapabilityCode());
            snapshot.setModelCode(capability.getModelCode());
        }
        snapshot.setConfigType(config.getConfigType());
        snapshot.setProviderCode(config.getProviderCode());
        snapshot.setOperation(template.getOperation());
        snapshot.setProviderConfig(config);
        snapshot.setApiTemplate(ProviderRuntimeDbConverter.toRuntime(template));
        snapshot.setPublishedAt(System.currentTimeMillis());
        snapshot.setVersion(String.valueOf(snapshot.getPublishedAt()));
        return snapshot;
    }

    private String normalize(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim().toLowerCase();
    }
}
