package com.bigyun.auth.domain;

import java.io.Serializable;

/**
 * Redis 中保存的 FaceID H5 活体会话。
 *
 * <p>auth 服务不直接连接业务数据库，活体过程中的临时状态只放 Redis，过期后自动清理。</p>
 */
public class FaceLivenessSessionCache implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String sessionId;

    private String nonce;

    private String scene;

    private String status;

    private Long userId;

    private String userName;

    private String phone;

    private String email;

    private String faceIdBizId;

    private String faceIdToken;

    private String verifyUrl;

    private String clientType;

    private String faceLoginMode;

    private String clientReturnUrl;

    private String livenessResult;

    private String errorMessage;

    private Long createdAt;

    private Long expireAt;

    private Long completedAt;

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

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

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

    public String getFaceIdBizId()
    {
        return faceIdBizId;
    }

    public void setFaceIdBizId(String faceIdBizId)
    {
        this.faceIdBizId = faceIdBizId;
    }

    public String getFaceIdToken()
    {
        return faceIdToken;
    }

    public void setFaceIdToken(String faceIdToken)
    {
        this.faceIdToken = faceIdToken;
    }

    public String getVerifyUrl()
    {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl)
    {
        this.verifyUrl = verifyUrl;
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

    public String getClientReturnUrl()
    {
        return clientReturnUrl;
    }

    public void setClientReturnUrl(String clientReturnUrl)
    {
        this.clientReturnUrl = clientReturnUrl;
    }

    public String getLivenessResult()
    {
        return livenessResult;
    }

    public void setLivenessResult(String livenessResult)
    {
        this.livenessResult = livenessResult;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Long getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt)
    {
        this.createdAt = createdAt;
    }

    public Long getExpireAt()
    {
        return expireAt;
    }

    public void setExpireAt(Long expireAt)
    {
        this.expireAt = expireAt;
    }

    public Long getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt)
    {
        this.completedAt = completedAt;
    }
}
