// -*- coding: utf-8 -*-
package com.bigyun.provider.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * 通用HTTP客户端
 *
 * 功能说明：
 * 封装RestTemplate，提供统一的HTTP请求接口，支持各种HTTP方法和请求类型。
 * 用于执行配置驱动的API调用，无需为每个Provider编写专门的HTTP客户端代码。
 *
 * 核心特性：
 * 1. 支持GET/POST/PUT/DELETE/PATCH等HTTP方法
 * 2. 支持自定义请求头
 * 3. 支持多种请求体类型（JSON、表单、二进制等）
 * 4. 支持超时配置
 * 5. 支持重试机制
 * 6. 统一的错误处理和日志记录
 *
 * 使用场景：
 * - 调用第三方存储服务API（阿里云OSS、腾讯云COS等）
 * - 调用第三方LLM服务API（OpenAI、百度文心等）
 * - 调用任意RESTful API
 *
 * @author BigYun
 * @date 2024-05-22
 */
@Component
public class GenericHttpClient {

    private static final Logger log = LoggerFactory.getLogger(GenericHttpClient.class);

    /**
     * 执行HTTP请求
     *
     * @param method HTTP方法（GET/POST/PUT/DELETE/PATCH）
     * @param url 请求URL（完整的URL，包含协议、域名、路径、参数）
     * @param headers 请求头（可为null）
     * @param body 请求体（可为null，GET请求通常为null）
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     * @throws HttpClientException 请求失败时抛出
     */
    public String execute(HttpMethod method, String url, HttpHeaders headers, Object body, int timeout) {
        return execute(method, url, headers, body, timeout, 0);
    }

    /**
     * 执行HTTP请求（支持重试）
     *
     * @param method HTTP方法
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体
     * @param timeout 超时时间（毫秒）
     * @param retryTimes 重试次数（0表示不重试）
     * @return 响应字符串
     * @throws HttpClientException 请求失败时抛出
     */
    public String execute(HttpMethod method, String url, HttpHeaders headers,
                         Object body, int timeout, int retryTimes) {
        int attempts = 0;
        int maxAttempts = retryTimes + 1; // 总尝试次数 = 初始请求 + 重试次数
        Exception lastException = null;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                log.info("执行HTTP请求: method={}, url={}, attempt={}/{}",
                        method, url, attempts, maxAttempts);

                // 创建RestTemplate（每次请求创建新实例以支持不同的超时配置）
                RestTemplate restTemplate = createRestTemplate(timeout);

                // 构建请求实体
                HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

                // 执行请求
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        method,
                        requestEntity,
                        String.class
                );

                // 检查响应状态
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("HTTP请求成功: status={}, url={}", response.getStatusCode(), url);
                    return response.getBody();
                } else {
                    log.warn("HTTP请求返回非2xx状态: status={}, url={}", response.getStatusCode(), url);
                    throw new HttpClientException("HTTP请求失败: " + response.getStatusCode());
                }

            } catch (HttpStatusCodeException e) {
                lastException = e;
                log.error("HTTP请求失败: method={}, url={}, attempt={}/{}, status={}, response={}",
                        method, url, attempts, maxAttempts, e.getStatusCode(), responseBodyOf(e));

                // 如果还有重试机会，等待后重试
                if (attempts < maxAttempts) {
                    try {
                        long waitTime = calculateRetryWaitTime(attempts);
                        log.info("等待{}ms后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new HttpClientException("重试被中断", ie);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.error("HTTP请求失败: method={}, url={}, attempt={}/{}, error={}",
                        method, url, attempts, maxAttempts, e.getMessage());

                // 如果还有重试机会，等待后重试
                if (attempts < maxAttempts) {
                    try {
                        long waitTime = calculateRetryWaitTime(attempts);
                        log.info("等待{}ms后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new HttpClientException("重试被中断", ie);
                    }
                }
            }
        }

        // 所有尝试都失败
        throw new HttpClientException(buildFailureMessage(url, retryTimes, lastException), lastException);
    }

    /**
     * 构建包含第三方状态码和响应体的失败信息
     */
    private String buildFailureMessage(String url, int retryTimes, Exception lastException) {
        String message = "HTTP请求失败，已重试" + retryTimes + "次，url=" + url;
        if (lastException instanceof HttpStatusCodeException) {
            HttpStatusCodeException statusException = (HttpStatusCodeException) lastException;
            String responseBody = responseBodyOf(statusException);
            if (hasText(responseBody)) {
                return message + "，状态码=" + statusException.getStatusCode() + "，响应=" + responseBody;
            }
            return message + "，状态码=" + statusException.getStatusCode();
        }
        if (lastException != null && lastException.getMessage() != null) {
            return message + "，原因=" + lastException.getMessage();
        }
        return message;
    }

    private String responseBodyOf(HttpStatusCodeException exception) {
        String responseBody = exception.getResponseBodyAsString();
        return hasText(responseBody) ? responseBody : exception.getMessage();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 执行GET请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     */
    public String get(String url, HttpHeaders headers, int timeout) {
        return execute(HttpMethod.GET, url, headers, null, timeout);
    }

    /**
     * 执行POST请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     */
    public String post(String url, HttpHeaders headers, Object body, int timeout) {
        return execute(HttpMethod.POST, url, headers, body, timeout);
    }

    /**
     * 执行PUT请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     */
    public String put(String url, HttpHeaders headers, Object body, int timeout) {
        return execute(HttpMethod.PUT, url, headers, body, timeout);
    }

    /**
     * 执行DELETE请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     */
    public String delete(String url, HttpHeaders headers, int timeout) {
        return execute(HttpMethod.DELETE, url, headers, null, timeout);
    }

    /**
     * 创建RestTemplate实例
     * 配置超时时间和字符编码
     *
     * @param timeout 超时时间（毫秒）
     * @return RestTemplate实例
     */
    private RestTemplate createRestTemplate(int timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // 配置超时时间
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // 这里可以添加全局的请求拦截逻辑
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    /**
     * 计算重试等待时间
     * 使用指数退避策略：第1次重试等待1秒，第2次等待2秒，第3次等待4秒...
     *
     * @param attemptNumber 当前尝试次数（从1开始）
     * @return 等待时间（毫秒）
     */
    private long calculateRetryWaitTime(int attemptNumber) {
        // 指数退避：2^(n-1) * 1000ms，最大不超过10秒
        long waitTime = (long) Math.pow(2, attemptNumber - 1) * 1000;
        return Math.min(waitTime, 10000);
    }

    /**
     * 构建HttpHeaders
     * 从Map转换为HttpHeaders对象
     *
     * @param headersMap 请求头Map
     * @return HttpHeaders对象
     */
    public HttpHeaders buildHeaders(Map<String, String> headersMap) {
        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null && !headersMap.isEmpty()) {
            headersMap.forEach(headers::set);
        }
        return headers;
    }

    /**
     * HTTP客户端异常
     * 封装HTTP请求过程中的各种异常
     */
    public static class HttpClientException extends RuntimeException {
        public HttpClientException(String message) {
            super(message);
        }

        public HttpClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
