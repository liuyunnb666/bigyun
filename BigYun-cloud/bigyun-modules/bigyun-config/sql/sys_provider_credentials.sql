-- Provider凭据表
-- 用于存储Provider配置的认证凭据信息，支持多种认证模式（API Key、AK/SK、OAuth等）

CREATE TABLE IF NOT EXISTS sys_provider_credentials (
    credential_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '凭据ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    credential_key VARCHAR(64) NOT NULL COMMENT '凭据键名（如：apiKey、accessKey、secretKey、token等）',
    credential_value VARCHAR(1000) COMMENT '凭据值（加密存储）',
    is_sensitive CHAR(1) DEFAULT 'Y' COMMENT '是否敏感信息（Y=是 N=否）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_id, credential_key) COMMENT '同一配置下凭据键名唯一',
    KEY idx_config_id (config_id) COMMENT '配置ID索引',
    CONSTRAINT fk_credential_config FOREIGN KEY (config_id)
        REFERENCES sys_provider_config(config_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Provider凭据表';

-- 数据迁移脚本（可选）
-- 将现有的 accessKey/secretKey 迁移到凭据表
-- 注意：执行前请备份数据，并确认现有数据已加密

-- INSERT INTO sys_provider_credentials (config_id, credential_key, credential_value, is_sensitive)
-- SELECT config_id, 'accessKey', access_key, 'Y'
-- FROM sys_provider_config
-- WHERE access_key IS NOT NULL AND access_key != '';

-- INSERT INTO sys_provider_credentials (config_id, credential_key, credential_value, is_sensitive)
-- SELECT config_id, 'secretKey', secret_key, 'Y'
-- FROM sys_provider_config
-- WHERE secret_key IS NOT NULL AND secret_key != '';
