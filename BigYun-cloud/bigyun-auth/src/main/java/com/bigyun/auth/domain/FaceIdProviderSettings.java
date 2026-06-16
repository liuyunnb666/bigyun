package com.bigyun.auth.domain;

import java.io.Serializable;

/**
 * FaceID H5 活体相关配置。
 *
 * <p>这些字段来自 Face++ Provider 的 extParamsJson，不写死在代码里，便于本地、测试和公网
 * 验收环境分别配置不同的回调地址。</p>
 */
public class FaceIdProviderSettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String apiBase;

    private String doUrl;

    private String notifyUrl;

    private String returnUrl;

    private String sceneId;

    private String procedureType;

    private String comparisonType;

    private String actionHttpMethod;

    private boolean allowTestImageFallback;

    private boolean allowAdminImageLogin;

    public String getApiBase()
    {
        return apiBase;
    }

    public void setApiBase(String apiBase)
    {
        this.apiBase = apiBase;
    }

    public String getDoUrl()
    {
        return doUrl;
    }

    public void setDoUrl(String doUrl)
    {
        this.doUrl = doUrl;
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
    }

    public String getSceneId()
    {
        return sceneId;
    }

    public void setSceneId(String sceneId)
    {
        this.sceneId = sceneId;
    }

    public String getProcedureType()
    {
        return procedureType;
    }

    public void setProcedureType(String procedureType)
    {
        this.procedureType = procedureType;
    }

    public String getComparisonType()
    {
        return comparisonType;
    }

    public void setComparisonType(String comparisonType)
    {
        this.comparisonType = comparisonType;
    }

    public String getActionHttpMethod()
    {
        return actionHttpMethod;
    }

    public void setActionHttpMethod(String actionHttpMethod)
    {
        this.actionHttpMethod = actionHttpMethod;
    }

    public boolean isAllowTestImageFallback()
    {
        return allowTestImageFallback;
    }

    public void setAllowTestImageFallback(boolean allowTestImageFallback)
    {
        this.allowTestImageFallback = allowTestImageFallback;
    }

    public boolean isAllowAdminImageLogin()
    {
        return allowAdminImageLogin;
    }

    public void setAllowAdminImageLogin(boolean allowAdminImageLogin)
    {
        this.allowAdminImageLogin = allowAdminImageLogin;
    }
}
