package com.bigyun.payment.domain;

import com.bigyun.common.core.annotation.Excel;
import com.bigyun.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @Excel(name = "Order no")
    private String orderNo;

    private Long businessId;

    private String businessType;

    private String channelCode;

    private BigDecimal amount;

    private String currency;

    private String payStatus;

    private String tradeNo;

    private String requestSnapshot;

    private String responseSnapshot;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    @NotBlank(message = "Order no cannot be blank")
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    @NotNull(message = "Business id cannot be blank")
    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    @NotBlank(message = "Business type cannot be blank")
    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
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

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public String getTradeNo()
    {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo)
    {
        this.tradeNo = tradeNo;
    }

    public String getRequestSnapshot()
    {
        return requestSnapshot;
    }

    public void setRequestSnapshot(String requestSnapshot)
    {
        this.requestSnapshot = requestSnapshot;
    }

    public String getResponseSnapshot()
    {
        return responseSnapshot;
    }

    public void setResponseSnapshot(String responseSnapshot)
    {
        this.responseSnapshot = responseSnapshot;
    }
}
