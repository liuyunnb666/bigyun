package com.bigyun.provider.db.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Provider 能力模型关系数据库实体。
 */
@Data
@TableName("sys_provider_capability_model_relation")
public class ProviderCapabilityModelRelationEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    private Long capabilityId;

    private Long targetCapabilityId;

    private String capabilityCode;

    private String capabilityName;

    private String businessScene;

    private String capabilityLayer;

    private String configType;

    private String providerCode;

    private String targetCapabilityCode;

    private String targetCapabilityName;

    private String targetProviderCode;

    private String modelCode;

    private String modelName;

    private String relationType;

    private String sameBusinessFlag;

    private String relationReason;

    private String status;

    private Integer sortOrder;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;
}
