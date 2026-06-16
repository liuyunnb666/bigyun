package com.bigyun.provider.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigyun.provider.domain.ProviderFieldConfigDO;
import com.bigyun.provider.mapper.ProviderFieldConfigMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Provider 动态字段加载器。
 * <p>
 * 该组件属于 config-service 的运行时能力，负责从 provider_field_config 表加载字段定义，
 * 并将结果缓存在内存中供控制器和后续配置流程直接使用。
 * 之所以放在 service 模块，是因为它依赖了数据库访问和 Spring Bean 生命周期，
 * 不适合继续放在 common 模块中承担跨模块共享职责。
 * </p>
 */
@Slf4j
@Component
public class ProviderFieldLoader
{
    /**
     * 数据库里没有配置字段时的通用兜底定义。
     * <p>
     * 这里仅作为接口级兜底，确保字段配置缺失时页面仍然具备最基本的可编辑能力。
     * </p>
     */
    private static final List<FieldDef> DEFAULT_FIELDS = Arrays.asList(
        new FieldDef("endpoint", "服务地址", "text", 0, null, null, null, null, 1),
        new FieldDef("accessKey", "Access Key", "password", 0, null, null, null, null, 2),
        new FieldDef("secretKey", "Secret Key", "password", 0, null, null, null, null, 3),
        new FieldDef("apiKey", "API Key", "password", 0, null, null, null, null, 4)
    );

    private final ProviderFieldConfigMapper providerFieldConfigMapper;

    private final Map<String, List<FieldDef>> fieldCache = new ConcurrentHashMap<>();

    public ProviderFieldLoader(ProviderFieldConfigMapper providerFieldConfigMapper)
    {
        this.providerFieldConfigMapper = providerFieldConfigMapper;
    }

    /**
     * 在 Bean 初始化完成后预加载一次字段配置。
     * <p>
     * 如果数据库读取失败，会记录告警并保留兜底字段，不会阻断整个服务启动。
     * </p>
     */
    @PostConstruct
    public void loadFields()
    {
        try
        {
            List<ProviderFieldConfigDO> records = providerFieldConfigMapper.selectList(
                new LambdaQueryWrapper<ProviderFieldConfigDO>()
                    .orderByAsc(ProviderFieldConfigDO::getProviderCode)
                    .orderByAsc(ProviderFieldConfigDO::getSortOrder)
                    .orderByAsc(ProviderFieldConfigDO::getId));

            fieldCache.clear();
            if (records != null && !records.isEmpty())
            {
                Map<String, List<FieldDef>> grouped = new HashMap<>();
                for (ProviderFieldConfigDO record : records)
                {
                    grouped.computeIfAbsent(record.getProviderCode(), key -> new ArrayList<>())
                        .add(toFieldDef(record));
                }
                fieldCache.putAll(grouped);
            }
            log.info("加载了 {} 个服务商的动态字段配置", fieldCache.size());
        }
        catch (Exception e)
        {
            log.warn("加载 Provider 动态字段配置失败，将继续使用默认字段定义", e);
        }
    }

    /**
     * 获取指定 Provider 的字段定义列表。
     *
     * @param providerCode Provider 编码
     * @return 数据库字段定义；若不存在则返回通用兜底字段
     */
    public List<FieldDef> getFields(String providerCode)
    {
        return fieldCache.getOrDefault(providerCode, DEFAULT_FIELDS);
    }

    /**
     * 主动刷新字段缓存。
     * <p>
     * 供配置修改后的管理流程调用，刷新逻辑与初始化加载保持一致。
     * </p>
     */
    public void refresh()
    {
        loadFields();
    }

    /**
     * 将数据库实体转换为前端可消费的字段定义。
     *
     * @param record provider_field_config 表记录
     * @return 内存缓存中使用的字段定义对象
     */
    private FieldDef toFieldDef(ProviderFieldConfigDO record)
    {
        return new FieldDef(
            record.getFieldKey(),
            record.getFieldLabel(),
            record.getFieldType(),
            record.getRequired() != null ? record.getRequired() : 0,
            record.getPlaceholder(),
            record.getDefaultValue(),
            record.getOptionsJson(),
            record.getHelpText(),
            record.getSortOrder() != null ? record.getSortOrder() : 0
        );
    }

    /**
     * 字段定义内部模型。
     * <p>
     * 控制器返回动态字段元数据时直接使用该结构进行中转，
     * 避免暴露数据库实体到接口层。
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldDef
    {
        private String key;
        private String label;
        private String type;
        private Integer required;
        private String placeholder;
        private String defaultValue;
        private String optionsJson;
        private String helpText;
        private Integer sortOrder;
    }
}
