package com.bigyun.provider.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.datasource.annotation.Master;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.provider.cache.CacheInvalidator;
import com.bigyun.provider.db.snapshot.ProviderRuntimeSnapshotPublisher;
import com.bigyun.provider.domain.ProviderConfig;
import com.bigyun.provider.factory.ProviderHandlerFactory;
import com.bigyun.provider.mapper.ProviderConfigMapper;
import com.bigyun.provider.service.IProviderConfigService;
import com.bigyun.provider.service.handler.GenericConfigDrivenHandler;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Provider 配置服务。
 * 负责配置查询、脱敏、保存校验、默认项切换，以及事务提交后的运行时快照发布。
 */
@Service
public class ProviderConfigServiceImpl extends ServiceImpl<ProviderConfigMapper, ProviderConfig>
        implements IProviderConfigService
{
    private static final String CONFIG_TYPE_FACE = "face";

    private static final String PROVIDER_FACEPLUS = "faceplus";

    private static final String FACEPLUS_CN_ENDPOINT = "https://api-cn.faceplusplus.com";

    private final ProviderHandlerFactory providerHandlerFactory;

    private final GenericConfigDrivenHandler genericConfigDrivenHandler;

    private final CacheInvalidator cacheInvalidator;

    private final ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher;

    public ProviderConfigServiceImpl(ProviderHandlerFactory providerHandlerFactory,
                                     GenericConfigDrivenHandler genericConfigDrivenHandler,
                                     CacheInvalidator cacheInvalidator,
                                     ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher)
    {
        this.providerHandlerFactory = providerHandlerFactory;
        this.genericConfigDrivenHandler = genericConfigDrivenHandler;
        this.cacheInvalidator = cacheInvalidator;
        this.runtimeSnapshotPublisher = runtimeSnapshotPublisher;
    }

    /**
     * 按后台管理查询条件加载 Provider 配置列表。
     * <p>
     * 返回给前端前会统一脱敏 AccessKey、SecretKey，只保留掩码字段，避免密钥出现在接口响应中。
     *
     * @param query 查询条件，支持配置类型、Provider 编码、名称和状态
     * @return 已脱敏的 Provider 配置列表
     */
    @Override
    public List<ProviderConfig> selectProviderConfigList(ProviderConfig query)
    {
        LambdaQueryWrapper<ProviderConfig> wrapper = buildListWrapper(query);
        List<ProviderConfig> list = list(wrapper);
        List<ProviderConfig> sanitized = new ArrayList<>(list.size());
        for (ProviderConfig item : list)
        {
            sanitized.add(sanitize(item));
        }
        return sanitized;
    }

    /**
     * 根据配置主键查询单条 Provider 配置。
     * <p>
     * 该方法面向管理端展示，返回前会清空明文密钥并填充掩码字段。
     *
     * @param configId Provider 配置主键
     * @return 已脱敏的 Provider 配置；不存在时返回 null
     */
    @Override
    public ProviderConfig selectProviderConfigById(Long configId)
    {
        return sanitize(getById(configId));
    }

    /**
     * 查询指定配置类型当前启用的默认 Provider 配置。
     * <p>
     * 该方法用于管理端查看默认项，因此同样返回脱敏后的实体。
     *
     * @param configType 配置类型，例如 storage、llm、face
     * @return 已脱敏的默认 Provider 配置
     * @throws ServiceException 当前类型没有可用默认配置时抛出
     */
    @Override
    public ProviderConfig selectDefaultProviderConfig(String configType)
    {
        return sanitize(getDefaultEntity(configType));
    }

    /**
     * 查询内部调用使用的默认 Provider 配置 DTO。
     * <p>
     * 与管理端查询不同，该方法保留运行时调用需要的真实字段，供 Feign、Handler 和运行时快照使用。
     *
     * @param configType 配置类型，例如 storage、llm、face
     * @return 默认 Provider 配置 DTO
     * @throws ServiceException 当前类型没有可用默认配置时抛出
     */
    @Override
    public ProviderConfigDTO selectDefaultProviderConfigInternal(String configType)
    {
        return toDto(getDefaultEntity(configType));
    }

    /**
     * 查询指定配置类型下所有启用的 Provider 配置。
     * <p>
     * 返回结果按默认项和更新时间排序，供运行时快照、能力路由和降级候选列表使用。
     *
     * @param configType 配置类型
     * @return 启用配置 DTO 列表
     */
    @Override
    public List<ProviderConfigDTO> selectEnabledProviderConfigsInternal(String configType)
    {
        LambdaQueryWrapper<ProviderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderConfig::getConfigType, normalizeCode(configType));
        wrapper.eq(ProviderConfig::getStatus, UserConstants.NORMAL);
        wrapper.orderByDesc(ProviderConfig::getIsDefault);
        wrapper.orderByDesc(ProviderConfig::getUpdateTime);
        List<ProviderConfig> list = list(wrapper);
        List<ProviderConfigDTO> result = new ArrayList<>(list.size());
        for (ProviderConfig config : list)
        {
            result.add(toDto(config));
        }
        return result;
    }

    /**
     * 查询指定配置类型和 Provider 编码的启用配置。
     * <p>
     * 主要用于业务方显式指定服务商时的运行时调用，不做脱敏处理。
     *
     * @param configType 配置类型
     * @param providerCode Provider 编码
     * @return 匹配的 Provider 配置 DTO
     * @throws ServiceException 未找到启用配置时抛出
     */
    @Override
    public ProviderConfigDTO getConfig(String configType, String providerCode)
    {
        LambdaQueryWrapper<ProviderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderConfig::getConfigType, normalizeCode(configType));
        wrapper.eq(ProviderConfig::getProviderCode, normalizeCode(providerCode));
        wrapper.eq(ProviderConfig::getStatus, UserConstants.NORMAL);
        wrapper.last("limit 1");
        ProviderConfig config = getOne(wrapper, false);
        if (config == null)
        {
            throw new ServiceException(
                    String.format("未找到匹配的 Provider 配置: configType=%s, providerCode=%s", configType, providerCode));
        }
        return toDto(config);
    }

    /**
     * 新增 Provider 配置。
     * <p>
     * 方法会先规范化编码和地址字段，再执行配置合法性校验、密钥继承处理和可选的连接校验。
     * 如果新增项被设为默认配置，会在同一事务内清除同类型其他默认标记；事务提交后再发布运行时快照并通知缓存刷新。
     *
     * @param config 待新增的 Provider 配置
     * @return 1 表示新增成功，0 表示未写入
     * @throws ServiceException 配置不受支持、必填项缺失或连接校验失败时抛出
     */
    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public int insertProviderConfig(ProviderConfig config)
    {
        normalize(config);
        validate(config, null);
        applyPlainSecrets(config, null);
        if (!StringUtils.equals(UserConstants.YES, config.getIsDefault()))
        {
            config.setIsDefault("N");
        }
        if (StringUtils.isBlank(config.getStatus()))
        {
            config.setStatus(UserConstants.NORMAL);
        }
        if (StringUtils.equals(UserConstants.YES, config.getIsDefault()))
        {
            clearDefaultFlag(config.getConfigType());
            config.setStatus(UserConstants.NORMAL);
        }
        if (requiresConnectionValidation(config, null)
                && !ProviderConfigConstants.PROVIDER_LOCAL.equals(config.getProviderCode()))
        {
            boolean isValid = genericConfigDrivenHandler.validateConnection(toDto(config));
            if (!isValid)
            {
                throw new ServiceException("API key validation failed. Please check provider config.");
            }
        }

        boolean saved = save(config);
        if (saved)
        {
            providerHandlerFactory.invalidateCache(config.getConfigType(), config.getProviderCode());
            notifyAfterCommit(config.getConfigType());
        }
        return saved ? 1 : 0;
    }

    /**
     * 更新 Provider 配置。
     * <p>
     * 更新前会把请求字段与数据库当前值合并，避免局部编辑时清空 endpoint、bucket、密钥等运行时字段。
     * 修改默认标记、运行时敏感字段或连接字段后，会在提交后刷新对应配置类型的运行时快照。
     *
     * @param config 管理端提交的配置变更
     * @return 1 表示更新成功，0 表示未写入
     * @throws ServiceException 配置不存在、配置校验失败或连接校验失败时抛出
     */
    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public int updateProviderConfig(ProviderConfig config)
    {
        ProviderConfig current = getById(config.getConfigId());
        if (current == null)
        {
            throw new ServiceException("Provider config does not exist.");
        }

        ProviderConfig merged = mergeForUpdate(config, current);
        normalize(merged);
        validate(merged, current);
        applyPlainSecrets(merged, current);
        if (StringUtils.equals(UserConstants.YES, merged.getIsDefault()))
        {
            clearDefaultFlag(merged.getConfigType());
            merged.setStatus(UserConstants.NORMAL);
        }
        if (requiresConnectionValidation(merged, current)
                && !ProviderConfigConstants.PROVIDER_LOCAL.equals(merged.getProviderCode()))
        {
            boolean isValid = genericConfigDrivenHandler.validateConnection(toDto(merged));
            if (!isValid)
            {
                throw new ServiceException("API key validation failed. Please check provider config.");
            }
        }

        boolean updated = updateById(merged);
        if (updated)
        {
            providerHandlerFactory.invalidateCache(merged.getConfigType(), merged.getProviderCode());
            notifyAfterCommit(merged.getConfigType());
        }
        return updated ? 1 : 0;
    }

    /**
     * 启用或停用 Provider 配置。
     * <p>
     * 默认配置不能被直接停用，必须先切换默认项，避免业务运行时找不到可用配置。
     * 状态更新成功后会清理 Handler 缓存，并在事务提交后发布新的运行时快照。
     *
     * @param configId Provider 配置主键
     * @param status 目标状态，沿用系统状态码 0/1
     * @param updateBy 操作人
     * @return 1 表示更新成功，0 表示未写入
     */
    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public int updateProviderConfigStatus(Long configId, String status, String updateBy)
    {
        ProviderConfig current = getById(configId);
        if (current == null)
        {
            throw new ServiceException("Provider config does not exist.");
        }
        if (StringUtils.equals(UserConstants.YES, current.getIsDefault())
                && !StringUtils.equals(UserConstants.NORMAL, status))
        {
            throw new ServiceException("Default provider config cannot be disabled directly. Please switch default first.");
        }
        ProviderConfig config = new ProviderConfig();
        config.setConfigId(configId);
        config.setStatus(status);
        config.setUpdateBy(updateBy);
        boolean updated = updateById(config);
        if (updated)
        {
            providerHandlerFactory.invalidateCache(current.getConfigType(), current.getProviderCode());
            notifyAfterCommit(current.getConfigType());
        }
        return updated ? 1 : 0;
    }

    /**
     * 将指定 Provider 配置切换为同类型默认配置。
     * <p>
     * 该操作会先校验目标配置是否满足默认运行要求，再清除同类型其他默认项，最后把目标配置设为启用且默认。
     * 成功提交后发布运行时快照，让业务侧下一次调用使用新的默认 Provider。
     *
     * @param configId 目标 Provider 配置主键
     * @param updateBy 操作人
     * @return 1 表示切换成功，0 表示未写入
     */
    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public int setDefaultProviderConfig(Long configId, String updateBy)
    {
        ProviderConfig current = getById(configId);
        if (current == null)
        {
            throw new ServiceException("Provider config does not exist.");
        }
        validateDefaultSwitch(current);
        clearDefaultFlag(current.getConfigType());
        ProviderConfig update = new ProviderConfig();
        update.setConfigId(configId);
        update.setIsDefault(UserConstants.YES);
        update.setStatus(UserConstants.NORMAL);
        update.setUpdateBy(updateBy);
        boolean updated = updateById(update);
        if (updated)
        {
            providerHandlerFactory.invalidateCache(current.getConfigType(), current.getProviderCode());
            notifyAfterCommit(current.getConfigType());
        }
        return updated ? 1 : 0;
    }

    /**
     * 测试 Provider 配置是否能连通第三方服务。
     * <p>
     * 测试时会复用更新逻辑的字段合并和密钥继承规则，但不写入数据库、不发布快照。
     *
     * @param config 待测试的配置；包含 configId 时会与当前数据库配置合并
     * @return true 表示连接校验通过
     */
    @Override
    public boolean testProviderConnection(ProviderConfig config)
    {
        ProviderConfig current = config.getConfigId() == null ? null : getById(config.getConfigId());
        ProviderConfig merged = current == null ? config : mergeForUpdate(config, current);
        normalize(merged);
        validate(merged, current);
        applyPlainSecrets(merged, current);
        return genericConfigDrivenHandler.validateConnection(toDto(merged));
    }

    /**
     * 批量删除 Provider 配置。
     * <p>
     * 删除默认配置后会尝试为同类型选择下一个启用配置作为默认项，避免运行时配置断档。
     * 所有受影响的配置类型会在事务提交后统一发布快照并通知缓存失效。
     *
     * @param configIds 待删除的 Provider 配置主键数组
     */
    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public void deleteProviderConfigByIds(Long[] configIds)
    {
        Set<String> changedTypes = new LinkedHashSet<>();
        for (Long configId : configIds)
        {
            ProviderConfig current = getById(configId);
            if (current == null)
            {
                continue;
            }
            removeById(configId);
            changedTypes.add(current.getConfigType());
            providerHandlerFactory.invalidateCache(current.getConfigType(), current.getProviderCode());
            if (StringUtils.equals(UserConstants.YES, current.getIsDefault()))
            {
                ensureDefaultExists(current.getConfigType());
            }
        }
        for (String configType : changedTypes)
        {
            notifyAfterCommit(configType);
        }
    }

    /**
     * 构建后台列表查询条件。
     *
     * @param query 查询条件
     * @return MyBatis-Plus 查询包装器
     */
    private LambdaQueryWrapper<ProviderConfig> buildListWrapper(ProviderConfig query)
    {
        LambdaQueryWrapper<ProviderConfig> wrapper = new LambdaQueryWrapper<>();
        if (query == null)
        {
            wrapper.orderByDesc(ProviderConfig::getIsDefault);
            wrapper.orderByDesc(ProviderConfig::getCreateTime);
            return wrapper;
        }
        wrapper.eq(StringUtils.isNotBlank(query.getConfigType()),
                ProviderConfig::getConfigType, normalizeCode(query.getConfigType()));
        wrapper.eq(StringUtils.isNotBlank(query.getProviderCode()),
                ProviderConfig::getProviderCode, normalizeCode(query.getProviderCode()));
        wrapper.like(StringUtils.isNotBlank(query.getProviderName()),
                ProviderConfig::getProviderName, query.getProviderName());
        wrapper.eq(StringUtils.isNotBlank(query.getStatus()),
                ProviderConfig::getStatus, query.getStatus());
        wrapper.orderByDesc(ProviderConfig::getIsDefault);
        wrapper.orderByDesc(ProviderConfig::getCreateTime);
        return wrapper;
    }

    /**
     * 校验 Provider 配置是否满足当前 Handler 的运行要求。
     * <p>
     * 更新场景会用当前配置补齐未传入的密钥，确保只改名称、备注等字段时不会因为密钥为空误判失败。
     *
     * @param config 已规范化并合并后的配置
     * @param current 数据库当前配置；新增时为 null
     */
    private void validate(ProviderConfig config, ProviderConfig current)
    {
        if (StringUtils.isNotBlank(config.getExtParamsJson()) && !JSON.isValid(config.getExtParamsJson()))
        {
            throw new ServiceException("Invalid extParamsJson format.");
        }

        String configType = config.getConfigType();
        String providerCode = config.getProviderCode();
        if (!providerHandlerFactory.isSupported(configType, providerCode))
        {
            throw new ServiceException(String.format("不支持的 Provider 配置类型: %s/%s", configType, providerCode));
        }

        validateFaceplusEndpoint(config);

        IProviderHandler<?, ?> handler = providerHandlerFactory.getHandler(configType, providerCode);
        ProviderConfigDTO dto = toDto(config);
        if (current != null)
        {
            if (StringUtils.isBlank(config.getAccessKey()))
            {
                dto.setAccessKey(current.getAccessKey());
            }
            if (StringUtils.isBlank(config.getSecretKey()))
            {
                dto.setSecretKey(current.getSecretKey());
            }
        }
        if (!handler.validateConfig(dto))
        {
            throw new ServiceException("Provider 配置校验失败");
        }
    }

    /**
     * 校验配置是否允许被切换为默认 Provider。
     * <p>
     * 存储类配置会额外检查 endpoint、bucket、basePath 和密钥字段，避免默认切换后文件服务不可用。
     *
     * @param config 目标 Provider 配置
     */
    private void validateDefaultSwitch(ProviderConfig config)
    {
        if (!providerHandlerFactory.isSupported(config.getConfigType(), config.getProviderCode()))
        {
            throw new ServiceException(
                    String.format("不支持的 Provider 配置类型: %s/%s", config.getConfigType(), config.getProviderCode()));
        }
        if (!ProviderConfigConstants.CONFIG_TYPE_STORAGE.equals(config.getConfigType()))
        {
            return;
        }

        String providerCode = config.getProviderCode();
        if (ProviderConfigConstants.PROVIDER_LOCAL.equals(providerCode))
        {
            require(config.getBasePath(), "Local storage basePath must not be blank.");
            return;
        }
        if (ProviderConfigConstants.PROVIDER_ALIYUN_OSS.equals(providerCode))
        {
            require(config.getEndpoint(), "切换 OSS 默认配置前必须填写 endpoint");
            require(config.getBucketName(), "切换 OSS 默认配置前必须填写 bucket");
            requireSecretValue(config.getAccessKey(), null, "切换 OSS 默认配置前必须填写 AccessKey");
            requireSecretValue(config.getSecretKey(), null, "切换 OSS 默认配置前必须填写 SecretKey");
            return;
        }
        if (ProviderConfigConstants.PROVIDER_TENCENT_COS.equals(providerCode))
        {
            require(config.getRegion(), "切换 COS 默认配置前必须填写 region");
            require(config.getBucketName(), "切换 COS 默认配置前必须填写 bucket");
            requireSecretValue(config.getAccessKey(), null, "切换 COS 默认配置前必须填写 AccessKey");
            requireSecretValue(config.getSecretKey(), null, "切换 COS 默认配置前必须填写 SecretKey");
            return;
        }
        if (ProviderConfigConstants.PROVIDER_MINIO.equals(providerCode))
        {
            require(config.getEndpoint(), "MinIO endpoint 不能为空");
            require(config.getBucketName(), "MinIO bucket 不能为空");
            requireSecretValue(config.getAccessKey(), null, "MinIO AccessKey 不能为空");
            requireSecretValue(config.getSecretKey(), null, "MinIO SecretKey 不能为空");
        }
    }

    /**
     * 处理明文密钥字段。
     * <p>
     * 本地存储不需要密钥，统一清空；更新第三方配置时如果请求未带密钥，则继承数据库当前值。
     *
     * @param config 待写入或待测试的配置
     * @param current 数据库当前配置；新增时为 null
     */
    private void applyPlainSecrets(ProviderConfig config, ProviderConfig current)
    {
        if (ProviderConfigConstants.PROVIDER_LOCAL.equals(config.getProviderCode()))
        {
            config.setAccessKey(null);
            config.setSecretKey(null);
            return;
        }
        if (StringUtils.isBlank(config.getAccessKey()) && current != null)
        {
            config.setAccessKey(current.getAccessKey());
        }
        if (StringUtils.isBlank(config.getSecretKey()) && current != null)
        {
            config.setSecretKey(current.getSecretKey());
        }
    }

    /**
     * 更新时先将请求与数据库当前配置合并，避免只改名称或备注时丢失运行时字段。
     */
    private ProviderConfig mergeForUpdate(ProviderConfig request, ProviderConfig current)
    {
        ProviderConfig merged = new ProviderConfig();
        BeanUtils.copyProperties(current, merged);
        merged.setConfigId(current.getConfigId());
        merged.setConfigType(valueOrCurrent(request.getConfigType(), current.getConfigType()));
        merged.setProviderCode(valueOrCurrent(request.getProviderCode(), current.getProviderCode()));
        merged.setProviderName(valueOrCurrent(request.getProviderName(), current.getProviderName()));
        merged.setEndpoint(valueOrCurrent(request.getEndpoint(), current.getEndpoint()));
        merged.setRegion(valueOrCurrent(request.getRegion(), current.getRegion()));
        merged.setBucketName(valueOrCurrent(request.getBucketName(), current.getBucketName()));
        merged.setAccessKey(secretOrCurrent(request.getAccessKey(), current.getAccessKey()));
        merged.setSecretKey(secretOrCurrent(request.getSecretKey(), current.getSecretKey()));
        merged.setDomain(valueOrCurrent(request.getDomain(), current.getDomain()));
        merged.setBasePath(valueOrCurrent(request.getBasePath(), current.getBasePath()));
        merged.setExtParamsJson(valueOrCurrent(request.getExtParamsJson(), current.getExtParamsJson()));
        merged.setModelType(valueOrCurrent(request.getModelType(), current.getModelType()));
        merged.setIsDefault(valueOrCurrent(request.getIsDefault(), current.getIsDefault()));
        merged.setStatus(valueOrCurrent(request.getStatus(), current.getStatus()));
        merged.setRemark(valueOrCurrent(request.getRemark(), current.getRemark()));
        merged.setUpdateBy(valueOrCurrent(request.getUpdateBy(), current.getUpdateBy()));
        return merged;
    }

    /**
     * 只有运行时敏感字段发生变化时，才重新校验第三方连接。
     */
    private boolean requiresConnectionValidation(ProviderConfig merged, ProviderConfig current)
    {
        if (merged == null)
        {
            return false;
        }
        if (current == null)
        {
            return true;
        }
        return fieldChanged(merged.getConfigType(), current.getConfigType())
                || fieldChanged(merged.getProviderCode(), current.getProviderCode())
                || fieldChanged(merged.getEndpoint(), current.getEndpoint())
                || fieldChanged(merged.getRegion(), current.getRegion())
                || fieldChanged(merged.getBucketName(), current.getBucketName())
                || fieldChanged(merged.getAccessKey(), current.getAccessKey())
                || fieldChanged(merged.getSecretKey(), current.getSecretKey())
                || fieldChanged(merged.getDomain(), current.getDomain())
                || fieldChanged(merged.getBasePath(), current.getBasePath())
                || fieldChanged(merged.getExtParamsJson(), current.getExtParamsJson())
                || fieldChanged(merged.getModelType(), current.getModelType());
    }

    /**
     * 查询指定配置类型的默认启用实体。
     *
     * @param configType 配置类型
     * @return 默认启用配置实体
     * @throws ServiceException 没有默认启用配置时抛出
     */
    private ProviderConfig getDefaultEntity(String configType)
    {
        LambdaQueryWrapper<ProviderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderConfig::getConfigType, normalizeCode(configType));
        wrapper.eq(ProviderConfig::getStatus, UserConstants.NORMAL);
        wrapper.eq(ProviderConfig::getIsDefault, UserConstants.YES);
        wrapper.last("limit 1");
        ProviderConfig config = getOne(wrapper, false);
        if (config == null)
        {
            throw new ServiceException("未找到可用的默认 Provider 配置: " + configType);
        }
        return config;
    }

    /**
     * 复制配置实体并清除敏感明文字段。
     *
     * @param source 数据库配置实体
     * @return 面向管理端展示的脱敏配置
     */
    private ProviderConfig sanitize(ProviderConfig source)
    {
        if (source == null)
        {
            return null;
        }
        ProviderConfig target = new ProviderConfig();
        BeanUtils.copyProperties(source, target);
        target.setAccessKeyMasked(maskSecret(source.getAccessKey()));
        target.setSecretKeyMasked(maskSecret(source.getSecretKey()));
        target.setAccessKey(null);
        target.setSecretKey(null);
        return target;
    }

    /**
     * 将数据库实体转换为运行时 DTO。
     *
     * @param source 数据库配置实体
     * @return Provider 运行时配置 DTO
     */
    private ProviderConfigDTO toDto(ProviderConfig source)
    {
        if (source == null)
        {
            return null;
        }
        ProviderConfigDTO target = new ProviderConfigDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 生成密钥掩码，仅保留末尾四位用于人工识别。
     *
     * @param value 原始密钥
     * @return 掩码字符串
     */
    private String maskSecret(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return "";
        }
        if (value.length() <= 4)
        {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }

    /**
     * 清除同配置类型下所有默认标记。
     *
     * @param configType 配置类型
     */
    private void clearDefaultFlag(String configType)
    {
        LambdaUpdateWrapper<ProviderConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProviderConfig::getConfigType, normalizeCode(configType));
        wrapper.set(ProviderConfig::getIsDefault, "N");
        update(wrapper);
    }

    /**
     * 确保指定配置类型仍然有默认启用配置。
     * <p>
     * 删除默认项后会选择最近更新的一条启用配置补位，降低运行时找不到默认配置的风险。
     *
     * @param configType 配置类型
     */
    private void ensureDefaultExists(String configType)
    {
        LambdaQueryWrapper<ProviderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderConfig::getConfigType, normalizeCode(configType));
        wrapper.eq(ProviderConfig::getStatus, UserConstants.NORMAL);
        wrapper.orderByDesc(ProviderConfig::getUpdateTime);
        wrapper.last("limit 1");
        ProviderConfig next = getOne(wrapper, false);
        if (next != null)
        {
            ProviderConfig update = new ProviderConfig();
            update.setConfigId(next.getConfigId());
            update.setIsDefault(UserConstants.YES);
            updateById(update);
        }
    }

    /**
     * 注册事务提交后的快照发布动作。
     * <p>
     * 如果当前没有事务同步上下文，则立即发布；否则等数据库提交成功后再通知运行时，避免 Redis 快照早于 DB 状态。
     *
     * @param configType 受影响的配置类型
     */
    private void notifyAfterCommit(String configType)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            publishRuntimeSnapshot(configType);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                publishRuntimeSnapshot(configType);
            }
        });
    }

    /**
     * 发布配置类型运行时快照并广播缓存失效事件。
     *
     * @param configType 受影响的配置类型
     */
    private void publishRuntimeSnapshot(String configType)
    {
        runtimeSnapshotPublisher.publishConfigType(configType);
        cacheInvalidator.notify(configType);
    }

    /**
     * 规范化配置字段，统一编码大小写、去除首尾空白和地址尾部斜杠。
     *
     * @param config 待规范化的 Provider 配置
     */
    private void normalize(ProviderConfig config)
    {
        config.setConfigType(normalizeCode(config.getConfigType()));
        config.setProviderCode(normalizeCode(config.getProviderCode()));
        config.setProviderName(trim(config.getProviderName()));
        config.setEndpoint(trimSlash(config.getEndpoint()));
        normalizeFaceplusEndpoint(config);
        config.setRegion(trim(config.getRegion()));
        config.setBucketName(trim(config.getBucketName()));
        config.setDomain(trimSlash(config.getDomain()));
        config.setBasePath(trim(config.getBasePath()));
        config.setExtParamsJson(trim(config.getExtParamsJson()));
    }

    /**
     * 规范化配置类型和 Provider 编码。
     *
     * @param value 原始编码
     * @return 小写后的编码；空值返回 null
     */
    private String normalizeCode(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim().toLowerCase();
    }

    /**
     * 清理普通文本字段。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本；空值返回 null
     */
    private String trim(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    /**
     * 为 Face++ 国内版配置补齐默认接口地址。
     *
     * @param config Provider 配置
     */
    private void normalizeFaceplusEndpoint(ProviderConfig config)
    {
        if (isFaceplus(config) && StringUtils.isBlank(config.getEndpoint()))
        {
            config.setEndpoint(FACEPLUS_CN_ENDPOINT);
        }
    }

    /**
     * 校验 Face++ 国内版接口地址，避免误配到海外 API 域名。
     *
     * @param config Provider 配置
     */
    private void validateFaceplusEndpoint(ProviderConfig config)
    {
        if (!isFaceplus(config))
        {
            return;
        }
        if (!FACEPLUS_CN_ENDPOINT.equalsIgnoreCase(config.getEndpoint()))
        {
            throw new ServiceException("当前使用国内版 Face++，API 端点必须为 " + FACEPLUS_CN_ENDPOINT);
        }
    }

    /**
     * 判断配置是否为 Face++ 人脸能力。
     *
     * @param config Provider 配置
     * @return true 表示 Face++ 人脸配置
     */
    private boolean isFaceplus(ProviderConfig config)
    {
        return config != null
                && CONFIG_TYPE_FACE.equals(config.getConfigType())
                && PROVIDER_FACEPLUS.equals(config.getProviderCode());
    }

    /**
     * 去除地址类字段末尾斜杠，避免拼接 URL 时产生双斜杠。
     *
     * @param value 原始地址
     * @return 去除尾部斜杠后的地址；空值返回 null
     */
    private String trimSlash(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        String normalized = value.trim();
        while (normalized.endsWith("/"))
        {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 校验普通必填文本。
     *
     * @param value 待校验文本
     * @param message 为空时抛出的业务错误信息
     */
    private void require(String value, String message)
    {
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException(message);
        }
    }

    /**
     * 校验密钥字段是否在请求或当前配置中存在。
     *
     * @param rawValue 请求中的密钥值
     * @param currentValue 数据库当前密钥值
     * @param message 两者都为空时抛出的业务错误信息
     */
    private void requireSecretValue(String rawValue, String currentValue, String message)
    {
        if (StringUtils.isBlank(rawValue) && StringUtils.isBlank(currentValue))
        {
            throw new ServiceException(message);
        }
    }

    /**
     * 判断两个运行时字段在清理空白后是否发生变化。
     *
     * @param left 新值
     * @param right 当前值
     * @return true 表示字段变化
     */
    private boolean fieldChanged(String left, String right)
    {
        return !StringUtils.equals(trim(left), trim(right));
    }

    /**
     * 局部更新时选择请求值或当前值。
     *
     * @param requestValue 请求传入值
     * @param currentValue 数据库当前值
     * @return 请求值非 null 时使用请求值，否则使用当前值
     */
    private String valueOrCurrent(String requestValue, String currentValue)
    {
        return requestValue != null ? requestValue : currentValue;
    }

    /**
     * 局部更新时选择密钥值。
     *
     * @param requestValue 请求传入密钥
     * @param currentValue 数据库当前密钥
     * @return 请求密钥非空时使用请求值，否则沿用当前密钥
     */
    private String secretOrCurrent(String requestValue, String currentValue)
    {
        return StringUtils.isBlank(requestValue) ? currentValue : requestValue;
    }
}
