-- ----------------------------
-- 待办消息表扩展：给msg_message添加type字段区分普通消息和系统通知
-- ----------------------------
ALTER TABLE `msg_message` 
ADD COLUMN `type` VARCHAR(20) NULL DEFAULT 'NORMAL' COMMENT '消息类型：NORMAL普通消息，SYSTEM系统通知' AFTER `conversation_key`,
ADD COLUMN `related_id` BIGINT NULL COMMENT '关联ID（绑定申请ID或获奖申请ID）' AFTER `type`;

-- ----------------------------
-- 添加待办消息机器人用户（如果不存在的话）
-- 系统消息机器人用户ID设为0
-- ----------------------------
INSERT IGNORE INTO `sys_user` (`user_id`, `username`, `password`, `real_name`, `status`, `create_time`) 
VALUES (0, 'todo_bot', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '待办消息', 1, NOW());

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) 
SELECT 0, `role_id` FROM `sys_role` WHERE `role_key` = 'admin' LIMIT 1;


