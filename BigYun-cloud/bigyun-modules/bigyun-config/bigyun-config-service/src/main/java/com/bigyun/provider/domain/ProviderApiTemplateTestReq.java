package com.bigyun.provider.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ProviderApiTemplateTestReq implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull(message = "Template must not be null.")
    private ProviderApiTemplate template;

    private Map<String, Object> testContext = new HashMap<>();

    public ProviderApiTemplate getTemplate()
    {
        return template;
    }

    public void setTemplate(ProviderApiTemplate template)
    {
        this.template = template;
    }

    public Map<String, Object> getTestContext()
    {
        return testContext;
    }

    public void setTestContext(Map<String, Object> testContext)
    {
        this.testContext = testContext == null ? new HashMap<>() : testContext;
    }
}
