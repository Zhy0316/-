-- ----------------------------
-- 给student_tutor_relation表添加驳回备注字段
-- ----------------------------
ALTER TABLE `student_tutor_relation`
  ADD COLUMN `reject_note` text COMMENT '驳回备注' AFTER `attach_file`;

-- ----------------------------
-- 给record_award表添加驳回备注字段
-- ----------------------------
ALTER TABLE `record_award`
  ADD COLUMN `reject_note` text COMMENT '驳回备注' AFTER `cert_file`;