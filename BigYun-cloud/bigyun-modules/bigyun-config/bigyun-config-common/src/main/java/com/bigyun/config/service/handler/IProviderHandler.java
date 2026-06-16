package com.bigyun.config.service.handler;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;

/**
 * Provider服务处理器接口
 * 每种服务类型需要实现此接口
 *
 * @param <T> 请求数据类型
 * @param <R> 响应数据类型
 */
public interface IProviderHandler<T, R> {

    /**
     * 执行服务调用
     *
     * @param config 配置信息
     * @param request 请求参数
     * @return 响应结果
     */
    ProviderResponse<R> execute(ProviderConfigDTO config, ProviderRequest<T> request);

    /**
     * 获取支持的配置类型
     *
     * @return 配置类型代码
     */
    String getSupportedConfigType();

    /**
     * 获取支持的服务商代码
     *
     * @return 服务商代码
     */
    String getSupportedProviderCode();

    /**
     * 验证配置是否有效
     *
     * @param config 配置信息
     * @return 是否有效
     */
    default boolean validateConfig(ProviderConfigDTO config) {
        return config != null && config.getAccessKey() != null;
    }
}
