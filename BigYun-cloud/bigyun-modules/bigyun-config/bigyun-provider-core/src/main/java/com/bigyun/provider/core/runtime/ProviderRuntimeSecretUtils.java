package com.bigyun.provider.core.runtime;

import com.bigyun.common.core.utils.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider 运行时脱敏工具。
 */
public final class ProviderRuntimeSecretUtils
{
    private static final Pattern SENSITIVE_PAIR_PATTERN = Pattern.compile(
            "(?i)(api[_-]?key|access[_-]?key|secret[_-]?key|api_secret|api_key|token|authorization)"
                    + "([\"'\\s:=]+)([^\\s,\"'&}]+)");

    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)(bearer\\s+)([^\\s,\"'}]+)");

    private ProviderRuntimeSecretUtils()
    {
    }

    public static String mask(String text)
    {
        if (StringUtils.isBlank(text))
        {
            return text;
        }
        String masked = replace(SENSITIVE_PAIR_PATTERN, text);
        return replace(BEARER_PATTERN, masked);
    }

    private static String replace(Pattern pattern, String text)
    {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find())
        {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + "******"));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
