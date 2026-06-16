package com.bigyun.provider.db.runtime;

import com.bigyun.provider.core.domain.ProviderApiTemplateRuntime;
import com.bigyun.provider.db.domain.ProviderApiTemplateEntity;
import org.springframework.beans.BeanUtils;

/**
 * Converts DB entities into DB-free runtime models.
 */
public final class ProviderRuntimeDbConverter
{
    private ProviderRuntimeDbConverter()
    {
    }

    public static ProviderApiTemplateRuntime toRuntime(ProviderApiTemplateEntity entity)
    {
        if (entity == null)
        {
            return null;
        }
        ProviderApiTemplateRuntime runtime = new ProviderApiTemplateRuntime();
        BeanUtils.copyProperties(entity, runtime);
        return runtime;
    }
}
