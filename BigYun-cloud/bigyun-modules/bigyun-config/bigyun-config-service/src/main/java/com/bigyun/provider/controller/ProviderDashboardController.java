package com.bigyun.provider.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.provider.domain.vo.ProviderDashboardVO;
import com.bigyun.provider.service.IProviderDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务配置总览控制器。
 */
@RestController
@RequestMapping("/dashboard")
public class ProviderDashboardController extends BaseController
{
    private final IProviderDashboardService providerDashboardService;

    public ProviderDashboardController(IProviderDashboardService providerDashboardService)
    {
        this.providerDashboardService = providerDashboardService;
    }

    /**
     * 获取服务配置总览数据。
     */
    @RequiresPermissions("config:dashboard:view")
    @GetMapping("/overview")
    public AjaxResult getOverview()
    {
        ProviderDashboardVO dashboardData = providerDashboardService.getDashboardData();
        return success(dashboardData);
    }
}
