package com.bigyun.auth.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码登录一次性授权码 Redis 载荷。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanGrantPayload implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 扫码会话 ID，用于和 Web 端提交的 sid 绑定校验。 */
    private String sid;

    /** 当前确认登录的真实用户 ID，由后端 token 解析得到。 */
    private Long userId;

    /** 一次性授权码，只能被登录策略消费一次。 */
    private String grantCode;
}
