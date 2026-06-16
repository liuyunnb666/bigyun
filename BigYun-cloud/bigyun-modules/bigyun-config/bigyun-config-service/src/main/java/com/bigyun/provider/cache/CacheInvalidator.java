package com.bigyun.provider.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Provider 配置缓存失效通知器。
 * <p>
 * 当 Provider 配置发生新增、删除或修改时，调用 {@link #notify(String)} 向 Redis Pub/Sub
 * 频道 {@code bigyun:config:changed} 发布变更通知。各服务实例中的
 * {@link com.bigyun.config.reader.ConfigReader} 收到通知后会刷新本地 Caffeine 缓存。
 * </p>
 *
 * <p><b>使用约定：</b></p>
 * <ul>
 *     <li>{@code configType} 为 {@code null} 时发送空字符串，表示全局刷新。</li>
 * </ul>
 *
 * @author bigyun
 */
@Component
public class CacheInvalidator
{
    /** Redis Pub/Sub 频道名称，用于发布 Provider 配置变更通知。 */
    public static final String CHANNEL = "bigyun:config:changed";

    private static final Logger log = LoggerFactory.getLogger(CacheInvalidator.class);

    /** Redis 字符串模板，用于发送 Pub/Sub 消息。 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 构造方法注入 Redis 模板。
     *
     * @param redisTemplate Redis 字符串模板
     */
    public CacheInvalidator(StringRedisTemplate redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发布 Provider 配置缓存失效通知。
     *
     * @param configType 发生变更的配置类型；为 {@code null} 时发送空字符串，表示全局刷新
     * @return {@code true} 表示通知发送成功，{@code false} 表示通知发送失败
     */
    public boolean notify(String configType)
    {
        try
        {
            redisTemplate.convertAndSend(CHANNEL, configType == null ? "" : configType);
            log.info("Provider config cache invalidation published: configType={}", configType);
            return true;
        }
        catch (Exception e)
        {
            log.warn("Provider config cache invalidation publish failed: {}", e.getMessage());
            return false;
        }
    }
}
