package com.bigyun.provider.service.impl;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.IProviderService;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.provider.factory.ProviderHandlerFactory;
import com.bigyun.provider.service.IProviderConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Provider统一服务实现
 *
 * 核心功能：
 * 1. 提供统一的服务调用接口，屏蔽底层实现差异
 * 2. 根据配置类型从数据库获取配置
 * 3. 通过工厂获取对应的Handler
 * 4. 执行服务调用并返回结果
 *
 * 调用流程：
 * <pre>
 * 业务代码
 *   ↓ providerService.execute("storage", request)
 * ProviderServiceImpl
 *   ↓ 1. 查询数据库获取默认storage配置
 *   ↓ 2. 根据配置的provider_code获取Handler
 *   ↓ 3. handler.execute(config, request)
 * LocalStorageHandler / AliyunOSSHandler / TencentCOSHandler
 *   ↓ 执行具体的存储操作
 * 返回结果
 * </pre>
 *
 * 使用示例：
 * <pre>
 * // 示例1：使用默认配置
 * StorageRequest request = StorageRequest.upload(...);
 * StorageResponse response = providerService.execute("storage",
 *     new ProviderRequest<>(request));
 *
 * // 示例2：指定服务商
 * StorageResponse response = providerService.execute("storage", "aliyun-oss",
 *     new ProviderRequest<>(request));
 * </pre>
 *
 * 配置切换：
 * 通过修改数据库中的is_default字段，即可切换默认服务商，无需修改代码
 *
 * @author BigYun
 * @date 2026-05-21
 */
@Service
public class ProviderServiceImpl implements IProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderServiceImpl.class);

    /** Provider配置服务，用于查询数据库配置 */
    private final IProviderConfigService providerConfigService;

    /** Provider Handler工厂，用于获取对应的Handler */
    private final ProviderHandlerFactory handlerFactory;

    public ProviderServiceImpl(
        IProviderConfigService providerConfigService,
        ProviderHandlerFactory handlerFactory) {
        this.providerConfigService = providerConfigService;
        this.handlerFactory = handlerFactory;
    }

    /**
     * 执行Provider服务调用（使用默认配置）
     *
     * 工作流程：
     * 1. 从数据库查询指定配置类型的默认配置（is_default='Y'）
     * 2. 根据配置中的provider_code通过工厂获取对应的Handler
     * 3. 调用Handler的execute方法执行具体操作
     * 4. 返回执行结果
     *
     * @param configType 配置类型（如：storage、llm、tts）
     * @param request    请求参数
     * @param <T>        请求数据类型
     * @param <R>        响应数据类型
     * @return 响应结果
     */
    @Override
    public <T, R> R execute(String configType, ProviderRequest<T> request) {
        // 获取默认配置
        ProviderConfigDTO config = providerConfigService.selectDefaultProviderConfigInternal(configType);
        return executeWithConfig(config, request);
    }

    /**
     * 执行Provider服务调用（指定服务商）
     *
     * 使用场景：
     * - 需要临时使用非默认服务商
     * - 需要同时使用多个服务商（如：同时上传到本地和OSS）
     * - 测试新服务商
     *
     * @param configType   配置类型
     * @param providerCode 服务商代码（如：local、aliyun-oss、tencent-cos）
     * @param request      请求参数
     * @param <T>          请求数据类型
     * @param <R>          响应数据类型
     * @return 响应结果
     */
    @Override
    public <T, R> R execute(String configType, String providerCode, ProviderRequest<T> request) {
        // 获取指定服务商的配置
        ProviderConfigDTO config = providerConfigService.getConfig(configType, providerCode);
        if (config == null) {
            throw new ServiceException(
                String.format("未找到配置: configType=%s, providerCode=%s", configType, providerCode));
        }
        return executeWithConfig(config, request);
    }

    /**
     * 获取默认配置
     *
     * @param configType 配置类型
     * @return 配置信息
     */
    @Override
    public ProviderConfigDTO getDefaultConfig(String configType) {
        return providerConfigService.selectDefaultProviderConfigInternal(configType);
    }

    /**
     * 获取指定配置
     *
     * @param configType   配置类型
     * @param providerCode 服务商代码
     * @return 配置信息
     */
    @Override
    public ProviderConfigDTO getConfig(String configType, String providerCode) {
        return providerConfigService.getConfig(configType, providerCode);
    }

    /**
     * 使用指定配置执行服务调用（核心方法）
     *
     * 执行步骤：
     * 1. 从配置中提取configType和providerCode
     * 2. 通过工厂获取对应的Handler
     * 3. 验证配置有效性
     * 4. 执行Handler的execute方法
     * 5. 处理响应结果
     *
     * @param config  配置信息
     * @param request 请求参数
     * @param <T>     请求数据类型
     * @param <R>     响应数据类型
     * @return 响应结果
     */
    private <T, R> R executeWithConfig(ProviderConfigDTO config, ProviderRequest<T> request) {
        String configType = config.getConfigType();
        String providerCode = config.getProviderCode();

        log.debug("执行Provider服务: configType={}, providerCode={}", configType, providerCode);

        // 通过工厂获取对应的Handler
        IProviderHandler<T, R> handler = handlerFactory.getHandler(configType, providerCode);

        // 验证配置有效性
        if (!handler.validateConfig(config)) {
            throw new ServiceException(
                String.format("配置验证失败: %s - %s", configType, providerCode));
        }

        // 执行服务调用
        ProviderResponse<R> response = handler.execute(config, request);

        // 处理响应
        if (!response.isSuccess()) {
            log.error("Provider服务调用失败: configType={}, providerCode={}, errorCode={}, errorMsg={}",
                configType, providerCode, response.getErrorCode(), response.getErrorMessage());
            throw new ServiceException(
                String.format("服务调用失败: %s", response.getErrorMessage()));
        }

        log.debug("Provider服务调用成功: configType={}, providerCode={}", configType, providerCode);
        return response.getData();
    }
}
