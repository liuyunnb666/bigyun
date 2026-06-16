package com.bigyun.auth.domain;

public class AliyunVideoLivenessResult
{
    private boolean pass;

    private Double liveConfidence;

    private Double faceConfidence;

    private String requestId;

    private String message;

    public boolean isPass()
    {
        return pass;
    }

    public void setPass(boolean pass)
    {
        this.pass = pass;
    }

    public Double getLiveConfidence()
    {
        return liveConfidence;
    }

    public void setLiveConfidence(Double liveConfidence)
    {
        this.liveConfidence = liveConfidence;
    }

    public Double getFaceConfidence()
    {
        return faceConfidence;
    }

    public void setFaceConfidence(Double faceConfidence)
    {
        this.faceConfidence = faceConfidence;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
