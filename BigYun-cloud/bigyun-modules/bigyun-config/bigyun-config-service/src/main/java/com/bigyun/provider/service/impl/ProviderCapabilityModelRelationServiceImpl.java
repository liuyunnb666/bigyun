package com.bigyun.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.CapabilityModelCompareVO;
import com.bigyun.config.domain.CapabilityModelRelationDTO;
import com.bigyun.config.domain.CapabilityModelSwitchReq;
import com.bigyun.provider.db.domain.ProviderCapabilityModelRelationEntity;
import com.bigyun.provider.db.domain.ProviderModelCatalogEntity;
import com.bigyun.provider.db.mapper.ProviderCapabilityModelRelationMapper;
import com.bigyun.provider.db.mapper.ProviderModelCatalogMapper;
import com.bigyun.provider.domain.ProviderCapability;
import com.bigyun.provider.service.IProviderCapabilityModelRelationService;
import com.bigyun.provider.service.IProviderCapabilityService;
import com.github.pagehelper.Page;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provider 能力模型关系服务实现。
 * <p>
 * 用于维护同类模型、主备模型、可替换模型和互斥模型之间的关系，支撑模型切换和配置评估页面。
 */
@Service
public class ProviderCapabilityModelRelationServiceImpl implements IProviderCapabilityModelRelationService
{
    @Autowired
    private ProviderCapabilityModelRelationMapper relationMapper;

    @Autowired
    private ProviderModelCatalogMapper modelCatalogMapper;

    @Autowired
    private IProviderCapabilityService providerCapabilityService;

    /**
     * 查询能力模型关系列表。
     * <p>
     * 查询结果会补齐能力名称、业务场景、目标能力和模型名称，便于前端直接展示。
     *
     * @param query 查询条件
     * @return 能力模型关系 DTO 列表
     */
    @Override
    public List<CapabilityModelRelationDTO> selectRelationList(CapabilityModelRelationDTO query)
    {
        QueryWrapper<ProviderCapabilityModelRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*");
        if (query != null)
        {
            wrapper.eq(query.getCapabilityId() != null, "capability_id", query.getCapabilityId())
                    .like(StringUtils.isNotBlank(query.getCapabilityCode()), "capability_code", query.getCapabilityCode())
                    .like(StringUtils.isNotBlank(query.getCapabilityName()), "capability_name", query.getCapabilityName())
                    .eq(StringUtils.isNotBlank(query.getBusinessScene()), "business_scene", query.getBusinessScene())
                    .eq(StringUtils.isNotBlank(query.getCapabilityLayer()), "capability_layer", query.getCapabilityLayer())
                    .eq(StringUtils.isNotBlank(query.getConfigType()), "config_type", query.getConfigType())
                    .eq(StringUtils.isNotBlank(query.getProviderCode()), "provider_code", query.getProviderCode())
                    .eq(StringUtils.isNotBlank(query.getModelCode()), "model_code", query.getModelCode())
                    .eq(StringUtils.isNotBlank(query.getRelationType()), "relation_type", query.getRelationType())
                    .eq(StringUtils.isNotBlank(query.getSameBusinessFlag()), "same_business_flag", query.getSameBusinessFlag())
                    .eq(StringUtils.isNotBlank(query.getStatus()), "status", query.getStatus());
        }
        wrapper.orderByDesc("sort_order")
                .orderByDesc("update_time")
                .orderByDesc("relation_id");
        List<ProviderCapabilityModelRelationEntity> relations = relationMapper.selectList(wrapper);
        if (relations == null || relations.isEmpty())
        {
            return Collections.emptyList();
        }
        Map<Long, ProviderCapability> capabilityMap = loadCapabilityMap();
        Map<String, String> modelNameMap = loadModelNameMap();
        Map<Long, ProviderCapability> targetCapabilityCache = new HashMap<>();
        List<CapabilityModelRelationDTO> result = createResultList(relations);
        for (ProviderCapabilityModelRelationEntity relation : relations)
        {
            result.add(toRelationDTO(relation, capabilityMap.get(relation.getCapabilityId()), capabilityMap,
                    targetCapabilityCache, modelNameMap));
        }
        return result;
    }

    /**
     * 根据关系主键查询能力模型关系详情。
     *
     * @param relationId 关系主键
     * @return 关系 DTO；不存在时返回 null
     */
    @Override
    public CapabilityModelRelationDTO selectRelationById(Long relationId)
    {
        if (relationId == null)
        {
            return null;
        }
        ProviderCapabilityModelRelationEntity relation = relationMapper.selectById(relationId);
        if (relation == null)
        {
            return null;
        }
        Map<Long, ProviderCapability> capabilityMap = loadCapabilityMap();
        return toRelationDTO(relation, capabilityMap.get(relation.getCapabilityId()), capabilityMap,
                new HashMap<>(), loadModelNameMap());
    }

    /**
     * 新增能力模型关系。
     * <p>
     * 会根据源能力和目标能力自动比较场景、层级、入参和出参结构，并生成默认关系类型和关系说明。
     *
     * @param relation 关系请求 DTO
     * @param updateBy 操作人
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRelation(CapabilityModelRelationDTO relation, String updateBy)
    {
        ProviderCapability source = requireCapability(relation == null ? null : relation.getCapabilityId(), "source capability");
        ProviderCapability target = resolveTargetCapability(relation, source);
        CapabilityModelCompareVO compareVO = compare(source.getCapabilityId(), target.getCapabilityId());

        ProviderCapabilityModelRelationEntity entity = new ProviderCapabilityModelRelationEntity();
        fillRelationFromCapabilities(entity, source, target);
        entity.setRelationType(normalizeRelationType(relation == null ? null : relation.getRelationType(), compareVO));
        entity.setSameBusinessFlag(isTruthy(relation == null ? null : relation.getSameBusinessFlag()) ? "1" : compareVO.getSameBusinessFlag());
        entity.setRelationReason(StringUtils.isNotBlank(relation == null ? null : relation.getRelationReason())
                ? relation.getRelationReason() : compareVO.getComparisonSummary());
        entity.setStatus(normalizeStatus(relation == null ? null : relation.getStatus()));
        entity.setSortOrder(relation != null && relation.getSortOrder() != null ? relation.getSortOrder() : 0);
        entity.setRemark(relation == null ? null : relation.getRemark());
        entity.setCreateBy(updateBy);
        entity.setUpdateBy(updateBy);
        return relationMapper.insert(entity);
    }

    /**
     * 更新能力模型关系。
     * <p>
     * 如果变更源能力或目标模型，会重新计算模型比较结果并刷新关系类型建议和摘要。
     *
     * @param relation 关系请求 DTO
     * @param updateBy 操作人
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRelation(CapabilityModelRelationDTO relation, String updateBy)
    {
        if (relation == null || relation.getRelationId() == null)
        {
            throw new ServiceException("Relation id is required.");
        }
        ProviderCapabilityModelRelationEntity current = relationMapper.selectById(relation.getRelationId());
        if (current == null)
        {
            throw new ServiceException("Capability relation does not exist.");
        }

        ProviderCapability source = relation.getCapabilityId() == null ? requireCapability(current.getCapabilityId(), "source capability")
                : requireCapability(relation.getCapabilityId(), "source capability");
        ProviderCapability target = resolveTargetCapability(relation, source);
        CapabilityModelCompareVO compareVO = compare(source.getCapabilityId(), target.getCapabilityId());

        fillRelationFromCapabilities(current, source, target);
        current.setRelationType(normalizeRelationType(relation.getRelationType(), compareVO));
        current.setSameBusinessFlag(isTruthy(relation.getSameBusinessFlag()) ? "1" : compareVO.getSameBusinessFlag());
        current.setRelationReason(StringUtils.isNotBlank(relation.getRelationReason())
                ? relation.getRelationReason() : compareVO.getComparisonSummary());
        current.setStatus(normalizeStatus(relation.getStatus()));
        current.setSortOrder(relation.getSortOrder() == null ? 0 : relation.getSortOrder());
        current.setRemark(relation.getRemark());
        current.setUpdateBy(updateBy);
        return relationMapper.updateById(current);
    }

    /**
     * 批量删除能力模型关系。
     *
     * @param relationIds 关系主键数组
     * @return 删除行数
     */
    @Override
    public int deleteRelationByIds(Long[] relationIds)
    {
        if (relationIds == null || relationIds.length == 0)
        {
            return 0;
        }
        return relationMapper.deleteBatchIds(Arrays.asList(relationIds));
    }

    /**
     * 比较两个能力模型是否属于同类能力。
     * <p>
     * 比较维度包括配置类型、业务场景、能力层级、能力编码以及输入输出契约结构。
     *
     * @param leftCapabilityId 左侧能力主键
     * @param rightCapabilityId 右侧能力主键
     * @return 比较结果和关系类型建议
     */
    @Override
    public CapabilityModelCompareVO compare(Long leftCapabilityId, Long rightCapabilityId)
    {
        ProviderCapability left = requireCapability(leftCapabilityId, "left capability");
        ProviderCapability right = requireCapability(rightCapabilityId, "right capability");

        CapabilityModelCompareVO vo = new CapabilityModelCompareVO();
        vo.setLeftCapabilityId(left.getCapabilityId());
        vo.setRightCapabilityId(right.getCapabilityId());
        vo.setLeftCapabilityCode(left.getCapabilityCode());
        vo.setRightCapabilityCode(right.getCapabilityCode());
        vo.setLeftCapabilityName(left.getCapabilityName());
        vo.setRightCapabilityName(right.getCapabilityName());
        vo.setLeftBusinessScene(left.getBusinessScene());
        vo.setRightBusinessScene(right.getBusinessScene());
        vo.setLeftCapabilityLayer(left.getCapabilityLayer());
        vo.setRightCapabilityLayer(right.getCapabilityLayer());
        vo.setLeftConfigType(left.getConfigType());
        vo.setRightConfigType(right.getConfigType());
        vo.setLeftProviderCode(left.getProviderCode());
        vo.setRightProviderCode(right.getProviderCode());
        vo.setLeftModelCode(left.getModelCode());
        vo.setRightModelCode(right.getModelCode());
        vo.setLeftModelName(resolveModelName(left));
        vo.setRightModelName(resolveModelName(right));

        boolean sameConfigType = equalsIgnoreBlank(left.getConfigType(), right.getConfigType());
        boolean sameBusinessScene = equalsIgnoreBlank(left.getBusinessScene(), right.getBusinessScene());
        boolean sameCapabilityLayer = equalsIgnoreBlank(left.getCapabilityLayer(), right.getCapabilityLayer());
        boolean sameCapabilityCode = equalsIgnoreBlank(left.getCapabilityCode(), right.getCapabilityCode());
        boolean sameInputSchema = equalsNormalizeSchema(left.getInputSchemaJson(), right.getInputSchemaJson());
        boolean sameOutputSchema = equalsNormalizeSchema(left.getOutputSchemaJson(), right.getOutputSchemaJson());
        boolean sameBusiness = sameConfigType && sameBusinessScene && sameCapabilityLayer && sameInputSchema && sameOutputSchema;

        vo.setSameTypeFlag(sameConfigType ? "1" : "0");
        vo.setSameBusinessFlag(sameBusiness ? "1" : "0");
        vo.setCompatibleFlag(sameBusiness ? "1" : "0");
        vo.setRelationTypeSuggestion(suggestRelationType(left, right, sameBusiness, sameCapabilityCode));

        List<String> sharedTraits = new ArrayList<>();
        List<String> mismatches = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        if (sameConfigType)
        {
            sharedTraits.add("配置类型一致");
            reasons.add("configType=" + safeText(left.getConfigType()));
        }
        else
        {
            mismatches.add("配置类型不同: " + safeText(left.getConfigType()) + " / " + safeText(right.getConfigType()));
        }

        if (sameBusinessScene)
        {
            sharedTraits.add("业务场景一致");
        }
        else
        {
            mismatches.add("业务场景不同: " + safeText(left.getBusinessScene()) + " / " + safeText(right.getBusinessScene()));
        }

        if (sameCapabilityLayer)
        {
            sharedTraits.add("能力层级一致");
        }
        else
        {
            mismatches.add("能力层级不同: " + safeText(left.getCapabilityLayer()) + " / " + safeText(right.getCapabilityLayer()));
        }

        if (sameInputSchema)
        {
            sharedTraits.add("入参结构一致");
        }
        else
        {
            mismatches.add("入参结构不同");
        }

        if (sameOutputSchema)
        {
            sharedTraits.add("出参结构一致");
        }
        else
        {
            mismatches.add("出参结构不同");
        }

        if (sameCapabilityCode)
        {
            sharedTraits.add("能力编码一致");
        }
        else
        {
            mismatches.add("能力编码不同: " + safeText(left.getCapabilityCode()) + " / " + safeText(right.getCapabilityCode()));
        }

        if (sameBusiness)
        {
            reasons.add("两个模型在业务场景、能力层级和契约结构上保持一致，可以视为同类模型。");
        }
        else
        {
            reasons.add("存在场景、层级或契约差异，建议按不同业务或互斥能力处理。");
        }

        vo.setSharedTraits(sharedTraits);
        vo.setMismatches(mismatches);
        vo.setReasons(reasons);
        vo.setComparisonSummary(buildSummary(vo, sameBusiness));
        return vo;
    }

    /**
     * 将目标模型切换为同能力编码下的默认模型。
     * <p>
     * 该方法只做切换前的能力一致性校验，真正的默认标记更新由 ProviderCapabilityService 统一处理。
     *
     * @param request 模型切换请求
     * @param updateBy 操作人
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int switchDefaultModel(CapabilityModelSwitchReq request, String updateBy)
    {
        if (request == null || request.getTargetCapabilityId() == null)
        {
            throw new ServiceException("Target capability id is required.");
        }
        ProviderCapability target = requireCapability(request.getTargetCapabilityId(), "target capability");
        if (!UserConstants.NORMAL.equals(target.getStatus()))
        {
            throw new ServiceException("Disabled capability cannot be switched as default.");
        }

        if (request.getSourceCapabilityId() != null)
        {
            ProviderCapability source = requireCapability(request.getSourceCapabilityId(), "source capability");
            if (!equalsIgnoreBlank(source.getCapabilityCode(), target.getCapabilityCode()))
            {
                throw new ServiceException("Only capabilities under the same capabilityCode can switch defaults.");
            }
        }
        return providerCapabilityService.setDefaultCapability(target.getCapabilityId(), updateBy);
    }

    /**
     * 查询可作为关系候选的能力列表。
     *
     * @param query 能力筛选条件
     * @return 候选能力列表
     */
    @Override
    public List<ProviderCapability> selectCandidateCapabilityList(ProviderCapability query)
    {
        List<ProviderCapability> capabilities = providerCapabilityService.selectCapabilityList(query);
        if (capabilities == null || capabilities.isEmpty())
        {
            return Collections.emptyList();
        }
        Map<String, String> modelNameMap = loadModelNameMap();
        for (ProviderCapability capability : capabilities)
        {
            capability.setCandidateDisplayName(buildCandidateDisplayName(capability, modelNameMap));
        }
        return capabilities;
    }

    /**
     * 查询并校验能力必须存在。
     *
     * @param capabilityId 能力主键
     * @param label 错误信息中的能力标签
     * @return 能力实体
     */
    private ProviderCapability requireCapability(Long capabilityId, String label)
    {
        if (capabilityId == null)
        {
            throw new ServiceException(label + " is required.");
        }
        ProviderCapability capability = providerCapabilityService.selectCapabilityById(capabilityId);
        if (capability == null)
        {
            throw new ServiceException(label + " does not exist.");
        }
        return capability;
    }

    /**
     * 根据关系请求解析目标能力。
     * <p>
     * 优先使用 targetCapabilityId；未传时按源能力编码、模型编码和配置类型寻找启用能力。
     *
     * @param relation 关系请求
     * @param source 源能力
     * @return 目标能力
     */
    private ProviderCapability resolveTargetCapability(CapabilityModelRelationDTO relation, ProviderCapability source)
    {
        if (relation == null)
        {
            throw new ServiceException("Relation request must not be null.");
        }
        if (relation.getTargetCapabilityId() != null)
        {
            ProviderCapability target = providerCapabilityService.selectCapabilityById(relation.getTargetCapabilityId());
            if (target == null)
            {
                throw new ServiceException("Target capability does not exist.");
            }
            return target;
        }
        if (StringUtils.isBlank(relation.getModelCode()))
        {
            throw new ServiceException("Target capability id or model code is required.");
        }
        ProviderCapability target = providerCapabilityService.selectCapabilityList(new ProviderCapability()).stream()
                .filter(item -> equalsIgnoreBlank(item.getCapabilityCode(), source.getCapabilityCode()))
                .filter(item -> equalsIgnoreBlank(item.getModelCode(), relation.getModelCode()))
                .filter(item -> equalsIgnoreBlank(item.getConfigType(), source.getConfigType()))
                .filter(item -> UserConstants.NORMAL.equals(item.getStatus()))
                .findFirst()
                .orElse(null);
        if (target != null)
        {
            return target;
        }
        throw new ServiceException("Target capability model not found.");
    }

    /**
     * 加载能力主键到能力实体的映射。
     *
     * @return 能力映射表
     */
    private Map<Long, ProviderCapability> loadCapabilityMap()
    {
        List<ProviderCapability> capabilities = providerCapabilityService.selectCapabilityList(null);
        if (capabilities == null || capabilities.isEmpty())
        {
            return Collections.emptyMap();
        }
        Map<Long, ProviderCapability> map = new LinkedHashMap<>();
        for (ProviderCapability capability : capabilities)
        {
            if (capability != null && capability.getCapabilityId() != null)
            {
                map.put(capability.getCapabilityId(), capability);
            }
        }
        return map;
    }

    /**
     * 保留 PageHelper 返回的分页元数据，避免实体转 DTO 后丢失 total。
     */
    private List<CapabilityModelRelationDTO> createResultList(List<ProviderCapabilityModelRelationEntity> relations)
    {
        if (relations instanceof Page)
        {
            Page<?> page = (Page<?>) relations;
            Page<CapabilityModelRelationDTO> result = new Page<>(page.getPageNum(), page.getPageSize());
            result.setTotal(page.getTotal());
            result.setPages(page.getPages());
            result.setReasonable(page.getReasonable());
            result.setPageSizeZero(page.getPageSizeZero());
            result.setStartRow(page.getStartRow());
            result.setEndRow(page.getEndRow());
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * 将关系实体转换为前端展示 DTO，并补齐源能力、目标能力和模型名称。
     *
     * @param relation 关系实体
     * @param capability 源能力实体
     * @return 关系 DTO
     */
    private CapabilityModelRelationDTO toRelationDTO(ProviderCapabilityModelRelationEntity relation, ProviderCapability capability,
                                                     Map<Long, ProviderCapability> capabilityMap,
                                                     Map<Long, ProviderCapability> targetCapabilityCache,
                                                     Map<String, String> modelNameMap)
    {
        CapabilityModelRelationDTO dto = new CapabilityModelRelationDTO();
        BeanUtils.copyProperties(relation, dto);
        if (capability != null)
        {
            dto.setCapabilityName(capability.getCapabilityName());
            dto.setBindingDisplayName(buildCandidateDisplayName(capability, modelNameMap));
            dto.setBusinessScene(capability.getBusinessScene());
            dto.setCapabilityLayer(capability.getCapabilityLayer());
            dto.setConfigType(capability.getConfigType());
            dto.setCapabilityCode(capability.getCapabilityCode());
        }
        ProviderCapability targetCapability = resolveTargetCapabilityByRelation(relation, capability, capabilityMap,
                targetCapabilityCache);
        if (targetCapability != null)
        {
            dto.setTargetCapabilityId(targetCapability.getCapabilityId());
            dto.setTargetCapabilityCode(targetCapability.getCapabilityCode());
            dto.setTargetCapabilityName(targetCapability.getCapabilityName());
            dto.setTargetProviderCode(targetCapability.getProviderCode());
            dto.setModelCode(targetCapability.getModelCode());
            dto.setModelName(resolveModelName(targetCapability, modelNameMap));
            dto.setTargetModelDisplayName(buildCandidateDisplayName(targetCapability, modelNameMap));
        }
        if (StringUtils.isBlank(dto.getModelName()))
        {
            dto.setModelName(resolveModelName(relation.getConfigType(), relation.getTargetProviderCode(),
                    relation.getModelCode(), modelNameMap));
        }
        if (StringUtils.isBlank(dto.getBindingDisplayName()))
        {
            dto.setBindingDisplayName(buildDisplayName(dto.getCapabilityName(), dto.getProviderCode(), dto.getModelName(), dto.getModelCode()));
        }
        if (StringUtils.isBlank(dto.getTargetModelDisplayName()))
        {
            dto.setTargetModelDisplayName(buildDisplayName(dto.getTargetCapabilityName(), dto.getTargetProviderCode(), dto.getModelName(), dto.getModelCode()));
        }
        return dto;
    }

    /**
     * 将源能力和目标能力快照写入关系实体，避免前端切换默认模型时再按 modelCode 模糊匹配。
     */
    private void fillRelationFromCapabilities(ProviderCapabilityModelRelationEntity relation, ProviderCapability source,
                                              ProviderCapability target)
    {
        relation.setCapabilityId(source.getCapabilityId());
        relation.setCapabilityCode(source.getCapabilityCode());
        relation.setCapabilityName(source.getCapabilityName());
        relation.setBusinessScene(source.getBusinessScene());
        relation.setCapabilityLayer(source.getCapabilityLayer());
        relation.setConfigType(source.getConfigType());
        relation.setProviderCode(target.getProviderCode());
        relation.setTargetCapabilityId(target.getCapabilityId());
        relation.setTargetCapabilityCode(target.getCapabilityCode());
        relation.setTargetCapabilityName(target.getCapabilityName());
        relation.setTargetProviderCode(target.getProviderCode());
        relation.setModelCode(target.getModelCode());
        relation.setModelName(resolveModelName(target));
    }

    /**
     * 根据关系中的模型编码反查目标能力。
     *
     * @param relation 关系实体
     * @param source 源能力实体
     * @return 目标能力；无法匹配时返回 null
     */
    private ProviderCapability resolveTargetCapabilityByRelation(ProviderCapabilityModelRelationEntity relation,
                                                                 ProviderCapability source,
                                                                 Map<Long, ProviderCapability> capabilityMap,
                                                                 Map<Long, ProviderCapability> targetCapabilityCache)
    {
        if (relation == null)
        {
            return null;
        }
        if (relation.getTargetCapabilityId() != null)
        {
            ProviderCapability target = targetCapabilityCache.computeIfAbsent(relation.getTargetCapabilityId(),
                    capabilityMap::get);
            if (target != null)
            {
                return target;
            }
        }
        if (source == null || StringUtils.isBlank(relation.getModelCode()))
        {
            return null;
        }
        if (capabilityMap == null || capabilityMap.isEmpty())
        {
            return null;
        }
        for (ProviderCapability capability : capabilityMap.values())
        {
            if (capability == null)
            {
                continue;
            }
            if (equalsIgnoreBlank(capability.getCapabilityCode(), source.getCapabilityCode())
                    && equalsIgnoreBlank(capability.getModelCode(), relation.getModelCode())
                    && equalsIgnoreBlank(capability.getConfigType(), relation.getConfigType())
                    && (StringUtils.isBlank(relation.getTargetProviderCode())
                    || equalsIgnoreBlank(capability.getProviderCode(), relation.getTargetProviderCode())
                    || equalsIgnoreBlank(capability.getProviderCode(), relation.getProviderCode())))
            {
                return capability;
            }
        }
        return null;
    }

    /**
     * 解析能力对应的模型展示名称。
     *
     * @param capability 能力实体
     * @return 模型名称；无法命中模型目录时返回模型编码
     */
    private String resolveModelName(ProviderCapability capability)
    {
        if (capability == null)
        {
            return null;
        }
        String modelName = resolveModelName(capability.getConfigType(), capability.getProviderCode(), capability.getModelCode());
        return StringUtils.isNotBlank(modelName) ? modelName : capability.getModelCode();
    }

    private String resolveModelName(ProviderCapability capability, Map<String, String> modelNameMap)
    {
        if (capability == null)
        {
            return null;
        }
        String modelName = resolveModelName(capability.getConfigType(), capability.getProviderCode(),
                capability.getModelCode(), modelNameMap);
        return StringUtils.isNotBlank(modelName) ? modelName : capability.getModelCode();
    }

    /**
     * 从模型目录中解析模型展示名称。
     *
     * @param configType 配置类型
     * @param providerCode Provider 编码
     * @param modelCode 模型编码
     * @return 模型名称；无法命中时返回模型编码
     */
    private String resolveModelName(String configType, String providerCode, String modelCode)
    {
        if (StringUtils.isBlank(modelCode))
        {
            return null;
        }
        QueryWrapper<ProviderModelCatalogEntity> wrapper = new QueryWrapper<>();
        wrapper.select("*")
                .eq(StringUtils.isNotBlank(configType), "config_type", configType)
                .eq(StringUtils.isNotBlank(providerCode), "provider_code", providerCode)
                .eq("model_code", modelCode)
                .last("LIMIT 1");
        ProviderModelCatalogEntity catalog = modelCatalogMapper.selectOne(wrapper);
        if (catalog == null && StringUtils.isNotBlank(configType))
        {
            QueryWrapper<ProviderModelCatalogEntity> fallbackWrapper = new QueryWrapper<>();
            fallbackWrapper.select("*")
                    .eq("model_code", modelCode)
                    .eq("config_type", configType)
                    .last("LIMIT 1");
            catalog = modelCatalogMapper.selectOne(fallbackWrapper);
        }
        return catalog == null ? modelCode : (StringUtils.isNotBlank(catalog.getModelName()) ? catalog.getModelName() : modelCode);
    }

    private String resolveModelName(String configType, String providerCode, String modelCode, Map<String, String> modelNameMap)
    {
        if (StringUtils.isBlank(modelCode))
        {
            return null;
        }
        if (modelNameMap == null || modelNameMap.isEmpty())
        {
            return modelCode;
        }
        String exactName = modelNameMap.get(modelNameKey(configType, providerCode, modelCode));
        if (StringUtils.isNotBlank(exactName))
        {
            return exactName;
        }
        String typeName = modelNameMap.get(modelNameKey(configType, "", modelCode));
        return StringUtils.isNotBlank(typeName) ? typeName : modelCode;
    }

    /**
     * 一次性加载模型目录，避免关系列表每行反查模型名称。
     */
    private Map<String, String> loadModelNameMap()
    {
        List<ProviderModelCatalogEntity> catalogs = modelCatalogMapper.selectList(new QueryWrapper<>());
        if (catalogs == null || catalogs.isEmpty())
        {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (ProviderModelCatalogEntity catalog : catalogs)
        {
            if (catalog == null || StringUtils.isBlank(catalog.getModelCode()))
            {
                continue;
            }
            String modelName = StringUtils.isNotBlank(catalog.getModelName()) ? catalog.getModelName() : catalog.getModelCode();
            result.put(modelNameKey(catalog.getConfigType(), catalog.getProviderCode(), catalog.getModelCode()), modelName);
            result.putIfAbsent(modelNameKey(catalog.getConfigType(), "", catalog.getModelCode()), modelName);
        }
        return result;
    }

    private String modelNameKey(String configType, String providerCode, String modelCode)
    {
        return normalizeText(configType) + "|" + normalizeText(providerCode) + "|" + normalizeText(modelCode);
    }

    /**
     * 忽略首尾空白比较两个文本，两个空值视为相等。
     */
    private boolean equalsIgnoreBlank(String left, String right)
    {
        if (StringUtils.isBlank(left) && StringUtils.isBlank(right))
        {
            return true;
        }
        return Objects.equals(normalizeText(left), normalizeText(right));
    }

    /**
     * 规范化 JSON Schema 后比较契约结构是否一致。
     */
    private boolean equalsNormalizeSchema(String left, String right)
    {
        return Objects.equals(normalizeSchema(left), normalizeSchema(right));
    }

    /**
     * 规范化 JSON Schema 文本，优先按 JSON 解析后重新序列化。
     */
    private String normalizeSchema(String schema)
    {
        if (StringUtils.isBlank(schema))
        {
            return "";
        }
        try
        {
            Object parsed = com.alibaba.fastjson2.JSON.parse(schema);
            return com.alibaba.fastjson2.JSON.toJSONString(parsed);
        }
        catch (Exception e)
        {
            return normalizeText(schema);
        }
    }

    /**
     * 清理普通文本用于比较。
     */
    private String normalizeText(String text)
    {
        return text == null ? "" : text.trim();
    }

    /**
     * 判断字符串是否表达启用或肯定语义。
     */
    private boolean isTruthy(String value)
    {
        return "1".equals(value) || "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * 规范化关系状态，未传时默认启用。
     */
    private String normalizeStatus(String status)
    {
        if (StringUtils.isBlank(status))
        {
            return UserConstants.NORMAL;
        }
        return status;
    }

    /**
     * 将空文本转为占位符，便于比较摘要展示。
     */
    private String safeText(String value)
    {
        return StringUtils.isBlank(value) ? "-" : value;
    }

    /**
     * 规范化关系类型，未指定时使用比较结果中的建议类型。
     */
    private String normalizeRelationType(String requested, CapabilityModelCompareVO compareVO)
    {
        if (StringUtils.isNotBlank(requested))
        {
            return requested;
        }
        return compareVO == null ? "mutual_exclusive" : compareVO.getRelationTypeSuggestion();
    }

    /**
     * 根据业务一致性和能力编码给出关系类型建议。
     */
    private String suggestRelationType(ProviderCapability left, ProviderCapability right,
                                       boolean sameBusiness, boolean sameCapabilityCode)
    {
        if (!sameBusiness)
        {
            return "mutual_exclusive";
        }
        if (sameCapabilityCode)
        {
            return "primary_backup";
        }
        if (equalsIgnoreBlank(left.getProviderCode(), right.getProviderCode()))
        {
            return "ab_test";
        }
        return "replaceable";
    }

    /**
     * 构建模型比较的人读摘要。
     */
    private String buildSummary(CapabilityModelCompareVO vo, boolean sameBusiness)
    {
        if (sameBusiness)
        {
            return String.format("两个模型可以按同类能力处理，建议优先使用 %s / %s 的互换或主备关系。",
                    safeText(vo.getLeftModelCode()), safeText(vo.getRightModelCode()));
        }
        return String.format("两个模型属于不同业务域，建议配置为互斥或独立关系，避免误切换。");
    }

    /**
     * 构建管理端候选显示名。能力名称保持业务含义，候选差异通过 Provider / 模型展示。
     */
    private String buildCandidateDisplayName(ProviderCapability capability)
    {
        if (capability == null)
        {
            return null;
        }
        return buildDisplayName(capability.getCapabilityName(), capability.getProviderCode(),
                resolveModelName(capability), capability.getModelCode());
    }

    private String buildCandidateDisplayName(ProviderCapability capability, Map<String, String> modelNameMap)
    {
        if (capability == null)
        {
            return null;
        }
        return buildDisplayName(capability.getCapabilityName(), capability.getProviderCode(),
                resolveModelName(capability, modelNameMap), capability.getModelCode());
    }

    private String buildDisplayName(String capabilityName, String providerCode, String modelName, String modelCode)
    {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(capabilityName))
        {
            builder.append(capabilityName);
        }
        else
        {
            builder.append("未命名能力");
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(providerCode))
        {
            parts.add(providerCode);
        }
        String displayModel = StringUtils.isNotBlank(modelName) ? modelName : modelCode;
        if (StringUtils.isNotBlank(displayModel))
        {
            parts.add(displayModel);
        }
        if (!parts.isEmpty())
        {
            builder.append("（").append(String.join(" / ", parts)).append("）");
        }
        return builder.toString();
    }
}
