package com.bigyun.payment.service;

import com.bigyun.payment.domain.PaymentChannelConfig;
import java.util.List;

public interface IPaymentChannelConfigService
{
    List<PaymentChannelConfig> selectPaymentChannelConfigList(PaymentChannelConfig config);

    PaymentChannelConfig selectPaymentChannelConfigById(Long configId);

    int insertPaymentChannelConfig(PaymentChannelConfig config);

    int updatePaymentChannelConfig(PaymentChannelConfig config);

    int deletePaymentChannelConfigByIds(Long[] configIds);
}
