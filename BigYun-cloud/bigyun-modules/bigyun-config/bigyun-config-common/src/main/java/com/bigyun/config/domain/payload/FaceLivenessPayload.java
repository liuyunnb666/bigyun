package com.bigyun.config.domain.payload;

import java.util.Map;

public class FaceLivenessPayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String imageBase64;

    private String videoUrl;

    public static FaceLivenessPayload staticImage(String imageBase64)
    {
        FaceLivenessPayload payload = new FaceLivenessPayload();
        payload.setImageBase64(imageBase64);
        return payload;
    }

    public static FaceLivenessPayload video(String videoUrl)
    {
        FaceLivenessPayload payload = new FaceLivenessPayload();
        payload.setVideoUrl(videoUrl);
        return payload;
    }

    public String getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "imageBase64", imageBase64);
        putIfNotBlank(context, "videoUrl", videoUrl);
        return context;
    }
}
