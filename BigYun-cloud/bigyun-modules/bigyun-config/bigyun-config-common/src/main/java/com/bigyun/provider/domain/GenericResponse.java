package com.bigyun.provider.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GenericResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, Object> data;

    private String rawResponse;

    private Integer statusCode;

    private Map<String, String> headers;

    public GenericResponse()
    {
        this.data = new HashMap<>();
    }

    public GenericResponse(Map<String, Object> data)
    {
        this.data = data != null ? data : new HashMap<>();
    }

    /**
     * 构造文本类 Provider 返回，常用于 LLM 本地兜底结果。
     */
    public static GenericResponse content(String content, String provider)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("provider", provider);
        GenericResponse response = new GenericResponse(data);
        response.setRawResponse(content);
        response.setStatusCode(200);
        return response;
    }

    public Object getData(String key)
    {
        return this.data != null ? this.data.get(key) : null;
    }

    public String getStringData(String key)
    {
        Object value = getData(key);
        return value == null ? null : value.toString();
    }

    public Map<String, Object> getData()
    {
        return data;
    }

    public void setData(Map<String, Object> data)
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
