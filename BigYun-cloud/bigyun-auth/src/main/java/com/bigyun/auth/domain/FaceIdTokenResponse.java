package com.bigyun.auth.domain;

import java.io.Serializable;

/**
 * FaceID H5 GetToken 的业务响应。
 */
public class FaceIdTokenResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String token;

    private String bizId;

    private String verifyUrl;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getVerifyUrl()
    {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl)
    {
        this.verifyUrl = verifyUrl;
    }
}
