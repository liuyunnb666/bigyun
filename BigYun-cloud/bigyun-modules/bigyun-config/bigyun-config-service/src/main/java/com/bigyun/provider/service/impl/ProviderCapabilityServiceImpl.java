package com.bigyun.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderCapabilityDTO;
import com.bigyun.provider.cache.CacheInvalidator;
import com.bigyun.provider.db.domain.ProviderCapabilityEntity;
import com.bigyun.provider.db.mapper.ProviderCapabilityManageMapper;
import com.bigyun.provider.db.snapshot.ProviderRuntimeSnapshotPublisher;
import com.bigyun.provider.domain.ProviderCapability;
import com.bigyun.provider.service.IProviderCapabilityService;
import com.github.pagehelper.Page;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Provider 能力配置服务实现。
 * <p>
 * 负责按 capabilityCode 维护默认能力、模型路由候选、启停状态，并在能力变化后刷新运行时快照。
 */
@Service
public class ProviderCapabilityServiceImpl implements IProviderCapabilityService
{
    @Autowired
    private ProviderCapabilityManageMapper providerCapabilityMapper;

    @Autowired
    private CacheInvalidator cacheInvalidator;

    @Autowired
    private ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher;

    /**
     * 查询指定能力编码当前可用的默认能力。
     * <p>
     * 排序优先级为默认标记、优先级和更新时间，供运行时能力路由选择首选 Provider 或模型。
     *
     * @param capabilityCode 能力编码，例如 ai-guidance-llm
     * @return 默认能力 DTO；没有启用能力时返回 null
     */
    @Override
    public ProviderCapabilityDTO selectDefaultCapability(String capabilityCode)
    {
        QueryWrapper<ProviderCapabilityEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*")
                .eq("capability_code", capabilityCode)
                .eq("status", "0")
                .orderByDesc("is_default")
                .orderByAsc("priority")
                .orderByDesc("update_time")
                .last("LIMIT 1");
        ProviderCapabilityEntity capability = providerCapabilityMapper.selectOne(wrapper);
        if (capability == null)
        {
            return null;
        }
        ProviderCapabilityDTO dto = new ProviderCapabilityDTO();
        BeanUtils.copyProperties(capability, dto);
        return dto;
    }

    /**
     * 按管理端筛选条件查询能力列表。
     *
     * @param query 查询条件，支持能力编码、场景、层级、配置类型、Provider 和默认状态
     * @return 能力配置列表
     */
    @Override
    public List<ProviderCapability> selectCapabilityList(ProviderCapability query)
    {
        QueryWrapper<ProviderCapabilityEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*");
        if (query != null)
        {
            wrapper.like(StringUtils.isNotBlank(query.getCapabilityCode()), "capability_code",
                    query.getCapabilityCode());
            wrapper.like(StringUtils.isNotBlank(query.getCapabilityName()), "capability_name",
                    query.getCapabilityName());
            wrapper.eq(StringUtils.isNotBlank(query.getBusinessScene()), "business_scene",
                    query.getBusinessScene());
            wrapper.eq(StringUtils.isNotBlank(query.getCapabilityLayer()), "capability_layer",
                    query.getCapabilityLayer());
            wrapper.eq(StringUtils.isNotBlank(query.getConfigType()), "config_type",
                    query.getConfigType());
            wrapper.eq(StringUtils.isNotBlank(query.getProviderCode()), "provider_code",
                    query.getProviderCode());
            wrapper.eq(StringUtils.isNotBlank(query.getStatus()), "status", query.getStatus());
            wrapper.eq(StringUtils.isNotBlank(query.getIsDefault()), "is_default",
                    query.getIsDefault());
        }
        wrapper.orderByDesc("is_default")
                .orderByAsc("capability_code")
                .orderByAsc("priority")
                .orderByDesc("update_time");
        List<ProviderCapabilityEntity> entities = providerCapabilityMapper.selectList(wrapper);
        List<ProviderCapability> result = createResultList(entities);
        if (entities != null)
        {
            for (ProviderCapabilityEntity entity : entities)
            {
                result.add(toManage(entity));
            }
        }
        return result;
    }

    /**
     * Entity 转管理端对象时保留 PageHelper 的分页总数，避免前端 total 只显示当前页数量。
     */
    private List<ProviderCapability> createResultList(List<ProviderCapabilityEntity> entities)
    {
        if (entities instanceof Page)
        {
            Page<?> sourcePage = (Page<?>) entities;
            Page<ProviderCapability> result = new Page<>(sourcePage.getPageNum(), sourcePage.getPageSize());
            result.setTotal(sourcePage.getTotal());
            result.setPages(sourcePage.getPages());
            result.setReasonable(sourcePage.getReasonable());
            result.setPageSizeZero(sourcePage.getPageSizeZero());
            result.setStartRow(sourcePage.getStartRow());
            result.setEndRow(sourcePage.getEndRow());
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * 根据能力主键查询能力详情。
     *
     * @param capabilityId 能力主键
     * @return 能力实体；不存在时返回 null
     */
    @Override
    public ProviderCapability selectCapabilityById(Long capabilityId)
    {
        QueryWrapper<ProviderCapabilityEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*").eq("capability_id", capabilityId).last("LIMIT 1");
        return toManage(providerCapabilityMapper.selectOne(wrapper));
    }

    /**
     * 将指定能力设置为同 capabilityCode 下的默认能力。
     * <p>
     * 会先清除同能力编码的其他默认标记，再设置目标能力为默认，事务提交后刷新能力快照和配置缓存。
     *
     * @param capabilityId 目标能力主键
     * @param updateBy 操作人
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefaultCapability(Long capabilityId, String updateBy)
    {
        ProviderCapability current = selectCapabilityById(capabilityId);
        if (current == null)
        {
            throw new ServiceException("Provider capability does not exist.");
        }
        if (!UserConstants.NORMAL.equals(current.getStatus()))
        {
            throw new ServiceException("Disabled capability cannot be set as default.");
        }
        LambdaUpdateWrapper<ProviderCapabilityEntity> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(ProviderCapabilityEntity::getCapabilityCode, current.getCapabilityCode())
                .set(ProviderCapabilityEntity::getIsDefault, "0")
                .set(ProviderCapabilityEntity::getUpdateBy, updateBy);
        providerCapabilityMapper.update(null, clearWrapper);

        ProviderCapabilityEntity update = new ProviderCapabilityEntity();
        update.setCapabilityId(capabilityId);
        update.setIsDefault("1");
        update.setUpdateBy(updateBy);
        int rows = providerCapabilityMapper.updateById(update);
        if (rows > 0)
        {
            notifyAfterCommit(current.getCapabilityCode(), current.getConfigType());
        }
        return rows;
    }

    /**
     * 新增 Provider 能力配置。
     * <p>
     * 会补齐默认状态和优先级；如果新增项被标记为默认，会清除同 capabilityCode 下其他默认项。
     *
     * @param capability 待新增的能力配置
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCapability(ProviderCapability capability)
    {
        validateCapability(capability, false);
        if (StringUtils.isBlank(capability.getStatus()))
        {
            capability.setStatus(UserConstants.NORMAL);
        }
        if (StringUtils.isBlank(capability.getIsDefault()))
        {
            capability.setIsDefault("0");
        }
        if (capability.getPriority() == null)
        {
            capability.setPriority(100);
        }
        clearDefaultIfNeeded(capability, null);
        int rows = providerCapabilityMapper.insert(toEntity(capability));
        if (rows > 0)
        {
            notifyAfterCommit(capability.getCapabilityCode(), capability.getConfigType());
        }
        return rows;
    }

    /**
     * 更新 Provider 能力配置。
     * <p>
     * 当 capabilityCode 或 configType 发生变化时，会同时刷新旧能力和新能力对应的运行时快照。
     *
     * @param capability 待更新的能力配置
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCapability(ProviderCapability capability)
    {
        validateCapability(capability, true);
        ProviderCapability current = selectCapabilityById(capability.getCapabilityId());
        if (current == null)
        {
            throw new ServiceException("Provider capability does not exist.");
        }
        clearDefaultIfNeeded(capability, capability.getCapabilityId());
        int rows = providerCapabilityMapper.updateById(toEntity(capability));
        if (rows > 0)
        {
            notifyAfterCommit(current.getCapabilityCode(), current.getConfigType());
            if (!current.getCapabilityCode().equals(capability.getCapabilityCode())
                    || !current.getConfigType().equals(capability.getConfigType()))
            {
                notifyAfterCommit(capability.getCapabilityCode(), capability.getConfigType());
            }
        }
        return rows;
    }

    /**
     * 启用或停用 Provider 能力。
     * <p>
     * 停用能力时会同时取消默认标记，避免运行时选中不可用能力。
     *
     * @param capabilityId 能力主键
     * @param status 目标状态，0=启用，1=停用
     * @param updateBy 操作人
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCapabilityStatus(Long capabilityId, String status, String updateBy)
    {
        ProviderCapability current = selectCapabilityById(capabilityId);
        if (current == null)
        {
            throw new ServiceException("Provider capability does not exist.");
        }
        if (!UserConstants.NORMAL.equals(status) && !UserConstants.EXCEPTION.equals(status))
        {
            throw new ServiceException("Provider capability status must be 0 or 1.");
        }
        ProviderCapabilityEntity update = new ProviderCapabilityEntity();
        update.setCapabilityId(capabilityId);
        update.setStatus(status);
        update.setUpdateBy(updateBy);
        if (UserConstants.EXCEPTION.equals(status))
        {
            update.setIsDefault("0");
        }
        int rows = providerCapabilityMapper.updateById(update);
        if (rows > 0)
        {
            notifyAfterCommit(current.getCapabilityCode(), current.getConfigType());
        }
        return rows;
    }

    /**
     * 批量删除 Provider 能力配置。
     * <p>
     * 每删除一条能力都会刷新对应 capabilityCode 的运行时快照。
     *
     * @param capabilityIds 待删除的能力主键数组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCapabilityByIds(Long[] capabilityIds)
    {
        if (capabilityIds == null || capabilityIds.length == 0)
        {
            return;
        }
        for (Long capabilityId : capabilityIds)
        {
            ProviderCapability current = selectCapabilityById(capabilityId);
            if (current == null)
            {
                continue;
            }
            providerCapabilityMapper.deleteById(capabilityId);
            notifyAfterCommit(current.getCapabilityCode(), current.getConfigType());
        }
    }

    /**
     * 校验能力配置的必填字段和状态枚举。
     *
     * @param capability 待校验的能力配置
     * @param requireId 是否要求 capabilityId 必填
     */
    private void validateCapability(ProviderCapability capability, boolean requireId)
    {
        if (capability == null)
        {
            throw new ServiceException("Provider capability must not be null.");
        }
        if (requireId && capability.getCapabilityId() == null)
        {
            throw new ServiceException("Provider capability ID must not be null.");
        }
        if (StringUtils.isBlank(capability.getCapabilityCode()))
        {
            throw new ServiceException("Capability code must not be blank.");
        }
        if (StringUtils.isBlank(capability.getCapabilityName()))
        {
            throw new ServiceException("Capability name must not be blank.");
        }
        if (StringUtils.isBlank(capability.getConfigType()))
        {
            throw new ServiceException("Config type must not be blank.");
        }
        if (StringUtils.isBlank(capability.getProviderCode()))
        {
            throw new ServiceException("Provider code must not be blank.");
        }
        if (StringUtils.isBlank(capability.getOperation()))
        {
            throw new ServiceException("Operation must not be blank.");
        }
        if (StringUtils.isNotBlank(capability.getStatus())
                && !UserConstants.NORMAL.equals(capability.getStatus())
                && !UserConstants.EXCEPTION.equals(capability.getStatus()))
        {
            throw new ServiceException("Provider capability status must be 0 or 1.");
        }
        if (StringUtils.isNotBlank(capability.getIsDefault())
                && !"0".equals(capability.getIsDefault()) && !"1".equals(capability.getIsDefault()))
        {
            throw new ServiceException("Provider capability default flag must be 1 or 0.");
        }
    }

    /**
     * 当目标能力被设置为默认时，清除同 capabilityCode 下其他默认能力。
     *
     * @param capability 待保存的能力配置
     * @param currentCapabilityId 当前能力主键；新增时为 null
     */
    private void clearDefaultIfNeeded(ProviderCapability capability, Long currentCapabilityId)
    {
        if (!"1".equals(capability.getIsDefault()))
        {
            return;
        }
        if (UserConstants.EXCEPTION.equals(capability.getStatus()))
        {
            throw new ServiceException("Disabled capability cannot be set as default.");
        }
        LambdaUpdateWrapper<ProviderCapabilityEntity> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(ProviderCapabilityEntity::getCapabilityCode, capability.getCapabilityCode())
                .ne(currentCapabilityId != null, ProviderCapabilityEntity::getCapabilityId, currentCapabilityId)
                .set(ProviderCapabilityEntity::getIsDefault, "0")
                .set(ProviderCapabilityEntity::getUpdateBy, capability.getUpdateBy());
        providerCapabilityMapper.update(null, clearWrapper);
    }

    private ProviderCapability toManage(ProviderCapabilityEntity entity)
    {
        if (entity == null)
        {
            return null;
        }
        ProviderCapability capability = new ProviderCapability();
        BeanUtils.copyProperties(entity, capability);
        return capability;
    }

    private ProviderCapabilityEntity toEntity(ProviderCapability capability)
    {
        if (capability == null)
        {
            return null;
        }
        ProviderCapabilityEntity entity = new ProviderCapabilityEntity();
        BeanUtils.copyProperties(capability, entity);
        return entity;
    }

    /**
     * 在事务提交后刷新能力快照。
     * <p>
     * 没有事务上下文时立即刷新；有事务时等待提交成功，避免运行时读到未提交状态。
     *
     * @param capabilityCode 能力编码
     * @param configType 配置类型
     */
    private void notifyAfterCommit(String capabilityCode, String configType)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            publishRuntimeSnapshot(capabilityCode, configType);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                publishRuntimeSnapshot(capabilityCode, configType);
            }
        });
    }

    /**
     * 发布指定能力的运行时快照并通知配置类型缓存失效。
     *
     * @param capabilityCode 能力编码
     * @param configType 配置类型
     */
    private void publishRuntimeSnapshot(String capabilityCode, String configType)
    {
        runtimeSnapshotPublisher.publishCapability(capabilityCode);
        cacheInvalidator.notify(configType);
    }
}
