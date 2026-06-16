-- Provider 动态字段配置表
CREATE TABLE IF NOT EXISTS provider_field_config (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    provider_code VARCHAR(50)  NOT NULL COMMENT '服务商代码，关联 ProviderCodeEnum',
    field_key     VARCHAR(50)  NOT NULL COMMENT '字段key（如 apiKey, endpoint）',
    field_label   VARCHAR(100) NOT NULL COMMENT '字段标签（如 API Key）',
    field_type    VARCHAR(20)  NOT NULL DEFAULT 'text' COMMENT '字段类型：text/password/number/select/textarea',
    required      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否必填：0否 1是',
    placeholder   VARCHAR(200)          DEFAULT NULL COMMENT '占位符文本',
    default_value VARCHAR(200)          DEFAULT NULL COMMENT '默认值',
    help_text     VARCHAR(500)          DEFAULT NULL COMMENT '帮助文本',
    sort_order    INT          NOT NULL DEFAULT 0 COMMENT '排序号，前端渲染顺序',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_provider_field (provider_code, field_key)
) COMMENT 'Provider动态字段配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 数据迁移：16个服务商的字段定义

-- 1. local 本地存储
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('local', 'basePath', '基础路径', 'text', 1, '/data/files', '/data/files', '本地文件存储的基础路径', 1),
('local', 'domain', '访问域名', 'text', 1, 'http://localhost:8080', 'http://localhost:8080', '文件访问的域名前缀', 2);

-- 2. minio MinIO对象存储
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('minio', 'endpoint', '服务地址', 'text', 1, 'http://localhost:9000', NULL, 'MinIO服务的访问地址', 1),
('minio', 'accessKey', 'Access Key', 'password', 1, 'minioadmin', NULL, 'MinIO的Access Key', 2),
('minio', 'secretKey', 'Secret Key', 'password', 1, 'minioadmin', NULL, 'MinIO的Secret Key', 3),
('minio', 'bucketName', '存储桶名称', 'text', 1, 'bigyun', NULL, 'MinIO中的存储桶名称', 4),
('minio', 'domain', '访问域名', 'text', 0, 'http://localhost:9000', NULL, '文件访问的域名前缀', 5);

-- 3. aliyun-oss 阿里云对象存储
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('aliyun-oss', 'endpoint', '服务地址', 'text', 1, 'oss-cn-hangzhou.aliyuncs.com', NULL, '阿里云OSS的Endpoint', 1),
('aliyun-oss', 'region', '地域', 'text', 1, 'cn-hangzhou', NULL, '阿里云OSS的地域代码', 2),
('aliyun-oss', 'accessKey', 'Access Key', 'password', 1, NULL, NULL, '阿里云的Access Key', 3),
('aliyun-oss', 'secretKey', 'Secret Key', 'password', 1, NULL, NULL, '阿里云的Secret Key', 4),
('aliyun-oss', 'bucketName', '存储桶名称', 'text', 1, NULL, NULL, '阿里云OSS的Bucket名称', 5),
('aliyun-oss', 'domain', '访问域名', 'text', 0, NULL, NULL, '文件访问的域名前缀', 6);

-- 4. tencent-cos 腾讯云对象存储
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('tencent-cos', 'endpoint', '服务地址', 'text', 1, 'cos.ap-beijing.myqcloud.com', NULL, '腾讯云COS的Endpoint', 1),
('tencent-cos', 'region', '地域', 'text', 1, 'ap-beijing', NULL, '腾讯云COS的地域代码', 2),
('tencent-cos', 'accessKey', 'Secret ID', 'password', 1, NULL, NULL, '腾讯云的Secret ID', 3),
('tencent-cos', 'secretKey', 'Secret Key', 'password', 1, NULL, NULL, '腾讯云的Secret Key', 4),
('tencent-cos', 'bucketName', '存储桶名称', 'text', 1, NULL, NULL, '腾讯云COS的Bucket名称', 5),
('tencent-cos', 'domain', '访问域名', 'text', 0, NULL, NULL, '文件访问的域名前缀', 6);

-- 5. openai-gpt OpenAI GPT
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('openai-gpt', 'apiKey', 'API Key', 'password', 1, 'sk-...', NULL, 'OpenAI的API Key', 1),
('openai-gpt', 'endpoint', 'API地址', 'text', 0, 'https://api.openai.com/v1', 'https://api.openai.com/v1', '可选，使用代理或第三方API时填写', 2),
('openai-gpt', 'model', '默认模型', 'select', 1, NULL, 'gpt-4', '选择使用的模型版本', 3),
('openai-gpt', 'maxTokens', '最大Token数', 'number', 0, '2000', '2000', '单次请求的最大Token数', 4),
('openai-gpt', 'temperature', '温度参数', 'number', 0, '0.0-2.0', '0.7', '控制输出的随机性，范围0-2', 5);

-- 6. anthropic-claude Anthropic Claude
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('anthropic-claude', 'apiKey', 'API Key', 'password', 1, NULL, NULL, 'Anthropic的API Key', 1),
('anthropic-claude', 'endpoint', 'API地址', 'text', 0, 'https://api.anthropic.com', 'https://api.anthropic.com', 'Anthropic API的地址', 2),
('anthropic-claude', 'model', '默认模型', 'select', 1, NULL, 'claude-3-opus', '选择使用的Claude模型版本', 3),
('anthropic-claude', 'maxTokens', '最大Token数', 'number', 0, '2000', '2000', '单次请求的最大Token数', 4);

-- 7. aliyun-qwen 阿里云通义千问
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('aliyun-qwen', 'apiKey', 'API Key', 'password', 1, NULL, NULL, '阿里云通义千问的API Key', 1),
('aliyun-qwen', 'endpoint', 'API地址', 'text', 0, 'https://dashscope.aliyuncs.com/api/v1', 'https://dashscope.aliyuncs.com/api/v1', '阿里云DashScope API地址', 2),
('aliyun-qwen', 'model', '默认模型', 'select', 1, NULL, 'qwen-max', '选择使用的通义千问模型版本', 3);

-- 8. baidu-wenxin 百度文心一言
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('baidu-wenxin', 'apiKey', 'API Key', 'password', 1, NULL, NULL, '百度文心一言的API Key', 1),
('baidu-wenxin', 'secretKey', 'Secret Key', 'password', 1, NULL, NULL, '百度文心一言的Secret Key', 2),
('baidu-wenxin', 'model', '默认模型', 'select', 1, NULL, 'ernie-bot', '选择使用的文心一言模型版本', 3);

-- 9. zhipu-glm 智谱清言
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('zhipu-glm', 'apiKey', 'API Key', 'password', 1, NULL, NULL, '智谱清言的API Key', 1),
('zhipu-glm', 'model', '默认模型', 'select', 1, NULL, 'glm-4', '选择使用的GLM模型版本', 2);

-- 10. aliyun-tts 阿里云文本转语音
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('aliyun-tts', 'accessKey', 'Access Key', 'password', 1, NULL, NULL, '阿里云的Access Key', 1),
('aliyun-tts', 'secretKey', 'Secret Key', 'password', 1, NULL, NULL, '阿里云的Secret Key', 2),
('aliyun-tts', 'appKey', 'App Key', 'password', 1, NULL, NULL, '阿里云语音服务的App Key', 3),
('aliyun-tts', 'voice', '语音类型', 'select', 0, NULL, 'xiaoyun', '选择语音类型和性别', 4);

-- 11. xunfei-tts 讯飞文本转语音
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('xunfei-tts', 'appId', 'App ID', 'password', 1, NULL, NULL, '讯飞的App ID', 1),
('xunfei-tts', 'apiKey', 'API Key', 'password', 1, NULL, NULL, '讯飞的API Key', 2),
('xunfei-tts', 'apiSecret', 'API Secret', 'password', 1, NULL, NULL, '讯飞的API Secret', 3),
('xunfei-tts', 'voice', '语音类型', 'select', 0, NULL, 'xiaoyan', '选择语音类型和性别', 4);

-- 12. aliyun-asr 阿里云语音识别
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('aliyun-asr', 'accessKey', 'Access Key', 'password', 1, NULL, NULL, '阿里云的Access Key', 1),
('aliyun-asr', 'secretKey', 'Secret Key', 'password', 1, NULL, NULL, '阿里云的Secret Key', 2),
('aliyun-asr', 'appKey', 'App Key', 'password', 1, NULL, NULL, '阿里云语音服务的App Key', 3);

-- 13. xunfei-asr 讯飞语音识别
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('xunfei-asr', 'appId', 'App ID', 'password', 1, NULL, NULL, '讯飞的App ID', 1),
('xunfei-asr', 'apiKey', 'API Key', 'password', 1, NULL, NULL, '讯飞的API Key', 2),
('xunfei-asr', 'apiSecret', 'API Secret', 'password', 1, NULL, NULL, '讯飞的API Secret', 3);

-- 14. aliyun-sms 阿里云短信
INSERT INTO provider_field_config (provider_code, field_key, field_label, field_type, required, placeholder, default_value, help_text, sort_order) VALUES
('aliyun-sms', 'accessKey', 'Access Key', 'password', 1, NULL, NULL, '阿里云的Access Key', 1),
('aliyun-sms', 'secretKey', 'Secret Key', 'password', 1, NULL, NULL, '阿里云的Secret Key', 2),
('aliyun-sms', 'signName', '短信签名', 'text', 1, NULL, NULL, '阿里云短信的签名名称', 3),
('aliyun-sms', 'templateCode', '模板代码', 'text', 1, NULL, NULL, '阿里云短信的模板代码', 4);

-- modelType 字段扩展
ALTER TABLE sys_provider_config ADD COLUMN model_type VARCHAR(50) DEFAULT NULL COMMENT '模型类型：chat/vision/intent/embedding（LLM专用）';
