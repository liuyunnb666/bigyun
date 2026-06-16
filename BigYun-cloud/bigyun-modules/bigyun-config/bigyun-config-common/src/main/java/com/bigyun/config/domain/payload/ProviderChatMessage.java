package com.bigyun.config.domain.payload;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider LLM 对话消息。
 * <p>
 * 用 typed DTO 组装 system/user/assistant 消息，避免业务层继续用 Map 拼 OpenAI 风格 messages。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderChatMessage implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 消息角色：system、user、assistant */
    private String role;

    /** 消息正文 */
    private String content;

    public static ProviderChatMessage system(String content)
    {
        return new ProviderChatMessage("system", content);
    }

    public static ProviderChatMessage user(String content)
    {
        return new ProviderChatMessage("user", content);
    }

    public static ProviderChatMessage assistant(String content)
    {
        return new ProviderChatMessage("assistant", content);
    }
}
