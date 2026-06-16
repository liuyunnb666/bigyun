-- =============================================
-- Provider 元数据初始化脚本
-- 用于前端下拉框显示配置类型和 Provider 选项
-- =============================================

-- 清理旧数据（可选，如果需要重新初始化）
-- DELETE FROM sys_provider_meta;

-- =============================================
-- 1. 插入配置类型（config_type）
-- =============================================

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `status`, `sort_order`) VALUES
('config_type', 'storage', '对象存储', 'OSS、MinIO、本地存储等文件存储服务', '0', 0),
('config_type', 'llm', '大语言模型', 'OpenAI GPT、Claude、文心一言、通义千问等', '0', 1),
('config_type', 'tts', '文本转语音', '阿里云TTS、腾讯云TTS、Azure TTS等', '0', 2),
('config_type', 'stt', '语音转文本', '阿里云ASR、腾讯云ASR、讯飞语音识别等', '0', 3),
('config_type', 'image_gen', '图像生成', 'DALL-E、Midjourney、Stable Diffusion等', '0', 4),
('config_type', 'image_recognition', '图像识别', '阿里云视觉、腾讯云视觉、百度AI等', '0', 5),
('config_type', 'ocr', '文字识别', '阿里云OCR、腾讯云OCR、百度OCR等', '0', 6),
('config_type', 'translation', '机器翻译', '阿里云翻译、腾讯云翻译、百度翻译等', '0', 7),
('config_type', 'sms', '短信服务', '阿里云短信、腾讯云短信、云片短信等', '0', 8),
('config_type', 'email', '邮件服务', 'SMTP、SendGrid、阿里云邮件推送等', '0', 9),
('config_type', 'payment', '支付服务', '支付宝、微信支付、Stripe等', '0', 10),
('config_type', 'map', '地图服务', '高德地图、百度地图、腾讯地图等', '0', 11)
ON DUPLICATE KEY UPDATE
    meta_name = VALUES(meta_name),
    meta_description = VALUES(meta_description),
    sort_order = VALUES(sort_order);

-- =============================================
-- 2. 插入存储类 Provider（storage）
-- =============================================

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('provider_code', 'local', '本地存储', '本地文件系统存储', 'storage', '0', 0),
('provider_code', 'minio', 'MinIO', '开源对象存储服务', 'storage', '0', 1),
('provider_code', 'aliyun-oss', '阿里云OSS', '阿里云对象存储服务', 'storage', '0', 2),
('provider_code', 'tencent-cos', '腾讯云COS', '腾讯云对象存储服务', 'storage', '0', 3),
('provider_code', 'qiniu-kodo', '七牛云Kodo', '七牛云对象存储服务', 'storage', '0', 4),
('provider_code', 'huawei-obs', '华为云OBS', '华为云对象存储服务', 'storage', '0', 5),
('provider_code', 'aws-s3', 'AWS S3', 'Amazon S3 对象存储', 'storage', '0', 6)
ON DUPLICATE KEY UPDATE
    meta_name = VALUES(meta_name),
    meta_description = VALUES(meta_description),
    sort_order = VALUES(sort_order);

-- =============================================
-- 3. 插入大语言模型 Provider（llm）
-- =============================================

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('provider_code', 'openai-gpt', 'OpenAI GPT', 'OpenAI GPT-3.5/GPT-4', 'llm', '0', 10),
('provider_code', 'anthropic-claude', 'Anthropic Claude', 'Claude 3 系列模型', 'llm', '0', 11),
('provider_code', 'aliyun-qwen', '阿里云通义千问', '阿里云通义千问大模型', 'llm', '0', 12),
('provider_code', 'baidu-wenxin', '百度文心一言', '百度文心大模型', 'llm', '0', 13),
('provider_code', 'tencent-hunyuan', '腾讯混元', '腾讯混元大模型', 'llm', '0', 14),
('provider_code', 'zhipu-glm', '智谱GLM', '智谱AI GLM大模型', 'llm', '0', 15),
('provider_code', 'moonshot-kimi', 'Moonshot Kimi', 'Moonshot AI Kimi大模型', 'llm', '0', 16),
('provider_code', 'deepseek', 'DeepSeek', 'DeepSeek大模型', 'llm', '0', 17)
ON DUPLICATE KEY UPDATE
    meta_name = VALUES(meta_name),
    meta_description = VALUES(meta_description),
    sort_order = VALUES(sort_order);

-- =============================================
-- 4. 插入文本转语音 Provider（tts）
-- =============================================

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('provider_code', 'aliyun-tts', '阿里云TTS', '阿里云语音合成服务', 'tts', '0', 20),
('provider_code', 'tencent-tts', '腾讯云TTS', '腾讯云语音合成服务', 'tts', '0', 21),
('provider_code', 'baidu-tts', '百度TTS', '百度语音合成服务', 'tts', '0', 22),
('provider_code', 'xunfei-tts', '讯飞TTS', '科大讯飞语音合成', 'tts', '0', 23),
('provider_code', 'azure-tts', 'Azure TTS', '微软Azure语音合成', 'tts', '0', 24)
ON DUPLICATE KEY UPDATE
    meta_name = VALUES(meta_name),
    meta_description = VALUES(meta_description),
    sort_order = VALUES(sort_order);

-- =============================================
-- 5. 插入语音转文本 Provider（stt）
-- =============================================

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('provider_code', 'aliyun-asr', '阿里云ASR', '阿里云语音识别服务', 'stt', '0', 30),
('provider_code', 'tencent-asr', '腾讯云ASR', '腾讯云语音识别服务', 'stt', '0', 31),
('provider_code', 'baidu-asr', '百度ASR', '百度语音识别服务', 'stt', '0', 32),
('provider_code', 'xunfei-asr', '讯飞ASR', '科大讯飞语音识别', 'stt', '0', 33),
('provider_code', 'azure-stt', 'Azure STT', '微软Azure语音识别', 'stt', '0', 34)
ON DUPLICATE KEY UPDATE
    meta_name = VALUES(meta_name),
    meta_description = VALUES(meta_description),
    sort_order = VALUES(sort_order);

-- =============================================
-- 6. 插入图像生成 Provider（image_gen）
-- =============================================

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('provider_code', 'openai-dalle', 'DALL-E', 'OpenAI DALL-E 图像生成', 'image_gen', '0', 40),
('provider_code', 'stable-diffusion', 'Stable Diffusion', 'Stable Diffusion 开源模型', 'image_gen', '0', 41),
('provider_code', 'midjourney', 'Midjourney', 'Midjourney 图像生成', 'image_gen', '0', 42),
('provider_code', 'aliyun-wanx', '阿里云万相', '阿里云万相图像生成', 'image_gen', '0', 43)
ON DUPLICATE KEY UPDATE
    meta_name = VALUES(meta_name),
    meta_description = VALUES(meta_description),
    sort_order = VALUES(sort_order);

-- =============================================
-- 验证数据
-- =============================================

-- 查看配置类型
SELECT meta_code, meta_name, meta_description
FROM sys_provider_meta
WHERE meta_type = 'config_type'
ORDER BY sort_order;

-- 查看存储类 Provider
SELECT meta_code, meta_name, meta_description
FROM sys_provider_meta
WHERE meta_type = 'provider_code' AND parent_code = 'storage'
ORDER BY sort_order;

-- 查看大语言模型 Provider
SELECT meta_code, meta_name, meta_description
FROM sys_provider_meta
WHERE meta_type = 'provider_code' AND parent_code = 'llm'
ORDER BY sort_order;
