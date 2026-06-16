package com.bigyun.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Prevents duplicate write submissions by locking a request fingerprint in Redis.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent
{
    /**
     * Lock timeout.
     */
    int timeout() default 10;

    /**
     * Timeout unit.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Error message returned for duplicate submissions.
     */
    String message() default "请求正在处理中，请勿重复提交";

    /**
     * Whether the client must provide X-Request-Id or Idempotency-Key.
     */
    boolean requiredKey() default false;
}
