package com.bigyun.auth.config;

import com.bigyun.provider.core.ProviderRuntimeExecutor;
import com.bigyun.provider.core.ProviderRuntimeInvoker;
import com.bigyun.provider.core.SnapshotOnlyProviderRuntimeExecutor;
import com.bigyun.provider.core.runtime.ConfigDrivenProviderRuntimeInvoker;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * auth 服务本地 Provider 运行时。
 * <p>
 * auth 不接入 Provider 配置库数据源，只消费 config-service 发布到 Redis 的运行时快照。
 * 快照缺失或执行失败时，由业务服务继续走原 remote config-service fallback。
 * </p>
 */
@Configuration
public class ProviderRuntimeAuthConfig
{
    @Bean
    @ConditionalOnMissingBean
    public ProviderRuntimeInvoker providerRuntimeInvoker()
    {
        return new ConfigDrivenProviderRuntimeInvoker();
    }

    @Bean
    @ConditionalOnBean({ProviderRuntimeSnapshotStore.class, ProviderRuntimeInvoker.class})
    @ConditionalOnMissingBean
    public ProviderRuntimeExecutor providerRuntimeExecutor(ProviderRuntimeSnapshotStore snapshotStore,
                                                           ProviderRuntimeInvoker runtimeInvoker)
    {
        return new SnapshotOnlyProviderRuntimeExecutor(snapshotStore, runtimeInvoker);
    }

    @Bean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnMissingBean
    public ProviderRuntimeSnapshotStore providerRuntimeSnapshotStore(
            RedisConnectionFactory connectionFactory,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider)
    {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null)
        {
            redisTemplate = new StringRedisTemplate(connectionFactory);
            redisTemplate.afterPropertiesSet();
        }
        return new ProviderRuntimeSnapshotStore(redisTemplate);
    }
}
