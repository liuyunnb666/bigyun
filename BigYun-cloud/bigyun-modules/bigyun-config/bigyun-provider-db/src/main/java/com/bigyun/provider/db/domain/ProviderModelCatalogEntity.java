package com.bigyun.provider.db.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Provider 模型目录数据库实体。
 */
@Data
@TableName("sys_provider_model_catalog")
public class ProviderModelCatalogEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "model_id", type = IdType.AUTO)
    private Long modelId;

    private String modelCode;

    private String modelName;

    private String configType;

    private String providerCode;

    private String deploymentMode;

    private String capabilityTags;

    private Integer contextWindow;

    private String inputSchemaJson;

    private String outputSchemaJson;

    private String pricingJson;

    private String docsUrl;

    private String isEnabled;

    private Integer sortOrder;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;
}
