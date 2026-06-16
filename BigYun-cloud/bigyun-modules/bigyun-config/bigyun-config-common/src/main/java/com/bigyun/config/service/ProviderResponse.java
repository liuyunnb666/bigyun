package com.bigyun.config.service;

/**
 * Provider服务响应封装
 *
 * @param <T> 响应数据类型
 */
public class ProviderResponse<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 服务商代码
     */
    private String providerCode;

    public static <T> ProviderResponse<T> success(T data) {
        ProviderResponse<T> response = new ProviderResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ProviderResponse<T> fail(String errorMessage) {
        ProviderResponse<T> response = new ProviderResponse<>();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public static <T> ProviderResponse<T> fail(String errorCode, String errorMessage) {
        ProviderResponse<T> response = new ProviderResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }
}
