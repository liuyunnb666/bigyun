package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.web.domain.BaseEntity;

@TableName("sys_provider_integration_guide")
public class ProviderIntegrationGuide extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "guide_id", type = IdType.AUTO)
    private Long guideId;

    private String configType;

    private String providerCode;

    private String providerName;

    private String vendorName;

    private String serviceName;

    private String integrationStatus;

    private String authType;

    private String endpointHint;

    private String docsUrl;

    private String consoleUrl;

    private String requiredFieldsJson;

    private String optionalFieldsJson;

    private String supportedOperationsJson;

    private String capabilityCodesJson;

    private String modelCodesJson;

    private String adapterNote;

    private Integer sortOrder;

    private String status;

    public Long getGuideId()
    {
        return guideId;
    }

    public void setGuideId(Long guideId)
    {
        this.guideId = guideId;
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

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public String getIntegrationStatus()
    {
        return integrationStatus;
    }

    public void setIntegrationStatus(String integrationStatus)
    {
        this.integrationStatus = integrationStatus;
    }

    public String getAuthType()
    {
        return authType;
    }

    public void setAuthType(String authType)
    {
        this.authType = authType;
    }

    public String getEndpointHint()
    {
        return endpointHint;
    }

    public void setEndpointHint(String endpointHint)
    {
        this.endpointHint = endpointHint;
    }

    public String getDocsUrl()
    {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl)
    {
        this.docsUrl = docsUrl;
    }

    public String getConsoleUrl()
    {
        return consoleUrl;
    }

    public void setConsoleUrl(String consoleUrl)
    {
        this.consoleUrl = consoleUrl;
    }

    public String getRequiredFieldsJson()
    {
        return requiredFieldsJson;
    }

    public void setRequiredFieldsJson(String requiredFieldsJson)
    {
        this.requiredFieldsJson = requiredFieldsJson;
    }

    public String getOptionalFieldsJson()
    {
        return optionalFieldsJson;
    }

    public void setOptionalFieldsJson(String optionalFieldsJson)
    {
        this.optionalFieldsJson = optionalFieldsJson;
    }

    public String getSupportedOperationsJson()
    {
        return supportedOperationsJson;
    }

    public void setSupportedOperationsJson(String supportedOperationsJson)
    {
        this.supportedOperationsJson = supportedOperationsJson;
    }

    public String getCapabilityCodesJson()
    {
        return capabilityCodesJson;
    }

    public void setCapabilityCodesJson(String capabilityCodesJson)
    {
        this.capabilityCodesJson = capabilityCodesJson;
    }

    public String getModelCodesJson()
    {
        return modelCodesJson;
    }

    public void setModelCodesJson(String modelCodesJson)
    {
        this.modelCodesJson = modelCodesJson;
    }

    public String getAdapterNote()
    {
        return adapterNote;
    }

    public void setAdapterNote(String adapterNote)
    {
        this.adapterNote = adapterNote;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
