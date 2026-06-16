package com.bigyun.config.domain.payload;

import java.util.Map;

public class FaceIdTokenPayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String faceIdApiBase;
    private String uuid;
    private String bizNo;
    private String returnUrl;
    private String notifyUrl;
    private String sceneId;
    private String procedureType;
    private String comparisonType;
    private String actionHttpMethod;

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "faceIdApiBase", faceIdApiBase);
        putIfNotBlank(context, "uuid", uuid);
        putIfNotBlank(context, "bizNo", bizNo);
        putIfNotBlank(context, "returnUrl", returnUrl);
        putIfNotBlank(context, "notifyUrl", notifyUrl);
        putIfNotBlank(context, "sceneId", sceneId);
        putIfNotBlank(context, "procedureType", procedureType);
        putIfNotBlank(context, "comparisonType", comparisonType);
        putIfNotBlank(context, "actionHttpMethod", actionHttpMethod);
        return context;
    }

    public String getFaceIdApiBase() { return faceIdApiBase; }
    public void setFaceIdApiBase(String faceIdApiBase) { this.faceIdApiBase = faceIdApiBase; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getSceneId() { return sceneId; }
    public void setSceneId(String sceneId) { this.sceneId = sceneId; }
    public String getProcedureType() { return procedureType; }
    public void setProcedureType(String procedureType) { this.procedureType = procedureType; }
    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }
    public String getActionHttpMethod() { return actionHttpMethod; }
    public void setActionHttpMethod(String actionHttpMethod) { this.actionHttpMethod = actionHttpMethod; }
}
