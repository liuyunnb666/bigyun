package com.bigyun.config.domain.payload;

import java.util.Map;

public class AliyunFaceComparePayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String imageUrlA;

    private String imageUrlB;

    private String imageBase64A;

    private String imageBase64B;

    public static AliyunFaceComparePayload urls(String imageUrlA, String imageUrlB)
    {
        AliyunFaceComparePayload payload = new AliyunFaceComparePayload();
        payload.setImageUrlA(imageUrlA);
        payload.setImageUrlB(imageUrlB);
        return payload;
    }

    public String getImageUrlA()
    {
        return imageUrlA;
    }

    public void setImageUrlA(String imageUrlA)
    {
        this.imageUrlA = imageUrlA;
    }

    public String getImageUrlB()
    {
        return imageUrlB;
    }

    public void setImageUrlB(String imageUrlB)
    {
        this.imageUrlB = imageUrlB;
    }

    public String getImageBase64A()
    {
        return imageBase64A;
    }

    public void setImageBase64A(String imageBase64A)
    {
        this.imageBase64A = imageBase64A;
    }

    public String getImageBase64B()
    {
        return imageBase64B;
    }

    public void setImageBase64B(String imageBase64B)
    {
        this.imageBase64B = imageBase64B;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "imageUrlA", imageUrlA);
        putIfNotBlank(context, "imageUrlB", imageUrlB);
        putIfNotBlank(context, "imageBase64A", imageBase64A);
        putIfNotBlank(context, "imageBase64B", imageBase64B);
        return context;
    }
}
