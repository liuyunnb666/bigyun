package com.bigyun.file.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 文件信息
 * 
 * @author bigyun
 */
public class SysFile
{
    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件地址
     */
    private String url;

    /**
     * 存储 provider
     */
    private String provider;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 临时访问地址，例如 OSS 私有对象的预签名 URL。
     */
    private String temporaryUrl;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getTemporaryUrl()
    {
        return temporaryUrl;
    }

    public void setTemporaryUrl(String temporaryUrl)
    {
        this.temporaryUrl = temporaryUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("name", getName())
            .append("url", getUrl())
            .append("provider", getProvider())
            .append("objectKey", getObjectKey())
            .append("temporaryUrl", getTemporaryUrl())
            .toString();
    }
}
