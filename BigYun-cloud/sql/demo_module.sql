-- BigYun community demo module schema

CREATE TABLE IF NOT EXISTS `demo_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'item id',
  `item_code` varchar(64) NOT NULL COMMENT 'item code',
  `item_name` varchar(100) NOT NULL COMMENT 'item name',
  `category` varchar(64) DEFAULT NULL COMMENT 'category',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT 'status 0 enabled 1 disabled',
  `create_by` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uk_demo_item_code` (`item_code`),
  KEY `idx_demo_item_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='community demo item';

INSERT INTO `demo_item` (`item_code`, `item_name`, `category`, `status`, `create_by`, `create_time`, `remark`) VALUES
('DEMO-001', 'Community demo item', 'sample', '0', 'admin', sysdate(), 'Seed item for community edition')
ON DUPLICATE KEY UPDATE `item_name` = VALUES(`item_name`), `category` = VALUES(`category`), `status` = VALUES(`status`);

-- Optional menu permissions for RuoYi/BigYun admin UI.
INSERT INTO sys_menu VALUES (2100, 'Demo管理', 0, 6, 'demo', NULL, '', '', 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), '', NULL, 'Community demo menu')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), path = VALUES(path), perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2101, 'Demo Item', 2100, 1, 'index', 'demo/index', '', '', 1, 0, 'C', '0', '0', 'demo:item:list', 'list', 'admin', sysdate(), '', NULL, 'Demo item list')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2102, 'Demo查询', 2101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'demo:item:query', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2103, 'Demo新增', 2101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'demo:item:add', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2104, 'Demo修改', 2101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'demo:item:edit', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
INSERT INTO sys_menu VALUES (2105, 'Demo删除', 2101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'demo:item:remove', '#', 'admin', sysdate(), '', NULL, '')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);
