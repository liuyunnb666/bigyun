package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@TableName("sys_provider_credentials")
public class ProviderCredential extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "credential_id", type = IdType.AUTO)
    private Long credentialId;

    @NotNull(message = "配置ID不能为空")
    private Long configId;

    @NotBlank(message = "凭据键名不能为空")
    @Size(max = 64, message = "凭据键名长度不能超过64个字符")
    private String credentialKey;

    @Size(max = 1000, message = "凭据值长度不能超过1000个字符")
    private String credentialValue;

    private String isSensitive;

    public Long getCredentialId()
    {
        return credentialId;
    }

    public void setCredentialId(Long credentialId)
    {
        this.credentialId = credentialId;
    }

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public String getCredentialKey()
    {
        return credentialKey;
    }

    public void setCredentialKey(String credentialKey)
    {
        this.credentialKey = credentialKey;
    }

    public String getCredentialValue()
    {
        return credentialValue;
    }

    public void setCredentialValue(String credentialValue)
    {
        this.credentialValue = credentialValue;
    }

    public String getIsSensitive()
    {
        return isSensitive;
    }

    public void setIsSensitive(String isSensitive)
    {
        this.isSensitive = isSensitive;
    }

    @Override
    public String toString()
    {
        return "ProviderCredential{" +
                "credentialId=" + credentialId +
                ", configId=" + configId +
                ", credentialKey='" + credentialKey + '\'' +
                ", credentialValue='[PROTECTED]'" +
                ", isSensitive='" + isSensitive + '\'' +
                '}';
    }
}
