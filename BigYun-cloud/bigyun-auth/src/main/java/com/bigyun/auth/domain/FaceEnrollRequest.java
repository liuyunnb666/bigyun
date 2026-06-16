package com.bigyun.auth.domain;

import jakarta.validation.constraints.Size;

/**
 * 人脸录入请求。
 */
public class FaceEnrollRequest
{
    @Size(max = 20_000_000)
    private String imageBase64;

    @Size(max = 1024)
    private String videoUrl;

    @Size(max = 512)
    private String videoObjectKey;

    @Size(max = 128)
    private String faceLivenessSessionId;

    @Size(max = 128)
    private String faceLivenessNonce;

    private Boolean testImageFallback;

    /**
     * 发起录入的客户端类型，例如 admin 或 portal。
     */
    @Size(max = 32)
    private String clientType;

    /**
     * 人脸录入模式：FACEID_H5 为活体录入，IMAGE_COMPARE 为后台图片 1:1 录入。
     */
    @Size(max = 32)
    private String faceLoginMode;

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

    public String getVideoObjectKey()
    {
        return videoObjectKey;
    }

    public void setVideoObjectKey(String videoObjectKey)
    {
        this.videoObjectKey = videoObjectKey;
    }

    public String getFaceLivenessSessionId()
    {
        return faceLivenessSessionId;
    }

    public void setFaceLivenessSessionId(String faceLivenessSessionId)
    {
        this.faceLivenessSessionId = faceLivenessSessionId;
    }

    public String getFaceLivenessNonce()
    {
        return faceLivenessNonce;
    }

    public void setFaceLivenessNonce(String faceLivenessNonce)
    {
        this.faceLivenessNonce = faceLivenessNonce;
    }

    public Boolean getTestImageFallback()
    {
        return testImageFallback;
    }

    public void setTestImageFallback(Boolean testImageFallback)
    {
        this.testImageFallback = testImageFallback;
    }

    public String getClientType()
    {
        return clientType;
    }

    public void setClientType(String clientType)
    {
        this.clientType = clientType;
    }

    public String getFaceLoginMode()
    {
        return faceLoginMode;
    }

    public void setFaceLoginMode(String faceLoginMode)
    {
        this.faceLoginMode = faceLoginMode;
    }
}
