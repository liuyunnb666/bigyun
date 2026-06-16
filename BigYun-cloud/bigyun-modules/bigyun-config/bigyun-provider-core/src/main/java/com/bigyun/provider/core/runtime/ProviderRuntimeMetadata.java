package com.bigyun.provider.core.runtime;

import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.domain.GenericResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider 运行时响应元信息工具。
 */
public final class ProviderRuntimeMetadata
{
    private ProviderRuntimeMetadata()
    {
    }

    public static GenericResponse enrich(GenericResponse response, ProviderRuntimeSnapshot snapshot,
                                         String runtimeSource, boolean fallback, long startTimeMillis)
    {
        if (snapshot == null)
        {
            return enrich(response, null, null, null, null, null, runtimeSource, fallback, startTimeMillis);
        }
        return enrich(response, snapshot.getCapabilityCode(), snapshot.getConfigType(), snapshot.getProviderCode(),
                snapshot.getOperation(), snapshot.getModelCode(), runtimeSource, fallback, startTimeMillis);
    }

    public static GenericResponse enrich(GenericResponse response, String capabilityCode, String configType,
                                         String providerCode, String operation, String modelCode,
                                         String runtimeSource, boolean fallback, long startTimeMillis)
    {
        GenericResponse target = response == null ? new GenericResponse() : response;
        Map<String, Object> data = target.getData();
        if (data == null)
        {
            data = new HashMap<>();
            target.setData(data);
        }
        putIfNotBlank(data, "capabilityCode", capabilityCode);
        putIfNotBlank(data, "configType", configType);
        putIfNotBlank(data, "providerCode", providerCode);
        putIfNotBlank(data, "operation", operation);
        putIfNotBlank(data, "modelCode", modelCode);
        putIfNotBlank(data, "runtimeSource", runtimeSource);
        data.put("fallback", fallback);
        data.put("durationMs", Math.max(System.currentTimeMillis() - startTimeMillis, 0L));
        return target;
    }

    private static void putIfNotBlank(Map<String, Object> data, String key, String value)
    {
        if (StringUtils.isNotBlank(value))
        {
            data.put(key, value);
        }
    }
}
