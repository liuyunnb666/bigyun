package com.bigyun.config.domain;

import java.io.Serializable;

public class CapabilityModelRelationDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long relationId;

    private Long capabilityId;

    private Long targetCapabilityId;

    private String capabilityCode;

    private String capabilityName;

    private String bindingDisplayName;

    private String businessScene;

    private String capabilityLayer;

    private String configType;

    private String providerCode;

    private String targetCapabilityCode;

    private String targetCapabilityName;

    private String targetModelDisplayName;

    private String targetProviderCode;

    private String modelCode;

    private String modelName;

    private String relationType;

    private String sameBusinessFlag;

    private String relationReason;

    private String status;

    private Integer sortOrder;

    private String remark;

    public Long getRelationId()
    {
        return relationId;
    }

    public void setRelationId(Long relationId)
    {
        this.relationId = relationId;
    }

    public Long getCapabilityId()
    {
        return capabilityId;
    }

    public void setCapabilityId(Long capabilityId)
    {
        this.capabilityId = capabilityId;
    }

    public Long getTargetCapabilityId()
    {
        return targetCapabilityId;
    }

    public void setTargetCapabilityId(Long targetCapabilityId)
    {
        this.targetCapabilityId = targetCapabilityId;
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

    public String getBindingDisplayName()
    {
        return bindingDisplayName;
    }

    public void setBindingDisplayName(String bindingDisplayName)
    {
        this.bindingDisplayName = bindingDisplayName;
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

    public String getTargetCapabilityCode()
    {
        return targetCapabilityCode;
    }

    public void setTargetCapabilityCode(String targetCapabilityCode)
    {
        this.targetCapabilityCode = targetCapabilityCode;
    }

    public String getTargetCapabilityName()
    {
        return targetCapabilityName;
    }

    public void setTargetCapabilityName(String targetCapabilityName)
    {
        this.targetCapabilityName = targetCapabilityName;
    }

    public String getTargetModelDisplayName()
    {
        return targetModelDisplayName;
    }

    public void setTargetModelDisplayName(String targetModelDisplayName)
    {
        this.targetModelDisplayName = targetModelDisplayName;
    }

    public String getTargetProviderCode()
    {
        return targetProviderCode;
    }

    public void setTargetProviderCode(String targetProviderCode)
    {
        this.targetProviderCode = targetProviderCode;
    }

    public String getModelCode()
    {
        return modelCode;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getRelationType()
    {
        return relationType;
    }

    public void setRelationType(String relationType)
    {
        this.relationType = relationType;
    }

    public String getSameBusinessFlag()
    {
        return sameBusinessFlag;
    }

    public void setSameBusinessFlag(String sameBusinessFlag)
    {
        this.sameBusinessFlag = sameBusinessFlag;
    }

    public String getRelationReason()
    {
        return relationReason;
    }

    public void setRelationReason(String relationReason)
    {
        this.relationReason = relationReason;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
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
