package com.bigyun.provider.core.runtime;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.provider.core.domain.ProviderApiTemplateRuntime;

/**
 * Optional DB-side resolver for runtime templates.
 * <p>
 * provider-core defines the boundary only. Implementations that read Provider
 * tables live in DB-capable modules such as config-common/config-service/ai-service.
 * </p>
 */
public interface ProviderApiTemplateResolver
{
    ProviderApiTemplateRuntime resolve(ProviderConfigDTO config, String operation);
}
