package com.bigyun.payment.mapper;

import com.bigyun.payment.domain.PaymentChannelConfig;
import java.util.List;

public interface PaymentChannelConfigMapper
{
    List<PaymentChannelConfig> selectPaymentChannelConfigList(PaymentChannelConfig config);

    PaymentChannelConfig selectPaymentChannelConfigById(Long configId);

    PaymentChannelConfig selectPaymentChannelConfigByCode(String channelCode);

    int insertPaymentChannelConfig(PaymentChannelConfig config);

    int updatePaymentChannelConfig(PaymentChannelConfig config);

    int deletePaymentChannelConfigById(Long configId);

    int deletePaymentChannelConfigByIds(Long[] configIds);
}
