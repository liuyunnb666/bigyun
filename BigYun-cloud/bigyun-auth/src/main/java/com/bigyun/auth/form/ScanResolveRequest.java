package com.bigyun.auth.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码登录会话解析请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanResolveRequest
{
    /** Web 登录二维码中的短码。 */
    @NotBlank
    @Size(max = 32)
    private String loginCode;

    /** 可选字段，兼容前端同时提交 sid 的场景。 */
    @Size(max = 64)
    private String sid;
}
