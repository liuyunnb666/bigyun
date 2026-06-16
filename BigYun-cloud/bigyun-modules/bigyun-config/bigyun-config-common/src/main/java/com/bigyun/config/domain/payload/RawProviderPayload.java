package com.bigyun.config.domain.payload;

import java.util.LinkedHashMap;
import java.util.Map;

public class RawProviderPayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private Map<String, Object> fields = new LinkedHashMap<>();

    public RawProviderPayload()
    {
    }

    public RawProviderPayload(Map<String, Object> fields)
    {
        if (fields != null)
        {
            this.fields.putAll(fields);
        }
    }

    public static RawProviderPayload of(Map<String, Object> fields)
    {
        return new RawProviderPayload(fields);
    }

    public Map<String, Object> getFields()
    {
        return fields;
    }

    public void setFields(Map<String, Object> fields)
    {
        this.fields = fields == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fields);
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        return new LinkedHashMap<>(fields);
    }
}
