package com.bigyun.common.core.constant;

/**
 * Idempotent request header constants.
 */
public class IdempotentConstants
{
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static final String IDEMPOTENT_TOKEN_HEADER = "Idempotency-Key";

    private IdempotentConstants()
    {
    }
}
