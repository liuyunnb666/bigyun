package com.bigyun.config.domain.payload;

import com.alibaba.fastjson2.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provider LLM/视觉对话请求 Payload。
 * <p>
 * 对外提供 typed messages 入口；写入模板上下文时仍输出 JSON 字符串，兼容现有 `${messages}` SQL 模板。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProviderChatPayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    /** OpenAI 兼容 messages JSON 字符串，供模板引擎直接替换。 */
    private String messages;

    /** 本地 typed 消息列表，仅用于业务侧 fallback 解析，不进入模板上下文。 */
    private transient List<ProviderChatMessage> messageList;

    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    private String prompt;
    private String imageUrl;
    private String reportType;

    public static ProviderChatPayload chat(List<ProviderChatMessage> messages, String model, Double temperature,
                                           Integer maxTokens, Boolean stream)
    {
        ProviderChatPayload payload = new ProviderChatPayload();
        payload.setMessageList(messages);
        payload.setMessages(messages == null ? null : JSON.toJSONString(messages));
        payload.setModel(model);
        payload.setTemperature(temperature);
        payload.setMaxTokens(maxTokens);
        payload.setStream(stream);
        return payload;
    }

    /**
     * 兼容旧调用：旧链路可能已经传入 JSON 字符串形式的 messages。
     */
    @Deprecated
    public static ProviderChatPayload chat(Object messages, String model, Double temperature,
                                           Integer maxTokens, Boolean stream)
    {
        ProviderChatPayload payload = new ProviderChatPayload();
        payload.setMessageList(toTypedMessages(messages));
        payload.setMessages(toMessagesJson(messages));
        payload.setModel(model);
        payload.setTemperature(temperature);
        payload.setMaxTokens(maxTokens);
        payload.setStream(stream);
        return payload;
    }

    public static ProviderChatPayload vision(String imageUrl, String prompt, Object messages)
    {
        ProviderChatPayload payload = new ProviderChatPayload();
        payload.setImageUrl(imageUrl);
        payload.setPrompt(prompt);
        payload.setMessageList(toTypedMessages(messages));
        payload.setMessages(toMessagesJson(messages));
        return payload;
    }

    private static String toMessagesJson(Object messages)
    {
        if (messages == null)
        {
            return null;
        }
        return messages instanceof List ? JSON.toJSONString(messages) : messages.toString();
    }

    private static List<ProviderChatMessage> toTypedMessages(Object messages)
    {
        if (!(messages instanceof List<?>))
        {
            return null;
        }
        List<ProviderChatMessage> result = new ArrayList<>();
        for (Object item : (List<?>) messages)
        {
            if (!(item instanceof ProviderChatMessage))
            {
                return null;
            }
            result.add((ProviderChatMessage) item);
        }
        return result;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotNull(context, "messages", messages);
        putIfNotBlank(context, "model", model);
        putIfNotNull(context, "temperature", temperature);
        putIfNotNull(context, "maxTokens", maxTokens);
        putIfNotNull(context, "stream", stream);
        putIfNotBlank(context, "prompt", prompt);
        putIfNotBlank(context, "imageUrl", imageUrl);
        putIfNotBlank(context, "reportType", reportType);
        return context;
    }
}
