package com.bigyun.auth.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码登录会话 Redis 缓存对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanSessionCache implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 扫码会话 ID。 */
    private String sid;

    /** 二维码短码，用于扫码端反查会话。 */
    private String loginCode;

    /** 当前会话状态：CREATED、RESOLVED、CONFIRMED。 */
    private String status;

    /** 创建时间，毫秒时间戳。 */
    private Long createdAt;

    /** 过期时间，毫秒时间戳。 */
    private Long expireAt;

    /** 确认扫码登录的用户 ID，由后端 token 解析得到。 */
    private Long confirmedUserId;

    /** 一次性登录授权码，Web 端轮询到后用它换取登录 token。 */
    private String grantCode;

    /** 确认时间，毫秒时间戳。 */
    private Long confirmedAt;
}
