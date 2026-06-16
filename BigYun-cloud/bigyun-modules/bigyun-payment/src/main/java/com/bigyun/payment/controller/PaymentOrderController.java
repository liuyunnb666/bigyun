package com.bigyun.payment.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.payment.domain.PaymentOrder;
import com.bigyun.payment.domain.dto.PaymentOrderCreateDTO;
import com.bigyun.payment.service.IPaymentOrderService;
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
@RequestMapping("/order")
public class PaymentOrderController extends BaseController
{
    private final IPaymentOrderService service;

    public PaymentOrderController(IPaymentOrderService service)
    {
        this.service = service;
    }

    @RequiresPermissions("payment:order:list")
    @GetMapping("/list")
    public TableDataInfo list(PaymentOrder paymentOrder)
    {
        startPage();
        List<?> list = service.selectPaymentOrderList(paymentOrder);
        return getDataTable(list);
    }

    @RequiresPermissions("payment:order:query")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(service.selectPaymentOrderById(orderId));
    }

    @RequiresPermissions("payment:order:add")
    @Log(title = "Payment order", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody PaymentOrderCreateDTO dto)
    {
        return success(service.createOrder(dto));
    }

    @RequiresPermissions("payment:order:edit")
    @Log(title = "Payment order", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PaymentOrder paymentOrder)
    {
        return toAjax(service.updateOrder(paymentOrder));
    }

    @RequiresPermissions("payment:order:remove")
    @Log(title = "Payment order", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(service.deleteOrderByIds(orderIds));
    }
}
