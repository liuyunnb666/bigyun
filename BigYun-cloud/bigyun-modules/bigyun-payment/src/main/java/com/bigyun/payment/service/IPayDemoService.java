package com.bigyun.payment.service;

import java.util.List;
import com.bigyun.payment.domain.PayChannelConfig;
import com.bigyun.payment.domain.PayDemoOrder;

/**
 * 支付骨架服务接口。
 *
 * @author bigyun
 */
public interface IPayDemoService
{
    List<PayChannelConfig> selectChannelConfigList(PayChannelConfig config);

    PayChannelConfig selectChannelConfigById(Long configId);

    int insertChannelConfig(PayChannelConfig config);

    int updateChannelConfig(PayChannelConfig config);

    int deleteChannelConfigByIds(Long[] configIds);

    List<PayDemoOrder> selectOrderList(PayDemoOrder order);

    PayDemoOrder selectOrderByNo(String orderNo);

    int createOrder(PayDemoOrder order);

    int closeOrder(String orderNo);
}