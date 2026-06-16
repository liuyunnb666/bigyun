package com.bigyun.config.domain;

import java.io.Serializable;
import java.util.Map;

public class ProviderExecuteResponse<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private T data;

    private String rawResponse;

    private Integer statusCode;

    private Map<String, String> headers;

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public String getRawResponse()
    {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse)
    {
        this.rawResponse = rawResponse;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }
}
