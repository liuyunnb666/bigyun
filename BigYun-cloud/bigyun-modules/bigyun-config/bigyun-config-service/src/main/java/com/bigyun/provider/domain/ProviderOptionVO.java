package com.bigyun.provider.domain;

import java.io.Serializable;

/**
 * Provider 下拉选项视图对象。
 * <p>
 * 用于按配置类型返回系统支持的服务商，例如 llm 类型下返回通义千问、DeepSeek 等。
 * 前端使用该接口渲染 Provider 选择器，避免页面和枚举之间出现双写不一致。
 * </p>
 */
public class ProviderOptionVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 服务商编码，例如 aliyun-qwen。 */
    private String code;

    /** 前端配置页使用的服务商编码字段。 */
    private String providerCode;

    /** 服务商名称，例如阿里云通义千问。 */
    private String name;

    /** 前端配置页使用的服务商名称字段。 */
    private String providerName;

    /** 所属配置类型，例如 llm。 */
    private String type;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getProviderCode()
    {
        return providerCode;
    }

    public void setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
