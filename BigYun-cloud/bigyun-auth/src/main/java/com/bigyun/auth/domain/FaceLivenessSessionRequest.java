package com.bigyun.auth.domain;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 创建 FaceID H5 活体会话的请求。
 */
public class FaceLivenessSessionRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Size(max = 64)
    private String userName;

    @Size(max = 32)
    private String phone;

    @Size(max = 128)
    private String email;

    @Size(max = 32)
    private String clientType;

    @Size(max = 32)
    private String faceLoginMode;

    @Size(max = 1024)
    private String returnUrl;

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
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

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
    }
}
