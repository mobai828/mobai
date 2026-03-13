-- =====================================================
-- 博客系统增强版 - 完整建表语句
-- 数据库: MySQL 5.7+ / MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `intro` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `role` VARCHAR(10) DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
    `email_verified` TINYINT DEFAULT 0 COMMENT '邮箱是否验证：0-未验证，1-已验证',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 分类表
-- =====================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- =====================================================
-- 3. 标签表
-- =====================================================
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';

-- =====================================================
-- 4. 文章表
-- =====================================================
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `content` LONGTEXT NOT NULL COMMENT '文章内容（Markdown）',
    `html_content` LONGTEXT DEFAULT NULL COMMENT '文章内容（HTML）',
    `cover` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已删除',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `scheduled_time` DATETIME DEFAULT NULL COMMENT '定时发布时间',
    `allow_comment` TINYINT DEFAULT 1 COMMENT '是否允许评论：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_scheduled_time` (`scheduled_time`),
    CONSTRAINT `fk_article_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- =====================================================
-- 5. 文章-标签关联表
-- =====================================================
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`),
    CONSTRAINT `fk_article_tag_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_article_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- =====================================================
-- 6. 评论表
-- =====================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（匿名评论为空）',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（用于嵌套回复）',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '匿名评论昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '匿名评论邮箱',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =====================================================
-- 7. 邮箱验证码表
-- =====================================================
DROP TABLE IF EXISTS `email_captcha`;
CREATE TABLE `email_captcha` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `captcha` VARCHAR(6) NOT NULL COMMENT '验证码',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：REGISTER-注册，LOGIN-登录，EMAIL_CHANGE-换绑邮箱，PASSWORD_RESET-重置密码',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `used` TINYINT DEFAULT 0 COMMENT '是否已使用：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_email_type` (`email`, `type`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- =====================================================
-- 8. OAuth第三方绑定表
-- =====================================================
DROP TABLE IF EXISTS `oauth_binding`;
CREATE TABLE `oauth_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `provider` VARCHAR(20) NOT NULL COMMENT '提供商：GITEE-码云，GITHUB-GitHub，WECHAT-微信',
    `oauth_id` VARCHAR(100) NOT NULL COMMENT '第三方用户ID',
    `oauth_name` VARCHAR(100) DEFAULT NULL COMMENT '第三方用户名',
    `oauth_avatar` VARCHAR(500) DEFAULT NULL COMMENT '第三方头像',
    `access_token` VARCHAR(500) DEFAULT NULL COMMENT '访问令牌',
    `refresh_token` VARCHAR(500) DEFAULT NULL COMMENT '刷新令牌',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_oauth_id` (`provider`, `oauth_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_oauth_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方OAuth绑定表';

-- =====================================================
-- 9. 文章点赞表
-- =====================================================
DROP TABLE IF EXISTS `article_like`;
CREATE TABLE `article_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- =====================================================
-- 10. 文章草稿表（自动保存）
-- =====================================================
DROP TABLE IF EXISTS `article_draft`;
CREATE TABLE `article_draft` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `article_id` BIGINT DEFAULT NULL COMMENT '文章ID（新文章为空）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `content` LONGTEXT DEFAULT NULL COMMENT '内容',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `cover` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`),
    CONSTRAINT `fk_draft_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章草稿表';

-- =====================================================
-- 11. 网站配置表
-- =====================================================
DROP TABLE IF EXISTS `site_config`;
CREATE TABLE `site_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站配置表';

-- =====================================================
-- 12. 友情链接表
-- =====================================================
DROP TABLE IF EXISTS `friend_link`;
CREATE TABLE `friend_link` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name` VARCHAR(100) NOT NULL COMMENT '链接名称',
    `url` VARCHAR(500) NOT NULL COMMENT '链接地址',
    `logo` VARCHAR(500) DEFAULT NULL COMMENT 'Logo图片',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='友情链接表';

-- =====================================================
-- 13. 文件表
-- =====================================================
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `url` VARCHAR(500) NOT NULL COMMENT '文件URL',
    `file_key` VARCHAR(255) DEFAULT NULL COMMENT '文件Key（用于云存储）',
    `type` INT NOT NULL COMMENT '类型：1-头像，2-文章封面，3-文章图片',
    `user_id` BIGINT DEFAULT NULL COMMENT '上传用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- =====================================================
-- 14. 登录日志表
-- =====================================================
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认管理员用户（密码: admin123，使用BCrypt加密）
INSERT INTO `user` (`username`, `email`, `password`, `nickname`, `role`, `email_verified`, `status`) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 1, 1);

-- 插入默认分类
INSERT INTO `category` (`name`) VALUES
('技术分享'),
('生活随笔'),
('学习笔记');

-- 插入默认标签
INSERT INTO `tag` (`name`) VALUES
('Java'),
('Spring Boot'),
('MySQL'),
('前端'),
('后端');

-- 插入默认网站配置
INSERT INTO `site_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '我的博客', '网站名称'),
('site_description', '一个简洁的个人博客系统', '网站描述'),
('site_keywords', '博客,技术,分享', 'SEO关键词'),
('site_logo', '/images/logo.png', '网站Logo'),
('site_favicon', '/images/favicon.ico', '网站图标'),
('icp_number', '', 'ICP备案号'),
('about_content', '欢迎来到我的博客！', '关于页面内容');

-- =====================================================
-- 完成
-- =====================================================
