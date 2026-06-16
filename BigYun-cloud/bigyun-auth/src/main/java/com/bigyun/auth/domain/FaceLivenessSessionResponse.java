package com.bigyun.auth.domain;

import java.io.Serializable;

/**
 * FaceID H5 活体会话创建结果。
 */
public class FaceLivenessSessionResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String sessionId;

    private String nonce;

    private String scene;

    private String status;

    private String bizId;

    private String verifyUrl;

    private Long expiresIn;

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getNonce()
    {
        return nonce;
    }

    public void setNonce(String nonce)
    {
        this.nonce = nonce;
    }

    public String getScene()
    {
        return scene;
    }

    public void setScene(String scene)
    {
        this.scene = scene;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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

    public Long getExpiresIn()
    {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn)
    {
        this.expiresIn = expiresIn;
    }
}
