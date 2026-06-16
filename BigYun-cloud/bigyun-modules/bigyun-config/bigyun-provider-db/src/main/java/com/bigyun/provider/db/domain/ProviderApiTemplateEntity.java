package com.bigyun.provider.db.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * Provider API template database entity.
 */
@Data
@TableName("sys_provider_api_template")
public class ProviderApiTemplateEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.AUTO)
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
