package com.bigyun.provider.core.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * Provider runtime API template.
 * <p>
 * This model is deliberately free of MyBatis annotations so business modules can
 * consume Redis snapshots without pulling database dependencies into provider-core.
 * </p>
 */
@Data
public class ProviderApiTemplateRuntime implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long templateId;

    private String configType;

    private String providerCode;

    private String operation;

    private String httpMethod;

    private String urlTemplate;

    private String headersJson;

    private String bodyType;

    private String bodyTemplate;

    private String responseType;

    private String responseMapping;

    private String authType;

    private String authConfigJson;

    private Integer timeout;

    private Integer retryTimes;

    private String isEnabled;
}
