package com.bigyun.config.enums;

/**
 * Provider 配置类型枚举。
 */
public enum ProviderConfigTypeEnum
{
    // ========== 存储类 ==========
    STORAGE("storage", "对象存储", "OSS、MinIO、本地存储等文件存储服务"),

    // ========== AI 能力类 ==========
    LLM("llm", "大语言模型", "OpenAI GPT、Claude、文心一言、通义千问等"),
    TTS("tts", "文本转语音", "阿里云 TTS、腾讯云 TTS、Azure TTS 等"),
    STT("stt", "语音转文本", "阿里云 ASR、腾讯云 ASR、讯飞语音识别等"),
    IMAGE_GEN("image_gen", "图像生成", "DALL-E、Midjourney、Stable Diffusion 等"),
    IMAGE_RECOGNITION("image_recognition", "图像识别", "阿里云视觉、腾讯云视觉等"),
    OCR("ocr", "文字识别", "阿里云 OCR、腾讯云 OCR、百度 OCR 等"),
    FACE("face", "人脸识别", "Face++、阿里云人脸识别、腾讯云人脸核身等"),
    VISION("vision", "视觉理解", "通义千问 VL、多模态视觉理解等"),
    VOICEPRINT("voiceprint", "声纹识别", "声纹注册、声纹校验、声纹登录等"),
    TRANSLATION("translation", "机器翻译", "阿里云翻译、腾讯云翻译、百度翻译等"),

    // ========== 通信类 ==========
    SMS("sms", "短信服务", "阿里云短信、腾讯云短信等"),
    EMAIL("email", "邮件服务", "阿里云邮件、腾讯云邮件等"),

    // ========== 支付类 ==========
    PAYMENT("payment", "支付服务", "支付宝、微信支付等"),

    // ========== 地图类 ==========
    MAP("map", "地图服务", "高德地图、百度地图等");

    private final String code;
    private final String name;
    private final String description;

    ProviderConfigTypeEnum(String code, String name, String description)
    {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public static ProviderConfigTypeEnum fromCode(String code)
    {
        for (ProviderConfigTypeEnum type : values())
        {
            if (type.code.equals(code))
            {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的配置类型: " + code);
    }
}
