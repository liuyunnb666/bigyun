package com.bigyun.payment.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.payment.domain.PaymentChannelConfig;
import com.bigyun.payment.service.IPaymentChannelConfigService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment/channel")
public class PaymentChannelConfigController extends BaseController
{
    private final IPaymentChannelConfigService service;

    public PaymentChannelConfigController(IPaymentChannelConfigService service)
    {
        this.service = service;
    }

    @RequiresPermissions("payment:channel:list")
    @GetMapping("/list")
    public TableDataInfo list(PaymentChannelConfig config)
    {
        startPage();
        List<PaymentChannelConfig> list = service.selectPaymentChannelConfigList(config);
        return getDataTable(list);
    }

    @RequiresPermissions("payment:channel:query")
    @GetMapping("/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId)
    {
        return success(service.selectPaymentChannelConfigById(configId));
    }

    @RequiresPermissions("payment:channel:add")
    @Log(title = "Payment channel", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PaymentChannelConfig config)
    {
        return toAjax(service.insertPaymentChannelConfig(config));
    }

    @RequiresPermissions("payment:channel:edit")
    @Log(title = "Payment channel", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PaymentChannelConfig config)
    {
        return toAjax(service.updatePaymentChannelConfig(config));
    }

    @RequiresPermissions("payment:channel:remove")
    @Log(title = "Payment channel", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(service.deletePaymentChannelConfigByIds(configIds));
    }
}
