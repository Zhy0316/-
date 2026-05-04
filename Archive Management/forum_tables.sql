

-- ============================================
-- 学习论坛/帖子模块
-- ============================================

-- ----------------------------
-- 论坛帖子主表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布者ID',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发布者角色(student/tutor/hr)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '帖子标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '帖子正文',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'article' COMMENT '内容类型(article文章/video视频/mixed混合)',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '学习分享' COMMENT '分类(学习分享/资源推荐/经验交流/提问求助/其他)',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞次数',
  `collect_count` int(11) NULL DEFAULT 0 COMMENT '收藏次数',
  `comment_count` int(11) NULL DEFAULT 0 COMMENT '评论次数',
  `is_top` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶(0否/1是)',
  `is_essence` tinyint(1) NULL DEFAULT 0 COMMENT '是否精华(0否/1是)',
  `status` tinyint(2) NULL DEFAULT 1 COMMENT '状态(0草稿/1已发布/2已删除)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '论坛帖子主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 帖子资源表（附件：视频、PPT、Word等）
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_resource`;
CREATE TABLE `forum_post_resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源类型(video视频/document文档/audio音频/image图片/other其他)',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件存储路径',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '缩略图路径(视频/图片)',
  `duration` int(11) NULL DEFAULT NULL COMMENT '时长/时长秒数(音视频)',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帖子资源/附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 帖子点赞表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_like`;
CREATE TABLE `forum_post_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id`, `user_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帖子点赞记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 帖子收藏表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_collect`;
CREATE TABLE `forum_post_collect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '收藏用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id`, `user_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帖子收藏记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 帖子评论表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_comment`;
CREATE TABLE `forum_post_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '父评论ID(用于回复，NULL为顶级评论)',
  `user_id` bigint(20) NOT NULL COMMENT '评论者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞次数',
  `status` tinyint(2) NULL DEFAULT 1 COMMENT '状态(0删除/1正常)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '评论时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id`) USING BTREE,
  INDEX `idx_parent`(`parent_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帖子评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 评论点赞表
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment_like`;
CREATE TABLE `forum_comment_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id`, `user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评论点赞记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 帖子浏览记录表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_view`;
CREATE TABLE `forum_post_view` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '浏览用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '浏览时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帖子浏览记录表' ROW_FORMAT = Dynamic;
