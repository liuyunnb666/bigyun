package com.bigyun.provider.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider 动态字段视图对象。
 * <p>
 * 字段主体来自 provider_field_config 表，options 来自 options_json 解析结果。
 * 该对象是前端配置表单的唯一字段元数据来源，保证“提示、候选项、默认值”和后端保存逻辑同源。
 * </p>
 */
public class ProviderFieldVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 字段键名，例如 endpoint、apiKey、model。 */
    private String key;

    /** 字段中文标签。 */
    private String label;

    /** 前端渲染类型，例如 text、password、number、select。 */
    private String type;

    /** 是否必填，true 表示前端和后端都应校验。 */
    private Boolean required;

    /** 输入框占位提示。 */
    private String placeholder;

    /** 默认值，新建配置时用于自动填充。 */
    private String defaultValue;

    /** 字段级帮助说明。 */
    private String helpText;

    /** 排序值，值越小越靠前。 */
    private Integer sortOrder;

    /** 候选项列表，由 options_json 解析得到；解析失败时为空列表。 */
    private List<ProviderFieldOptionVO> options = new ArrayList<>();

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Boolean getRequired()
    {
        return required;
    }

    public void setRequired(Boolean required)
    {
        this.required = required;
    }

    public String getPlaceholder()
    {
        return placeholder;
    }

    public void setPlaceholder(String placeholder)
    {
        this.placeholder = placeholder;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getHelpText()
    {
        return helpText;
    }

    public void setHelpText(String helpText)
    {
        this.helpText = helpText;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public List<ProviderFieldOptionVO> getOptions()
    {
        return options;
    }

    public void setOptions(List<ProviderFieldOptionVO> options)
    {
        this.options = options == null ? new ArrayList<ProviderFieldOptionVO>() : options;
    }
}
