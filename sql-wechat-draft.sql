CREATE TABLE IF NOT EXISTS `wechat_house_draft` (
  `id` bigint NOT NULL,
  `source_key` varchar(128) NOT NULL,
  `source_platform` varchar(32) DEFAULT 'WECHAT_PC',
  `source_group_key` varchar(128) DEFAULT NULL,
  `source_group_name` varchar(255) DEFAULT NULL,
  `sender_key` varchar(128) DEFAULT NULL,
  `sender_display_name` varchar(255) DEFAULT NULL,
  `message_time` varchar(64) DEFAULT NULL,
  `collector_receive_time` varchar(64) DEFAULT NULL,
  `message_order` int DEFAULT NULL,
  `visible_text` text,
  `raw_payload_json` longtext,
  `extracted_json` longtext,
  `field_confidence_json` longtext,
  `overall_confidence` decimal(5,2) DEFAULT NULL,
  `draft_status` varchar(32) DEFAULT 'PENDING',
  `reviewer_user_id` bigint DEFAULT NULL,
  `review_note` varchar(1024) DEFAULT NULL,
  `created_house_id` bigint DEFAULT NULL,
  `failure_reason` varchar(1024) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wechat_house_draft_source_key` (`source_key`)
);

CREATE TABLE IF NOT EXISTS `wechat_draft_attachment` (
  `id` bigint NOT NULL,
  `draft_id` bigint NOT NULL,
  `source_key` varchar(128) NOT NULL,
  `media_hash` varchar(128) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `mime_type` varchar(128) DEFAULT NULL,
  `content_type` int DEFAULT -1,
  `collector_local_path` varchar(1024) DEFAULT NULL,
  `draft_oss_url` varchar(1024) DEFAULT NULL,
  `draft_oss_object_key` varchar(512) DEFAULT NULL,
  `promotion_status` varchar(32) DEFAULT 'STAGED',
  `promoted_house_attachment_id` bigint DEFAULT NULL,
  `promoted_oss_url` varchar(1024) DEFAULT NULL,
  `promoted_oss_object_key` varchar(512) DEFAULT NULL,
  `correlation_status` varchar(32) DEFAULT NULL,
  `correlation_score` decimal(5,2) DEFAULT NULL,
  `correlation_reason` varchar(1024) DEFAULT NULL,
  `raw_metadata_json` longtext,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wechat_draft_attachment_source_key` (`draft_id`,`source_key`)
);

-- 草稿菜单/权限建议（按实际 parent_id 调整）：
-- INSERT INTO sys_menu (parent_id, name, type, path, component, perms, icon, sort_value, status, create_time, update_time, is_deleted)
-- VALUES
-- (2, '微信房源草稿', 1, 'wechatDraft', 'system/wechatDraft/list', NULL, 'el-icon-chat-line-square', 10, 1, NOW(), NOW(), 0),
-- (<draft_menu_id>, '查看', 2, '', '', 'bnt.wechatDraft.list', '', 1, 1, NOW(), NOW(), 0),
-- (<draft_menu_id>, '详情', 2, '', '', 'bnt.wechatDraft.view', '', 2, 1, NOW(), NOW(), 0),
-- (<draft_menu_id>, '修改', 2, '', '', 'bnt.wechatDraft.update', '', 3, 1, NOW(), NOW(), 0),
-- (<draft_menu_id>, '审批', 2, '', '', 'bnt.wechatDraft.approve', '', 4, 1, NOW(), NOW(), 0),
-- (<draft_menu_id>, '拒绝', 2, '', '', 'bnt.wechatDraft.reject', '', 5, 1, NOW(), NOW(), 0),
-- (<draft_menu_id>, '重试', 2, '', '', 'bnt.wechatDraft.retry', '', 6, 1, NOW(), NOW(), 0);
