package com.bigyun.payment.service;

import com.bigyun.payment.domain.PaymentOrder;
import com.bigyun.payment.domain.dto.PaymentOrderCreateDTO;
import com.bigyun.payment.domain.vo.PaymentOrderVO;
import java.util.List;

public interface IPaymentOrderService
{
    List<PaymentOrderVO> selectPaymentOrderList(PaymentOrder paymentOrder);

    PaymentOrderVO selectPaymentOrderById(Long orderId);

    PaymentOrderVO createOrder(PaymentOrderCreateDTO dto);

    int updateOrder(PaymentOrder paymentOrder);

    int deleteOrderByIds(Long[] orderIds);
}
