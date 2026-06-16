package com.bigyun.config.service;

import com.bigyun.config.domain.ProviderConfigDTO;

/**
 * 统一的Provider服务调用接口
 * 所有业务模块通过此接口调用第三方服务
 */
public interface IProviderService {

    /**
     * 执行Provider服务调用
     *
     * @param configType 配置类型（llm/tts/stt/storage等）
     * @param request 请求参数
     * @return 响应结果
     */
    <T, R> R execute(String configType, ProviderRequest<T> request);

    /**
     * 执行Provider服务调用（指定服务商）
     *
     * @param configType 配置类型
     * @param providerCode 服务商代码
     * @param request 请求参数
     * @return 响应结果
     */
    <T, R> R execute(String configType, String providerCode, ProviderRequest<T> request);

    /**
     * 获取默认配置
     *
     * @param configType 配置类型
     * @return 配置信息
     */
    ProviderConfigDTO getDefaultConfig(String configType);

    /**
     * 获取指定配置
     *
     * @param configType 配置类型
     * @param providerCode 服务商代码
     * @return 配置信息
     */
    ProviderConfigDTO getConfig(String configType, String providerCode);
}
