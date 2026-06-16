package com.bigyun.config.domain.payload;

import java.util.Map;

public class FaceIdResultPayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String faceIdApiBase;

    private String bizId;

    public FaceIdResultPayload()
    {
    }

    public FaceIdResultPayload(String faceIdApiBase, String bizId)
    {
        this.faceIdApiBase = faceIdApiBase;
        this.bizId = bizId;
    }

    public static FaceIdResultPayload of(String faceIdApiBase, String bizId)
    {
        return new FaceIdResultPayload(faceIdApiBase, bizId);
    }

    public String getFaceIdApiBase()
    {
        return faceIdApiBase;
    }

    public void setFaceIdApiBase(String faceIdApiBase)
    {
        this.faceIdApiBase = faceIdApiBase;
    }

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "faceIdApiBase", faceIdApiBase);
        putIfNotBlank(context, "bizId", bizId);
        return context;
    }
}
