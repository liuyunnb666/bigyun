package com.bigyun.common.security.utils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Internal call HMAC signing helper.
 */
public class InnerAuthSignUtils
{
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private InnerAuthSignUtils()
    {
    }

    public static String nonce()
    {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String sign(String secret, String timestamp, String nonce, String method, String path)
    {
        String payload = String.join("\n",
                defaultString(timestamp),
                defaultString(nonce),
                defaultString(method).toUpperCase(),
                defaultString(path));
        try
        {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(defaultString(secret).getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Inner auth signature failed", e);
        }
    }

    public static boolean equals(String left, String right)
    {
        if (left == null || right == null)
        {
            return false;
        }
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length)
        {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++)
        {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }

    private static String defaultString(String value)
    {
        return value == null ? "" : value;
    }
}
