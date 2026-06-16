package com.bigyun.provider.domain;

import com.bigyun.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provider 能力绑定管理端对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderCapability extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 能力绑定主键。 */
    private Long capabilityId;

    /** 业务能力编码，如 ai-guidance-llm。 */
    private String capabilityCode;

    /** 能力名称。 */
    private String capabilityName;

    /** 管理端候选显示名，用于区分同一能力下的不同 Provider / 模型，不落库。 */
    private String candidateDisplayName;

    /** 业务场景，如 ai_guidance。 */
    private String businessScene;

    /** 能力层级，如 extract、verify、understand、generate。 */
    private String capabilityLayer;

    /** Provider 配置类型，如 llm、ocr、vision、face。 */
    private String configType;

    /** Provider 编码。 */
    private String providerCode;

    /** Provider 操作编码，如 chat。 */
    private String operation;

    /** 默认模型编码。 */
    private String modelCode;

    /** 优先级，数值越小优先级越高。 */
    private Integer priority;

    /** 是否当前默认绑定，1 是，0 否。 */
    private String isDefault;

    /** 状态：0 启用，1 停用。 */
    private String status;

    /** 入参结构说明 JSON。 */
    private String inputSchemaJson;

    /** 出参结构说明 JSON。 */
    private String outputSchemaJson;

    /** 业务路由建议，帮助后台理解该能力对应的业务用途。 */
    private String routingHint;

    /** 降级能力编码，当前为空时表示由业务侧 fallback 处理。 */
    private String fallbackCapabilityCode;
}
