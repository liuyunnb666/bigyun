package com.bigyun.config.enums;

/**
 * Provider 服务商代码枚举。
 */
public enum ProviderCodeEnum
{
    // ========== 存储类 ==========
    LOCAL_STORAGE("local", "本地存储", ProviderConfigTypeEnum.STORAGE),
    MINIO("minio", "MinIO", ProviderConfigTypeEnum.STORAGE),
    ALIYUN_OSS("aliyun-oss", "阿里云 OSS", ProviderConfigTypeEnum.STORAGE),
    TENCENT_COS("tencent-cos", "腾讯云 COS", ProviderConfigTypeEnum.STORAGE),
    QINIU_KODO("qiniu-kodo", "七牛云 Kodo", ProviderConfigTypeEnum.STORAGE),

    // ========== LLM 大语言模型 ==========
    OPENAI_GPT("openai-gpt", "OpenAI GPT", ProviderConfigTypeEnum.LLM),
    ANTHROPIC_CLAUDE("anthropic-claude", "Anthropic Claude", ProviderConfigTypeEnum.LLM),
    ALIYUN_QWEN("aliyun-qwen", "阿里云通义千问", ProviderConfigTypeEnum.LLM),
    BAIDU_WENXIN("baidu-wenxin", "百度文心一言", ProviderConfigTypeEnum.LLM),
    TENCENT_HUNYUAN("tencent-hunyuan", "腾讯混元", ProviderConfigTypeEnum.LLM),
    ZHIPU_GLM("zhipu-glm", "智谱 GLM", ProviderConfigTypeEnum.LLM),
    MOONSHOT_KIMI("moonshot-kimi", "月之暗面 Kimi", ProviderConfigTypeEnum.LLM),
    DEEPSEEK("deepseek", "DeepSeek", ProviderConfigTypeEnum.LLM),
    BIGYUN_DEMO_LLM("bigyun-demo-llm", "BigYun Demo LLM", ProviderConfigTypeEnum.LLM),

    // ========== TTS 文本转语音 ==========
    ALIYUN_TTS("aliyun-tts", "阿里云 TTS", ProviderConfigTypeEnum.TTS),
    TENCENT_TTS("tencent-tts", "腾讯云 TTS", ProviderConfigTypeEnum.TTS),
    BAIDU_TTS("baidu-tts", "百度 TTS", ProviderConfigTypeEnum.TTS),
    AZURE_TTS("azure-tts", "微软 Azure TTS", ProviderConfigTypeEnum.TTS),
    XUNFEI_TTS("xunfei-tts", "讯飞 TTS", ProviderConfigTypeEnum.TTS),

    // ========== STT 语音转文本 ==========
    ALIYUN_ASR("aliyun-asr", "阿里云 ASR", ProviderConfigTypeEnum.STT),
    TENCENT_ASR("tencent-asr", "腾讯云 ASR", ProviderConfigTypeEnum.STT),
    BAIDU_ASR("baidu-asr", "百度 ASR", ProviderConfigTypeEnum.STT),
    XUNFEI_ASR("xunfei-asr", "讯飞语音识别", ProviderConfigTypeEnum.STT),

    // ========== 图像生成 ==========
    OPENAI_DALLE("openai-dalle", "OpenAI DALL-E", ProviderConfigTypeEnum.IMAGE_GEN),
    STABLE_DIFFUSION("stable-diffusion", "Stable Diffusion", ProviderConfigTypeEnum.IMAGE_GEN),
    MIDJOURNEY("midjourney", "Midjourney", ProviderConfigTypeEnum.IMAGE_GEN),
    ALIYUN_WANX("aliyun-wanx", "阿里云万相", ProviderConfigTypeEnum.IMAGE_GEN),

    // ========== 图像识别 ==========
    ALIYUN_VISION("aliyun-vision", "阿里云视觉", ProviderConfigTypeEnum.IMAGE_RECOGNITION),
    TENCENT_VISION("tencent-vision", "腾讯云视觉", ProviderConfigTypeEnum.IMAGE_RECOGNITION),
    BAIDU_VISION("baidu-vision", "百度视觉", ProviderConfigTypeEnum.IMAGE_RECOGNITION),

    // ========== OCR 文字识别 ==========
    ALIYUN_OCR("aliyun-ocr", "阿里云 OCR", ProviderConfigTypeEnum.OCR),
    TENCENT_OCR("tencent-ocr", "腾讯云 OCR", ProviderConfigTypeEnum.OCR),
    BAIDU_OCR("baidu-ocr", "百度 OCR", ProviderConfigTypeEnum.OCR),
    HUAWEI_OCR("huawei-ocr", "华为云 OCR", ProviderConfigTypeEnum.OCR),
    PADDLEOCR("paddleocr", "PaddleOCR", ProviderConfigTypeEnum.OCR),

    // ========== 人脸识别 ==========
    FACEPLUS("faceplus", "Face++", ProviderConfigTypeEnum.FACE),
    BAIDU_FACE("baidu-face", "百度人脸识别", ProviderConfigTypeEnum.FACE),
    TENCENT_FACE("tencent-face", "腾讯云人脸识别", ProviderConfigTypeEnum.FACE),
    ALIYUN_FACEBODY("aliyun-facebody", "阿里云人脸人体", ProviderConfigTypeEnum.FACE),

    // ========== 视觉理解 ==========
    QWEN_VL("qwen-vl", "通义千问 VL", ProviderConfigTypeEnum.VISION),

    // ========== 声纹识别 ==========
    XUNFEI_VOICEPRINT("xunfei-voiceprint", "讯飞声纹", ProviderConfigTypeEnum.VOICEPRINT),

    // ========== 机器翻译 ==========
    ALIYUN_TRANSLATE("aliyun-translate", "阿里云翻译", ProviderConfigTypeEnum.TRANSLATION),
    TENCENT_TRANSLATE("tencent-translate", "腾讯云翻译", ProviderConfigTypeEnum.TRANSLATION),
    BAIDU_TRANSLATE("baidu-translate", "百度翻译", ProviderConfigTypeEnum.TRANSLATION),

    // ========== 短信服务 ==========
    ALIYUN_SMS("aliyun-sms", "阿里云短信", ProviderConfigTypeEnum.SMS),
    TENCENT_SMS("tencent-sms", "腾讯云短信", ProviderConfigTypeEnum.SMS),

    // ========== 邮件服务 ==========
    ALIYUN_EMAIL("aliyun-email", "阿里云邮件", ProviderConfigTypeEnum.EMAIL),
    TENCENT_EMAIL("tencent-email", "腾讯云邮件", ProviderConfigTypeEnum.EMAIL),

    // ========== 支付服务 ==========
    ALIPAY("alipay", "支付宝", ProviderConfigTypeEnum.PAYMENT),
    WECHAT_PAY("wechat-pay", "微信支付", ProviderConfigTypeEnum.PAYMENT),

    // ========== 地图服务 ==========
    AMAP("amap", "高德地图", ProviderConfigTypeEnum.MAP),
    BAIDU_MAP("baidu-map", "百度地图", ProviderConfigTypeEnum.MAP);

    private final String code;
    private final String name;
    private final ProviderConfigTypeEnum type;

    ProviderCodeEnum(String code, String name, ProviderConfigTypeEnum type)
    {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    public ProviderConfigTypeEnum getType()
    {
        return type;
    }

    public static ProviderCodeEnum fromCode(String code)
    {
        for (ProviderCodeEnum provider : values())
        {
            if (provider.code.equals(code))
            {
                return provider;
            }
        }
        throw new IllegalArgumentException("未知的服务商代码: " + code);
    }
}
