CREATE TABLE IF NOT EXISTS `tb_game` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `source` varchar(20) NOT NULL COMMENT '来源 STEAM/PSN/SWITCH/CUSTOM',
  `source_game_id` varchar(100) DEFAULT NULL COMMENT '来源游戏编码',
  `game_name` varchar(255) NOT NULL COMMENT '游戏名称',
  `game_alias` varchar(255) DEFAULT NULL COMMENT '博客显示名称',
  `game_intro` varchar(1000) DEFAULT NULL COMMENT '游戏简介或评测摘要',
  `source_cover` varchar(500) DEFAULT NULL COMMENT '来源封面',
  `custom_cover` varchar(500) DEFAULT NULL COMMENT '自定义封面',
  `game_url` varchar(500) DEFAULT NULL COMMENT '游戏链接',
  `platforms` varchar(255) DEFAULT NULL COMMENT '平台编码，逗号分隔',
  `tags` varchar(500) DEFAULT NULL COMMENT '游戏标签，逗号分隔',
  `screenshots` longtext COMMENT '游戏截图URL JSON数组',
  `screenshot_layout` varchar(20) NOT NULL DEFAULT 'CAROUSEL' COMMENT '截图排版 CAROUSEL/FEATURED/GRID',
  `play_status` varchar(20) DEFAULT NULL COMMENT '游玩状态',
  `personal_score` decimal(3,1) DEFAULT NULL COMMENT '个人评分',
  `sort_order` int NOT NULL DEFAULT 9999 COMMENT '排序',
  `is_visible` tinyint NOT NULL DEFAULT 0 COMMENT '是否公开',
  `playtime_forever` int NOT NULL DEFAULT 0 COMMENT '累计游玩分钟',
  `playtime_two_weeks` int NOT NULL DEFAULT 0 COMMENT '近两周游玩分钟',
  `last_played_time` datetime DEFAULT NULL COMMENT '最后游玩时间',
  `review_content` longtext COMMENT 'Markdown评测',
  `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_game` (`source`, `source_game_id`),
  KEY `idx_visible_sort` (`is_visible`, `sort_order`),
  KEY `idx_play_status` (`play_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人游戏档案';

SET @game_source_id_type = (
  SELECT `DATA_TYPE`
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'tb_game'
    AND `COLUMN_NAME` = 'source_game_id'
  LIMIT 1
);
SET @game_source_id_sql = IF(
  @game_source_id_type = 'varchar',
  'SELECT 1',
  'ALTER TABLE `tb_game` MODIFY COLUMN `source_game_id` varchar(100) DEFAULT NULL COMMENT ''来源游戏编码'''
);
PREPARE game_source_id_stmt FROM @game_source_id_sql;
EXECUTE game_source_id_stmt;
DEALLOCATE PREPARE game_source_id_stmt;

SET @game_alias_column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'tb_game'
    AND `COLUMN_NAME` = 'game_alias'
);
SET @game_alias_column_sql = IF(
  @game_alias_column_exists > 0,
  'SELECT 1',
  'ALTER TABLE `tb_game` ADD COLUMN `game_alias` varchar(255) DEFAULT NULL COMMENT ''博客显示名称'' AFTER `game_name`'
);
PREPARE game_alias_column_stmt FROM @game_alias_column_sql;
EXECUTE game_alias_column_stmt;
DEALLOCATE PREPARE game_alias_column_stmt;

SET @game_tags_column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'tb_game'
    AND `COLUMN_NAME` = 'tags'
);
SET @game_tags_column_sql = IF(
  @game_tags_column_exists > 0,
  'SELECT 1',
  'ALTER TABLE `tb_game` ADD COLUMN `tags` varchar(500) DEFAULT NULL COMMENT ''游戏标签，逗号分隔'' AFTER `platforms`'
);
PREPARE game_tags_column_stmt FROM @game_tags_column_sql;
EXECUTE game_tags_column_stmt;
DEALLOCATE PREPARE game_tags_column_stmt;

SET @game_screenshots_column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'tb_game'
    AND `COLUMN_NAME` = 'screenshots'
);
SET @game_screenshots_column_sql = IF(
  @game_screenshots_column_exists > 0,
  'SELECT 1',
  'ALTER TABLE `tb_game` ADD COLUMN `screenshots` longtext COMMENT ''游戏截图URL JSON数组'' AFTER `tags`'
);
PREPARE game_screenshots_column_stmt FROM @game_screenshots_column_sql;
EXECUTE game_screenshots_column_stmt;
DEALLOCATE PREPARE game_screenshots_column_stmt;

SET @game_screenshot_layout_column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'tb_game'
    AND `COLUMN_NAME` = 'screenshot_layout'
);
SET @game_screenshot_layout_column_sql = IF(
  @game_screenshot_layout_column_exists > 0,
  'SELECT 1',
  'ALTER TABLE `tb_game` ADD COLUMN `screenshot_layout` varchar(20) NOT NULL DEFAULT ''CAROUSEL'' COMMENT ''截图排版 CAROUSEL/FEATURED/GRID'' AFTER `screenshots`'
);
PREPARE game_screenshot_layout_column_stmt FROM @game_screenshot_layout_column_sql;
EXECUTE game_screenshot_layout_column_stmt;
DEALLOCATE PREPARE game_screenshot_layout_column_stmt;

CREATE TABLE IF NOT EXISTS `tb_game_field` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` int NOT NULL COMMENT '游戏id',
  `field_name` varchar(100) NOT NULL COMMENT '字段名称',
  `field_value` text COMMENT '字段值',
  `field_unit` varchar(30) DEFAULT NULL COMMENT '单位',
  `field_type` varchar(20) NOT NULL DEFAULT 'TEXT' COMMENT '字段类型',
  `group_name` varchar(100) NOT NULL DEFAULT '游戏数据' COMMENT '字段分组',
  `show_on_card` tinyint NOT NULL DEFAULT 0 COMMENT '是否在列表展示',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_game_sort` (`game_id`, `sort_order`),
  KEY `idx_game_card` (`game_id`, `show_on_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏自定义字段';

CREATE TABLE IF NOT EXISTS `tb_game_screenshot` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` int NOT NULL COMMENT '游戏id',
  `original_url` varchar(500) NOT NULL COMMENT '原图地址，仅后台使用',
  `display_url` varchar(500) NOT NULL COMMENT '前台展示图地址',
  `original_width` int DEFAULT NULL COMMENT '原图宽度',
  `original_height` int DEFAULT NULL COMMENT '原图高度',
  `display_width` int DEFAULT NULL COMMENT '展示图宽度',
  `display_height` int DEFAULT NULL COMMENT '展示图高度',
  `frame_type` varchar(20) NOT NULL DEFAULT 'AUTO' COMMENT '画框 AUTO/LANDSCAPE/PORTRAIT/SQUARE',
  `column_span` int NOT NULL DEFAULT 6 COMMENT '十二列网格占用列数 4/6/8/12',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_game_sort` (`game_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏截图排版';

CREATE TABLE IF NOT EXISTS `tb_game_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `platform` varchar(20) NOT NULL COMMENT '平台编码',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_config_key` (`platform`, `config_key`),
  KEY `idx_platform_sort` (`platform`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏平台同步配置';

UPDATE `tb_game`
SET `play_status` = 'PLAYED'
WHERE `play_status` IN ('COMPLETED', 'PAUSED');

UPDATE `tb_game`
SET `platforms` = REPLACE(REPLACE(REPLACE(REPLACE(`platforms`,
    'WEGAME', 'PC'), 'BATTLE_NET', 'PC'), 'XBOX', 'PC'), 'OTHER', 'PC')
WHERE `platforms` REGEXP 'WEGAME|BATTLE_NET|XBOX|OTHER';

UPDATE `tb_game`
SET `platforms` = 'PC'
WHERE `platforms` IS NULL OR TRIM(`platforms`) = '';

INSERT INTO `tb_game_config`
  (`platform`, `config_name`, `config_key`, `config_value`, `sort_order`, `create_time`)
SELECT 'STEAM', 'Steam Web API Key', 'STEAM_WEB_API_KEY', '', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM `tb_game_config`
  WHERE `platform` = 'STEAM' AND `config_key` = 'STEAM_WEB_API_KEY'
);

INSERT INTO `tb_game_config`
  (`platform`, `config_name`, `config_key`, `config_value`, `sort_order`, `create_time`)
SELECT 'STEAM', 'Steam ID', 'STEAM_ID', '', 2, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM `tb_game_config`
  WHERE `platform` = 'STEAM' AND `config_key` = 'STEAM_ID'
);

INSERT INTO `tb_menu` (`id`, `name`, `path`, `component`, `icon`, `create_time`, `order_num`, `parent_id`, `is_hidden`)
SELECT 9001, '游戏管理', '/game-submenu', 'Layout', 'el-icon-mydashujukeshihuaico-', CURRENT_TIMESTAMP, 6, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `id` = 9001 OR `path` = '/game-submenu');

INSERT INTO `tb_menu` (`id`, `name`, `path`, `component`, `icon`, `create_time`, `order_num`, `parent_id`, `is_hidden`)
SELECT 9002, '游戏列表', '/games', '/game/GameList.vue', 'el-icon-mydashujukeshihuaico-', CURRENT_TIMESTAMP, 1, 9001, 0
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `id` = 9002 OR (`path` = '/games' AND `parent_id` = 9001));

INSERT INTO `tb_menu` (`id`, `name`, `path`, `component`, `icon`, `create_time`, `order_num`, `parent_id`, `is_hidden`)
SELECT 9003, '编辑游戏', '/games/edit/:gameId', '/game/GameEdit.vue', 'el-icon-myfabiaowenzhang', CURRENT_TIMESTAMP, 2, 9001, 1
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `id` = 9003 OR `path` = '/games/edit/:gameId');

INSERT INTO `tb_menu` (`id`, `name`, `path`, `component`, `icon`, `create_time`, `order_num`, `parent_id`, `is_hidden`)
SELECT 9004, '同步配置', '/game-config', '/game/GameConfig.vue', 'el-icon-setting', CURRENT_TIMESTAMP, 2, 9001, 0
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `id` = 9004 OR `path` = '/game-config');

INSERT INTO `tb_role_menu` (`role_id`, `menu_id`)
SELECT `id`, 9001 FROM `tb_role` r
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `tb_role_menu` rm WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = 9001);

INSERT INTO `tb_role_menu` (`role_id`, `menu_id`)
SELECT `id`, 9002 FROM `tb_role` r
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `tb_role_menu` rm WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = 9002);

INSERT INTO `tb_role_menu` (`role_id`, `menu_id`)
SELECT `id`, 9003 FROM `tb_role` r
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `tb_role_menu` rm WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = 9003);

INSERT INTO `tb_role_menu` (`role_id`, `menu_id`)
SELECT `id`, 9004 FROM `tb_role` r
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `tb_role_menu` rm WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = 9004);

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9100, '游戏档案模块', NULL, NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `id` = 9100 OR `resource_name` = '游戏档案模块');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9101, '查看公开游戏列表', '/games/list', 'POST', 9100, 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/games/list' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9102, '查看公开游戏详情', '/games/detail', 'POST', 9100, 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/games/detail' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9103, '查看后台游戏列表', '/admin/games/list', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/list' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9104, '查看后台游戏详情', '/admin/games/detail', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/detail' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9105, '保存或修改游戏', '/admin/games/save', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/save' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9106, '删除自定义游戏', '/admin/games/delete', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/delete' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9107, '同步Steam游戏', '/admin/games/steam/sync', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/steam/sync' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9108, '上传游戏图片', '/admin/games/images', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/images' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9109, '查看游戏平台同步配置', '/admin/games/config/list', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/config/list' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9110, '保存游戏平台同步配置', '/admin/games/config/save', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/config/save' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9111, '一次性同步PSN游戏', '/admin/games/psn/sync-once', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/psn/sync-once' AND `request_method` = 'POST');

INSERT INTO `tb_resource` (`id`, `resource_name`, `url`, `request_method`, `parent_id`, `is_anonymous`, `create_time`)
SELECT 9112, '读取游戏图片源文件', '/admin/games/images/source', 'POST', 9100, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM `tb_resource` WHERE `url` = '/admin/games/images/source' AND `request_method` = 'POST');

INSERT INTO `tb_role_resource` (`role_id`, `resource_id`)
SELECT r.`id`, re.`id`
FROM `tb_role` r
JOIN `tb_resource` re ON re.`id` BETWEEN 9103 AND 9110
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `tb_role_resource` rr
    WHERE rr.`role_id` = r.`id` AND rr.`resource_id` = re.`id`
  );

INSERT INTO `tb_role_resource` (`role_id`, `resource_id`)
SELECT r.`id`, re.`id`
FROM `tb_role` r
JOIN `tb_resource` re ON re.`id` = 9111
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `tb_role_resource` rr
    WHERE rr.`role_id` = r.`id` AND rr.`resource_id` = re.`id`
  );

INSERT INTO `tb_role_resource` (`role_id`, `resource_id`)
SELECT r.`id`, re.`id`
FROM `tb_role` r
JOIN `tb_resource` re ON re.`id` = 9112
WHERE r.`role_label` = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `tb_role_resource` rr
    WHERE rr.`role_id` = r.`id` AND rr.`resource_id` = re.`id`
  );

DELETE rr
FROM `tb_role_resource` rr
JOIN `tb_resource` re ON re.`id` = rr.`resource_id`
WHERE re.`url` IN ('/admin/games/switch/auth-once', '/admin/games/switch/sync-once');

DELETE FROM `tb_resource`
WHERE `url` IN ('/admin/games/switch/auth-once', '/admin/games/switch/sync-once');
