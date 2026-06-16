package com.bigyun.config.service.handler.llm;

/**
 * LLM响应结果
 */
public class LLMResponse {

    /**
     * 生成的内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * Token使用情况
     */
    private Usage usage;

    /**
     * 完成原因（stop/length/content_filter等）
     */
    private String finishReason;

    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;

        public Usage() {
        }

        public Usage(int promptTokens, int completionTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}
