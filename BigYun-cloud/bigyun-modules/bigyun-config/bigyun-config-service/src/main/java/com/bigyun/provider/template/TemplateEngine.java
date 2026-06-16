// -*- coding: utf-8 -*-
package com.bigyun.provider.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板引擎
 *
 * 功能说明：
 * 支持${variable}语法的字符串模板渲染，用于动态替换配置中的变量。
 * 主要用于渲染URL模板、请求头模板、请求体模板等。
 *
 * 模板语法：
 * - ${variable} - 从上下文中获取变量值并替换
 * - 支持嵌套对象访问（如：${config.endpoint}）
 * - 变量不存在时保持原样或替换为空字符串
 *
 * 使用示例：
 * <pre>
 * 模板：https://${bucketName}.${endpoint}/${filePath}
 * 上下文：{bucketName: "my-bucket", endpoint: "oss.aliyun.com", filePath: "test.jpg"}
 * 结果：https://my-bucket.oss.aliyun.com/test.jpg
 * </pre>
 *
 * @author BigYun
 * @date 2024-05-22
 */
@Component
public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);

    /**
     * 变量匹配正则表达式
     * 匹配 ${variable} 格式的变量
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 渲染模板
     * 将模板中的${variable}替换为context中对应的值
     *
     * @param template 模板字符串，如："${endpoint}/${path}"
     * @param context 变量上下文，如：{endpoint: "api.com", path: "test"}
     * @return 渲染后的字符串，如："api.com/test"
     */
    public String render(String template, Map<String, Object> context) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        if (context == null || context.isEmpty()) {
            log.warn("模板上下文为空，返回原始模板: {}", template);
            return template;
        }

        try {
            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1); // 获取变量名（不含${}）
                Object value = getVariableValue(variableName, context);

                // 将变量替换为对应的值
                String replacement = value != null ? value.toString() : "";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);

            return result.toString();
        } catch (Exception e) {
            log.error("模板渲染失败: template={}, error={}", template, e.getMessage(), e);
            return template;
        }
    }

    /**
     * 从上下文中获取变量值
     * 支持简单变量和嵌套对象访问
     *
     * @param variableName 变量名，如："endpoint" 或 "config.endpoint"
     * @param context 变量上下文
     * @return 变量值，如果不存在则返回null
     */
    private Object getVariableValue(String variableName, Map<String, Object> context) {
        if (variableName == null || variableName.isEmpty()) {
            return null;
        }

        // 支持嵌套对象访问，如：config.endpoint
        if (variableName.contains(".")) {
            String[] parts = variableName.split("\\.");
            Object current = context;

            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                    if (current == null) {
                        log.debug("变量不存在: {}", variableName);
                        return null;
                    }
                } else {
                    log.warn("无法访问嵌套属性: {}", variableName);
                    return null;
                }
            }

            return current;
        }

        // 简单变量访问
        Object value = context.get(variableName);
        if (value == null) {
            log.debug("变量不存在: {}", variableName);
        }
        return value;
    }

    /**
     * 检查模板中是否包含变量
     *
     * @param template 模板字符串
     * @return true=包含变量，false=不包含变量
     */
    public boolean hasVariables(String template) {
        if (template == null || template.isEmpty()) {
            return false;
        }
        return VARIABLE_PATTERN.matcher(template).find();
    }

    /**
     * 提取模板中的所有变量名
     *
     * @param template 模板字符串
     * @return 变量名列表
     */
    public java.util.List<String> extractVariables(String template) {
        java.util.List<String> variables = new java.util.ArrayList<>();
        if (template == null || template.isEmpty()) {
            return variables;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
}
