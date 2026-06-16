-- Provider configuration center schema for BigYun community edition.
-- This file uses placeholders only. Do not store real AK/SK/token/password here.

CREATE TABLE IF NOT EXISTS `sys_provider_meta` (
  `meta_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'meta id',
  `meta_type` varchar(32) NOT NULL COMMENT 'config_type/provider_code',
  `meta_code` varchar(64) NOT NULL COMMENT 'meta code',
  `meta_name` varchar(100) NOT NULL COMMENT 'meta name',
  `meta_description` varchar(500) DEFAULT NULL COMMENT 'description',
  `parent_code` varchar(64) DEFAULT NULL COMMENT 'parent code',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0 enabled 1 disabled',
  `sort_order` int DEFAULT 0 COMMENT 'sort',
  `create_by` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`meta_id`),
  UNIQUE KEY `uk_provider_meta_type_code` (`meta_type`, `meta_code`),
  KEY `idx_provider_meta_parent` (`parent_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider meta';

CREATE TABLE IF NOT EXISTS `sys_provider_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'config id',
  `config_type` varchar(64) NOT NULL COMMENT 'storage/llm/ocr/payment',
  `provider_code` varchar(64) NOT NULL COMMENT 'provider code',
  `provider_name` varchar(100) NOT NULL COMMENT 'provider name',
  `endpoint` varchar(255) DEFAULT NULL COMMENT 'endpoint',
  `region` varchar(100) DEFAULT NULL COMMENT 'region',
  `bucket_name` varchar(100) DEFAULT NULL COMMENT 'bucket name',
  `access_key` varchar(500) DEFAULT NULL COMMENT 'access key placeholder or encrypted value',
  `secret_key` varchar(500) DEFAULT NULL COMMENT 'secret key placeholder or encrypted value',
  `domain` varchar(255) DEFAULT NULL COMMENT 'domain',
  `base_path` varchar(255) DEFAULT NULL COMMENT 'base path',
  `ext_params_json` text DEFAULT NULL COMMENT 'extra params json',
  `model_type` varchar(50) DEFAULT NULL COMMENT 'model type',
  `is_default` char(1) NOT NULL DEFAULT 'N' COMMENT 'Y/N',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0 enabled 1 disabled',
  `create_by` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_provider_config_type_provider` (`config_type`, `provider_code`),
  KEY `idx_provider_config_type` (`config_type`),
  KEY `idx_provider_config_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider config';

CREATE TABLE IF NOT EXISTS `sys_provider_api_template` (
  `template_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'template id',
  `config_type` varchar(64) NOT NULL COMMENT 'config type',
  `provider_code` varchar(64) NOT NULL COMMENT 'provider code',
  `operation` varchar(64) NOT NULL COMMENT 'operation',
  `http_method` varchar(16) NOT NULL DEFAULT 'POST' COMMENT 'http method',
  `url_template` varchar(500) NOT NULL COMMENT 'url template',
  `headers_json` text DEFAULT NULL COMMENT 'headers template',
  `body_type` varchar(32) DEFAULT 'json' COMMENT 'body type',
  `body_template` text DEFAULT NULL COMMENT 'body template',
  `response_type` varchar(32) DEFAULT 'json' COMMENT 'response type',
  `response_mapping` text DEFAULT NULL COMMENT 'response mapping',
  `auth_type` varchar(32) DEFAULT 'none' COMMENT 'none/bearer/custom',
  `auth_config_json` text DEFAULT NULL COMMENT 'auth template',
  `timeout` int DEFAULT 30000 COMMENT 'timeout ms',
  `retry_times` int DEFAULT 0 COMMENT 'retry times',
  `is_enabled` char(1) NOT NULL DEFAULT '1' COMMENT '1 enabled 0 disabled',
  `create_by` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_provider_template` (`config_type`, `provider_code`, `operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider api template';

CREATE TABLE IF NOT EXISTS `provider_field_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `provider_code` varchar(50) NOT NULL COMMENT 'provider code',
  `field_key` varchar(50) NOT NULL COMMENT 'field key',
  `field_label` varchar(100) NOT NULL COMMENT 'field label',
  `field_type` varchar(20) NOT NULL DEFAULT 'text' COMMENT 'text/password/number/select/textarea',
  `required` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 no 1 yes',
  `placeholder` varchar(200) DEFAULT NULL COMMENT 'placeholder',
  `default_value` varchar(200) DEFAULT NULL COMMENT 'default value',
  `help_text` varchar(500) DEFAULT NULL COMMENT 'help text',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT 'sort',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_field` (`provider_code`, `field_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider field config';

CREATE TABLE IF NOT EXISTS `sys_provider_credentials` (
  `credential_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'credential id',
  `config_id` bigint NOT NULL COMMENT 'config id',
  `credential_key` varchar(64) NOT NULL COMMENT 'credential key',
  `credential_value` varchar(1000) DEFAULT NULL COMMENT 'encrypted credential or placeholder',
  `is_sensitive` char(1) DEFAULT 'Y' COMMENT 'Y/N',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`credential_id`),
  UNIQUE KEY `uk_provider_credential_config_key` (`config_id`, `credential_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider credentials';

CREATE TABLE IF NOT EXISTS `sys_provider_capability` (
  `capability_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'capability id',
  `config_type` varchar(64) NOT NULL COMMENT 'config type',
  `capability_code` varchar(64) NOT NULL COMMENT 'capability code',
  `capability_name` varchar(100) NOT NULL COMMENT 'capability name',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0 enabled 1 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`capability_id`),
  UNIQUE KEY `uk_provider_capability` (`config_type`, `capability_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider capability';

CREATE TABLE IF NOT EXISTS `sys_provider_model_catalog` (
  `model_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'model id',
  `provider_code` varchar(64) NOT NULL COMMENT 'provider code',
  `model_code` varchar(128) NOT NULL COMMENT 'model code',
  `model_name` varchar(128) NOT NULL COMMENT 'model name',
  `model_type` varchar(50) DEFAULT NULL COMMENT 'model type',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0 enabled 1 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`model_id`),
  UNIQUE KEY `uk_provider_model` (`provider_code`, `model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider model catalog';

CREATE TABLE IF NOT EXISTS `sys_provider_capability_model_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'relation id',
  `capability_id` bigint NOT NULL COMMENT 'capability id',
  `model_id` bigint NOT NULL COMMENT 'model id',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0 enabled 1 disabled',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_provider_capability_model` (`capability_id`, `model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider capability model relation';

CREATE TABLE IF NOT EXISTS `sys_provider_capability_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'log id',
  `config_type` varchar(64) DEFAULT NULL COMMENT 'config type',
  `provider_code` varchar(64) DEFAULT NULL COMMENT 'provider code',
  `operation` varchar(64) DEFAULT NULL COMMENT 'operation',
  `success` char(1) DEFAULT NULL COMMENT 'Y/N',
  `cost_ms` bigint DEFAULT NULL COMMENT 'cost ms',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`log_id`),
  KEY `idx_provider_log_provider` (`provider_code`, `operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider capability log';

CREATE TABLE IF NOT EXISTS `sys_provider_integration_guide` (
  `guide_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'guide id',
  `provider_code` varchar(64) NOT NULL COMMENT 'provider code',
  `guide_title` varchar(128) NOT NULL COMMENT 'guide title',
  `guide_content` text DEFAULT NULL COMMENT 'guide content',
  `sort_order` int DEFAULT 0 COMMENT 'sort',
  PRIMARY KEY (`guide_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='provider integration guide';

INSERT INTO `sys_provider_meta` (`meta_type`, `meta_code`, `meta_name`, `meta_description`, `parent_code`, `status`, `sort_order`) VALUES
('config_type', 'storage', 'Object Storage', 'Local/MinIO/OSS/COS storage', NULL, '0', 1),
('config_type', 'llm', 'LLM', 'Configurable large language model providers', NULL, '0', 2),
('config_type', 'ocr', 'OCR', 'Configurable OCR providers', NULL, '0', 3),
('config_type', 'payment', 'Payment', 'Payment provider placeholders', NULL, '0', 4),
('provider_code', 'local', 'Local Storage', 'Local file storage', 'storage', '0', 1),
('provider_code', 'minio', 'MinIO', 'MinIO object storage', 'storage', '0', 2),
('provider_code', 'openai-gpt', 'OpenAI Compatible', 'OpenAI-compatible LLM endpoint', 'llm', '0', 10),
('provider_code', 'aliyun-qwen', 'Aliyun Qwen', 'Aliyun DashScope LLM endpoint', 'llm', '0', 11),
('provider_code', 'alipay', 'Alipay', 'Payment placeholder', 'payment', '0', 20),
('provider_code', 'wechat-pay', 'WeChat Pay', 'Payment placeholder', 'payment', '0', 21)
ON DUPLICATE KEY UPDATE meta_name = VALUES(meta_name), meta_description = VALUES(meta_description), parent_code = VALUES(parent_code), sort_order = VALUES(sort_order);

INSERT INTO `sys_provider_config` (`config_type`, `provider_code`, `provider_name`, `endpoint`, `access_key`, `secret_key`, `domain`, `base_path`, `ext_params_json`, `model_type`, `is_default`, `status`, `create_by`, `remark`) VALUES
('storage', 'local', 'Local Storage', NULL, NULL, NULL, 'http://localhost:9300', '/data/bigyun/uploadPath', NULL, NULL, 'Y', '0', 'admin', 'Community default local storage'),
('llm', 'openai-gpt', 'OpenAI Compatible', '${OPENAI_BASE_URL}', '${OPENAI_API_KEY}', NULL, NULL, NULL, '{"model":"${OPENAI_MODEL}","temperature":0.7,"maxTokens":2000}', 'chat', 'Y', '1', 'admin', 'Disabled until placeholders are configured'),
('payment', 'alipay', 'Alipay Skeleton', '${ALIPAY_GATEWAY_URL}', '${ALIPAY_APP_ID}', '${ALIPAY_APP_SECRET}', NULL, NULL, '{"merchantId":"${ALIPAY_MERCHANT_ID}"}', NULL, 'N', '1', 'admin', 'Skeleton only, no real payment call'),
('payment', 'wechat-pay', 'WeChat Pay Skeleton', '${WECHAT_PAY_GATEWAY_URL}', '${WECHAT_APP_ID}', '${WECHAT_APP_SECRET}', NULL, NULL, '{"merchantId":"${WECHAT_MERCHANT_ID}"}', NULL, 'N', '1', 'admin', 'Skeleton only, no real payment call')
ON DUPLICATE KEY UPDATE provider_name = VALUES(provider_name), endpoint = VALUES(endpoint), access_key = VALUES(access_key), secret_key = VALUES(secret_key), ext_params_json = VALUES(ext_params_json), status = VALUES(status), remark = VALUES(remark);

INSERT INTO `provider_field_config` (`provider_code`, `field_key`, `field_label`, `field_type`, `required`, `placeholder`, `default_value`, `help_text`, `sort_order`) VALUES
('local', 'basePath', 'Base Path', 'text', 1, '/data/bigyun/uploadPath', '/data/bigyun/uploadPath', 'Local upload directory', 1),
('local', 'domain', 'Domain', 'text', 1, 'http://localhost:9300', 'http://localhost:9300', 'Public file domain', 2),
('openai-gpt', 'apiKey', 'API Key', 'password', 1, '${OPENAI_API_KEY}', NULL, 'Use environment-specific secret management', 1),
('openai-gpt', 'endpoint', 'Endpoint', 'text', 1, '${OPENAI_BASE_URL}', NULL, 'OpenAI-compatible base URL', 2),
('alipay', 'appId', 'App ID', 'password', 1, '${ALIPAY_APP_ID}', NULL, 'Placeholder only', 1),
('alipay', 'merchantId', 'Merchant ID', 'text', 1, '${ALIPAY_MERCHANT_ID}', NULL, 'Placeholder only', 2),
('wechat-pay', 'appId', 'App ID', 'password', 1, '${WECHAT_APP_ID}', NULL, 'Placeholder only', 1),
('wechat-pay', 'merchantId', 'Merchant ID', 'text', 1, '${WECHAT_MERCHANT_ID}', NULL, 'Placeholder only', 2)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label), field_type = VALUES(field_type), placeholder = VALUES(placeholder), help_text = VALUES(help_text), sort_order = VALUES(sort_order);
