// -*- coding: utf-8 -*-
package com.bigyun.provider.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 认证处理器
 *
 * 功能说明：
 * 根据配置的认证类型添加认证信息到HTTP请求头。
 * 支持多种常见的认证方式，包括Basic、Bearer、API Key、自定义签名等。
 *
 * 支持的认证类型：
 * 1. none - 无认证
 * 2. basic - HTTP Basic认证（用户名密码）
 * 3. bearer - Bearer Token认证
 * 4. apikey - API Key认证（添加到请求头）
 * 5. custom - 自定义签名认证（如阿里云OSS、腾讯云COS的签名）
 *
 * 使用场景：
 * - 为存储服务API添加签名认证
 * - 为LLM服务API添加Token认证
 * - 为任意第三方API添加认证信息
 *
 * @author BigYun
 * @date 2024-05-22
 */
@Component
public class AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationHandler.class);
    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加认证信息到请求头
     *
     * @param headers 请求头对象（会被修改）
     * @param authType 认证类型：none/basic/bearer/apikey/custom
     * @param authConfig 认证配置JSON字符串
     * @param context 变量上下文（包含accessKey、secretKey等认证凭证）
     */
    public void addAuthentication(HttpHeaders headers, String authType,
                                  String authConfig, Map<String, Object> context) {
        if (authType == null || "none".equalsIgnoreCase(authType)) {
            log.debug("无需认证");
            return;
        }

        try {
            switch (authType.toLowerCase()) {
                case "basic":
                    addBasicAuth(headers, authConfig, context);
                    break;
                case "bearer":
                    addBearerAuth(headers, authConfig, context);
                    break;
                case "apikey":
                    addApiKeyAuth(headers, authConfig, context);
                    break;
                case "custom":
                    addCustomAuth(headers, authConfig, context);
                    break;
                default:
                    log.warn("不支持的认证类型: {}", authType);
            }
        } catch (Exception e) {
            log.error("添加认证信息失败: authType={}, error={}", authType, e.getMessage(), e);
            throw new AuthenticationException("认证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加Basic认证
     * Authorization: Basic base64(username:password)
     */
    private void addBasicAuth(HttpHeaders headers, String authConfig, Map<String, Object> context) throws Exception {
        Map<String, String> config = parseAuthConfig(authConfig);

        String username = getConfigValue(config, "username", context);
        String password = getConfigValue(config, "password", context);

        if (username == null || password == null) {
            throw new AuthenticationException("Basic认证缺少用户名或密码");
        }

        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization", "Basic " + encodedCredentials);
        log.debug("已添加Basic认证");
    }

    /**
     * 添加Bearer Token认证
     * Authorization: Bearer {token}
     */
    private void addBearerAuth(HttpHeaders headers, String authConfig, Map<String, Object> context) throws Exception {
        Map<String, String> config = parseAuthConfig(authConfig);

        String token = getConfigValue(config, "token", context);
        if (token == null) {
            token = getConfigValue(config, "tokenField", context);
        }

        if (token == null) {
            throw new AuthenticationException("Bearer认证缺少token");
        }

        headers.set("Authorization", "Bearer " + token);
        log.debug("已添加Bearer认证");
    }

    /**
     * 添加API Key认证
     * 将API Key添加到指定的请求头
     */
    private void addApiKeyAuth(HttpHeaders headers, String authConfig, Map<String, Object> context) throws Exception {
        Map<String, String> config = parseAuthConfig(authConfig);

        String headerName = config.getOrDefault("header", "X-API-Key");
        String apiKey = getConfigValue(config, "value", context);

        if (apiKey == null) {
            throw new AuthenticationException("API Key认证缺少key值");
        }

        headers.set(headerName, apiKey);
        log.debug("已添加API Key认证: header={}", headerName);
    }

    /**
     * 添加自定义认证
     * 支持各种自定义签名算法
     */
    private void addCustomAuth(HttpHeaders headers, String authConfig, Map<String, Object> context) throws Exception {
        Map<String, String> config = parseAuthConfig(authConfig);

        String signatureMethod = config.get("signatureMethod");
        if (signatureMethod == null) {
            log.warn("自定义认证未指定签名方法，跳过");
            return;
        }

        // 根据不同的签名方法处理
        switch (signatureMethod.toUpperCase()) {
            case "OSS":
                addAliyunOSSAuth(headers, config, context);
                break;
            case "COS":
                addTencentCOSAuth(headers, config, context);
                break;
            case "QINIU":
                addQiniuAuth(headers, config, context);
                break;
            case "HMAC-SHA1":
            case "HMAC-SHA256":
                addHmacAuth(headers, config, context, signatureMethod);
                break;
            default:
                log.warn("不支持的签名方法: {}", signatureMethod);
        }
    }

    /**
     * 添加阿里云OSS签名认证
     */
    private void addAliyunOSSAuth(HttpHeaders headers, Map<String, String> config, Map<String, Object> context) {
        // 阿里云OSS签名逻辑
        // Authorization: OSS AccessKeyId:Signature
        // 注意：完整的OSS签名需要更复杂的逻辑，这里仅作示例
        log.debug("添加阿里云OSS签名认证");

        String accessKeyId = getValueFromContext(config.get("accessKeyIdField"), context);
        String accessKeySecret = getValueFromContext(config.get("accessKeySecretField"), context);

        if (accessKeyId != null && accessKeySecret != null) {
            // 简化版签名，实际使用时需要完整的签名算法
            context.put("accessKeyId", accessKeyId);
            context.put("signature", "PLACEHOLDER_SIGNATURE");
        }
    }

    /**
     * 添加腾讯云COS签名认证
     */
    private void addTencentCOSAuth(HttpHeaders headers, Map<String, String> config, Map<String, Object> context) {
        log.debug("添加腾讯云COS签名认证");
        // 腾讯云COS签名逻辑
        // 实际使用时需要实现完整的签名算法
    }

    /**
     * 添加七牛云签名认证
     */
    private void addQiniuAuth(HttpHeaders headers, Map<String, String> config, Map<String, Object> context) {
        log.debug("添加七牛云签名认证");
        // 七牛云签名逻辑
    }

    /**
     * 添加HMAC签名认证
     */
    private void addHmacAuth(HttpHeaders headers, Map<String, String> config,
                            Map<String, Object> context, String algorithm) throws Exception {
        String secret = getConfigValue(config, "secret", context);
        String message = getConfigValue(config, "message", context);

        if (secret == null || message == null) {
            throw new AuthenticationException("HMAC认证缺少secret或message");
        }

        String signature = calculateHmac(message, secret, algorithm);
        String headerName = config.getOrDefault("header", "X-Signature");
        headers.set(headerName, signature);

        log.debug("已添加HMAC签名认证: algorithm={}", algorithm);
    }

    /**
     * 计算HMAC签名
     */
    private String calculateHmac(String message, String secret, String algorithm) throws Exception {
        String javaAlgorithm = algorithm.replace("-", "");
        Mac mac = Mac.getInstance(javaAlgorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), javaAlgorithm);
        mac.init(secretKeySpec);

        byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    /**
     * 解析认证配置JSON
     */
    private Map<String, String> parseAuthConfig(String authConfig) throws Exception {
        if (authConfig == null || authConfig.trim().isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(authConfig, Map.class);
    }

    /**
     * 获取配置值
     * 支持直接值和从上下文中获取
     */
    private String getConfigValue(Map<String, String> config, String key, Map<String, Object> context) {
        String value = config.get(key);
        if (value == null) {
            return null;
        }

        // 如果值本身就是 ${key}，保持原来的直接取上下文行为
        Matcher exactMatcher = TEMPLATE_VARIABLE_PATTERN.matcher(value);
        if (exactMatcher.matches()) {
            return getValueFromContext(exactMatcher.group(1), context);
        }

        return renderTemplateValue(value, context);
    }

    private String renderTemplateValue(String value, Map<String, Object> context) {
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(value);
        StringBuffer rendered = new StringBuffer();
        boolean hasVariable = false;
        while (matcher.find()) {
            hasVariable = true;
            String contextValue = getValueFromContext(matcher.group(1), context);
            if (contextValue == null) {
                return null;
            }
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(contextValue));
        }
        if (!hasVariable) {
            return value;
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    /**
     * 从上下文中获取值
     */
    private String getValueFromContext(String key, Map<String, Object> context) {
        if (key == null || context == null) {
            return null;
        }

        Object value = context.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 认证异常
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
