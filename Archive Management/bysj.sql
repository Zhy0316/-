/*
 Navicat Premium Data Transfer

 Source Server         : malo
 Source Server Type    : MySQL
 Source Server Version : 50730
 Source Host           : localhost:3306
 Source Schema         : bysj

 Target Server Type    : MySQL
 Target Server Version : 50730
 File Encoding         : 65001

 Date: 04/03/2026 11:11:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bus_job_application
-- ----------------------------
DROP TABLE IF EXISTS `bus_job_application`;
CREATE TABLE `bus_job_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `recruitment_id` bigint(20) NOT NULL COMMENT '招聘职位ID',
  `student_id` bigint(20) NOT NULL COMMENT '申请学生ID',
  `status` tinyint(2) NULL DEFAULT 0 COMMENT '状态(0:已投递, 1:有意向, 2:不匹配)',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '留言信息',
  `apply_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '求职申请与交流记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bus_recruitment
-- ----------------------------
DROP TABLE IF EXISTS `bus_recruitment`;
CREATE TABLE `bus_recruitment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enterprise_id` bigint(20) NOT NULL COMMENT '发布企业ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '招聘标题',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '职位名称',
  `salary_range` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '薪资范围',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工作地点',
  `requirement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '任职要求',
  `status` tinyint(2) NULL DEFAULT 1 COMMENT '状态(1:招聘中, 0:已结束)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '企业招聘信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for eval_comment
-- ----------------------------
DROP TABLE IF EXISTS `eval_comment`;
CREATE TABLE `eval_comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tutor_id` bigint(20) NOT NULL COMMENT '导师ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '关联的目标ID(如日记ID,作品ID)',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'GENERAL' COMMENT '类型(DIARY, PORTFOLIO, GENERAL-综合评价)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指导意见/评语',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '导师评语表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for eval_growth_score
-- ----------------------------
DROP TABLE IF EXISTS `eval_growth_score`;
CREATE TABLE `eval_growth_score`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `term_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '统计年度/学期',
  `academic_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '学业成绩分',
  `award_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '获奖加分',
  `research_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '科研能力分',
  `practice_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '实践表现分',
  `total_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '综合成长分',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生成长综合评分表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for info_enterprise
-- ----------------------------
DROP TABLE IF EXISTS `info_enterprise`;
CREATE TABLE `info_enterprise`  (
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '企业名称',
  `credit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `industry` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属行业',
  `license_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业执照/认证材料',
  `audit_status` tinyint(2) NULL DEFAULT 0 COMMENT '认证状态(0:待审核, 1:已认证, 2:驳回)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '企业简介',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '企业信息及认证表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for info_student
-- ----------------------------
DROP TABLE IF EXISTS `info_student`;
CREATE TABLE `info_student`  (
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学号',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'M' COMMENT '性别(M/F)',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属学院',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级',
  `enrollment_year` int(4) NULL DEFAULT NULL COMMENT '入学年份',
  `resume_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人简历文件路径',
  `political_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `nation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '民族',
  `native_place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '籍贯',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生档案详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for info_tutor
-- ----------------------------
DROP TABLE IF EXISTS `info_tutor`;
CREATE TABLE `info_tutor`  (
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `tutor_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工号',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称(教授/副教授等)',
  `research_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '研究方向',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '导师信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_academic
-- ----------------------------
DROP TABLE IF EXISTS `record_academic`;
CREATE TABLE `record_academic`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID(sys_user.user_id)',
  `academic_year` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学年',
  `course_nature` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程性质',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `credit` decimal(5, 2) NULL DEFAULT 0.0 COMMENT '学分',
  `gpa` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '绩点',
  `score` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '成绩',
  `is_invalidated` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '否' COMMENT '是否成绩作废',
  `credit_gpa` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '学分绩点',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_year`(`student_id`, `academic_year`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生成绩表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_award
-- ----------------------------
DROP TABLE IF EXISTS `record_award`;
CREATE TABLE `record_award`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `award_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '奖项名称',
  `award_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '级别(国家级/省级/校级)',
  `issuing_authority` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颁发单位',
  `award_date` date NULL DEFAULT NULL COMMENT '获奖日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '证书简介',
  `cert_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '证书文件路径',
  `audit_status` tinyint(2) NULL DEFAULT 0 COMMENT '审核状态(0:待审, 1:通过)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '获奖证书记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_diary
-- ----------------------------
DROP TABLE IF EXISTS `record_diary`;
CREATE TABLE `record_diary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '心情标签',
  `attachment_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件路径',
  `record_date` date NULL DEFAULT NULL COMMENT '记录发生日期',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发布时间',
  `visibility` tinyint(1) NULL DEFAULT 1 COMMENT '可见性(0:私密-仅自己可见, 1:公开-导师可见)',
  `is_starred` tinyint(1) NULL DEFAULT 0 COMMENT '是否星标(0:否, 1:是)',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组/分类名称',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签，多个用逗号分隔',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '成长记录/日记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_portfolio
-- ----------------------------
DROP TABLE IF EXISTS `record_portfolio`;
CREATE TABLE `record_portfolio`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作品标题',
  `work_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作品类型(代码/设计图/文章等)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '作品描述',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作品文件或链接',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片路径',
  `upload_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '个人作品集表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_practice
-- ----------------------------
DROP TABLE IF EXISTS `record_practice`;
CREATE TABLE `record_practice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `activity_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实践/实习主题名称',
  `organization` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '实习单位/组织',
  `start_date` date NULL DEFAULT NULL COMMENT '开始时间',
  `end_date` date NULL DEFAULT NULL COMMENT '结束时间',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '实践内容/心得',
  `proof_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '证明材料',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '社会实践与实习记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_research
-- ----------------------------
DROP TABLE IF EXISTS `record_research`;
CREATE TABLE `record_research`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `project_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目名称',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '担任角色(负责/参与)',
  `start_date` date NULL DEFAULT NULL COMMENT '开始时间',
  `end_date` date NULL DEFAULT NULL COMMENT '结束时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目描述',
  `result_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '成果附件',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科研项目记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符 (admin, student, tutor, hr)',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名/账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(加密存储)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
  `status` tinyint(2) NULL DEFAULT 1 COMMENT '状态(1:正常, 0:禁用)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student_tutor_relation
-- ----------------------------
DROP TABLE IF EXISTS `student_tutor_relation`;
CREATE TABLE `student_tutor_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `tutor_id` bigint(20) NOT NULL COMMENT '导师ID',
  `status` tinyint(2) NULL DEFAULT 0 COMMENT '绑定状态(0:待审核, 1:已绑定, 2:已拒绝)',
  `apply_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `audit_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_tutor`(`student_id`, `tutor_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生导师绑定关系表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;