-- =============================================
-- Provider配置管理系统 - 数据库表结构
-- =============================================

-- 1. Provider元数据表（存储枚举定义）
CREATE TABLE IF NOT EXISTS `sys_provider_meta` (
    `meta_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '元数据ID',
    `meta_type` VARCHAR(32) NOT NULL COMMENT '元数据类型: config_type(配置类型) / provider_code(服务商代码)',
    `meta_code` VARCHAR(64) NOT NULL COMMENT '元数据代码',
    `meta_name` VARCHAR(100) NOT NULL COMMENT '元数据名称',
    `meta_description` VARCHAR(500) DEFAULT NULL COMMENT '元数据描述',
    `parent_code` VARCHAR(64) DEFAULT NULL COMMENT '父级代码（provider_code类型时，关联config_type）',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态: 0=正常 1=停用',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`meta_id`),
    UNIQUE KEY `uk_type_code` (`meta_type`, `meta_code`),
    KEY `idx_parent_code` (`parent_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Provider元数据表';

-- 2. Provider配置表（已存在，确认结构）
-- 如果表已存在，请确保包含以下字段
CREATE TABLE IF NOT EXISTS `sys_provider_config` (
    `config_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_type` VARCHAR(64) NOT NULL COMMENT '配置类型: storage/llm/tts/stt等',
    `provider_code` VARCHAR(64) NOT NULL COMMENT 'Provider编码: local/aliyun-oss/openai-gpt等',
    `provider_name` VARCHAR(100) NOT NULL COMMENT 'Provider名称',
    `endpoint` VARCHAR(255) DEFAULT NULL COMMENT '访问端点',
    `region` VARCHAR(100) DEFAULT NULL COMMENT '地域',
    `bucket_name` VARCHAR(100) DEFAULT NULL COMMENT 'Bucket名称',
    `access_key` VARCHAR(500) DEFAULT NULL COMMENT 'Access Key（加密存储）',
    `secret_key` VARCHAR(500) DEFAULT NULL COMMENT 'Secret Key（加密存储）',
    `domain` VARCHAR(255) DEFAULT NULL COMMENT '自定义域名',
    `base_path` VARCHAR(255) DEFAULT NULL COMMENT '基础路径',
    `ext_params_json` TEXT DEFAULT NULL COMMENT '扩展参数JSON',
    `is_default` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '是否默认配置: Y=是 N=否',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态: 0=正常 1=停用',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_type_provider` (`config_type`, `provider_code`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Provider配置表';

-- =============================================
-- 初始化数据示例
-- =============================================

-- 插入配置类型元数据（由ProviderEnumManager自动同步，这里仅作示例）
INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `status`, `sort_order`) VALUES
('config_type', 'storage', '对象存储', 'OSS、MinIO、本地存储等文件存储服务', '0', 0),
('config_type', 'llm', '大语言模型', 'OpenAI GPT、Claude、文心一言、通义千问等', '0', 1),
('config_type', 'tts', '文本转语音', '阿里云TTS、腾讯云TTS、Azure TTS等', '0', 2),
('config_type', 'stt', '语音转文本', '阿里云ASR、腾讯云ASR、讯飞语音识别等', '0', 3);

-- 插入服务商元数据（由ProviderEnumManager自动同步，这里仅作示例）
INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('provider_code', 'local', '本地存储', '对象存储', 'storage', '0', 0),
('provider_code', 'aliyun-oss', '阿里云OSS', '对象存储', 'storage', '0', 1),
('provider_code', 'tencent-cos', '腾讯云COS', '对象存储', 'storage', '0', 2),
('provider_code', 'openai-gpt', 'OpenAI GPT', '大语言模型', 'llm', '0', 10),
('provider_code', 'aliyun-qwen', '阿里云通义千问', '大语言模型', 'llm', '0', 11);

-- 插入示例配置（本地存储）
INSERT INTO `sys_provider_config` (
    `config_type`, `provider_code`, `provider_name`,
    `base_path`, `domain`,
    `is_default`, `status`, `create_by`
) VALUES (
    'storage', 'local', '本地存储',
    '/data/upload', 'https://example.com',
    'Y', '0', 'admin'
);

-- 插入示例配置（阿里云OSS）
-- 注意：access_key 和 secret_key 需要加密后存储
INSERT INTO `sys_provider_config` (
    `config_type`, `provider_code`, `provider_name`,
    `endpoint`, `region`, `bucket_name`, `domain`,
    `access_key`, `secret_key`,
    `is_default`, `status`, `create_by`, `remark`
) VALUES (
    'storage', 'aliyun-oss', '阿里云OSS',
    'oss-cn-hangzhou.aliyuncs.com', 'cn-hangzhou', 'my-bucket', 'https://cdn.example.com',
    NULL, NULL,
    'N', '0', 'admin', '需要配置实际的密钥'
);

-- 插入示例配置（OpenAI GPT）
INSERT INTO `sys_provider_config` (
    `config_type`, `provider_code`, `provider_name`,
    `ext_params_json`,
    `is_default`, `status`, `create_by`
) VALUES (
    'llm', 'openai-gpt', 'OpenAI GPT',
    '{"apiKey":"ENCRYPTED_API_KEY","endpoint":"https://api.openai.com/v1","model":"gpt-4","maxTokens":2000,"temperature":0.7}',
    'Y', '0', 'admin'
);
