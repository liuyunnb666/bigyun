package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.web.domain.BaseEntity;

@TableName("sys_provider_capability_log")
public class ProviderCapabilityLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private String capabilityCode;

    private String configType;

    private String providerCode;

    private String operation;

    private String modelCode;

    private String runtimeSource;

    private String fallback;

    private Long durationMs;

    private String status;

    private String statusCode;

    private String errorMessage;

    private String requestSummary;

    private String responseSummary;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

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

    public String getFallback()
    {
        return fallback;
    }

    public void setFallback(String fallback)
    {
        this.fallback = fallback;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(String statusCode)
    {
        this.statusCode = statusCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getRequestSummary()
    {
        return requestSummary;
    }

    public void setRequestSummary(String requestSummary)
    {
        this.requestSummary = requestSummary;
    }

    public String getResponseSummary()
    {
        return responseSummary;
    }

    public void setResponseSummary(String responseSummary)
    {
        this.responseSummary = responseSummary;
    }
}
