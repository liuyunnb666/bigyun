package com.bigyun.config.domain;

import java.io.Serializable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Provider capability call log DTO.
 * <p>
 * Only sanitized route metadata and summaries are allowed here. Do not put prompt,
 * secret keys, tokens, or raw third-party responses into this DTO.
 */
public class ProviderCapabilityLogDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Capability code must not be blank.")
    @Size(max = 100, message = "Capability code length must not exceed 100.")
    private String capabilityCode;

    @NotBlank(message = "Config type must not be blank.")
    @Size(max = 64, message = "Config type length must not exceed 64.")
    private String configType;

    @NotBlank(message = "Provider code must not be blank.")
    @Size(max = 100, message = "Provider code length must not exceed 100.")
    private String providerCode;

    @NotBlank(message = "Operation must not be blank.")
    @Size(max = 100, message = "Operation length must not exceed 100.")
    private String operation;

    @Size(max = 100, message = "Model code length must not exceed 100.")
    private String modelCode;

    @Size(max = 64, message = "Runtime source length must not exceed 64.")
    private String runtimeSource;

    @Size(max = 16, message = "Fallback length must not exceed 16.")
    private String fallback;

    @Min(value = 0, message = "Duration must not be negative.")
    private Long durationMs;

    @Size(max = 32, message = "Status length must not exceed 32.")
    private String status;

    @Size(max = 64, message = "Status code length must not exceed 64.")
    private String statusCode;

    @Size(max = 500, message = "Error message length must not exceed 500.")
    private String errorMessage;

    @Size(max = 500, message = "Request summary length must not exceed 500.")
    private String requestSummary;

    @Size(max = 500, message = "Response summary length must not exceed 500.")
    private String responseSummary;

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
