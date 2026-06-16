package com.bigyun.payment.service.impl;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.payment.domain.PaymentOrder;
import com.bigyun.payment.domain.dto.PaymentOrderCreateDTO;
import com.bigyun.payment.domain.vo.PaymentOrderVO;
import com.bigyun.payment.mapper.PaymentOrderMapper;
import com.bigyun.payment.service.IPaymentOrderService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrderServiceImpl implements IPaymentOrderService
{
    private final PaymentOrderMapper paymentOrderMapper;

    public PaymentOrderServiceImpl(PaymentOrderMapper paymentOrderMapper)
    {
        this.paymentOrderMapper = paymentOrderMapper;
    }

    @Override
    public List<PaymentOrderVO> selectPaymentOrderList(PaymentOrder paymentOrder)
    {
        return paymentOrderMapper.selectPaymentOrderList(paymentOrder).stream().map(this::toVO).toList();
    }

    @Override
    public PaymentOrderVO selectPaymentOrderById(Long orderId)
    {
        PaymentOrder order = paymentOrderMapper.selectPaymentOrderById(orderId);
        return order == null ? null : toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderVO createOrder(PaymentOrderCreateDTO dto)
    {
        if (paymentOrderMapper.selectPaymentOrderByOrderNo(buildOrderNo(dto)) != null)
        {
            throw new ServiceException("Payment order already exists");
        }
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(buildOrderNo(dto));
        order.setBusinessId(dto.getBusinessId());
        order.setBusinessType(dto.getBusinessType());
        order.setChannelCode(dto.getChannelCode());
        order.setAmount(dto.getAmount());
        order.setCurrency("CNY");
        order.setPayStatus("UNPAID");
        order.setRequestSnapshot(dto.getSubject());
        paymentOrderMapper.insertPaymentOrder(order);
        return toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrder(PaymentOrder paymentOrder)
    {
        return paymentOrderMapper.updatePaymentOrder(paymentOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOrderByIds(Long[] orderIds)
    {
        return paymentOrderMapper.deletePaymentOrderByIds(orderIds);
    }

    private String buildOrderNo(PaymentOrderCreateDTO dto)
    {
        return "PAY" + dto.getBusinessType() + dto.getBusinessId();
    }

    private PaymentOrderVO toVO(PaymentOrder order)
    {
        PaymentOrderVO vo = new PaymentOrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setChannelCode(order.getChannelCode());
        vo.setAmount(order.getAmount());
        vo.setPayStatus(order.getPayStatus());
        return vo;
    }
}
