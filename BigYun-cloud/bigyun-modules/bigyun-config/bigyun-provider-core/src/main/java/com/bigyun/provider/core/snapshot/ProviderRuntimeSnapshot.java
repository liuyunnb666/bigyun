package com.bigyun.provider.core.snapshot;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.provider.core.domain.ProviderApiTemplateRuntime;
import java.io.Serializable;
import lombok.Data;

/**
 * Provider 运行时快照。
 * <p>
 * 快照包含一次运行所需的能力绑定、Provider 配置和 API 模板。
 * 其中可能带有运行时必需的敏感字段，因此只允许进入 Redis 运行时缓存，
 * 不允许原样输出到日志、接口响应或页面。
 */
@Data
public class ProviderRuntimeSnapshot implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 业务能力编码，例如 ai-guidance-llm。直接按 Provider 发布时可以为空。 */
    private String capabilityCode;

    /** Provider 配置类型，例如 llm、ocr、vision。 */
    private String configType;

    /** Provider 编码，例如 bigyun-demo-llm。 */
    private String providerCode;

    /** Provider 操作编码，例如 chat、recognize。 */
    private String operation;

    /** 能力默认模型编码，可以为空。 */
    private String modelCode;

    /** 运行时 Provider 配置。 */
    private ProviderConfigDTO providerConfig;

    /** 运行时 API 模板。 */
    private ProviderApiTemplateRuntime apiTemplate;

    /** 发布版本号，用于判断 Redis 快照是否已经刷新。 */
    private String version;

    /** 发布时间戳，单位毫秒。 */
    private Long publishedAt;
}
