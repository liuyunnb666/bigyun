package com.bigyun.auth.domain;

/**
 * 阿里云静默活体检测结果。
 *
 * <p>该对象只保存本次请求的判断结果，不保存人脸原图或 base64。</p>
 */
public class AliyunStaticLivenessResult
{
    private boolean pass;

    private String label;

    private String suggestion;

    private Double rate;

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

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    public Double getRate()
    {
        return rate;
    }

    public void setRate(Double rate)
    {
        this.rate = rate;
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
