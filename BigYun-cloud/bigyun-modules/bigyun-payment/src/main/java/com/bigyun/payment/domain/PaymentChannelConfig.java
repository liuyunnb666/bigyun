package com.bigyun.payment.domain;

import com.bigyun.common.core.annotation.Excel;
import com.bigyun.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PaymentChannelConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;

    @Excel(name = "Channel")
    private String channelCode;

    @Excel(name = "Channel name")
    private String channelName;

    @Excel(name = "App id")
    private String appId;

    private String appSecret;

    private String merchantId;

    private String notifyUrl;

    private String returnUrl;

    private String status;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "Channel code cannot be blank")
    @Size(max = 32, message = "Channel code length cannot exceed 32 characters")
    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    @NotBlank(message = "Channel name cannot be blank")
    @Size(max = 64, message = "Channel name length cannot exceed 64 characters")
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

    public String getAppSecret()
    {
        return appSecret;
    }

    public void setAppSecret(String appSecret)
    {
        this.appSecret = appSecret;
    }

    public String getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(String merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
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
