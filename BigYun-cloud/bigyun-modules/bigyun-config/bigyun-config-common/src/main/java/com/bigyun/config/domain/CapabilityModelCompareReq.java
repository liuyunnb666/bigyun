package com.bigyun.config.domain;

import java.io.Serializable;

public class CapabilityModelCompareReq implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long leftCapabilityId;

    private Long rightCapabilityId;

    private Long relationId;

    public Long getLeftCapabilityId()
    {
        return leftCapabilityId;
    }

    public void setLeftCapabilityId(Long leftCapabilityId)
    {
        this.leftCapabilityId = leftCapabilityId;
    }

    public Long getRightCapabilityId()
    {
        return rightCapabilityId;
    }

    public void setRightCapabilityId(Long rightCapabilityId)
    {
        this.rightCapabilityId = rightCapabilityId;
    }

    public Long getRelationId()
    {
        return relationId;
    }

    public void setRelationId(Long relationId)
    {
        this.relationId = relationId;
    }
}
