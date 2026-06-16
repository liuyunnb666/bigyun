// -*- coding: utf-8 -*-
package com.bigyun.provider.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应解析器
 *
 * 功能说明：
 * 根据配置的响应映射规则解析HTTP响应，提取需要的字段。
 * 支持JSON、XML、Text等多种响应格式，支持JSONPath表达式提取字段。
 *
 * 核心特性：
 * 1. 支持JSON格式响应解析（使用JSONPath表达式）
 * 2. 支持XML格式响应解析
 * 3. 支持纯文本响应
 * 4. 支持变量替换（从上下文中获取变量值）
 * 5. 支持嵌套字段提取
 *
 * JSONPath表达式示例：
 * - $.data.url - 提取data对象中的url字段
 * - $.choices[0].message.content - 提取数组第一个元素的嵌套字段
 * - $..name - 递归查找所有name字段
 *
 * 使用场景：
 * - 解析存储服务返回的文件URL
 * - 解析LLM服务返回的对话内容
 * - 解析任意API的响应数据
 *
 * @author BigYun
 * @date 2024-05-22
 */
@Component
public class ResponseParser {

    private static final Logger log = LoggerFactory.getLogger(ResponseParser.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析响应
     *
     * @param responseBody 响应体字符串
     * @param responseType 响应类型：json/xml/text
     * @param mappingJson 字段映射JSON配置
     * @param context 变量上下文（用于变量替换）
     * @return 解析后的数据Map
     */
    public Map<String, Object> parse(String responseBody, String responseType,
                                     String mappingJson, Map<String, Object> context) {
        if (responseBody == null) {
            log.warn("响应体为空");
            return new HashMap<>();
        }

        try {
            // 根据响应类型选择解析方法
            switch (responseType.toLowerCase()) {
                case "json":
                    return parseJson(responseBody, mappingJson, context);
                case "xml":
                    return parseXml(responseBody, mappingJson, context);
                case "text":
                    return parseText(responseBody, mappingJson, context);
                default:
                    log.warn("不支持的响应类型: {}, 按JSON处理", responseType);
                    return parseJson(responseBody, mappingJson, context);
            }
        } catch (Exception e) {
            log.error("响应解析失败: responseType={}, error={}", responseType, e.getMessage(), e);
            // 返回原始响应
            Map<String, Object> result = new HashMap<>();
            result.put("raw", responseBody);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 解析JSON响应
     *
     * @param responseBody JSON响应体
     * @param mappingJson 字段映射配置
     * @param context 变量上下文
     * @return 解析后的数据Map
     */
    private Map<String, Object> parseJson(String responseBody, String mappingJson,
                                          Map<String, Object> context) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // 如果没有映射配置，返回整个响应
        if (mappingJson == null || mappingJson.trim().isEmpty()) {
            log.debug("没有映射配置，返回原始JSON");
            result.put("raw", responseBody);
            return result;
        }

        // 解析映射配置
        Map<String, String> mapping = objectMapper.readValue(mappingJson, Map.class);

        // 遍历映射配置，提取字段
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String fieldName = entry.getKey();
            String fieldPath = entry.getValue();

            try {
                Object value = extractFieldValue(responseBody, fieldPath, context);
                result.put(fieldName, value);
                log.debug("提取字段成功: {}={}", fieldName, value);
            } catch (Exception e) {
                log.warn("提取字段失败: field={}, path={}, error={}",
                        fieldName, fieldPath, e.getMessage());
                result.put(fieldName, null);
            }
        }

        return result;
    }

    /**
     * 提取字段值
     * 支持JSONPath表达式和变量替换
     *
     * @param responseBody 响应体
     * @param fieldPath 字段路径（JSONPath表达式或变量模板）
     * @param context 变量上下文
     * @return 字段值
     */
    private Object extractFieldValue(String responseBody, String fieldPath,
                                     Map<String, Object> context) {
        // 如果是JSONPath表达式（以$开头）
        if (fieldPath.startsWith("$")) {
            try {
                return JsonPath.read(responseBody, fieldPath);
            } catch (Exception e) {
                log.warn("JSONPath提取失败: path={}, error={}", fieldPath, e.getMessage());
                return null;
            }
        }

        // 如果包含变量（${variable}），进行变量替换
        if (fieldPath.contains("${")) {
            return replaceVariables(fieldPath, context);
        }

        // 否则作为常量返回
        return fieldPath;
    }

    /**
     * 替换变量
     * 将字符串中的${variable}替换为context中的值
     *
     * @param template 模板字符串
     * @param context 变量上下文
     * @return 替换后的字符串
     */
    private String replaceVariables(String template, Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    /**
     * 解析XML响应
     *
     * @param responseBody XML响应体
     * @param mappingJson 字段映射配置
     * @param context 变量上下文
     * @return 解析后的数据Map
     */
    private Map<String, Object> parseXml(String responseBody, String mappingJson,
                                         Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();

        // TODO: 实现XML解析逻辑
        // 可以使用XPath表达式提取字段
        log.warn("XML解析暂未实现，返回原始响应");
        result.put("raw", responseBody);

        return result;
    }

    /**
     * 解析纯文本响应
     *
     * @param responseBody 文本响应体
     * @param mappingJson 字段映射配置
     * @param context 变量上下文
     * @return 解析后的数据Map
     */
    private Map<String, Object> parseText(String responseBody, String mappingJson,
                                          Map<String, Object> context) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // 如果有映射配置，按映射规则处理
        if (mappingJson != null && !mappingJson.trim().isEmpty()) {
            Map<String, String> mapping = objectMapper.readValue(mappingJson, Map.class);

            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                // 支持变量替换
                if (fieldValue.contains("${")) {
                    result.put(fieldName, replaceVariables(fieldValue, context));
                } else {
                    result.put(fieldName, fieldValue);
                }
            }
        } else {
            // 没有映射配置，直接返回原始文本
            result.put("content", responseBody);
        }

        return result;
    }

    /**
     * 解析JSON字符串为Map
     *
     * @param jsonString JSON字符串
     * @return Map对象
     */
    public Map<String, Object> parseJsonToMap(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            log.error("JSON解析失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    public String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("对象转JSON失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 验证JSON格式
     *
     * @param jsonString JSON字符串
     * @return true=有效的JSON，false=无效的JSON
     */
    public boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
