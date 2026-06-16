package com.bigyun.provider.core.runtime;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.provider.domain.GenericResponse;

/**
 * Provider 运行时专用适配器。
 * <p>
 * 普通 HTTP 能力继续走配置模板；少数不能用通用模板表达的调用，例如阿里云 RPC 签名，
 * 通过 adapter 放在 provider-core 内执行，避免业务模块强依赖 config-service 在线。
 * </p>
 */
public interface ProviderRuntimeAdapter
{
    boolean supports(ProviderConfigDTO config, String operation);

    GenericResponse invoke(ProviderConfigDTO config, String operation, ProviderPayload payload);
}
