-- 为导师表添加学院字段的迁移脚本
-- 执行日期: 2026-03-04

-- 1. 添加学院字段
ALTER TABLE `info_tutor` 
ADD COLUMN `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属学院' AFTER `research_field`;

-- 2. 为现有测试数据更新学院信息（如果存在）
UPDATE `info_tutor` SET `college` = '计算机学院' WHERE `tutor_no` = 'T001';
UPDATE `info_tutor` SET `college` = '计算机学院' WHERE `tutor_no` = 'T002';
UPDATE `info_tutor` SET `college` = '信息学院' WHERE `tutor_no` = 'T003';

-- 3. 验证更新
SELECT * FROM `info_tutor`;
