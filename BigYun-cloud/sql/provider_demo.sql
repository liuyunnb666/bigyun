-- Provider demo data for community edition. Placeholders are intentionally disabled by default.

INSERT INTO `sys_provider_capability` (`config_type`, `capability_code`, `capability_name`, `status`, `create_time`) VALUES
('llm', 'chat', 'Chat completion', '0', sysdate()),
('storage', 'upload', 'Object upload', '0', sysdate()),
('payment', 'create_order', 'Create payment order skeleton', '0', sysdate())
ON DUPLICATE KEY UPDATE capability_name = VALUES(capability_name), status = VALUES(status);

INSERT INTO `sys_provider_api_template` (`config_type`, `provider_code`, `operation`, `http_method`, `url_template`, `headers_json`, `body_type`, `body_template`, `response_type`, `response_mapping`, `auth_type`, `auth_config_json`, `timeout`, `retry_times`, `is_enabled`, `create_by`, `remark`) VALUES
('llm', 'openai-gpt', 'chat', 'POST', '${endpoint}/chat/completions', '{"Authorization":"Bearer ${accessKey}","Content-Type":"application/json"}', 'json', '{"model":"${model}","messages":${messages},"temperature":${temperature},"max_tokens":${maxTokens}}', 'json', '{"content":"$.choices[0].message.content"}', 'bearer', '{"token":"${accessKey}"}', 30000, 0, '0', 'admin', 'Template disabled until configured'),
('payment', 'alipay', 'create_order', 'POST', '${endpoint}/gateway.do', '{"Content-Type":"application/json"}', 'json', '{"appId":"${accessKey}","merchantId":"${merchantId}","orderNo":"${orderNo}","amount":"${amount}"}', 'json', '{"tradeNo":"$.tradeNo"}', 'custom', '{}', 30000, 0, '0', 'admin', 'Skeleton template only, no real merchant secret'),
('payment', 'wechat-pay', 'create_order', 'POST', '${endpoint}/v3/pay/transactions/native', '{"Content-Type":"application/json"}', 'json', '{"appid":"${accessKey}","mchid":"${merchantId}","out_trade_no":"${orderNo}","amount":{"total":"${amountCent}","currency":"CNY"}}', 'json', '{"tradeNo":"$.prepay_id"}', 'custom', '{}', 30000, 0, '0', 'admin', 'Skeleton template only, no real merchant secret')
ON DUPLICATE KEY UPDATE url_template = VALUES(url_template), body_template = VALUES(body_template), is_enabled = VALUES(is_enabled), remark = VALUES(remark);
