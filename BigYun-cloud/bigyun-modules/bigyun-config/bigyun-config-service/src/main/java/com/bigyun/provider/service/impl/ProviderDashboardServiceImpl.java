package com.bigyun.provider.service.impl;

import com.bigyun.provider.domain.ProviderConfig;
import com.bigyun.provider.domain.vo.ProviderDashboardVO;
import com.bigyun.provider.mapper.ProviderConfigMapper;
import com.bigyun.provider.service.IProviderDashboardService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provider 服务配置总览实现。
 * <p>
 * 该服务面向管理端首页，基于 Provider 配置表聚合总数、类型分布、默认服务商和最近更新配置。
 */
@Service
public class ProviderDashboardServiceImpl implements IProviderDashboardService
{
    private final ProviderConfigMapper providerConfigMapper;

    private static final Map<String, String> CONFIG_TYPE_NAMES = new HashMap<>();
    private static final Map<String, String> CONFIG_TYPE_ICONS = new HashMap<>();

    static
    {
        CONFIG_TYPE_NAMES.put("storage", "对象存储");
        CONFIG_TYPE_NAMES.put("llm", "大模型");
        CONFIG_TYPE_NAMES.put("tts", "语音合成");
        CONFIG_TYPE_NAMES.put("stt", "语音识别");
        CONFIG_TYPE_NAMES.put("image", "图像服务");
        CONFIG_TYPE_NAMES.put("image_gen", "图像生成");
        CONFIG_TYPE_NAMES.put("image_recognition", "图像识别");
        CONFIG_TYPE_NAMES.put("translation", "翻译服务");
        CONFIG_TYPE_NAMES.put("ocr", "OCR识别");
        CONFIG_TYPE_NAMES.put("face", "人脸识别");
        CONFIG_TYPE_NAMES.put("vision", "视觉理解");
        CONFIG_TYPE_NAMES.put("voiceprint", "声纹识别");
        CONFIG_TYPE_NAMES.put("embedding", "向量嵌入");
        CONFIG_TYPE_NAMES.put("rerank", "重排序");
        CONFIG_TYPE_NAMES.put("moderation", "内容审核");
        CONFIG_TYPE_NAMES.put("search", "搜索服务");

        CONFIG_TYPE_ICONS.put("storage", "upload");
        CONFIG_TYPE_ICONS.put("llm", "education");
        CONFIG_TYPE_ICONS.put("tts", "microphone");
        CONFIG_TYPE_ICONS.put("stt", "service");
        CONFIG_TYPE_ICONS.put("image", "picture");
        CONFIG_TYPE_ICONS.put("image_gen", "picture");
        CONFIG_TYPE_ICONS.put("image_recognition", "camera");
        CONFIG_TYPE_ICONS.put("translation", "chat-line-round");
        CONFIG_TYPE_ICONS.put("ocr", "document");
        CONFIG_TYPE_ICONS.put("face", "user");
        CONFIG_TYPE_ICONS.put("vision", "view");
        CONFIG_TYPE_ICONS.put("voiceprint", "headset");
        CONFIG_TYPE_ICONS.put("embedding", "connection");
        CONFIG_TYPE_ICONS.put("rerank", "sort");
        CONFIG_TYPE_ICONS.put("moderation", "warning");
        CONFIG_TYPE_ICONS.put("search", "search");
    }

    public ProviderDashboardServiceImpl(ProviderConfigMapper providerConfigMapper)
    {
        this.providerConfigMapper = providerConfigMapper;
    }

    /**
     * 获取 Provider 配置总览数据。
     * <p>
     * 当前配置规模较小，直接读取配置表后在内存中聚合，避免为每个卡片编写重复 SQL。
     *
     * @return Provider 总览 VO
     */
    @Override
    public ProviderDashboardVO getDashboardData()
    {
        ProviderDashboardVO dashboard = new ProviderDashboardVO();
        List<ProviderConfig> allConfigs = providerConfigMapper.selectList(null);

        dashboard.setOverviewStats(buildOverviewStats(allConfigs));
        dashboard.setConfigTypeList(buildConfigTypeStats(allConfigs));
        dashboard.setRecentConfigs(buildRecentConfigs(allConfigs));
        return dashboard;
    }

    /**
     * 构建总览统计。
     */
    private ProviderDashboardVO.OverviewStats buildOverviewStats(List<ProviderConfig> allConfigs)
    {
        ProviderDashboardVO.OverviewStats stats = new ProviderDashboardVO.OverviewStats();
        long totalCount = allConfigs.size();
        long enabledCount = allConfigs.stream().filter(config -> "0".equals(config.getStatus())).count();
        long disabledCount = totalCount - enabledCount;

        stats.setTotalCount(totalCount);
        stats.setEnabledCount(enabledCount);
        stats.setDisabledCount(disabledCount);
        stats.setTypeCountMap(allConfigs.stream()
                .collect(Collectors.groupingBy(ProviderConfig::getConfigType, Collectors.counting())));
        return stats;
    }

    /**
     * 构建按配置类型聚合的统计结果。
     */
    private List<ProviderDashboardVO.ConfigTypeStats> buildConfigTypeStats(List<ProviderConfig> allConfigs)
    {
        Map<String, List<ProviderConfig>> typeGroupMap = allConfigs.stream()
                .collect(Collectors.groupingBy(ProviderConfig::getConfigType));

        List<ProviderDashboardVO.ConfigTypeStats> configTypeList = new ArrayList<>();
        for (Map.Entry<String, List<ProviderConfig>> entry : typeGroupMap.entrySet())
        {
            String configType = entry.getKey();
            List<ProviderConfig> configs = entry.getValue();
            long enabledCount = configs.stream().filter(config -> "0".equals(config.getStatus())).count();

            ProviderDashboardVO.ConfigTypeStats stats = new ProviderDashboardVO.ConfigTypeStats();
            stats.setConfigType(configType);
            stats.setTypeName(CONFIG_TYPE_NAMES.getOrDefault(configType, configType));
            stats.setTypeDesc(CONFIG_TYPE_NAMES.getOrDefault(configType, configType) + "配置");
            stats.setTotalCount((long) configs.size());
            stats.setEnabledCount(enabledCount);
            stats.setDisabledCount((long) configs.size() - enabledCount);
            stats.setDefaultProvider(resolveDefaultProvider(configs));
            stats.setIcon(CONFIG_TYPE_ICONS.getOrDefault(configType, "setting"));
            configTypeList.add(stats);
        }

        configTypeList.sort((left, right) -> right.getTotalCount().compareTo(left.getTotalCount()));
        return configTypeList;
    }

    /**
     * 构建最近更新的配置列表。
     */
    private List<ProviderDashboardVO.RecentConfigVO> buildRecentConfigs(List<ProviderConfig> allConfigs)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return allConfigs.stream()
                .sorted((left, right) -> {
                    Date leftDate = left.getUpdateTime() != null ? left.getUpdateTime() : left.getCreateTime();
                    Date rightDate = right.getUpdateTime() != null ? right.getUpdateTime() : right.getCreateTime();
                    if (leftDate == null && rightDate == null)
                    {
                        return 0;
                    }
                    if (leftDate == null)
                    {
                        return 1;
                    }
                    if (rightDate == null)
                    {
                        return -1;
                    }
                    return rightDate.compareTo(leftDate);
                })
                .limit(10)
                .map(config -> {
                    ProviderDashboardVO.RecentConfigVO recentConfig = new ProviderDashboardVO.RecentConfigVO();
                    recentConfig.setConfigId(config.getConfigId());
                    recentConfig.setConfigType(config.getConfigType());
                    recentConfig.setProviderCode(config.getProviderCode());
                    recentConfig.setProviderName(config.getProviderName());
                    recentConfig.setStatus(config.getStatus());
                    recentConfig.setIsDefault(config.getIsDefault());

                    Date updateTime = config.getUpdateTime() != null ? config.getUpdateTime() : config.getCreateTime();
                    recentConfig.setUpdateTime(updateTime != null ? formatter.format(updateTime) : "");
                    recentConfig.setUpdateBy(config.getUpdateBy() != null ? config.getUpdateBy() : config.getCreateBy());
                    return recentConfig;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从同配置类型的配置列表中解析默认 Provider 展示名。
     *
     * @param configs 同一配置类型下的配置列表
     * @return 默认 Provider 名称；没有默认项时返回 null
     */
    private String resolveDefaultProvider(List<ProviderConfig> configs)
    {
        return configs.stream()
                .filter(config -> "Y".equals(config.getIsDefault()))
                .findFirst()
                .map(config -> isNotBlank(config.getProviderName()) ? config.getProviderName() : config.getProviderCode())
                .orElse(null);
    }

    /**
     * 判断文本是否非空。
     */
    private boolean isNotBlank(String value)
    {
        return value != null && !value.trim().isEmpty();
    }
}
