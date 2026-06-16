package com.bigyun.config.domain;

import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.ProviderPayloadConverter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ProviderExecuteRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Config type must not be blank.")
    private String configType;

    @NotBlank(message = "Provider code must not be blank.")
    private String providerCode;

    @NotBlank(message = "Operation must not be blank.")
    private String operation;

    @NotNull(message = "Provider payload must not be null.")
    private ProviderPayload payload;

    @Deprecated
    private Map<String, Object> params = new HashMap<>();

    public static ProviderExecuteRequest of(String configType, String providerCode,
                                            String operation, ProviderPayload payload)
    {
        ProviderExecuteRequest request = new ProviderExecuteRequest();
        request.setConfigType(configType);
        request.setProviderCode(providerCode);
        request.setOperation(operation);
        request.setPayload(payload);
        return request;
    }

    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    public String getProviderCode()
    {
        return providerCode;
    }

    public void setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
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

    public Map<String, Object> toContextMap()
    {
        return ProviderPayloadConverter.toContextMap(this);
    }
}
