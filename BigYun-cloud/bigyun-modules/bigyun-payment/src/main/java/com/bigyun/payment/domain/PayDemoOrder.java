package com.bigyun.payment.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.bigyun.common.core.annotation.Excel;
import com.bigyun.common.core.web.domain.BaseEntity;

/**
 * 支付订单骨架。
 *
 * @author bigyun
 */
public class PayDemoOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @Excel(name = "订单号")
    private String orderNo;

    @Excel(name = "渠道编码")
    private String channelCode;

    @Excel(name = "订单标题")
    private String subject;

    @Excel(name = "金额")
    private BigDecimal amount;

    @Excel(name = "订单状态")
    private String orderStatus;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    @NotBlank(message = "订单号不能为空")
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    @NotBlank(message = "渠道编码不能为空")
    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    @NotBlank(message = "订单标题不能为空")
    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {
        this.orderStatus = orderStatus;
    }
}