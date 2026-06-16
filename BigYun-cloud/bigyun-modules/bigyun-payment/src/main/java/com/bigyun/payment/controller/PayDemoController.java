package com.bigyun.payment.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.payment.domain.PayChannelConfig;
import com.bigyun.payment.domain.PayDemoOrder;
import com.bigyun.payment.service.IPayDemoService;

/**
 * 支付骨架接口。
 *
 * @author bigyun
 */
@RestController
@RequestMapping("/demo")
public class PayDemoController extends BaseController
{
    private final IPayDemoService payDemoService;

    public PayDemoController(IPayDemoService payDemoService)
    {
        this.payDemoService = payDemoService;
    }

    @RequiresPermissions("pay:config:list")
    @GetMapping("/config/list")
    public TableDataInfo configList(PayChannelConfig config)
    {
        startPage();
        List<PayChannelConfig> list = payDemoService.selectChannelConfigList(config);
        return getDataTable(list);
    }

    @RequiresPermissions("pay:config:query")
    @GetMapping("/config/{configId}")
    public AjaxResult getConfig(@PathVariable Long configId)
    {
        return success(payDemoService.selectChannelConfigById(configId));
    }

    @RequiresPermissions("pay:config:add")
    @Log(title = "支付渠道配置", businessType = BusinessType.INSERT)
    @PostMapping("/config")
    public AjaxResult addConfig(@Valid @RequestBody PayChannelConfig config)
    {
        config.setCreateBy(SecurityUtils.getUsername());
        return toAjax(payDemoService.insertChannelConfig(config));
    }

    @RequiresPermissions("pay:config:edit")
    @Log(title = "支付渠道配置", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult editConfig(@Valid @RequestBody PayChannelConfig config)
    {
        config.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(payDemoService.updateChannelConfig(config));
    }

    @RequiresPermissions("pay:config:remove")
    @Log(title = "支付渠道配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/config/{configIds}")
    public AjaxResult removeConfig(@PathVariable Long[] configIds)
    {
        return toAjax(payDemoService.deleteChannelConfigByIds(configIds));
    }

    @RequiresPermissions("pay:order:list")
    @GetMapping("/order/list")
    public TableDataInfo orderList(PayDemoOrder order)
    {
        startPage();
        List<PayDemoOrder> list = payDemoService.selectOrderList(order);
        return getDataTable(list);
    }

    @RequiresPermissions("pay:order:query")
    @GetMapping("/{orderNo}")
    public AjaxResult getOrder(@PathVariable String orderNo)
    {
        return success(payDemoService.selectOrderByNo(orderNo));
    }

    @RequiresPermissions("pay:order:add")
    @Log(title = "支付订单骨架", businessType = BusinessType.INSERT)
    @PostMapping("/order")
    public AjaxResult createOrder(@Valid @RequestBody PayDemoOrder order)
    {
        order.setCreateBy(SecurityUtils.getUsername());
        return toAjax(payDemoService.createOrder(order));
    }

    @RequiresPermissions("pay:order:edit")
    @Log(title = "支付订单骨架", businessType = BusinessType.UPDATE)
    @PostMapping("/{orderNo}/close")
    public AjaxResult closeOrder(@PathVariable String orderNo)
    {
        return toAjax(payDemoService.closeOrder(orderNo));
    }
}