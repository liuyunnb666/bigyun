package com.bigyun.config.reader;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.datasource.annotation.Slave;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.ProviderConfigEntity;
import com.bigyun.config.mapper.ProviderConfigReadMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * Provider 配置读取器（带 Caffeine 缓存 + 主从切换 + 缓存刷新窗口）。
 * <p>
 * <b>核心架构：</b>
 * <ol>
 *   <li><b>双层缓存</b>：defaultCache（默认配置单条缓存）+ listCache（列表缓存），均使用 Caffeine 本地缓存，TTL=5 分钟</li>
 *   <li><b>主从切换</b>：配置变更后 10 秒内（REFRESH_MASTER_READ_WINDOW），强制从主库读取，避免主从延迟导致读到旧数据</li>
 *   <li><b>缓存刷新</b>：收到 Redis Pub/Sub 通知时调用 {@link #refresh(String)} 或 {@link #refreshAll()} 清空本地缓存</li>
 * </ol>
 * </p>
 *
 * <p><b>使用方式：</b>由各业务模块通过 {@code @Bean} 方法创建，注入 {@link ProviderConfigReadMapper}。</p>
 *
 * @author bigyun
 */
public class ConfigReader
{
    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);

    /** Caffeine 缓存过期时间：5 分钟 */
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private static final Duration REFRESH_MASTER_READ_WINDOW = Duration.ofSeconds(10);

    /** Provider 配置数据库访问 Mapper（读库） */
    private final ProviderConfigReadMapper mapper;

    /**
     * <p>key=configType（小写规范化后），value=强制主库读取的截止时间戳（毫秒）。</p>
     */
    private final ConcurrentHashMap<String, Long> masterReadUntil = new ConcurrentHashMap<>();

    /** 全局主库读取截止时间（refreshAll 时设置） */
    private volatile long masterReadAllUntil;

    /** 默认配置缓存，key=configType，value=ProviderConfigDTO */
    private Cache<String, ProviderConfigDTO> defaultCache;

    /** 配置列表缓存，key=configType，value=已启用配置 DTO 列表 */
    private Cache<String, List<ProviderConfigDTO>> listCache;

    /**
     * 构造函数。
     *
     * @param mapper Provider 配置只读 Mapper
     */
    public ConfigReader(ProviderConfigReadMapper mapper)
    {
        this.mapper = mapper;
    }

    /**
     * 初始化 Caffeine 缓存实例。
     * <p>在 Spring Bean 初始化完成后自动调用。</p>
     */
    @PostConstruct
    public void init()
    {
        this.defaultCache = Caffeine.newBuilder().maximumSize(50).expireAfterWrite(CACHE_TTL).build();
        this.listCache = Caffeine.newBuilder().maximumSize(50).expireAfterWrite(CACHE_TTL).build();
        log.info("ConfigReader initialized");
    }

    /**
     * 获取指定类型的默认 Provider 配置。
     * <p>查询结果自动缓存。</p>
     *
     * @param configType 配置类型（如 "llm", "storage"）
     * @return 默认 Provider 配置 DTO（含完整密钥）
     * @throws ServiceException 如果未找到可用的默认配置
     */
    @Slave
    public ProviderConfigDTO getDefaultConfig(String configType)
    {
        String normalizedType = normalizeCode(configType);
        // 优先从本地缓存获取
        ProviderConfigDTO cached = defaultCache.getIfPresent(normalizedType);
        if (cached != null)
        {
            return cached;
        }

        ProviderConfigEntity entity = readAfterRefresh(normalizedType, () -> mapper.selectOne(new LambdaQueryWrapper<ProviderConfigEntity>()
                .eq(ProviderConfigEntity::getConfigType, normalizedType)
                .eq(ProviderConfigEntity::getIsDefault, UserConstants.YES)
                .eq(ProviderConfigEntity::getStatus, UserConstants.NORMAL)
                .last("limit 1")));
        if (entity == null)
        {
            throw new ServiceException("未找到可用的默认 Provider 配置: " + configType);
        }

        ProviderConfigDTO dto = toDto(entity);
        defaultCache.put(normalizedType, dto);
        return dto;
    }

    /**
     *
     * @param configType 配置类型
     */
    @Slave
    public List<ProviderConfigDTO> listEnabledConfigs(String configType)
    {
        String normalizedType = normalizeCode(configType);
        // 优先从本地缓存获取
        List<ProviderConfigDTO> cached = listCache.getIfPresent(normalizedType);
        if (cached != null)
        {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<ProviderConfigEntity> entities = readAfterRefresh(normalizedType, () -> mapper.selectList(new LambdaQueryWrapper<ProviderConfigEntity>()
                .eq(ProviderConfigEntity::getConfigType, normalizedType)
                .eq(ProviderConfigEntity::getStatus, UserConstants.NORMAL)
                .orderByDesc(ProviderConfigEntity::getIsDefault)
                .orderByDesc(ProviderConfigEntity::getUpdateTime)));
        if (entities == null || entities.isEmpty())
        {
            return Collections.emptyList();
        }

        List<ProviderConfigDTO> result = new ArrayList<>(entities.size());
        for (ProviderConfigEntity entity : entities)
        {
            result.add(toDto(entity));
        }
        listCache.put(normalizedType, result);
        return result;
    }

    /**
     * <p>清空 defaultCache 和 listCache，并标记全局主库读取。</p>
     */
    public void refreshAll()
    {
        defaultCache.invalidateAll();
        listCache.invalidateAll();
        markMasterRead(null);
        log.info("ConfigReader cache refreshed");
    }

    /**
     * 如果 configType 为 null 或 blank，则等同于 refreshAll。
     *
     * @param configType 配置类型（null/blank 时刷新全部）
     */
    public void refresh(String configType)
    {
        if (StringUtils.isBlank(configType))
        {
            refreshAll();
            return;
        }
        String normalizedType = normalizeCode(configType);
        defaultCache.invalidate(normalizedType);
        listCache.invalidate(normalizedType);
        markMasterRead(normalizedType);
        log.info("ConfigReader cache refreshed: configType={}", normalizedType);
    }

    /**
     * 使用动态数据源切换（DynamicDataSourceContextHolder.push("master")）。
     *
     * @param configType 配置类型（用于判断该类型是否在刷新窗口内）
     * @param supplier 数据查询逻辑（Lambda 表达式）
     * @param <T> 返回类型
     * @return 查询结果
     */
    private <T> T readAfterRefresh(String configType, Supplier<T> supplier)
    {
        if (!shouldReadMaster(configType))
        {
            return supplier.get();
        }
        // 切换到主库数据源
        DynamicDataSourceContextHolder.push("master");
        try
        {
            return supplier.get();
        }
        finally
        {
            // 恢复数据源
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * <ul>
     *   <li>{@code masterReadAllUntil}：refreshAll 设置的全局标记</li>
     *   <li>{@code masterReadUntil.get(configType)}：refresh(configType) 设置的单个类型标记</li>
     * </ul>
     * </p>
     *
     * @param configType 配置类型（null 时仅检查全局标记）
     * @return true=应该走主库，false=走从库
     */
    private boolean shouldReadMaster(String configType)
    {
        long now = System.currentTimeMillis();
        Long typeDeadline = configType == null ? null : masterReadUntil.get(configType);
        if (typeDeadline != null && typeDeadline <= now)
        {
            // 标记已过期，清理
            masterReadUntil.remove(configType, typeDeadline);
            typeDeadline = null;
        }
        return Math.max(masterReadAllUntil, typeDeadline == null ? 0L : typeDeadline) > now;
    }

    /**
     * <p>设置 10 秒（REFRESH_MASTER_READ_WINDOW）内的主库读取窗口。
     * configType 为 null 时设置全局标记，否则设置特定类型的标记。</p>
     *
     * @param configType 配置类型（null=全局标记）
     */
    private void markMasterRead(String configType)
    {
        long deadline = System.currentTimeMillis() + REFRESH_MASTER_READ_WINDOW.toMillis();
        if (StringUtils.isBlank(configType))
        {
            masterReadAllUntil = deadline;
            return;
        }
        masterReadUntil.put(configType, deadline);
    }

    /**
     * 实体转 DTO。
     *
     * @param entity ProviderConfigEntity
     * @return ProviderConfigDTO
     */
    private ProviderConfigDTO toDto(ProviderConfigEntity entity)
    {
        ProviderConfigDTO dto = new ProviderConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     *
     * @param value 原始值
     * @return 标准化后的值（null 输入返回 null）
     */
    private String normalizeCode(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim().toLowerCase();
    }
}
