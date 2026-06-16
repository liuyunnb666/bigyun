package com.bigyun.config.service.handler.llm;

import java.util.List;

/**
 * LLM请求参数
 */
public class LLMRequest {

    /**
     * 对话消息列表
     */
    private List<Message> messages;

    /**
     * 最大token数（可选，会被配置覆盖）
     */
    private Integer maxTokens;

    /**
     * 温度参数（可选，会被配置覆盖）
     */
    private Double temperature;

    /**
     * 是否流式输出
     */
    private Boolean stream;

    public static class Message {
        private String role;    // system/user/assistant
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
