package com.bigyun.payment.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.payment.domain.PayChannelConfig;
import com.bigyun.payment.domain.PayDemoOrder;
import com.bigyun.payment.mapper.PayDemoMapper;
import com.bigyun.payment.service.IPayDemoService;

/**
 * 支付骨架服务实现。
 *
 * @author bigyun
 */
@Service
public class PayDemoServiceImpl implements IPayDemoService
{
    private final PayDemoMapper payDemoMapper;

    public PayDemoServiceImpl(PayDemoMapper payDemoMapper)
    {
        this.payDemoMapper = payDemoMapper;
    }

    @Override
    public List<PayChannelConfig> selectChannelConfigList(PayChannelConfig config)
    {
        return payDemoMapper.selectChannelConfigList(config);
    }

    @Override
    public PayChannelConfig selectChannelConfigById(Long configId)
    {
        return payDemoMapper.selectChannelConfigById(configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertChannelConfig(PayChannelConfig config)
    {
        checkChannelUnique(config);
        if (StringUtils.isEmpty(config.getStatus()))
        {
            config.setStatus("0");
        }
        return payDemoMapper.insertChannelConfig(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateChannelConfig(PayChannelConfig config)
    {
        checkChannelUnique(config);
        return payDemoMapper.updateChannelConfig(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteChannelConfigByIds(Long[] configIds)
    {
        return payDemoMapper.deleteChannelConfigByIds(configIds);
    }

    @Override
    public List<PayDemoOrder> selectOrderList(PayDemoOrder order)
    {
        return payDemoMapper.selectOrderList(order);
    }

    @Override
    public PayDemoOrder selectOrderByNo(String orderNo)
    {
        return payDemoMapper.selectOrderByNo(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createOrder(PayDemoOrder order)
    {
        if (payDemoMapper.selectOrderByNo(order.getOrderNo()) != null)
        {
            throw new ServiceException("支付订单号已存在");
        }
        if (StringUtils.isEmpty(order.getOrderStatus()))
        {
            order.setOrderStatus("WAIT_PAY");
        }
        return payDemoMapper.insertOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeOrder(String orderNo)
    {
        PayDemoOrder order = new PayDemoOrder();
        order.setOrderNo(orderNo);
        order.setOrderStatus("CLOSED");
        return payDemoMapper.updateOrderStatus(order);
    }

    private void checkChannelUnique(PayChannelConfig config)
    {
        PayChannelConfig exists = payDemoMapper.selectChannelConfigByCode(config.getChannelCode());
        if (exists != null && !exists.getConfigId().equals(config.getConfigId()))
        {
            throw new ServiceException("支付渠道编码已存在");
        }
    }
}