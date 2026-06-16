package com.bigyun.provider.domain.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 服务配置总览视图对象。
 */
public class ProviderDashboardVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 总览统计。
     */
    private OverviewStats overviewStats;

    /**
     * 按配置类型聚合的统计结果。
     */
    private List<ConfigTypeStats> configTypeList;

    /**
     * 最近更新的配置列表。
     */
    private List<RecentConfigVO> recentConfigs;

    public OverviewStats getOverviewStats()
    {
        return overviewStats;
    }

    public void setOverviewStats(OverviewStats overviewStats)
    {
        this.overviewStats = overviewStats;
    }

    public List<ConfigTypeStats> getConfigTypeList()
    {
        return configTypeList;
    }

    public void setConfigTypeList(List<ConfigTypeStats> configTypeList)
    {
        this.configTypeList = configTypeList;
    }

    public List<RecentConfigVO> getRecentConfigs()
    {
        return recentConfigs;
    }

    public void setRecentConfigs(List<RecentConfigVO> recentConfigs)
    {
        this.recentConfigs = recentConfigs;
    }

    /**
     * 总览统计信息。
     */
    public static class OverviewStats implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Long totalCount;
        private Long enabledCount;
        private Long disabledCount;
        private Map<String, Long> typeCountMap;

        public Long getTotalCount()
        {
            return totalCount;
        }

        public void setTotalCount(Long totalCount)
        {
            this.totalCount = totalCount;
        }

        public Long getEnabledCount()
        {
            return enabledCount;
        }

        public void setEnabledCount(Long enabledCount)
        {
            this.enabledCount = enabledCount;
        }

        public Long getDisabledCount()
        {
            return disabledCount;
        }

        public void setDisabledCount(Long disabledCount)
        {
            this.disabledCount = disabledCount;
        }

        public Map<String, Long> getTypeCountMap()
        {
            return typeCountMap;
        }

        public void setTypeCountMap(Map<String, Long> typeCountMap)
        {
            this.typeCountMap = typeCountMap;
        }
    }

    /**
     * 单个配置类型的聚合统计。
     */
    public static class ConfigTypeStats implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private String configType;
        private String typeName;
        private String typeDesc;
        private Long totalCount;
        private Long enabledCount;
        private Long disabledCount;
        private String defaultProvider;
        private String icon;

        public String getConfigType()
        {
            return configType;
        }

        public void setConfigType(String configType)
        {
            this.configType = configType;
        }

        public String getTypeName()
        {
            return typeName;
        }

        public void setTypeName(String typeName)
        {
            this.typeName = typeName;
        }

        public String getTypeDesc()
        {
            return typeDesc;
        }

        public void setTypeDesc(String typeDesc)
        {
            this.typeDesc = typeDesc;
        }

        public Long getTotalCount()
        {
            return totalCount;
        }

        public void setTotalCount(Long totalCount)
        {
            this.totalCount = totalCount;
        }

        public Long getEnabledCount()
        {
            return enabledCount;
        }

        public void setEnabledCount(Long enabledCount)
        {
            this.enabledCount = enabledCount;
        }

        public Long getDisabledCount()
        {
            return disabledCount;
        }

        public void setDisabledCount(Long disabledCount)
        {
            this.disabledCount = disabledCount;
        }

        public String getDefaultProvider()
        {
            return defaultProvider;
        }

        public void setDefaultProvider(String defaultProvider)
        {
            this.defaultProvider = defaultProvider;
        }

        public String getIcon()
        {
            return icon;
        }

        public void setIcon(String icon)
        {
            this.icon = icon;
        }
    }

    /**
     * 最近更新配置视图。
     */
    public static class RecentConfigVO implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Long configId;
        private String configType;
        private String providerCode;
        private String providerName;
        private String status;
        private String isDefault;
        private String updateTime;
        private String updateBy;

        public Long getConfigId()
        {
            return configId;
        }

        public void setConfigId(Long configId)
        {
            this.configId = configId;
        }

        public String getConfigType()
        {
            return configType;
        }

        public void setConfigType(String configType)
        {
            this.configType = configType;
        }

        public String getProviderCode()
        {
            return providerCode;
        }

        public void setProviderCode(String providerCode)
        {
            this.providerCode = providerCode;
        }

        public String getProviderName()
        {
            return providerName;
        }

        public void setProviderName(String providerName)
        {
            this.providerName = providerName;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

        public String getIsDefault()
        {
            return isDefault;
        }

        public void setIsDefault(String isDefault)
        {
            this.isDefault = isDefault;
        }

        public String getUpdateTime()
        {
            return updateTime;
        }

        public void setUpdateTime(String updateTime)
        {
            this.updateTime = updateTime;
        }

        public String getUpdateBy()
        {
            return updateBy;
        }

        public void setUpdateBy(String updateBy)
        {
            this.updateBy = updateBy;
        }
    }
}
