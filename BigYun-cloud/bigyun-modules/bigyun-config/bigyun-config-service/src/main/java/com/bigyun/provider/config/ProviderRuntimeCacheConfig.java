package com.bigyun.provider.config;

import com.bigyun.config.mapper.ProviderConfigReadMapper;
import com.bigyun.config.reader.ConfigReader;
import com.bigyun.provider.cache.CacheInvalidator;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Provider 运行时缓存配置。
 * <p>
 * config-service 是配置控制面，也会通过 provider-core 执行内部 Provider 请求，因此需要在本服务内创建
 * {@link ConfigReader}。Redis 监听器负责接收默认配置切换通知，并刷新本地缓存。
 * </p>
 */
@Configuration
public class ProviderRuntimeCacheConfig
{
    /**
     * 创建 Provider 配置读取器。
     */
    @Bean
    public ConfigReader configReader(ProviderConfigReadMapper mapper)
    {
        return new ConfigReader(mapper);
    }

    /**
     * 监听 Provider 配置变更消息，刷新 config-service 内部运行时缓存。
     */
    @Bean
    public MessageListener providerConfigChangeListener(
            ConfigReader configReader,
            ObjectProvider<ProviderRuntimeSnapshotStore> snapshotStoreProvider)
    {
        return new MessageListener()
        {
            @Override
            public void onMessage(Message message, byte[] pattern)
            {
                String configType = new String(message.getBody(), StandardCharsets.UTF_8);
                configReader.refresh(configType);
                ProviderRuntimeSnapshotStore snapshotStore = snapshotStoreProvider.getIfAvailable();
                if (snapshotStore != null)
                {
                    snapshotStore.invalidateLocalCache();
                }
            }
        };
    }

    /**
     * 注册 Redis Pub/Sub 监听容器。
     */
    @Bean
    public RedisMessageListenerContainer providerConfigChangeListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListener providerConfigChangeListener)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(providerConfigChangeListener, new ChannelTopic(CacheInvalidator.CHANNEL));
        return container;
    }
}
