package com.bigyun.provider.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigyun.config.enums.ProviderCodeEnum;
import com.bigyun.config.enums.ProviderConfigTypeEnum;
import com.bigyun.provider.domain.ProviderMeta;
import com.bigyun.provider.mapper.ProviderMetaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provider枚举管理器
 *
 * 核心功能：
 * 1. 应用启动时自动执行，将Java枚举同步到数据库
 * 2. 实现枚举与数据库的双向绑定
 * 3. 支持枚举的新增和更新（不删除已有数据，保证数据安全）
 *
 * 设计思想：
 * - 枚举是代码层面的元数据定义（类型安全、编译时检查）
 * - 数据库是运行时的元数据存储（支持动态查询、前端展示）
 * - 通过自动同步，保证两者的一致性
 *
 * 使用场景：
 * 1. 开发人员在枚举中新增配置类型或服务商
 * 2. 重启应用后，自动同步到数据库
 * 3. 前端可以从数据库动态获取最新的配置选项
 *
 * @author BigYun
 * @date 2026-05-21
 */
@Component
public class ProviderEnumManager implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProviderEnumManager.class);

    private final ProviderMetaMapper providerMetaMapper;

    public ProviderEnumManager(ProviderMetaMapper providerMetaMapper) {
        this.providerMetaMapper = providerMetaMapper;
    }

    /**
     * 应用启动时自动执行
     */
    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("开始同步Provider枚举到数据库...");
        log.info("========================================");
        syncProviderEnums();
        log.info("========================================");
        log.info("Provider枚举同步完成");
        log.info("========================================");
    }

    /**
     * 同步枚举到数据库
     * 包括配置类型枚举和服务商枚举
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncProviderEnums() {
        // 1. 同步配置类型枚举（如：storage、llm、tts等）
        syncConfigTypes();

        // 2. 同步服务商枚举（如：local、aliyun-oss、openai-gpt等）
        syncProviderCodes();
    }

    /**
     * 同步配置类型枚举
     * 遍历ProviderConfigTypeEnum，将每个枚举值同步到数据库
     */
    private void syncConfigTypes() {
        log.info("开始同步配置类型枚举...");

        for (ProviderConfigTypeEnum typeEnum : ProviderConfigTypeEnum.values()) {
            // 查询数据库中是否已存在该配置类型
            LambdaQueryWrapper<ProviderMeta> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProviderMeta::getMetaType, "config_type");
            wrapper.eq(ProviderMeta::getMetaCode, typeEnum.getCode());

            ProviderMeta existing = providerMetaMapper.selectOne(wrapper);

            if (existing == null) {
                // 不存在，新增
                ProviderMeta meta = new ProviderMeta();
                meta.setMetaType("config_type");
                meta.setMetaCode(typeEnum.getCode());
                meta.setMetaName(typeEnum.getName());
                meta.setMetaDescription(typeEnum.getDescription());
                meta.setStatus("0");
                meta.setSortOrder(typeEnum.ordinal());
                providerMetaMapper.insert(meta);
                log.info("  [新增] 配置类型: {} - {}", typeEnum.getCode(), typeEnum.getName());
            } else {
                // 已存在，检查是否需要更新
                boolean needUpdate = false;

                if (!existing.getMetaName().equals(typeEnum.getName())) {
                    existing.setMetaName(typeEnum.getName());
                    needUpdate = true;
                }

                if (!existing.getMetaDescription().equals(typeEnum.getDescription())) {
                    existing.setMetaDescription(typeEnum.getDescription());
                    needUpdate = true;
                }

                if (existing.getSortOrder() == null || existing.getSortOrder() != typeEnum.ordinal()) {
                    existing.setSortOrder(typeEnum.ordinal());
                    needUpdate = true;
                }

                if (needUpdate) {
                    providerMetaMapper.updateById(existing);
                    log.info("  [更新] 配置类型: {} - {}", typeEnum.getCode(), typeEnum.getName());
                }
            }
        }

        log.info("配置类型枚举同步完成，共处理 {} 个配置类型", ProviderConfigTypeEnum.values().length);
    }

    /**
     * 同步服务商枚举
     * 遍历ProviderCodeEnum，将每个枚举值同步到数据库
     */
    private void syncProviderCodes() {
        log.info("开始同步服务商枚举...");

        for (ProviderCodeEnum providerEnum : ProviderCodeEnum.values()) {
            // 查询数据库中是否已存在该服务商
            LambdaQueryWrapper<ProviderMeta> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProviderMeta::getMetaType, "provider_code");
            wrapper.eq(ProviderMeta::getMetaCode, providerEnum.getCode());

            ProviderMeta existing = providerMetaMapper.selectOne(wrapper);

            if (existing == null) {
                // 不存在，新增
                ProviderMeta meta = new ProviderMeta();
                meta.setMetaType("provider_code");
                meta.setMetaCode(providerEnum.getCode());
                meta.setMetaName(providerEnum.getName());
                meta.setMetaDescription(providerEnum.getType().getName());
                meta.setParentCode(providerEnum.getType().getCode());
                meta.setStatus("0");
                meta.setSortOrder(providerEnum.ordinal());
                providerMetaMapper.insert(meta);
                log.info("  [新增] 服务商: {} - {} (所属类型: {})",
                    providerEnum.getCode(),
                    providerEnum.getName(),
                    providerEnum.getType().getName());
            } else {
                // 已存在，检查是否需要更新
                boolean needUpdate = false;

                if (!existing.getMetaName().equals(providerEnum.getName())) {
                    existing.setMetaName(providerEnum.getName());
                    needUpdate = true;
                }

                if (!existing.getParentCode().equals(providerEnum.getType().getCode())) {
                    existing.setParentCode(providerEnum.getType().getCode());
                    needUpdate = true;
                }

                if (existing.getSortOrder() == null || existing.getSortOrder() != providerEnum.ordinal()) {
                    existing.setSortOrder(providerEnum.ordinal());
                    needUpdate = true;
                }

                if (needUpdate) {
                    providerMetaMapper.updateById(existing);
                    log.info("  [更新] 服务商: {} - {}", providerEnum.getCode(), providerEnum.getName());
                }
            }
        }

        log.info("服务商枚举同步完成，共处理 {} 个服务商", ProviderCodeEnum.values().length);
    }

    /**
     * 获取所有配置类型（从数据库）
     *
     * @return 配置类型列表
     */
    public List<ProviderMeta> getAllConfigTypes() {
        LambdaQueryWrapper<ProviderMeta> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderMeta::getMetaType, "config_type");
        wrapper.eq(ProviderMeta::getStatus, "0");
        wrapper.orderByAsc(ProviderMeta::getSortOrder);
        return providerMetaMapper.selectList(wrapper);
    }

    /**
     * 获取指定类型的所有服务商（从数据库）
     *
     * @param configType 配置类型代码（如：storage、llm）
     * @return 服务商列表
     */
    public List<ProviderMeta> getProvidersByType(String configType) {
        LambdaQueryWrapper<ProviderMeta> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderMeta::getMetaType, "provider_code");
        wrapper.eq(ProviderMeta::getParentCode, configType);
        wrapper.eq(ProviderMeta::getStatus, "0");
        wrapper.orderByAsc(ProviderMeta::getSortOrder);
        return providerMetaMapper.selectList(wrapper);
    }

    /**
     * 验证配置类型是否存在
     *
     * @param configType 配置类型代码
     * @return true=存在，false=不存在
     */
    public boolean isValidConfigType(String configType) {
        try {
            ProviderConfigTypeEnum.fromCode(configType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 验证服务商代码是否存在
     *
     * @param providerCode 服务商代码
     * @return true=存在，false=不存在
     */
    public boolean isValidProviderCode(String providerCode) {
        try {
            ProviderCodeEnum.fromCode(providerCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取服务商所属的配置类型
     *
     * @param providerCode 服务商代码
     * @return 配置类型代码，如果不存在返回null
     */
    public String getConfigTypeByProvider(String providerCode) {
        try {
            ProviderCodeEnum providerEnum = ProviderCodeEnum.fromCode(providerCode);
            return providerEnum.getType().getCode();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
