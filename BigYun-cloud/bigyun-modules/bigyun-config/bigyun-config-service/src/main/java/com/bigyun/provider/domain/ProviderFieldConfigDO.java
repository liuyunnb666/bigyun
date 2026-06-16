package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Provider 动态字段配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("provider_field_config")
public class ProviderFieldConfigDO {

    @TableId
    private Long id;

    @TableField("provider_code")
    private String providerCode;

    @TableField("field_key")
    private String fieldKey;

    @TableField("field_label")
    private String fieldLabel;

    @TableField("field_type")
    private String fieldType;

    @TableField("required")
    private Integer required;

    @TableField("placeholder")
    private String placeholder;

    @TableField("default_value")
    private String defaultValue;

    @TableField("options_json")
    private String optionsJson;

    @TableField("help_text")
    private String helpText;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
