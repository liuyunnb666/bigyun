package com.bigyun.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.provider.db.domain.ProviderModelCatalogEntity;
import com.bigyun.provider.db.mapper.ProviderModelCatalogMapper;
import com.bigyun.provider.domain.ProviderModelCatalog;
import com.bigyun.provider.service.IProviderModelCatalogService;
import com.github.pagehelper.Page;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Provider 模型目录服务实现。
 * <p>
 * 模型目录用于维护各 Provider 支持的模型编码、展示名称、部署方式和能力标签，供能力配置和推荐页复用。
 */
@Service
public class ProviderModelCatalogServiceImpl implements IProviderModelCatalogService
{
    private final ProviderModelCatalogMapper modelCatalogMapper;

    public ProviderModelCatalogServiceImpl(ProviderModelCatalogMapper modelCatalogMapper)
    {
        this.modelCatalogMapper = modelCatalogMapper;
    }

    /**
     * 按后台筛选条件查询模型目录列表。
     *
     * @param query 查询条件，支持模型编码、名称、配置类型、Provider、部署方式、标签和启用状态
     * @return 模型目录列表
     */
    @Override
    public List<ProviderModelCatalog> selectModelCatalogList(ProviderModelCatalog query)
    {
        QueryWrapper<ProviderModelCatalogEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*");
        if (query != null)
        {
            wrapper.like(StringUtils.isNotBlank(query.getModelCode()), "model_code", query.getModelCode());
            wrapper.like(StringUtils.isNotBlank(query.getModelName()), "model_name", query.getModelName());
            wrapper.eq(StringUtils.isNotBlank(query.getConfigType()), "config_type", query.getConfigType());
            wrapper.eq(StringUtils.isNotBlank(query.getProviderCode()), "provider_code", query.getProviderCode());
            wrapper.eq(StringUtils.isNotBlank(query.getDeploymentMode()), "deployment_mode", query.getDeploymentMode());
            wrapper.like(StringUtils.isNotBlank(query.getCapabilityTags()), "capability_tags", query.getCapabilityTags());
            wrapper.eq(StringUtils.isNotBlank(query.getIsEnabled()), "is_enabled", query.getIsEnabled());
        }
        wrapper.orderByAsc("config_type")
                .orderByAsc("provider_code")
                .orderByAsc("sort_order")
                .orderByDesc("update_time");
        return toManageList(modelCatalogMapper.selectList(wrapper));
    }

    /**
     * 根据模型主键查询模型目录详情。
     *
     * @param modelId 模型主键
     * @return 模型目录实体；主键为空或不存在时返回 null
     */
    @Override
    public ProviderModelCatalog selectModelCatalogById(Long modelId)
    {
        return modelId == null ? null : toManage(modelCatalogMapper.selectById(modelId));
    }

    /**
     * 新增模型目录。
     * <p>
     * 未传启用状态时默认启用，未传排序时默认 0。
     *
     * @param modelCatalog 待新增的模型目录
     * @return 受影响行数
     */
    @Override
    public int insertModelCatalog(ProviderModelCatalog modelCatalog)
    {
        validateModelCatalog(modelCatalog);
        if (StringUtils.isBlank(modelCatalog.getIsEnabled()))
        {
            modelCatalog.setIsEnabled("1");
        }
        if (modelCatalog.getSortOrder() == null)
        {
            modelCatalog.setSortOrder(0);
        }
        return modelCatalogMapper.insert(toEntity(modelCatalog));
    }

    /**
     * 更新模型目录。
     *
     * @param modelCatalog 待更新的模型目录
     * @return 受影响行数
     */
    @Override
    public int updateModelCatalog(ProviderModelCatalog modelCatalog)
    {
        if (modelCatalog == null || modelCatalog.getModelId() == null)
        {
            throw new ServiceException("Model ID must not be null.");
        }
        validateModelCatalog(modelCatalog);
        return modelCatalogMapper.updateById(toEntity(modelCatalog));
    }

    /**
     * 启用或停用模型目录。
     *
     * @param modelId 模型主键
     * @param isEnabled 启用状态，1=启用，0=停用
     * @param updateBy 操作人
     * @return 受影响行数
     */
    @Override
    public int updateModelCatalogStatus(Long modelId, String isEnabled, String updateBy)
    {
        if (modelId == null)
        {
            throw new ServiceException("Model ID must not be null.");
        }
        if (!"1".equals(isEnabled) && !"0".equals(isEnabled))
        {
            throw new ServiceException("Model enabled status must be 1 or 0.");
        }
        ProviderModelCatalogEntity update = new ProviderModelCatalogEntity();
        update.setModelId(modelId);
        update.setIsEnabled(isEnabled);
        update.setUpdateBy(updateBy);
        return modelCatalogMapper.updateById(update);
    }

    /**
     * 批量删除模型目录。
     *
     * @param modelIds 模型主键数组
     */
    @Override
    public void deleteModelCatalogByIds(Long[] modelIds)
    {
        if (modelIds == null || modelIds.length == 0)
        {
            return;
        }
        for (Long modelId : modelIds)
        {
            modelCatalogMapper.deleteById(modelId);
        }
    }

    private List<ProviderModelCatalog> toManageList(List<ProviderModelCatalogEntity> entities)
    {
        List<ProviderModelCatalog> result = createResultList(entities);
        if (entities == null)
        {
            return result;
        }
        for (ProviderModelCatalogEntity entity : entities)
        {
            result.add(toManage(entity));
        }
        return result;
    }

    /**
     * Service 层需要把 DB Entity 转成管理端对象，同时保留 PageHelper 分页总数。
     */
    private List<ProviderModelCatalog> createResultList(List<ProviderModelCatalogEntity> entities)
    {
        if (entities instanceof Page)
        {
            Page<?> sourcePage = (Page<?>) entities;
            Page<ProviderModelCatalog> result = new Page<>(sourcePage.getPageNum(), sourcePage.getPageSize());
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

    private ProviderModelCatalog toManage(ProviderModelCatalogEntity entity)
    {
        if (entity == null)
        {
            return null;
        }
        ProviderModelCatalog modelCatalog = new ProviderModelCatalog();
        BeanUtils.copyProperties(entity, modelCatalog);
        return modelCatalog;
    }

    private ProviderModelCatalogEntity toEntity(ProviderModelCatalog modelCatalog)
    {
        if (modelCatalog == null)
        {
            return null;
        }
        ProviderModelCatalogEntity entity = new ProviderModelCatalogEntity();
        BeanUtils.copyProperties(modelCatalog, entity);
        return entity;
    }

    /**
     * 校验模型目录必填字段和启用状态。
     *
     * @param modelCatalog 待校验的模型目录
     */
    private void validateModelCatalog(ProviderModelCatalog modelCatalog)
    {
        if (modelCatalog == null)
        {
            throw new ServiceException("Model catalog must not be null.");
        }
        if (StringUtils.isBlank(modelCatalog.getModelCode()))
        {
            throw new ServiceException("Model code must not be blank.");
        }
        if (StringUtils.isBlank(modelCatalog.getModelName()))
        {
            throw new ServiceException("Model name must not be blank.");
        }
        if (StringUtils.isBlank(modelCatalog.getConfigType()))
        {
            throw new ServiceException("Config type must not be blank.");
        }
        if (StringUtils.isBlank(modelCatalog.getProviderCode()))
        {
            throw new ServiceException("Provider code must not be blank.");
        }
        if (StringUtils.isBlank(modelCatalog.getIsEnabled()))
        {
            modelCatalog.setIsEnabled("1");
        }
        if (!"1".equals(modelCatalog.getIsEnabled()) && !"0".equals(modelCatalog.getIsEnabled()))
        {
            throw new ServiceException("Model enabled status must be 1 or 0.");
        }
    }
}
