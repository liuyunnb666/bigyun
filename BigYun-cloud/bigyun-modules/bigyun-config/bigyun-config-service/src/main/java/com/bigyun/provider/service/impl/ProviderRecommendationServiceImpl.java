package com.bigyun.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.provider.db.domain.ProviderModelCatalogEntity;
import com.bigyun.provider.db.mapper.ProviderModelCatalogMapper;
import com.bigyun.provider.domain.ProviderIntegrationGuide;
import com.bigyun.provider.domain.ProviderModelCatalog;
import com.bigyun.provider.domain.ProviderRecommendationVO;
import com.bigyun.provider.mapper.ProviderIntegrationGuideMapper;
import com.bigyun.provider.service.IProviderRecommendationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provider 推荐服务实现。
 * <p>
 * 将 Provider 集成指南与模型目录合并为前端可展示的推荐卡片，帮助配置人员选择服务商和模型。
 */
@Service
public class ProviderRecommendationServiceImpl implements IProviderRecommendationService
{
    @Autowired
    private ProviderIntegrationGuideMapper providerIntegrationGuideMapper;

    @Autowired
    private ProviderModelCatalogMapper providerModelCatalogMapper;

    /**
     * 查询指定配置类型下的 Provider 推荐列表。
     * <p>
     * 先读取启用的集成指南，再按 Provider 编码批量加载模型目录，最后组装成推荐 VO。
     *
     * @param configType 配置类型，例如 llm、ocr、face
     * @return 推荐列表；配置类型为空或没有指南时返回空列表
     */
    @Override
    public List<ProviderRecommendationVO> selectRecommendations(String configType)
    {
        if (StringUtils.isBlank(configType))
        {
            return Collections.emptyList();
        }

        List<ProviderIntegrationGuide> guides = providerIntegrationGuideMapper.selectList(
                new LambdaQueryWrapper<ProviderIntegrationGuide>()
                        .eq(ProviderIntegrationGuide::getConfigType, configType)
                        .eq(ProviderIntegrationGuide::getStatus, "0")
                        .orderByAsc(ProviderIntegrationGuide::getSortOrder)
                        .orderByAsc(ProviderIntegrationGuide::getGuideId));
        if (guides == null || guides.isEmpty())
        {
            return Collections.emptyList();
        }

        List<String> providerCodes = guides.stream()
                .map(ProviderIntegrationGuide::getProviderCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<ProviderModelCatalog>> modelMap = Collections.emptyMap();
        if (!providerCodes.isEmpty())
        {
            List<ProviderModelCatalogEntity> models = providerModelCatalogMapper.selectList(
                    new LambdaQueryWrapper<ProviderModelCatalogEntity>()
                            .eq(ProviderModelCatalogEntity::getConfigType, configType)
                            .in(ProviderModelCatalogEntity::getProviderCode, providerCodes)
                            .orderByAsc(ProviderModelCatalogEntity::getSortOrder)
                            .orderByAsc(ProviderModelCatalogEntity::getModelId));
            modelMap = models == null ? Collections.emptyMap()
                    : models.stream().map(this::toManageModel)
                            .collect(Collectors.groupingBy(ProviderModelCatalog::getProviderCode));
        }

        List<ProviderRecommendationVO> result = new ArrayList<>();
        for (ProviderIntegrationGuide guide : guides)
        {
            ProviderRecommendationVO vo = new ProviderRecommendationVO();
            BeanUtils.copyProperties(guide, vo);
            vo.setModels(modelMap.get(guide.getProviderCode()));
            result.add(vo);
        }
        return result;
    }

    /**
     * 模型目录数据库实体只在 provider-db 层使用，推荐接口向前端仍返回管理端对象。
     */
    private ProviderModelCatalog toManageModel(ProviderModelCatalogEntity entity)
    {
        ProviderModelCatalog model = new ProviderModelCatalog();
        BeanUtils.copyProperties(entity, model);
        return model;
    }
}
