package com.bigyun.config.domain;

import java.io.Serializable;

public class ProviderCapabilityDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long capabilityId;

    private String capabilityCode;

    private String capabilityName;

    private String businessScene;

    private String capabilityLayer;

    private String configType;

    private String providerCode;

    private String operation;

    private String modelCode;

    private Integer priority;

    private String isDefault;

    private String status;

    private String inputSchemaJson;

    private String outputSchemaJson;

    private String routingHint;

    private String fallbackCapabilityCode;

    private String remark;

    public Long getCapabilityId()
    {
        return capabilityId;
    }

    public void setCapabilityId(Long capabilityId)
    {
        this.capabilityId = capabilityId;
    }

    public String getCapabilityCode()
    {
        return capabilityCode;
    }

    public void setCapabilityCode(String capabilityCode)
    {
        this.capabilityCode = capabilityCode;
    }

    public String getCapabilityName()
    {
        return capabilityName;
    }

    public void setCapabilityName(String capabilityName)
    {
        this.capabilityName = capabilityName;
    }

    public String getBusinessScene()
    {
        return businessScene;
    }

    public void setBusinessScene(String businessScene)
    {
        this.businessScene = businessScene;
    }

    public String getCapabilityLayer()
    {
        return capabilityLayer;
    }

    public void setCapabilityLayer(String capabilityLayer)
    {
        this.capabilityLayer = capabilityLayer;
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

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getInputSchemaJson()
    {
        return inputSchemaJson;
    }

    public void setInputSchemaJson(String inputSchemaJson)
    {
        this.inputSchemaJson = inputSchemaJson;
    }

    public String getOutputSchemaJson()
    {
        return outputSchemaJson;
    }

    public void setOutputSchemaJson(String outputSchemaJson)
    {
        this.outputSchemaJson = outputSchemaJson;
    }

    public String getRoutingHint()
    {
        return routingHint;
    }

    public void setRoutingHint(String routingHint)
    {
        this.routingHint = routingHint;
    }

    public String getFallbackCapabilityCode()
    {
        return fallbackCapabilityCode;
    }

    public void setFallbackCapabilityCode(String fallbackCapabilityCode)
    {
        this.fallbackCapabilityCode = fallbackCapabilityCode;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
