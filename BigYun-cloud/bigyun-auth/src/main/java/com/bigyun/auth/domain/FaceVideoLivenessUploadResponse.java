package com.bigyun.auth.domain;

public class FaceVideoLivenessUploadResponse
{
    private String videoUrl;

    private String fileUrl;

    private String objectKey;

    private String provider;

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }
}
