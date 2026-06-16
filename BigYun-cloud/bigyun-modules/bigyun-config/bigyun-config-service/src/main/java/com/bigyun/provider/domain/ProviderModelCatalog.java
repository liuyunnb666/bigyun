package com.bigyun.provider.domain;

import com.bigyun.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Provider 模型目录管理端对象。
 */
public class ProviderModelCatalog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long modelId;

    @NotBlank(message = "Model code must not be blank.")
    private String modelCode;

    @NotBlank(message = "Model name must not be blank.")
    private String modelName;

    @NotBlank(message = "Config type must not be blank.")
    private String configType;

    @NotBlank(message = "Provider code must not be blank.")
    private String providerCode;

    private String deploymentMode;

    private String capabilityTags;

    @Min(value = 0, message = "Context window must not be negative.")
    private Integer contextWindow;

    private String inputSchemaJson;

    private String outputSchemaJson;

    private String pricingJson;

    private String docsUrl;

    private String isEnabled;

    @Min(value = 0, message = "Sort order must not be negative.")
    private Integer sortOrder;

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
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

    public String getDeploymentMode()
    {
        return deploymentMode;
    }

    public void setDeploymentMode(String deploymentMode)
    {
        this.deploymentMode = deploymentMode;
    }

    public String getCapabilityTags()
    {
        return capabilityTags;
    }

    public void setCapabilityTags(String capabilityTags)
    {
        this.capabilityTags = capabilityTags;
    }

    public Integer getContextWindow()
    {
        return contextWindow;
    }

    public void setContextWindow(Integer contextWindow)
    {
        this.contextWindow = contextWindow;
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

    public String getPricingJson()
    {
        return pricingJson;
    }

    public void setPricingJson(String pricingJson)
    {
        this.pricingJson = pricingJson;
    }

    public String getDocsUrl()
    {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl)
    {
        this.docsUrl = docsUrl;
    }

    public String getIsEnabled()
    {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

}
