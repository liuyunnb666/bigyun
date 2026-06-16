package com.bigyun.system.domain;

import com.bigyun.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 用户人脸识别凭据 sys_user_face。
 */
public class SysUserFace extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long faceId;

    private Long userId;

    private String providerCode;

    private String faceToken;

    private String status;

    private String livenessMode;

    private String lastLivenessBizId;

    private String lastLivenessStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastEnrollTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLivenessTime;

    public Long getFaceId()
    {
        return faceId;
    }

    public void setFaceId(Long faceId)
    {
        this.faceId = faceId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getProviderCode()
    {
        return providerCode;
    }

    public void setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
    }

    public String getFaceToken()
    {
        return faceToken;
    }

    public void setFaceToken(String faceToken)
    {
        this.faceToken = faceToken;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getLivenessMode()
    {
        return livenessMode;
    }

    public void setLivenessMode(String livenessMode)
    {
        this.livenessMode = livenessMode;
    }

    public String getLastLivenessBizId()
    {
        return lastLivenessBizId;
    }

    public void setLastLivenessBizId(String lastLivenessBizId)
    {
        this.lastLivenessBizId = lastLivenessBizId;
    }

    public String getLastLivenessStatus()
    {
        return lastLivenessStatus;
    }

    public void setLastLivenessStatus(String lastLivenessStatus)
    {
        this.lastLivenessStatus = lastLivenessStatus;
    }

    public Date getLastEnrollTime()
    {
        return lastEnrollTime;
    }

    public void setLastEnrollTime(Date lastEnrollTime)
    {
        this.lastEnrollTime = lastEnrollTime;
    }

    public Date getLastLoginTime()
    {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastLivenessTime()
    {
        return lastLivenessTime;
    }

    public void setLastLivenessTime(Date lastLivenessTime)
    {
        this.lastLivenessTime = lastLivenessTime;
    }
}
