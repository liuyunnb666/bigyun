package com.bigyun.config.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Provider 配置数据库实体类。
 * <p>
 * 映射数据库表 {@code sys_provider_config}，存储各类第三方服务 Provider 的配置信息。
 * 继承自 {@link BaseEntity}，自动获得 createBy/createTime/updateBy/updateTime/remark 等基础字段。
 * </p>
 * <p>
 * <b>支持的 Provider 类型（configType）：</b>
 * <ul>
 *   <li>{@code llm} - 大语言模型服务（如 OpenAI 等）</li>
 *   <li>{@code storage} - 对象存储服务（如阿里云 OSS、腾讯云 COS、Minio、本地）</li>
 * </ul>
 * </p>
 *
 * @author bigyun
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("sys_provider_config")
public class ProviderConfigEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置主键 ID，自增 */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /** 配置类型：llm-大语言模型，storage-对象存储 */
    private String configType;

    /** Provider 编码（如 openai、aliyun_oss、local，统一小写） */
    private String providerCode;

    /** Provider 显示名称 */
    private String providerName;

    /** API 端点 URL */
    private String endpoint;

    /** 区域（如 OSS 的 oss-cn-hangzhou） */
    private String region;

    /** 存储桶名称（仅 storage 类型使用） */
    private String bucketName;

    /** 访问密钥 AccessKey */
    private String accessKey;

    /** 访问密钥 SecretKey */
    private String secretKey;

    /** 自定义域名（如 CDN 域名） */
    private String domain;

    private String basePath;

    private String extParamsJson;

    /** 模型类型（仅 llm 类型使用，如 gpt-4） */
    private String modelType;

    /** 是否默认配置：Y-是 N-否 */
    private String isDefault;

    /** 状态：0-正常，1-停用 */
    private String status;

    // ==================== Getter/Setter ====================

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
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

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

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

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

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
}
