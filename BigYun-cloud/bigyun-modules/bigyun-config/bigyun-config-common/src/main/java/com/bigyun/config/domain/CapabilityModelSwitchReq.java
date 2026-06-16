package com.bigyun.config.domain;

import java.io.Serializable;

public class CapabilityModelSwitchReq implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long targetCapabilityId;

    private Long sourceCapabilityId;

    private Boolean refreshRuntime = Boolean.TRUE;

    private String reason;

    public Long getTargetCapabilityId()
    {
        return targetCapabilityId;
    }

    public void setTargetCapabilityId(Long targetCapabilityId)
    {
        this.targetCapabilityId = targetCapabilityId;
    }

    public Long getSourceCapabilityId()
    {
        return sourceCapabilityId;
    }

    public void setSourceCapabilityId(Long sourceCapabilityId)
    {
        this.sourceCapabilityId = sourceCapabilityId;
    }

    public Boolean getRefreshRuntime()
    {
        return refreshRuntime;
    }

    public void setRefreshRuntime(Boolean refreshRuntime)
    {
        this.refreshRuntime = refreshRuntime;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
