package com.bigyun.auth.domain;

import java.io.Serializable;

/**
 * FaceID H5 GetResult 的业务响应。
 */
public class FaceIdResultResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String bizId;

    private String bizNo;

    private String result;

    private String livenessResult;

    private String imageBest;

    private String errorMessage;

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getBizNo()
    {
        return bizNo;
    }

    public void setBizNo(String bizNo)
    {
        this.bizNo = bizNo;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getLivenessResult()
    {
        return livenessResult;
    }

    public void setLivenessResult(String livenessResult)
    {
        this.livenessResult = livenessResult;
    }

    public String getImageBest()
    {
        return imageBest;
    }

    public void setImageBest(String imageBest)
    {
        this.imageBest = imageBest;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
