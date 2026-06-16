package com.bigyun.common.security.aspect;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.bigyun.common.core.constant.IdempotentConstants;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.ServletUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.security.annotation.Idempotent;
import com.bigyun.common.security.utils.SecurityUtils;

/**
 * Redis based duplicate submit guard.
 */
@Aspect
@Component
public class IdempotentAspect implements Ordered
{
    private static final String KEY_PREFIX = "security:idempotent:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable
    {
        String key = buildKey(point, idempotent);
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, "1",
                idempotent.timeout(), idempotent.timeUnit());
        if (!Boolean.TRUE.equals(locked))
        {
            throw new ServiceException(idempotent.message());
        }
        try
        {
            return point.proceed();
        }
        catch (Throwable e)
        {
            stringRedisTemplate.delete(key);
            throw e;
        }
    }

    private String buildKey(ProceedingJoinPoint point, Idempotent idempotent)
    {
        HttpServletRequest request = ServletUtils.getRequest();
        String requestId = getHeader(request, IdempotentConstants.REQUEST_ID_HEADER);
        String idempotencyKey = getHeader(request, IdempotentConstants.IDEMPOTENT_TOKEN_HEADER);
        String clientKey = StringUtils.defaultIfBlank(idempotencyKey, requestId);
        if (idempotent.requiredKey() && StringUtils.isBlank(clientKey))
        {
            throw new ServiceException("请传入幂等请求标识");
        }

        Long currentUserId = SecurityUtils.getUserId();
        String userId = currentUserId == null ? "anonymous" : String.valueOf(currentUserId);
        String method = request == null ? "" : StringUtils.defaultString(request.getMethod());
        String uri = request == null ? point.getSignature().toLongString() : StringUtils.defaultString(request.getRequestURI());
        String fingerprint = StringUtils.isNotBlank(clientKey) ? clientKey : hash(Arrays.deepToString(safeArgs(point.getArgs())));
        return KEY_PREFIX + userId + ":" + method + ":" + uri + ":" + hash(fingerprint);
    }

    private String getHeader(HttpServletRequest request, String name)
    {
        return request == null ? null : request.getHeader(name);
    }

    private Object[] safeArgs(Object[] args)
    {
        if (args == null || args.length == 0)
        {
            return new Object[0];
        }
        Object[] safeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];
            safeArgs[i] = safeArg(arg);
        }
        return safeArgs;
    }

    private Object safeArg(Object arg)
    {
        if (arg instanceof MultipartFile)
        {
            MultipartFile file = (MultipartFile) arg;
            return file.getOriginalFilename() + ":" + file.getSize();
        }
        if (arg instanceof MultipartFile[])
        {
            MultipartFile[] files = (MultipartFile[]) arg;
            return Arrays.stream(files).map(this::safeArg).toArray();
        }
        return arg;
    }

    private String hash(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(StringUtils.defaultString(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Override
    public int getOrder()
    {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
