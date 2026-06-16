package com.bigyun.payment.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentOrderCreateDTO
{
    @NotBlank(message = "Business type cannot be blank")
    private String businessType;

    @NotNull(message = "Business id cannot be blank")
    private Long businessId;

    @NotBlank(message = "Channel code cannot be blank")
    private String channelCode;

    private BigDecimal amount;

    private String subject;

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }
}
