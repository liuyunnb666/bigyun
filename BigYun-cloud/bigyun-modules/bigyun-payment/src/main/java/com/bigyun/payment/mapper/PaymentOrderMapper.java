package com.bigyun.payment.mapper;

import com.bigyun.payment.domain.PaymentOrder;
import java.util.List;

public interface PaymentOrderMapper
{
    List<PaymentOrder> selectPaymentOrderList(PaymentOrder paymentOrder);

    PaymentOrder selectPaymentOrderById(Long orderId);

    PaymentOrder selectPaymentOrderByOrderNo(String orderNo);

    int insertPaymentOrder(PaymentOrder paymentOrder);

    int updatePaymentOrder(PaymentOrder paymentOrder);

    int deletePaymentOrderById(Long orderId);

    int deletePaymentOrderByIds(Long[] orderIds);
}
