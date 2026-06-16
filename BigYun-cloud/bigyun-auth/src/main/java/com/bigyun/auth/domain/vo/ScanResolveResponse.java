package com.bigyun.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码登录会话解析响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanResolveResponse
{
    private String sid;

    private String status;

    private Long expireAt;
}
