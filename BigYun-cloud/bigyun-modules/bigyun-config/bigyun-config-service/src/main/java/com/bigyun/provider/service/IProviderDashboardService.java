package com.bigyun.provider.service;

import com.bigyun.provider.domain.vo.ProviderDashboardVO;

/**
 * Provider 配置大屏服务接口
 */
public interface IProviderDashboardService
{
    /**
     * 获取配置大屏数据
     *
     * @return 配置大屏视图对象
     */
    ProviderDashboardVO getDashboardData();
}
