package com.bigyun.config.service;

import java.util.Map;

/**
 * Provider服务请求封装
 *
 * @param <T> 请求数据类型
 */
public class ProviderRequest<T> {

    /**
     * 操作类型（如：chat、upload、recognize等）
     */
    private String action;

    /**
     * 请求数据
     */
    private T data;

    /**
     * 额外参数
     */
    private Map<String, Object> params;

    public ProviderRequest() {
    }

    public ProviderRequest(String action, T data) {
        this.action = action;
        this.data = data;
    }

    public static <T> ProviderRequest<T> of(String action, T data) {
        return new ProviderRequest<>(action, data);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public ProviderRequest<T> addParam(String key, Object value) {
        if (this.params == null) {
            this.params = new java.util.HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }
}
