package com.bigyun.auth.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 人脸录入状态。
 */
public class FaceStatusResponse
{
    private Boolean hasFace;

    private String providerCode;

    private String statusLabel;

    private String livenessMode;

    private String lastLivenessBizId;

    private String lastLivenessStatus;

    private String warning;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastEnrollTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLivenessTime;

    public Boolean getHasFace()
    {
        return hasFace;
    }

    public void setHasFace(Boolean hasFace)
    {
        this.hasFace = hasFace;
    }

    public String getProviderCode()
    {
        return providerCode;
    }

    public void setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
    }

    public String getStatusLabel()
    {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel)
    {
        this.statusLabel = statusLabel;
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

    public String getWarning()
    {
        return warning;
    }

    public void setWarning(String warning)
    {
        this.warning = warning;
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
