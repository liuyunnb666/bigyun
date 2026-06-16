package com.bigyun.payment.service.impl;

import com.bigyun.payment.domain.PaymentChannelConfig;
import com.bigyun.payment.mapper.PaymentChannelConfigMapper;
import com.bigyun.payment.service.IPaymentChannelConfigService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentChannelConfigServiceImpl implements IPaymentChannelConfigService
{
    private final PaymentChannelConfigMapper paymentChannelConfigMapper;

    public PaymentChannelConfigServiceImpl(PaymentChannelConfigMapper paymentChannelConfigMapper)
    {
        this.paymentChannelConfigMapper = paymentChannelConfigMapper;
    }

    @Override
    public List<PaymentChannelConfig> selectPaymentChannelConfigList(PaymentChannelConfig config)
    {
        return paymentChannelConfigMapper.selectPaymentChannelConfigList(config);
    }

    @Override
    public PaymentChannelConfig selectPaymentChannelConfigById(Long configId)
    {
        return paymentChannelConfigMapper.selectPaymentChannelConfigById(configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPaymentChannelConfig(PaymentChannelConfig config)
    {
        if (config.getStatus() == null || config.getStatus().isEmpty())
        {
            config.setStatus("0");
        }
        return paymentChannelConfigMapper.insertPaymentChannelConfig(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePaymentChannelConfig(PaymentChannelConfig config)
    {
        return paymentChannelConfigMapper.updatePaymentChannelConfig(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePaymentChannelConfigByIds(Long[] configIds)
    {
        return paymentChannelConfigMapper.deletePaymentChannelConfigByIds(configIds);
    }
}
