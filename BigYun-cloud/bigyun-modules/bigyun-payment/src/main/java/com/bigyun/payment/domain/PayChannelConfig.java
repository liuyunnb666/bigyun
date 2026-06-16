package com.bigyun.payment.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.bigyun.common.core.annotation.Excel;
import com.bigyun.common.core.web.domain.BaseEntity;

/**
 * 支付渠道配置骨架。
 * <p>社区版只保存配置结构，真实商户密钥请在本地环境自行维护。</p>
 *
 * @author bigyun
 */
public class PayChannelConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;

    @Excel(name = "渠道编码")
    private String channelCode;

    @Excel(name = "渠道名称")
    private String channelName;

    @Excel(name = "应用编号")
    private String appId;

    @Excel(name = "商户号")
    private String merchantId;

    private String publicKey;

    private String privateKey;

    private String notifyUrl;

    @Excel(name = "状态")
    private String status;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "渠道编码不能为空")
    @Size(max = 32, message = "渠道编码长度不能超过32个字符")
    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    @NotBlank(message = "渠道名称不能为空")
    @Size(max = 64, message = "渠道名称长度不能超过64个字符")
    public String getChannelName()
    {
        return channelName;
    }

    public void setChannelName(String channelName)
    {
        this.channelName = channelName;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(String merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
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