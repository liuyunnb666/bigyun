package com.bigyun.provider.domain;

public class ProviderRuntimeSnapshotVO
{
    private String capabilityCode;

    private String configType;

    private String providerCode;

    private String operation;

    private String modelCode;

    private String runtimeSource;

    private String version;

    private Long publishedAt;

    private Boolean hasProviderConfig;

    private Boolean hasApiTemplate;

    public String getCapabilityCode()
    {
        return capabilityCode;
    }

    public void setCapabilityCode(String capabilityCode)
    {
        this.capabilityCode = capabilityCode;
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

    public String getModelCode()
    {
        return modelCode;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    public String getRuntimeSource()
    {
        return runtimeSource;
    }

    public void setRuntimeSource(String runtimeSource)
    {
        this.runtimeSource = runtimeSource;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Long getPublishedAt()
    {
        return publishedAt;
    }

    public void setPublishedAt(Long publishedAt)
    {
        this.publishedAt = publishedAt;
    }

    public Boolean getHasProviderConfig()
    {
        return hasProviderConfig;
    }

    public void setHasProviderConfig(Boolean hasProviderConfig)
    {
        this.hasProviderConfig = hasProviderConfig;
    }

    public Boolean getHasApiTemplate()
    {
        return hasApiTemplate;
    }

    public void setHasApiTemplate(Boolean hasApiTemplate)
    {
        this.hasApiTemplate = hasApiTemplate;
    }
}
