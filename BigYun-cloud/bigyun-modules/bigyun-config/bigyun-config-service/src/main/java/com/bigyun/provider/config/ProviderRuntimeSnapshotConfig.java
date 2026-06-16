package com.bigyun.provider.config;

import com.bigyun.config.mapper.ProviderConfigReadMapper;
import com.bigyun.config.reader.ConfigReader;
import com.bigyun.provider.core.ProviderRuntimeExecutor;
import com.bigyun.provider.core.ProviderRuntimeInvoker;
import com.bigyun.provider.core.runtime.ConfigDrivenProviderRuntimeInvoker;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import com.bigyun.provider.db.mapper.ProviderApiTemplateReadMapper;
import com.bigyun.provider.db.mapper.ProviderCapabilityReadMapper;
import com.bigyun.provider.db.runtime.DbProviderApiTemplateResolver;
import com.bigyun.provider.db.runtime.DbProviderRuntimeExecutor;
import com.bigyun.provider.db.snapshot.ProviderRuntimeSnapshotPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Provider 运行时快照发布配置。
 * <p>
 * config-service 作为控制面负责把数据库中的 Provider 配置发布到 Redis。业务模块运行时优先消费 Redis
 * 快照，Redis 未命中时再读取配置库兜底。
 * </p>
 */
@Configuration
public class ProviderRuntimeSnapshotConfig
{
    private static final Logger log = LoggerFactory.getLogger(ProviderRuntimeSnapshotConfig.class);

    /**
     * Redis 快照读写组件，负责运行时快照的本地短缓存和 Redis 存取。
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderRuntimeSnapshotStore providerRuntimeSnapshotStore(StringRedisTemplate redisTemplate)
    {
        return new ProviderRuntimeSnapshotStore(redisTemplate);
    }

    /**
     * 通用 Provider HTTP 执行器。config-service 和业务模块都复用这一套模板渲染、鉴权、请求和响应解析逻辑。
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderRuntimeInvoker providerRuntimeInvoker(ProviderApiTemplateReadMapper templateReadMapper)
    {
        return new ConfigDrivenProviderRuntimeInvoker(new DbProviderApiTemplateResolver(templateReadMapper));
    }

    @Bean
    @ConditionalOnMissingBean
    public ProviderRuntimeExecutor providerRuntimeExecutor(ProviderCapabilityReadMapper capabilityReadMapper,
                                                           ConfigReader configReader,
                                                           ProviderRuntimeInvoker runtimeInvoker,
                                                           ProviderRuntimeSnapshotStore snapshotStore)
    {
        DbProviderRuntimeExecutor executor = new DbProviderRuntimeExecutor(capabilityReadMapper,
                configReader, runtimeInvoker);
        executor.setSnapshotStore(snapshotStore);
        return executor;
    }

    /**
     * 运行时快照发布器，配置变更、默认切换和手动刷新时通过它重建 Redis 快照。
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderRuntimeSnapshotPublisher providerRuntimeSnapshotPublisher(
            ProviderRuntimeSnapshotStore snapshotStore,
            ProviderCapabilityReadMapper capabilityReadMapper,
            ProviderConfigReadMapper configReadMapper,
            ProviderApiTemplateReadMapper templateReadMapper)
    {
        return new ProviderRuntimeSnapshotPublisher(snapshotStore, capabilityReadMapper,
                configReadMapper, templateReadMapper);
    }

    /**
     * config-service 启动后尝试全量发布一次快照。发布失败只记录告警，不阻断服务启动。
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> providerRuntimeSnapshotStartupPublisher(
            ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher)
    {
        return event -> {
            try
            {
                int count = runtimeSnapshotPublisher.publishAll();
                log.info("Provider runtime snapshots published on config-service startup: count={}", count);
            }
            catch (Exception e)
            {
                log.warn("Provider runtime snapshot startup publish failed: {}", e.getMessage());
            }
        };
    }
}
