package com.bigyun.file.config;

import com.bigyun.config.mapper.ProviderConfigReadMapper;
import com.bigyun.config.reader.ConfigReader;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * <p>
 * <ul>
 *   <li>{@link ConfigReader}：Provider 配置读取器，注入只读 Mapper</li>
 *   <li>{@link MessageListener}：Redis 消息监听器，监听配置变更频道</li>
 *   <li>{@link RedisMessageListenerContainer}：Redis 消息监听容器</li>
 * </ul>
 * </p>
 *
 * <p><b>工作流程：</b>
 * <ol>
 *   <li>配置管理服务修改 Provider 配置后，通过 Redis Pub/Sub 发送变更通知到频道 {@code bigyun:config:changed}</li>
 *   <li>本模块的 RedisMessageListenerContainer 收到消息后，调用 ConfigReader.refresh() 刷新本地 Caffeine 缓存</li>
 * </ol>
 * </p>
 *
 * @author bigyun
 */
@Configuration
public class CacheConfig
{
    /** 配置变更 Redis 频道名称，需与 CacheInvalidator.CHANNEL 保持一致 */
    private static final String CONFIG_CHANGED_CHANNEL = "bigyun:config:changed";

    /**
     * 创建 ConfigReader Bean。
     * <p>注入 ProviderConfigReadMapper，用于从数据库读取 Provider 配置。</p>
     *
     * @param mapper Provider 配置只读 Mapper
     * @return ConfigReader 实例
     */
    @Bean
    public ConfigReader configReader(ProviderConfigReadMapper mapper)
    {
        return new ConfigReader(mapper);
    }

    /**
     * 创建 Provider 配置变更消息监听器。
     * <p>收到 Redis 消息后，解析 configType 并调用 ConfigReader.refresh() 刷新缓存。</p>
     *
     * @param configReader 配置读取器
     * @return MessageListener 实例
     */
    @Bean
    public MessageListener providerConfigChangeListener(ConfigReader configReader)
    {
        return new MessageListener()
        {
            /**
             * 处理 Redis Pub/Sub 消息。
             * <p>从消息体中提取 configType（UTF-8 编码），触发对应类型的缓存刷新。
             * 如果 configType 为空字符串则表示全局刷新。</p>
             *
             * @param message Redis 消息（body 为 configType 的 UTF-8 字节）
             * @param pattern 匹配的频道模式（未使用）
             */
            @Override
            public void onMessage(Message message, byte[] pattern)
            {
                String configType = new String(message.getBody(), StandardCharsets.UTF_8);
                configReader.refresh(configType);
            }
        };
    }

    /**
     * 创建 Redis 消息监听容器。
     *
     * @param connectionFactory Redis 连接工厂
     * @param providerConfigChangeListener 配置变更消息监听器
     * @return RedisMessageListenerContainer 实例
     */
    @Bean
    public RedisMessageListenerContainer configChangeListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListener providerConfigChangeListener)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(providerConfigChangeListener, new ChannelTopic(CONFIG_CHANGED_CHANNEL));
        return container;
    }
}
