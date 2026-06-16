package com.bigyun.provider.db.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Provider capability binding database entity.
 */
@Data
@TableName("sys_provider_capability")
public class ProviderCapabilityEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "capability_id", type = IdType.AUTO)
    private Long capabilityId;

    private String capabilityCode;

    private String capabilityName;

    private String businessScene;

    private String capabilityLayer;

    private String configType;

    private String providerCode;

    private String operation;

    private String modelCode;

    private Integer priority;

    private String isDefault;

    private String status;

    private String inputSchemaJson;

    private String outputSchemaJson;

    private String routingHint;

    private String fallbackCapabilityCode;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;
}
