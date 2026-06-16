package com.bigyun.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建扫码登录会话响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanCreateResponse
{
    private String sid;

    private String loginCode;

    private String status;

    private Long expiresIn;

    private Long expireAt;
}
