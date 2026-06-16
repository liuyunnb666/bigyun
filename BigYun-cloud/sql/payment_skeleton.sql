-- BigYun payment skeleton schema. This file contains no real merchant secrets.

CREATE TABLE IF NOT EXISTS `payment_channel_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'config id',
  `channel_code` varchar(32) NOT NULL COMMENT 'ALIPAY/WECHAT',
  `channel_name` varchar(64) NOT NULL COMMENT 'channel name',
  `app_id` varchar(128) DEFAULT NULL COMMENT 'app id placeholder',
  `app_secret` varchar(500) DEFAULT NULL COMMENT 'secret placeholder; store encrypted value in production',
  `merchant_id` varchar(128) DEFAULT NULL COMMENT 'merchant id placeholder',
  `notify_url` varchar(255) DEFAULT NULL COMMENT 'notify callback url',
  `return_url` varchar(255) DEFAULT NULL COMMENT 'return url',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT 'status 0 enabled 1 disabled',
  `create_by` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_payment_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='payment channel skeleton config';

CREATE TABLE IF NOT EXISTS `payment_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'order id',
  `order_no` varchar(64) NOT NULL COMMENT 'payment order no',
  `business_id` bigint NOT NULL COMMENT 'business id',
  `business_type` varchar(64) NOT NULL COMMENT 'business type',
  `channel_code` varchar(32) NOT NULL COMMENT 'payment channel',
  `amount` decimal(12,2) DEFAULT 0.00 COMMENT 'amount',
  `currency` varchar(8) NOT NULL DEFAULT 'CNY' COMMENT 'currency',
  `pay_status` varchar(32) NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/CLOSED/REFUNDING/REFUNDED',
  `trade_no` varchar(128) DEFAULT NULL COMMENT 'third-party trade no placeholder',
  `request_snapshot` text DEFAULT NULL COMMENT 'request snapshot without secrets',
  `response_snapshot` text DEFAULT NULL COMMENT 'response snapshot without secrets',
  `create_by` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_payment_order_no` (`order_no`),
  KEY `idx_payment_business` (`business_type`, `business_id`),
  KEY `idx_payment_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='payment order skeleton';

INSERT INTO `payment_channel_config` (`channel_code`, `channel_name`, `app_id`, `app_secret`, `merchant_id`, `notify_url`, `return_url`, `status`, `create_by`, `remark`) VALUES
('ALIPAY', 'Alipay sandbox skeleton', '${ALIPAY_APP_ID}', '${ALIPAY_APP_SECRET}', '${ALIPAY_MERCHANT_ID}', 'http://localhost:8080/payment/notify/alipay', 'http://localhost:8080/payment/return/alipay', '1', 'admin', 'Disabled by default; replace placeholders before use'),
('WECHAT', 'WeChat Pay sandbox skeleton', '${WECHAT_APP_ID}', '${WECHAT_APP_SECRET}', '${WECHAT_MERCHANT_ID}', 'http://localhost:8080/payment/notify/wechat', 'http://localhost:8080/payment/return/wechat', '1', 'admin', 'Disabled by default; replace placeholders before use')
ON DUPLICATE KEY UPDATE channel_name = VALUES(channel_name), app_id = VALUES(app_id), app_secret = VALUES(app_secret), merchant_id = VALUES(merchant_id), status = VALUES(status), remark = VALUES(remark);

INSERT INTO sys_menu VALUES (2200, '支付骨架', 0, 7, 'pay', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', NULL, 'Payment skeleton menu')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), path = VALUES(path);
INSERT INTO sys_menu VALUES (2201, '支付示例', 2200, 1, 'demo', 'pay/demo/index', '', '', 1, 0, 'C', '0', '0', 'pay:order:list', 'money', 'admin', sysdate(), '', NULL, 'Payment skeleton demo')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2202, '支付订单', 2200, 2, 'order', 'pay/demo/index', '', '', 1, 0, 'C', '0', '0', 'pay:order:list', 'order', 'admin', sysdate(), '', NULL, 'Payment order skeleton')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2203, '渠道查询', 2201, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:config:query', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2204, '渠道新增', 2201, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:config:add', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2205, '渠道修改', 2201, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:config:edit', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2206, '渠道删除', 2201, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:config:remove', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2207, '订单查询', 2202, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:order:query', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2208, '订单新增', 2202, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:order:add', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2209, '订单修改', 2202, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:order:edit', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2210, '订单删除', 2202, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pay:order:remove', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
