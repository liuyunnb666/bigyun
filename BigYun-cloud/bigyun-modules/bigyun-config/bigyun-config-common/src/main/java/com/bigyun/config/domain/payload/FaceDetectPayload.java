package com.bigyun.config.domain.payload;

import java.util.Map;

public class FaceDetectPayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String imageBase64;

    public FaceDetectPayload()
    {
    }

    public FaceDetectPayload(String imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    public static FaceDetectPayload of(String imageBase64)
    {
        return new FaceDetectPayload(imageBase64);
    }

    public String getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "imageBase64", imageBase64);
        return context;
    }
}
