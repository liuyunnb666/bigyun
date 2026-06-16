package com.bigyun.auth.domain;

import java.io.Serializable;

/**
 * 前端轮询活体状态的响应。
 */
public class FaceLivenessStatusResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String sessionId;

    private String scene;

    private String status;

    private String livenessResult;

    private String bizId;

    private String message;

    private Boolean canComplete;

    private Long expiresIn;

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
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

    public String getLivenessResult()
    {
        return livenessResult;
    }

    public void setLivenessResult(String livenessResult)
    {
        this.livenessResult = livenessResult;
    }

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Boolean getCanComplete()
    {
        return canComplete;
    }

    public void setCanComplete(Boolean canComplete)
    {
        this.canComplete = canComplete;
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
