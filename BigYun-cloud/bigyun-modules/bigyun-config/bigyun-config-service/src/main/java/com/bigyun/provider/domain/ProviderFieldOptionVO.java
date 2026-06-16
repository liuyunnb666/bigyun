package com.bigyun.provider.domain;

import java.io.Serializable;

/**
 * Provider 字段候选项视图对象。
 * <p>
 * 主要用于把 provider_field_config.options_json 中维护的模型、端点等候选项返回给前端。
 * 前端只读取这里的 label/value/helpText，不再在 Vue 页面里硬编码模型列表。
 * </p>
 */
public class ProviderFieldOptionVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 前端展示文案，例如“DeepSeek V4 Flash（推荐）”。 */
    private String label;

    /** 实际保存值，例如“deepseek-v4-flash”。 */
    private String value;

    /** 选项级提示文案，例如旧模型废弃时间或端点适用区域。 */
    private String helpText;

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getHelpText()
    {
        return helpText;
    }

    public void setHelpText(String helpText)
    {
        this.helpText = helpText;
    }
}
