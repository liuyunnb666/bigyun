package com.bigyun.provider.core;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.domain.GenericResponse;

/**
 * Provider 运行时调用适配器。
 * <p>
 * {@code provider-core} 只定义运行时边界。业务模块优先使用 Redis 快照或配置库中读取到的 Provider 配置和
 * API 模板完成调用，config-service 不再是唯一运行时依赖。
 * </p>
 */
public interface ProviderRuntimeInvoker
{
    /**
     * 使用已经解析出的 Provider 配置执行一次具体操作。
     */
    GenericResponse invoke(ProviderConfigDTO config, String operation, ProviderPayload payload);

    /**
     * 使用已发布的运行时快照执行。
     * <p>
     * 默认实现兼容旧调用；配置驱动运行时会覆盖该方法，直接使用快照中的 API 模板，避免 Redis 命中后再次访问
     * 数据库。
     * </p>
     */
    default GenericResponse invoke(ProviderRuntimeSnapshot snapshot, ProviderPayload payload)
    {
        return invoke(snapshot.getProviderConfig(), snapshot.getOperation(), payload);
    }
}
