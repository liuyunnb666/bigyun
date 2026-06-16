package com.bigyun.config.domain.payload;

import java.util.Map;

public class FaceComparePayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String faceToken1;

    private String faceToken2;

    public FaceComparePayload()
    {
    }

    public FaceComparePayload(String faceToken1, String faceToken2)
    {
        this.faceToken1 = faceToken1;
        this.faceToken2 = faceToken2;
    }

    public static FaceComparePayload of(String faceToken1, String faceToken2)
    {
        return new FaceComparePayload(faceToken1, faceToken2);
    }

    public String getFaceToken1()
    {
        return faceToken1;
    }

    public void setFaceToken1(String faceToken1)
    {
        this.faceToken1 = faceToken1;
    }

    public String getFaceToken2()
    {
        return faceToken2;
    }

    public void setFaceToken2(String faceToken2)
    {
        this.faceToken2 = faceToken2;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "face_token1", faceToken1);
        putIfNotBlank(context, "face_token2", faceToken2);
        return context;
    }
}
