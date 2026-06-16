package com.bigyun.system.domain.form;

import lombok.Data;

/**
 * 内部远程退出登录请求。
 *
 * @author bigyun
 */
@Data
public class RemoteLogoutRequest
{
    /** 登录令牌 */
    private String token;
}
