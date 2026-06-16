package com.bigyun.provider.core.snapshot;

import com.bigyun.common.core.utils.StringUtils;

/**
 * Provider 运行时快照 Redis key 规则。
 * <p>
 * Redis 只保存已经发布的运行时副本，数据库仍然是 Provider 配置真源。
 */
public final class ProviderRuntimeSnapshotKeys
{
    /** 按业务能力读取当前默认 Provider 绑定。 */
    public static final String CAPABILITY_PREFIX = "bigyun:provider:runtime:capability:";

    /** 按明确 Provider 操作读取运行时配置。 */
    public static final String PROVIDER_PREFIX = "bigyun:provider:runtime:provider:";

    /** 当前运行时快照发布版本，用于快速判断 Redis 是否已刷新。 */
    public static final String VERSION_KEY = "bigyun:provider:runtime:version";

    private ProviderRuntimeSnapshotKeys()
    {
    }

    public static String capabilityKey(String capabilityCode)
    {
        return CAPABILITY_PREFIX + normalize(capabilityCode);
    }

    public static String providerKey(String configType, String providerCode, String operation)
    {
        return PROVIDER_PREFIX + normalize(configType) + ":" + normalize(providerCode) + ":" + normalize(operation);
    }

    private static String normalize(String value)
    {
        return StringUtils.isBlank(value) ? "" : value.trim().toLowerCase();
    }
}
