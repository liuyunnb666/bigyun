package com.bigyun.provider.db.runtime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.provider.core.domain.ProviderApiTemplateRuntime;
import com.bigyun.provider.core.runtime.ProviderApiTemplateResolver;
import com.bigyun.provider.db.domain.ProviderApiTemplateEntity;
import com.bigyun.provider.db.mapper.ProviderApiTemplateReadMapper;

/**
 * DB-backed runtime template resolver.
 */
public class DbProviderApiTemplateResolver implements ProviderApiTemplateResolver
{
    private final ProviderApiTemplateReadMapper templateReadMapper;

    public DbProviderApiTemplateResolver(ProviderApiTemplateReadMapper templateReadMapper)
    {
        this.templateReadMapper = templateReadMapper;
    }

    @Override
    public ProviderApiTemplateRuntime resolve(ProviderConfigDTO config, String operation)
    {
        if (config == null)
        {
            return null;
        }
        ProviderApiTemplateEntity template = ProviderRuntimeDataSource.readFromSlave(() ->
                templateReadMapper.selectOne(new LambdaQueryWrapper<ProviderApiTemplateEntity>()
                        .eq(ProviderApiTemplateEntity::getConfigType, config.getConfigType())
                        .eq(ProviderApiTemplateEntity::getProviderCode, config.getProviderCode())
                        .eq(ProviderApiTemplateEntity::getOperation, operation)
                        .eq(ProviderApiTemplateEntity::getIsEnabled, "1")
                        .last("limit 1")));
        return ProviderRuntimeDbConverter.toRuntime(template);
    }
}
