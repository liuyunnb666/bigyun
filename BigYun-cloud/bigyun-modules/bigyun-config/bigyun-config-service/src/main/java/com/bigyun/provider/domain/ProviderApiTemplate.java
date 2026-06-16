// -*- coding: utf-8 -*-
package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Provider API模板配置实体类
 *
 * 功能说明：
 * 用于存储第三方服务的API调用配置，实现零代码添加新Provider。
 * 通过配置HTTP请求的各个要素（URL、请求头、请求体、响应解析等），
 * 使得系统可以动态调用任意第三方API，无需编写专门的Handler代码。
 *
 * 核心特性：
 * 1. 支持变量模板语法（${variable}），可在运行时动态替换
 * 2. 支持多种HTTP方法（GET/POST/PUT/DELETE/PATCH）
 * 3. 支持多种认证方式（Basic/Bearer/API Key/自定义签名）
 * 4. 支持JSONPath表达式解析响应
 * 5. 支持超时和重试配置
 *
 * 使用场景：
 * - 添加新的存储服务（如七牛云、AWS S3）
 * - 添加新的LLM服务（如Claude、Gemini）
 * - 添加新的翻译服务、OCR服务等
 *
 * 对应数据库表：sys_provider_api_template
 *
 * @author BigYun
 * @date 2024-05-22
 */
@TableName("sys_provider_api_template")
public class ProviderApiTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID（主键，自增）
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 配置类型
     * 取值：storage（存储）、llm（大语言模型）、tts（语音合成）、stt（语音识别）、
     *      image（图像生成）、translation（翻译）、ocr（文字识别）等
     */
    @NotBlank(message = "Config type must not be blank.")
    private String configType;

    /**
     * Provider编码
     * 取值：aliyun-oss、tencent-cos、openai-gpt、anthropic-claude等
     * 与sys_provider_config表的provider_code字段对应
     */
    @NotBlank(message = "Provider code must not be blank.")
    private String providerCode;

    /**
     * 操作类型
     * 取值：upload（上传）、delete（删除）、chat（聊天）、translate（翻译）等
     * 不同的配置类型支持不同的操作
     */
    @NotBlank(message = "Operation must not be blank.")
    private String operation;

    /**
     * HTTP请求方法
     * 取值：GET、POST、PUT、DELETE、PATCH
     */
    @NotBlank(message = "HTTP method must not be blank.")
    private String httpMethod;

    /**
     * URL模板
     * 支持变量语法，如：https://${bucketName}.${endpoint}/${filePath}
     * 运行时会从配置和请求中提取变量值进行替换
     */
    @NotBlank(message = "URL template must not be blank.")
    private String urlTemplate;

    /**
     * 请求头JSON配置
     * JSON格式，支持变量，如：
     * {"Content-Type": "${contentType}", "Authorization": "Bearer ${accessKey}"}
     */
    private String headersJson;

    /**
     * 请求体类型
     * 取值：json（JSON格式）、form（表单）、multipart（多部分表单）、
     *      binary（二进制）、text（纯文本）
     */
    private String bodyType;

    /**
     * 请求体模板
     * 根据bodyType不同，格式也不同：
     * - json: JSON格式字符串，支持变量
     * - binary: 通常为 ${fileData}
     * - form: 表单字段定义
     */
    private String bodyTemplate;

    /**
     * 响应类型
     * 取值：json（JSON格式）、xml（XML格式）、text（纯文本）
     */
    private String responseType;

    /**
     * 响应字段映射JSON
     * 定义如何从响应中提取字段，支持JSONPath表达式，如：
     * {"fileUrl": "$.data.url", "fileSize": "$.data.size"}
     * 或直接使用变量：{"fileUrl": "https://${bucketName}.${endpoint}/${filePath}"}
     */
    private String responseMapping;

    /**
     * 认证类型
     * 取值：none（无认证）、basic（基础认证）、bearer（Bearer Token）、
     *      apikey（API Key）、custom（自定义签名）
     */
    private String authType;

    /**
     * 认证配置JSON
     * 根据authType不同，配置内容也不同：
     * - basic: {"username": "${accessKey}", "password": "${secretKey}"}
     * - bearer: {"token": "${accessKey}"}
     * - apikey: {"header": "X-API-Key", "value": "${accessKey}"}
     * - custom: {"signatureMethod": "hmac-sha1", "signatureFields": [...]}
     */
    private String authConfigJson;

    /**
     * 超时时间（毫秒）
     * 默认30000（30秒）
     */
    @Min(value = 1, message = "Timeout must be greater than 0.")
    private Integer timeout;

    /**
     * 重试次数
     * 0表示不重试，大于0表示失败后重试的次数
     */
    @Min(value = 0, message = "Retry times must not be negative.")
    private Integer retryTimes;

    /**
     * 是否启用
     * 0=否，1=是
     * 可用于临时禁用某个模板而不删除
     */
    private String isEnabled;

    // ==================== Getter和Setter方法 ====================

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public String getHeadersJson() {
        return headersJson;
    }

    public void setHeadersJson(String headersJson) {
        this.headersJson = headersJson;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseMapping() {
        return responseMapping;
    }

    public void setResponseMapping(String responseMapping) {
        this.responseMapping = responseMapping;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthConfigJson() {
        return authConfigJson;
    }

    public void setAuthConfigJson(String authConfigJson) {
        this.authConfigJson = authConfigJson;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return "ProviderApiTemplate{" +
                "templateId=" + templateId +
                ", configType='" + configType + '\'' +
                ", providerCode='" + providerCode + '\'' +
                ", operation='" + operation + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", urlTemplate='" + urlTemplate + '\'' +
                ", authType='" + authType + '\'' +
                ", timeout=" + timeout +
                ", retryTimes=" + retryTimes +
                ", isEnabled='" + isEnabled + '\'' +
                '}';
    }
}
