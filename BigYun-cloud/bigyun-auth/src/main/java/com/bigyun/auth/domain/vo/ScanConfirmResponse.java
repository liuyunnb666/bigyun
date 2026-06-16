package com.bigyun.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码登录确认响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanConfirmResponse
{
    private String sid;

    private String status;

    private String grantCode;
}
