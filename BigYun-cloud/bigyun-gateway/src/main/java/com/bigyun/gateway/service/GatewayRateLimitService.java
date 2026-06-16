package com.bigyun.gateway.service;

import com.bigyun.common.core.exception.CaptchaException;
import com.bigyun.common.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class GatewayRateLimitService
{
    private static final Logger LOG = LoggerFactory.getLogger(GatewayRateLimitService.class);
    private static final String MESSAGE_TOO_FREQUENT = "操作过于频繁，请稍后再试";
    private static final String CAPTCHA_KEY_PREFIX = "gateway:rl:captcha:";
    private static final int CAPTCHA_LIMIT = 5;
    private static final long CAPTCHA_WINDOW_SECONDS = 60L;

    @Autowired
    private RedisService redisService;

    public void checkCaptcha(ServerRequest request) throws CaptchaException
    {
        String ip = resolveIp(request);
        String key = CAPTCHA_KEY_PREFIX + ip;
        try
        {
            Long count = redisService.redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L)
            {
                redisService.expire(key, CAPTCHA_WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count != null && count > CAPTCHA_LIMIT)
            {
                LOG.warn("Captcha rate limited, ip={}", ip);
                throw new CaptchaException(MESSAGE_TOO_FREQUENT);
            }
        }
        catch (CaptchaException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            LOG.warn("Captcha rate limit failed, ip={}", ip, e);
            throw new CaptchaException(MESSAGE_TOO_FREQUENT);
        }
    }

    private String resolveIp(ServerRequest request)
    {
        if (request == null)
        {
            return "unknown";
        }
        String ip = firstNotBlank(
                request.headers().firstHeader("x-forwarded-for"),
                request.headers().firstHeader("X-Forwarded-For"),
                request.headers().firstHeader("X-Real-IP"),
                request.headers().firstHeader("Proxy-Client-IP"));
        if (isNotBlank(ip))
        {
            return normalizeIp(ip);
        }
        Optional<InetSocketAddress> remoteAddress = request.remoteAddress();
        return remoteAddress.map(address -> normalizeIp(address.getAddress() == null
                ? address.getHostString()
                : address.getAddress().getHostAddress())).orElse("unknown");
    }

    private String normalizeIp(String ip)
    {
        String value = ip;
        int commaIndex = value.indexOf(',');
        if (commaIndex > -1)
        {
            value = value.substring(0, commaIndex);
        }
        value = value.trim();
        if ("0:0:0:0:0:0:0:1".equals(value))
        {
            return "127.0.0.1";
        }
        return value.length() > 128 ? value.substring(0, 128) : value;
    }

    private String firstNotBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String value : values)
        {
            if (isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private boolean isNotBlank(String value)
    {
        return value != null && value.trim().length() > 0 && !"unknown".equalsIgnoreCase(value.trim());
    }
}
