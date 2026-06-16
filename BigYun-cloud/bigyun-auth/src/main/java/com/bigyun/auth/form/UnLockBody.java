package com.bigyun.auth.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系统解锁对象
 * 
 * @author bigyun
 */
public class UnLockBody
{
    /**
     * 用户密码
     */
    @NotBlank
    @Size(max = 128)
    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
