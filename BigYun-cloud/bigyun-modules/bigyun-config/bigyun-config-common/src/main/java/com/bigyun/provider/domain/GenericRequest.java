package com.bigyun.provider.domain;

import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.ProviderPayloadConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GenericRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String operation;

    private ProviderPayload payload;

    @Deprecated
    private Map<String, Object> params;

    private byte[] fileData;

    private String contentType;

    public GenericRequest()
    {
        this.params = new HashMap<>();
    }

    public GenericRequest(String operation)
    {
        this.operation = operation;
        this.params = new HashMap<>();
    }

    @Deprecated
    public GenericRequest(String operation, Map<String, Object> params)
    {
        this.operation = operation;
        this.params = params != null ? params : new HashMap<>();
    }

    public GenericRequest(String operation, ProviderPayload payload)
    {
        this.operation = operation;
        this.payload = payload;
        this.params = new HashMap<>();
    }

    @Deprecated
    public GenericRequest addParam(String key, Object value)
    {
        if (this.params == null)
        {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }

    @Deprecated
    public GenericRequest addParams(Map<String, Object> params)
    {
        if (params != null)
        {
            if (this.params == null)
            {
                this.params = new HashMap<>();
            }
            this.params.putAll(params);
        }
        return this;
    }

    public Object getParam(String key)
    {
        Map<String, Object> context = toContextMap();
        return context.get(key);
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public ProviderPayload getPayload()
    {
        return payload;
    }

    public void setPayload(ProviderPayload payload)
    {
        this.payload = payload;
    }

    @Deprecated
    public Map<String, Object> getParams()
    {
        return params;
    }

    @Deprecated
    public void setParams(Map<String, Object> params)
    {
        this.params = params == null ? new HashMap<>() : params;
    }

    public void setContextMap(Map<String, Object> context)
    {
        this.params = context == null ? new HashMap<>() : context;
    }

    public Map<String, Object> toContextMap()
    {
        return ProviderPayloadConverter.toContextMap(payload, params);
    }

    public byte[] getFileData()
    {
        return fileData;
    }

    public void setFileData(byte[] fileData)
    {
        this.fileData = fileData;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }
}
