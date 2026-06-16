package com.bigyun.auth.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码登录确认请求。
 *
 * <p>确认 Web 登录时只允许提交 sid 和 loginCode。真实用户身份必须由后端从当前
 * Authorization token 中读取，不能信任请求体里的用户字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanConfirmRequest
{
    /** Web 登录页创建的扫码会话 ID。 */
    @NotBlank
    @Size(max = 64)
    private String sid;

    /** 二维码短码，用于和 sid 做二次匹配校验。 */
    @NotBlank
    @Size(max = 32)
    private String loginCode;
}
