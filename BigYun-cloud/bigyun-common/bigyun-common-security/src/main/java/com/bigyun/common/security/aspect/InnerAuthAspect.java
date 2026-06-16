package com.bigyun.common.security.aspect;

import java.time.Duration;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.bigyun.common.core.constant.InnerAuthConstants;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.exception.InnerAuthException;
import com.bigyun.common.core.utils.ServletUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.security.annotation.InnerAuth;
import com.bigyun.common.security.utils.InnerAuthSignUtils;

/**
 * Internal service call authorization.
 */
@Aspect
@Component
public class InnerAuthAspect implements Ordered
{
    private static final String NONCE_KEY_PREFIX = "security:inner-auth:nonce:";

    @Value("${security.inner-auth.strict:false}")
    private boolean strict;

    @Value("${security.inner-auth.secret:bigyun-inner-auth}")
    private String innerAuthSecret;

    @Value("${security.inner-auth.allowed-clock-skew-seconds:300}")
    private long allowedClockSkewSeconds;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(innerAuth)")
    public Object innerAround(ProceedingJoinPoint point, InnerAuth innerAuth) throws Throwable
    {
        HttpServletRequest request = ServletUtils.getRequest();
        String source = request.getHeader(SecurityConstants.FROM_SOURCE);
        if (!StringUtils.equals(SecurityConstants.INNER, source))
        {
            throw new InnerAuthException("没有内部访问权限，不允许访问");
        }
        if (strict)
        {
            checkSignature(request);
        }

        String userid = request.getHeader(SecurityConstants.DETAILS_USER_ID);
        String username = request.getHeader(SecurityConstants.DETAILS_USERNAME);
        if (innerAuth.isUser() && (StringUtils.isEmpty(userid) || StringUtils.isEmpty(username)))
        {
            throw new InnerAuthException("没有设置用户信息，不允许访问");
        }
        return point.proceed();
    }

    private void checkSignature(HttpServletRequest request)
    {
        String timestamp = request.getHeader(InnerAuthConstants.TIMESTAMP_HEADER);
        String nonce = request.getHeader(InnerAuthConstants.NONCE_HEADER);
        String signature = request.getHeader(InnerAuthConstants.SIGNATURE_HEADER);
        if (StringUtils.isAnyBlank(timestamp, nonce, signature))
        {
            throw new InnerAuthException("内部访问签名缺失，不允许访问");
        }
        long timestampMillis;
        try
        {
            timestampMillis = Long.parseLong(timestamp);
        }
        catch (NumberFormatException e)
        {
            throw new InnerAuthException("内部访问时间戳非法，不允许访问");
        }
        long skewMillis = Math.abs(System.currentTimeMillis() - timestampMillis);
        if (skewMillis > Duration.ofSeconds(allowedClockSkewSeconds).toMillis())
        {
            throw new InnerAuthException("内部访问时间戳已过期，不允许访问");
        }
        String nonceKey = NONCE_KEY_PREFIX + nonce;
        Boolean firstSeen = stringRedisTemplate.opsForValue().setIfAbsent(nonceKey, "1",
                Duration.ofSeconds(allowedClockSkewSeconds));
        if (!Boolean.TRUE.equals(firstSeen))
        {
            throw new InnerAuthException("内部访问随机数重复，不允许访问");
        }
        String expected = InnerAuthSignUtils.sign(innerAuthSecret, timestamp, nonce,
                request.getMethod(), request.getRequestURI());
        if (!InnerAuthSignUtils.equals(expected, signature))
        {
            throw new InnerAuthException("内部访问签名非法，不允许访问");
        }
    }

    @Override
    public int getOrder()
    {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
