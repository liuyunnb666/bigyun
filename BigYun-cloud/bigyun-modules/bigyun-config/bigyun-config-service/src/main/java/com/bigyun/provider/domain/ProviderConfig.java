package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.annotation.Excel;
import com.bigyun.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("sys_provider_config")
public class ProviderConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "config_id", type = IdType.AUTO)
    @Excel(name = "配置ID")
    private Long configId;

    @Excel(name = "配置类型")
    private String configType;

    @Excel(name = "Provider编码")
    private String providerCode;

    @Excel(name = "Provider名称")
    private String providerName;

    private String endpoint;

    private String region;

    private String bucketName;

    private String accessKey;

    private String secretKey;

    private String domain;

    private String basePath;

    private String extParamsJson;

    private String modelType;

    @Excel(name = "默认配置", readConverterExp = "Y=是,N=否")
    private String isDefault;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @TableField(exist = false)
    private String accessKeyMasked;

    @TableField(exist = false)
    private String secretKeyMasked;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "配置类型不能为空")
    @Size(max = 64, message = "配置类型长度不能超过64个字符")
    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    @NotBlank(message = "Provider编码不能为空")
    @Size(max = 64, message = "Provider编码长度不能超过64个字符")
    public String getProviderCode()
    {
        return providerCode;
    }

    public void setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
    }

    @NotBlank(message = "Provider名称不能为空")
    @Size(max = 100, message = "Provider名称长度不能超过100个字符")
    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }

    @Size(max = 255, message = "访问端点长度不能超过255个字符")
    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    @Size(max = 100, message = "地域长度不能超过100个字符")
    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    @Size(max = 100, message = "Bucket长度不能超过100个字符")
    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    @Size(max = 255, message = "自定义域名长度不能超过255个字符")
    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    @Size(max = 255, message = "基础路径长度不能超过255个字符")
    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getExtParamsJson()
    {
        return extParamsJson;
    }

    public void setExtParamsJson(String extParamsJson)
    {
        this.extParamsJson = extParamsJson;
    }

    public String getModelType()
    {
        return modelType;
    }

    public void setModelType(String modelType)
    {
        this.modelType = modelType;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAccessKeyMasked()
    {
        return accessKeyMasked;
    }

    public void setAccessKeyMasked(String accessKeyMasked)
    {
        this.accessKeyMasked = accessKeyMasked;
    }

    public String getSecretKeyMasked()
    {
        return secretKeyMasked;
    }

    public void setSecretKeyMasked(String secretKeyMasked)
    {
        this.secretKeyMasked = secretKeyMasked;
    }
}