package com.bigyun.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询扫码登录会话状态响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanStatusResponse
{
    private String sid;

    private String status;

    private String grantCode;

    private Long expiresIn;

    private Long expireAt;
}
