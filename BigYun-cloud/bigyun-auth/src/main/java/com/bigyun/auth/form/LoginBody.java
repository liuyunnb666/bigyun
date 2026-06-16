package com.bigyun.auth.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录对象
 *
 * @author bigyun
 */
public class LoginBody
{
    /**
     * 用户名
     */
    @NotBlank
    @Size(max = 64)
    private String username;

    /**
     * 用户密码
     */
    @NotBlank
    @Size(max = 128)
    private String password;

    /**
     * 验证码
     */
    @Size(max = 64)
    private String code;

    /**
     * 唯一标识
     */
    @Size(max = 64)
    private String uuid;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
