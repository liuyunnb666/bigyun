package com.bigyun.system.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginReq
{
    @NotBlank
    @Size(max = 16)
    private String type;

    @Size(max = 64)
    private String code;

    @Size(max = 64)
    private String userName;

    @Size(max = 128)
    private String password;

    @Size(max = 32)
    private String phone;

    @Size(max = 128)
    private String email;

    @Size(max = 64)
    private String uuid;

    @Size(max = 64)
    private String grantCode;

    @Size(max = 64)
    private String sid;

    @Size(max = 20_000_000)
    private String imageBase64;

    @Size(max = 1024)
    private String videoUrl;

    @Size(max = 512)
    private String videoObjectKey;

    private Long userId;

    @Size(max = 128)
    private String faceLivenessSessionId;

    @Size(max = 128)
    private String faceLivenessNonce;

    private Boolean testImageFallback;

    /**
     * 发起登录的客户端类型，例如 admin 或 portal。
     * 人脸登录需要该字段区分后台图片 1:1 和门户 FaceID 活体。
     */
    @Size(max = 32)
    private String clientType;

    /**
     * 人脸登录模式：FACEID_H5 为活体链路，IMAGE_COMPARE 为后台图片 1:1 链路。
     */
    @Size(max = 32)
    private String faceLoginMode;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getGrantCode()
    {
        return grantCode;
    }

    public void setGrantCode(String grantCode)
    {
        this.grantCode = grantCode;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
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

    public String getVideoObjectKey()
    {
        return videoObjectKey;
    }

    public void setVideoObjectKey(String videoObjectKey)
    {
        this.videoObjectKey = videoObjectKey;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
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
