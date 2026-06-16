package com.bigyun.payment.mapper;

import java.util.List;
import com.bigyun.payment.domain.PayChannelConfig;
import com.bigyun.payment.domain.PayDemoOrder;

/**
 * 支付骨架数据访问层。
 *
 * @author bigyun
 */
public interface PayDemoMapper
{
    List<PayChannelConfig> selectChannelConfigList(PayChannelConfig config);

    PayChannelConfig selectChannelConfigById(Long configId);

    PayChannelConfig selectChannelConfigByCode(String channelCode);

    int insertChannelConfig(PayChannelConfig config);

    int updateChannelConfig(PayChannelConfig config);

    int deleteChannelConfigByIds(Long[] configIds);

    List<PayDemoOrder> selectOrderList(PayDemoOrder order);

    PayDemoOrder selectOrderByNo(String orderNo);

    int insertOrder(PayDemoOrder order);

    int updateOrderStatus(PayDemoOrder order);
}