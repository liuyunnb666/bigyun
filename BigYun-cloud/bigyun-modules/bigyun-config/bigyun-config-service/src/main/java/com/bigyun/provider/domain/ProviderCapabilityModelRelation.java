package com.bigyun.provider.domain;

import com.bigyun.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderCapabilityModelRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

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
}
