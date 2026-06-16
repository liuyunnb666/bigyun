package com.bigyun.config.domain.payload;

import com.bigyun.config.domain.ProviderExecuteRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProviderPayloadConverter
{
    private ProviderPayloadConverter()
    {
    }

    @SuppressWarnings("deprecation")
    public static Map<String, Object> toContextMap(ProviderExecuteRequest request)
    {
        if (request == null)
        {
            return new LinkedHashMap<>();
        }
        return toContextMap(request.getPayload(), request.getParams());
    }

    public static Map<String, Object> toContextMap(ProviderPayload payload, Map<String, Object> legacyParams)
    {
        Map<String, Object> context = new LinkedHashMap<>();
        if (payload != null)
        {
            Map<String, Object> payloadContext = payload.toContextMap();
            if (payloadContext != null)
            {
                context.putAll(payloadContext);
            }
        }
        if (legacyParams != null)
        {
            context.putAll(legacyParams);
        }
        return context;
    }

    public static ProviderPayload withDefaultModel(ProviderPayload payload, String modelCode)
    {
        if (modelCode == null || modelCode.trim().isEmpty())
        {
            return payload;
        }
        Map<String, Object> context = toContextMap(payload, null);
        // capability 的 modelCode 是后台能力中心的默认绑定，应覆盖业务侧硬编码的兜底 model。
        context.put("model", modelCode);
        return RawProviderPayload.of(context);
    }
}
