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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '求职申请与交流记录' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '企业招聘信息表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '导师评语表' ROW_FORMAT = Dynamic;

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
  `ai_analysis` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'AI成长分析报告',
  `ai_resume` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'AI生成简历',
  `ai_learning_advice` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'AI个性化学习建议',
  `ai_update_time` datetime(0) NULL DEFAULT NULL COMMENT 'AI分析最后更新时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学生成长综合评分表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '企业信息及认证表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for info_student
-- ----------------------------
DROP TABLE IF EXISTS `info_student`;
CREATE TABLE `info_student`  (
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学号',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'M' COMMENT '性别(M/F)',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属学院',
  `warning_flag` tinyint(1) NULL DEFAULT 0 COMMENT '预警标记(0正常/1需关注)',
  `warning_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预警原因',
  `last_active_time` datetime(0) NULL DEFAULT NULL COMMENT '最后活跃时间',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级',
  `enrollment_year` int(4) NULL DEFAULT NULL COMMENT '入学年份',
  `resume_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人简历文件路径',
  `political_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `nation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '民族',
  `native_place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '籍贯',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `grade` int(4) NULL DEFAULT NULL COMMENT '年级',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学生档案详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for info_tutor
-- ----------------------------
DROP TABLE IF EXISTS `info_tutor`;
CREATE TABLE `info_tutor`  (
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `tutor_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工号',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称(教授/副教授等)',
  `research_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '研究方向',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属学院',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '导师信息表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学生成绩表' ROW_FORMAT = Dynamic;

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
  `award_level_id` bigint(20) NULL DEFAULT NULL COMMENT '获奖等级ID(关联dict_award_level)',
  `award_score` int(11) NULL DEFAULT 0 COMMENT '获奖分数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '获奖证书记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_diary
-- ----------------------------
DROP TABLE IF EXISTS `record_diary`;
CREATE TABLE `record_diary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '情绪标签',
  `is_milestone` tinyint(1) NULL DEFAULT 0 COMMENT '是否里程碑事件',
  `tutor_visible` tinyint(1) NULL DEFAULT 0 COMMENT '导师可见',
  `tutor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '导师批注',
  `tutor_comment_time` datetime(0) NULL DEFAULT NULL COMMENT '导师批注时间',
  `attachment_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件路径',
  `record_date` date NULL DEFAULT NULL COMMENT '记录发生日期',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发布时间',
  `visibility` tinyint(1) NULL DEFAULT 1 COMMENT '可见性(0:私密-仅自己可见, 1:公开-导师可见)',
  `is_starred` tinyint(1) NULL DEFAULT 0 COMMENT '是否星标(0:否, 1:是)',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组/分类名称',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签，多个用逗号分隔',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_time` (`student_id`,`record_date`),
  KEY `idx_milestone` (`is_milestone`),
  KEY `idx_tutor_visible` (`tutor_visible`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '成长记录/日记表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '个人作品集表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '社会实践与实习记录' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '科研项目记录' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student_tutor_relation
-- ----------------------------
DROP TABLE IF EXISTS `student_tutor_relation`;
CREATE TABLE `student_tutor_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `tutor_id` bigint(20) NOT NULL COMMENT '导师ID',
  `status` tinyint(2) NULL DEFAULT 0 COMMENT '绑定状态(0:待审核, 1:已绑定, 2:已拒绝)',
  `apply_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `audit_time` datetime(0) NULL DEFAULT NULL,
  `apply_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '学生自荐信',
  `attach_file` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '附件路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学生导师绑定关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dict_award_level
-- ----------------------------
DROP TABLE IF EXISTS `dict_award_level`;
CREATE TABLE `dict_award_level`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级名称',
  `level_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级代码',
  `score` int(11) NULL DEFAULT 0 COMMENT '对应分数',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level_code`(`level_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '获奖等级字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dict_award_level
-- ----------------------------
INSERT INTO `dict_award_level` VALUES (1, '国家级', 'NATIONAL', 50, 1, '国家奖学金、全国竞赛一等奖等');
INSERT INTO `dict_award_level` VALUES (2, '省级', 'PROVINCIAL', 30, 2, '省级竞赛、省优秀毕业生等');
INSERT INTO `dict_award_level` VALUES (3, '校级', 'SCHOOL', 15, 3, '校级奖学金、校级竞赛等');
INSERT INTO `dict_award_level` VALUES (4, '院级', 'COLLEGE', 8, 4, '院级表彰等');
INSERT INTO `dict_award_level` VALUES (5, '其他', 'OTHER', 3, 5, '社团表彰等');

-- ----------------------------
-- Table structure for msg_message
-- ----------------------------
DROP TABLE IF EXISTS `msg_message`;
CREATE TABLE `msg_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读(0:未读,1:已读)',
  `conversation_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话唯一键(小ID_大ID)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation`(`conversation_key`) USING BTREE,
  INDEX `idx_to_user`(`to_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '站内消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learn_resource
-- ----------------------------
DROP TABLE IF EXISTS `learn_resource`;
CREATE TABLE `learn_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '其他' COMMENT '分类(课程资料/竞赛资源/就业指导/其他)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源描述',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件路径',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外链地址',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者ID',
  `uploader_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上传者角色',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞次数',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学习资源共享表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 招聘表新增字段
-- ----------------------------
ALTER TABLE `bus_recruitment`
  ADD COLUMN `job_type` varchar(20) NULL DEFAULT '全职' COMMENT '职位类型(全职/兼职/实习)' AFTER `requirement`,
  ADD COLUMN `headcount` int(4) NULL DEFAULT 1 COMMENT '招聘人数' AFTER `job_type`,
  ADD COLUMN `deadline` date NULL COMMENT '截止日期' AFTER `headcount`,
  ADD COLUMN `education` varchar(20) NULL DEFAULT '本科' COMMENT '学历要求' AFTER `deadline`,
  ADD COLUMN `experience` varchar(30) NULL COMMENT '工作经验要求' AFTER `education`,
  ADD COLUMN `description` text NULL COMMENT '职位详细描述' AFTER `experience`;

-- ----------------------------
-- 投递表新增字段
-- ----------------------------
ALTER TABLE `bus_job_application`
  ADD COLUMN `interview_time` datetime NULL COMMENT '面试时间' AFTER `apply_time`,
  ADD COLUMN `interview_note` varchar(500) NULL COMMENT '面试备注/通知内容' AFTER `interview_time`,
  ADD COLUMN `final_status` tinyint(2) NULL DEFAULT 0 COMMENT '最终状态(0待筛选/1简历通过/2面试邀请/3录用/4淘汰)' AFTER `interview_note`;

-- ----------------------------
-- 日记标签字典表
-- ----------------------------
DROP TABLE IF EXISTS `dict_diary_tag`;
CREATE TABLE `dict_diary_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
  `tag_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签类型(emotion情绪/theme主题/milestone里程碑)',
  `tag_icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签图标(emoji或图标类名)',
  `tag_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签颜色(hex颜色值)',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序序号',
  `is_system` tinyint(1) NULL DEFAULT 1 COMMENT '是否系统标签(1是/0否)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`tag_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '日记标签字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 日记标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `diary_tag_relation`;
CREATE TABLE `diary_tag_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `diary_id` bigint(20) NOT NULL COMMENT '日记ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_diary`(`diary_id`) USING BTREE,
  INDEX `idx_tag`(`tag_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '日记标签关联表' ROW_FORMAT = Dynamic;

-- 插入系统标签
INSERT INTO `dict_diary_tag` (`tag_name`, `tag_type`, `tag_icon`, `tag_color`, `sort_order`, `is_system`) VALUES
('😊 开心', 'emotion', '😊', '#67C23A', 1, 1),
('😰 焦虑', 'emotion', '😰', '#E6A23C', 2, 1),
('💪 奋斗', 'emotion', '💪', '#409EFF', 3, 1),
('🤔 迷茫', 'emotion', '🤔', '#909399', 4, 1),
('😢 难过', 'emotion', '😢', '#F56C6C', 5, 1),
('😌 平静', 'emotion', '😌', '#95D475', 6, 1),
('🎉 兴奋', 'emotion', '🎉', '#FF6B9D', 7, 1),
('😴 疲惫', 'emotion', '😴', '#C0C4CC', 8, 1),
('📚 学习', 'theme', '📚', '#409EFF', 10, 1),
('🎭 社团', 'theme', '🎭', '#E6A23C', 11, 1),
('💼 实习', 'theme', '💼', '#67C23A', 12, 1),
('👥 人际关系', 'theme', '👥', '#FF6B9D', 13, 1),
('🎯 职业规划', 'theme', '🎯', '#F56C6C', 14, 1),
('🏃 运动健身', 'theme', '🏃', '#95D475', 15, 1),
('🎨 兴趣爱好', 'theme', '🎨', '#C77EB5', 16, 1),
('💡 思考感悟', 'theme', '💡', '#FFA500', 17, 1),
('🌟 生活日常', 'theme', '🌟', '#87CEEB', 18, 1),
('🎯 第一次获奖', 'milestone', '🎯', '#F56C6C', 20, 1),
('🚀 第一个项目', 'milestone', '🚀', '#409EFF', 21, 1),
('📖 通过重要考试', 'milestone', '📖', '#67C23A', 22, 1),
('💼 拿到offer', 'milestone', '💼', '#E6A23C', 23, 1),
('🎓 论文发表', 'milestone', '🎓', '#C77EB5', 24, 1),
('🏆 竞赛获奖', 'milestone', '🏆', '#FFD700', 25, 1),
('👔 第一次实习', 'milestone', '👔', '#95D475', 26, 1),
('🎪 重要活动', 'milestone', '🎪', '#FF6B9D', 27, 1);

-- ----------------------------
-- 导师指导记录表
-- ----------------------------
DROP TABLE IF EXISTS `tutor_guidance_record`;
CREATE TABLE `tutor_guidance_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `tutor_id` bigint(20) NOT NULL COMMENT '导师ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `guidance_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指导类型(线上/线下/电话/邮件)',
  `guidance_topic` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指导主题',
  `guidance_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '指导内容',
  `guidance_date` date NULL DEFAULT NULL COMMENT '指导日期',
  `duration` int(11) NULL DEFAULT NULL COMMENT '时长(分钟)',
  `student_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '学生反馈',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tutor`(`tutor_id`) USING BTREE,
  INDEX `idx_student`(`student_id`) USING BTREE,
  INDEX `idx_date`(`guidance_date`) USING BTREE,
  KEY `idx_tutor_student` (`tutor_id`,`student_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '导师指导记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 学生目标表
-- ----------------------------
DROP TABLE IF EXISTS `student_goal`;
CREATE TABLE `student_goal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `tutor_id` bigint(20) NULL DEFAULT NULL COMMENT '指导导师ID',
  `goal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标类型(学期/学年/长期)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '目标描述',
  `target_date` date NULL DEFAULT NULL COMMENT '目标日期',
  `status` tinyint(2) NULL DEFAULT 0 COMMENT '状态(0进行中/1已完成/2已放弃)',
  `progress` int(11) NULL DEFAULT 0 COMMENT '完成进度0-100',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `complete_time` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_status`(`student_id`, `status`) USING BTREE,
  INDEX `idx_tutor`(`tutor_id`) USING BTREE,
  KEY `idx_goal_type` (`goal_type`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学生成长目标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 导师预约表
-- ----------------------------
DROP TABLE IF EXISTS `tutor_appointment`;
CREATE TABLE `tutor_appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `tutor_id` bigint(20) NOT NULL COMMENT '导师ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `appointment_time` datetime(0) NOT NULL COMMENT '预约时间',
  `duration` int(11) NULL DEFAULT 30 COMMENT '时长(分钟)',
  `topic` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '咨询主题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '问题描述',
  `status` tinyint(2) NULL DEFAULT 0 COMMENT '状态(0待确认/1已确认/2已完成/3已取消)',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '反馈记录',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tutor_time`(`tutor_id`, `appointment_time`) USING BTREE,
  INDEX `idx_student`(`student_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  KEY `idx_appointment_status` (`tutor_id`,`status`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '导师预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 导师批量消息表
-- ----------------------------
DROP TABLE IF EXISTS `tutor_batch_message`;
CREATE TABLE `tutor_batch_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tutor_id` bigint(20) NOT NULL COMMENT '导师ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标类型(all全部/selected指定/warning预警)',
  `target_students` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '目标学生ID列表(JSON数组)',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发送时间',
  `read_count` int(11) NULL DEFAULT 0 COMMENT '已读数量',
  `total_count` int(11) NULL DEFAULT 0 COMMENT '总发送数量',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tutor`(`tutor_id`) USING BTREE,
  INDEX `idx_send_time`(`send_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '导师批量消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 成长报告表
-- ----------------------------
DROP TABLE IF EXISTS `growth_report`;
CREATE TABLE `growth_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `report_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报告类型(semester学期/year学年/custom自定义)',
  `term_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '统计周期(如2023-2024-1)',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报告标题',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '报告摘要',
  `academic_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '学业表现总结',
  `ability_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '能力发展总结',
  `achievement_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '荣誉成就总结',
  `growth_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '成长轨迹总结',
  `suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '未来建议',
  `report_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '报告详细数据(JSON格式)',
  `pdf_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'PDF文件路径',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '查看次数',
  `status` tinyint(2) NULL DEFAULT 1 COMMENT '状态(0草稿/1已生成/2已归档)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_term`(`student_id`, `term_year`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '学生成长报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 报告分享表
-- ----------------------------
DROP TABLE IF EXISTS `growth_report_share`;
CREATE TABLE `growth_report_share` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分享ID',
  `report_id` bigint(20) NOT NULL COMMENT '报告ID',
  `share_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享码',
  `share_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享类型(public公开/tutor导师/enterprise企业)',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '查看次数',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_share_code`(`share_code`) USING BTREE,
  INDEX `idx_report`(`report_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '成长报告分享记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 报告模板表
-- ----------------------------
DROP TABLE IF EXISTS `growth_report_template`;
CREATE TABLE `growth_report_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `template_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板类型(semester/year/custom)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '模板描述',
  `sections` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报告章节配置(JSON格式)',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认模板',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '成长报告模板表' ROW_FORMAT = Dynamic;

-- 插入模板
INSERT INTO `growth_report_template` VALUES 
(1, '学期成长报告', 'semester', '标准学期成长报告模板', '[{"section":"academic","title":"学业表现","weight":0.4},{"section":"ability","title":"能力发展","weight":0.2},{"section":"achievement","title":"荣誉成就","weight":0.2},{"section":"growth","title":"成长轨迹","weight":0.2}]', 1, 1, NOW()),
(2, '学年成长报告', 'year', '标准学年成长报告模板', '[{"section":"academic","title":"学业表现","weight":0.35},{"section":"ability","title":"能力发展","weight":0.25},{"section":"achievement","title":"荣誉成就","weight":0.2},{"section":"growth","title":"成长轨迹","weight":0.2}]', 1, 2, NOW());

-- 插入测试数据
INSERT INTO `tutor_guidance_record` (`tutor_id`, `student_id`, `guidance_type`, `guidance_topic`, `guidance_content`, `guidance_date`, `duration`) VALUES
(2, 5, '线下', '学业规划讨论', '讨论了下学期的选课计划和学习目标，建议加强数学基础课程的学习。', '2026-04-15', 60),
(2, 5, '线上', '实习准备指导', '指导简历撰写和面试技巧，推荐了几家适合的实习企业。', '2026-04-20', 45),
(3, 6, '线下', '科研项目讨论', '讨论了毕业设计选题方向，确定了研究课题和实施计划。', '2026-04-18', 90);

INSERT INTO `student_goal` (`student_id`, `tutor_id`, `goal_type`, `title`, `description`, `target_date`, `status`, `progress`) VALUES
(5, 2, '学期', '提升GPA到3.5以上', '本学期重点提升数学和专业课成绩，争取GPA达到3.5', '2026-07-01', 0, 60),
(5, 2, '学年', '获得国家级竞赛奖项', '参加全国大学生数学建模竞赛，争取获得国家级奖项', '2026-12-31', 0, 30),
(6, 3, '学期', '完成毕业设计开题', '完成毕业设计的文献综述和开题报告', '2026-06-15', 0, 80);

INSERT INTO `tutor_appointment` (`tutor_id`, `student_id`, `appointment_time`, `duration`, `topic`, `description`, `status`) VALUES
(2, 5, '2026-05-10 14:00:00', 60, '期中考试复习指导', '希望导师帮忙梳理高等数学的重点知识', 1),
(3, 6, '2026-05-12 10:00:00', 90, '毕业设计进度汇报', '汇报毕业设计的最新进展，讨论遇到的技术问题', 0);

SET FOREIGN_KEY_CHECKS = 1;