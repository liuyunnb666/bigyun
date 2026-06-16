package com.bigyun.provider.factory;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.service.handler.IProviderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider处理器工厂
 *
 * 设计模式：工厂模式 + 策略模式
 * 参考实现：LoginStrategyFactory
 *
 * 核心功能：
 * 1. 自动注册所有IProviderHandler实现类（通过Spring依赖注入）
 * 2. 根据配置类型和服务商代码路由到对应的Handler
 * 3. 提供Handler的查询和验证功能
 *
 * 工作原理：
 * 1. Spring容器启动时，自动注入所有标注了@Component的IProviderHandler实现类
 * 2. 构造函数遍历所有Handler，调用其getSupportedConfigType()和getSupportedProviderCode()
 * 3. 将Handler注册到二级Map中：Map<configType, Map<providerCode, Handler>>
 * 4. 业务调用时，通过getHandler(configType, providerCode)获取对应的Handler
 *
 * 扩展方式：
 * 新增Handler只需：
 * 1. 实现IProviderHandler接口
 * 2. 添加@Component注解
 * 3. 实现getSupportedConfigType()和getSupportedProviderCode()方法
 * 4. 重启应用，自动注册
 *
 * 示例：
 * <pre>
 * // 新增七牛云存储Handler
 * @Component
 * public class QiniuKodoHandler implements IProviderHandler<StorageRequest, StorageResponse> {
 *     public String getSupportedConfigType() { return "storage"; }
 *     public String getSupportedProviderCode() { return "qiniu-kodo"; }
 *     // ... 实现execute方法
 * }
 * // 重启后自动注册，无需修改工厂代码
 * </pre>
 *
 * @author BigYun
 * @date 2026-05-21
 */
@Component
public class ProviderHandlerFactory {

    private static final Logger log = LoggerFactory.getLogger(ProviderHandlerFactory.class);

    /**
     * Handler注册表
     * 数据结构：Map<配置类型, Map<服务商代码, Handler实例>>
     *
     * 示例数据：
     * {
     *   "storage": {
     *     "local": LocalStorageHandler实例,
     *     "aliyun-oss": AliyunOSSHandler实例,
     *     "tencent-cos": TencentCOSHandler实例
     *   },
     *   "llm": {
     *     "openai-gpt": OpenAIGPTHandler实例,
     *     "aliyun-qwen": AliyunQwenHandler实例
     *   }
     * }
     */
    private final Map<String, Map<String, IProviderHandler<?, ?>>> handlerRegistry = new HashMap<>();

    /**
     * Handler缓存
     * 数据结构：Map<"configType:providerCode", Handler实例>
     * 用于加速频繁的getHandler()调用
     */
    private final Map<String, IProviderHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    /**
     * 通用配置驱动Handler（fallback handler）
     * 当找不到专用Handler时，使用此Handler作为回退
     * 通过API模板配置驱动，实现零代码扩展
     */
    private IProviderHandler<?, ?> genericConfigDrivenHandler;

    /**
     * 构造函数，Spring自动注入所有IProviderHandler实现类
     *
     * @param handlers Spring容器中所有IProviderHandler实现类的列表
     */
    public ProviderHandlerFactory(List<IProviderHandler<?, ?>> handlers) {
        log.info("========================================");
        log.info("开始初始化Provider Handler工厂...");
        log.info("========================================");

        // 遍历所有Handler，注册到handlerRegistry
        for (IProviderHandler<?, ?> handler : handlers) {
            String configType = handler.getSupportedConfigType();
            String providerCode = handler.getSupportedProviderCode();

            // 验证Handler配置的完整性
            if (StringUtils.isEmpty(configType) || StringUtils.isEmpty(providerCode)) {
                log.warn("  [跳过] Handler {} 未正确配置 configType 或 providerCode",
                    handler.getClass().getSimpleName());
                continue;
            }

            // 特殊处理：GenericConfigDrivenHandler作为fallback handler
            // 它的configType和providerCode都是"*"，表示支持所有类型
            if ("*".equals(configType) && "*".equals(providerCode)) {
                this.genericConfigDrivenHandler = handler;
                log.info("  [注册] 通用配置驱动Handler (fallback): {}",
                    handler.getClass().getSimpleName());
                continue;
            }

            // 注册Handler到二级Map
            handlerRegistry
                .computeIfAbsent(configType, k -> new HashMap<>())
                .put(providerCode, handler);

            log.info("  [注册] {} -> {} ({})",
                configType,
                providerCode,
                handler.getClass().getSimpleName());
        }

        log.info("========================================");
        log.info("Provider Handler工厂初始化完成");
        log.info("共注册 {} 个配置类型，{} 个专用Handler",
            handlerRegistry.size(),
            handlers.size() - (genericConfigDrivenHandler != null ? 1 : 0));
        if (genericConfigDrivenHandler != null) {
            log.info("已启用通用配置驱动Handler，支持零代码扩展");
        }
        log.info("========================================");
    }

    /**
     * 获取指定的Handler
     *
     * 查找逻辑（优先级从高到低）：
     * 1. 优先查找缓存
     * 2. 缓存未命中，查找专用Handler（如AliyunOSSHandler）
     * 3. 如果没有专用Handler，使用GenericConfigDrivenHandler（配置驱动）
     * 4. 如果都没有，抛出异常
     *
     * 使用示例：
     * <pre>
     * // 获取阿里云OSS的Handler
     * IProviderHandler<StorageRequest, StorageResponse> handler =
     *     factory.getHandler("storage", "aliyun-oss");
     * </pre>
     *
     * @param configType   配置类型（如：storage、llm、tts）
     * @param providerCode 服务商代码（如：local、aliyun-oss、openai-gpt）
     * @param <T>          请求数据类型
     * @param <R>          响应数据类型
     * @return Handler实例
     * @throws ServiceException 如果配置类型或服务商不支持
     */
    @SuppressWarnings("unchecked")
    public <T, R> IProviderHandler<T, R> getHandler(String configType, String providerCode) {
        // 参数验证
        if (StringUtils.isEmpty(configType)) {
            throw new ServiceException("配置类型不能为空");
        }
        if (StringUtils.isEmpty(providerCode)) {
            throw new ServiceException("服务商代码不能为空");
        }

        // 1. 优先查找缓存
        String cacheKey = configType + ":" + providerCode;
        IProviderHandler<?, ?> cachedHandler = handlerCache.get(cacheKey);
        if (cachedHandler != null) {
            log.debug("使用缓存Handler: configType={}, providerCode={}", configType, providerCode);
            return (IProviderHandler<T, R>) cachedHandler;
        }

        // 2. 缓存未命中，查找专用Handler
        Map<String, IProviderHandler<?, ?>> typeHandlers = handlerRegistry.get(configType);
        if (typeHandlers != null) {
            IProviderHandler<?, ?> handler = typeHandlers.get(providerCode);
            if (handler != null) {
                handlerCache.put(cacheKey, handler);
                log.debug("使用专用Handler: configType={}, providerCode={}, handler={}",
                    configType, providerCode, handler.getClass().getSimpleName());
                return (IProviderHandler<T, R>) handler;
            }
        }

        // 3. 如果没有专用Handler，使用GenericConfigDrivenHandler
        if (genericConfigDrivenHandler != null) {
            handlerCache.put(cacheKey, genericConfigDrivenHandler);
            log.info("未找到专用Handler，使用配置驱动Handler: configType={}, providerCode={}",
                configType, providerCode);
            return (IProviderHandler<T, R>) genericConfigDrivenHandler;
        }

        // 4. 都没有，抛出异常
        throw new ServiceException(
            String.format("不支持的服务商: %s (配置类型: %s)", providerCode, configType));
    }

    /**
     * 检查是否支持指定的配置类型和服务商
     *
     * 查找逻辑（优先级从高到低）：
     * 1. 优先查找专用Handler（如AliyunOSSHandler）
     * 2. 如果没有专用Handler，检查是否有GenericConfigDrivenHandler作为回退
     *
     * @param configType   配置类型
     * @param providerCode 服务商代码
     * @return true=支持，false=不支持
     */
    public boolean isSupported(String configType, String providerCode) {
        // 1. 优先查找专用Handler
        Map<String, IProviderHandler<?, ?>> typeHandlers = handlerRegistry.get(configType);
        if (typeHandlers != null && typeHandlers.containsKey(providerCode)) {
            return true;
        }

        // 2. 回退逻辑：如果找不到专用Handler，但genericConfigDrivenHandler存在，也返回true
        // 这样新模型即使还没有专用Handler，也可以通过配置驱动的方式使用
        return genericConfigDrivenHandler != null;
    }

    /**
     * 获取指定配置类型下所有已注册的服务商代码
     *
     * @param configType 配置类型
     * @return 服务商代码列表
     */
    public List<String> getSupportedProviders(String configType) {
        Map<String, IProviderHandler<?, ?>> typeHandlers = handlerRegistry.get(configType);
        if (typeHandlers == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(typeHandlers.keySet());
    }

    /**
     * 获取所有已注册的配置类型
     *
     * @return 配置类型列表
     */
    public List<String> getSupportedConfigTypes() {
        return new ArrayList<>(handlerRegistry.keySet());
    }

    /**
     * 清除指定配置类型和服务商的缓存
     *
     * @param configType   配置类型
     * @param providerCode 服务商代码
     */
    public void invalidateCache(String configType, String providerCode) {
        String cacheKey = configType + ":" + providerCode;
        handlerCache.remove(cacheKey);
        log.debug("清除Handler缓存: configType={}, providerCode={}", configType, providerCode);
    }

    /**
     * 清除所有缓存
     */
    public void invalidateAll() {
        handlerCache.clear();
        log.debug("清除所有Handler缓存");
    }
}
