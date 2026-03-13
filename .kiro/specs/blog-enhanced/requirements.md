# Requirements Document

## Introduction

本文档定义了个人博客系统增强版的功能需求。该系统基于现有的 Spring Boot 博客系统进行扩展，增加多方式登录、邮箱验证、头像上传、评论增强、后台管理等功能，并采用模块化设计以支持未来扩展。

## Glossary

- **Blog_System**: 个人博客系统，提供文章发布、评论、用户管理等功能
- **User**: 系统用户，包括普通用户和管理员
- **Article**: 博客文章，支持 Markdown 格式
- **Comment**: 文章评论，支持嵌套回复
- **Category**: 文章分类
- **Tag**: 文章标签
- **Captcha**: 邮箱验证码，用于验证用户身份
- **OAuth**: 第三方授权登录协议
- **Storage_Provider**: 文件存储提供者（本地/云存储）

## Requirements

### Requirement 1: 多方式登录

**User Story:** As a user, I want to login using different methods, so that I can choose the most convenient way to access my account.

#### Acceptance Criteria

1. WHEN a user submits username and password THEN the Blog_System SHALL authenticate the user and return a JWT token
2. WHEN a user submits email and password THEN the Blog_System SHALL authenticate the user and return a JWT token
3. WHEN a user requests email captcha login THEN the Blog_System SHALL send a 6-digit captcha to the email address
4. WHEN a user submits email and valid captcha THEN the Blog_System SHALL authenticate the user without password
5. WHEN a captcha expires after 5 minutes THEN the Blog_System SHALL reject the captcha and prompt the user to request a new one
6. WHEN a user initiates Gitee OAuth login THEN the Blog_System SHALL redirect to Gitee authorization page
7. WHEN Gitee returns authorization code THEN the Blog_System SHALL exchange for access token and create or link user account
8. IF login credentials are invalid THEN the Blog_System SHALL return an error message without revealing which field is incorrect

### Requirement 2: 邮箱注册系统

**User Story:** As a visitor, I want to register using my email, so that I can create an account and start using the blog system.

#### Acceptance Criteria

1. WHEN a user submits registration with email THEN the Blog_System SHALL send a verification captcha to the email
2. WHEN a user submits valid captcha within 5 minutes THEN the Blog_System SHALL complete the registration
3. WHEN a user submits password THEN the Blog_System SHALL encrypt it using BCrypt before storage
4. IF the email is already registered THEN the Blog_System SHALL reject registration and display an error message
5. IF the username is already taken THEN the Blog_System SHALL reject registration and display an error message
6. WHEN registration completes THEN the Blog_System SHALL create user with default USER role

### Requirement 3: 用户资料管理

**User Story:** As a user, I want to manage my profile, so that I can personalize my account and keep my information up to date.

#### Acceptance Criteria

1. WHEN a user updates nickname THEN the Blog_System SHALL save the new nickname immediately
2. WHEN a user updates self-introduction THEN the Blog_System SHALL save the text with maximum 500 characters
3. WHEN a user uploads avatar image THEN the Blog_System SHALL validate file type (JPG/PNG/GIF) and size (max 2MB)
4. WHEN avatar upload succeeds THEN the Blog_System SHALL store the file and update user avatar URL
5. WHEN a user requests email change THEN the Blog_System SHALL send verification captcha to the new email
6. WHEN a user submits valid captcha for email change THEN the Blog_System SHALL update the email address
7. WHEN a user changes password THEN the Blog_System SHALL verify old password before accepting new password
8. IF old password verification fails THEN the Blog_System SHALL reject password change request

### Requirement 4: 文章系统增强

**User Story:** As an author, I want enhanced article features, so that I can create and manage content more effectively.

#### Acceptance Criteria

1. WHEN a user writes article content THEN the Blog_System SHALL render Markdown to HTML for preview
2. WHEN a user edits article THEN the Blog_System SHALL auto-save draft every 30 seconds
3. WHEN a user sets scheduled publish time THEN the Blog_System SHALL publish the article at the specified time
4. WHEN a user uploads cover image THEN the Blog_System SHALL associate it with the article
5. WHEN a user views article THEN the Blog_System SHALL increment view count by 1
6. WHEN a user clicks like button THEN the Blog_System SHALL increment like count and record user action
7. WHEN the same user clicks like again THEN the Blog_System SHALL decrement like count (toggle behavior)

### Requirement 5: 评论系统增强

**User Story:** As a reader, I want to comment on articles and receive notifications, so that I can engage with content and authors.

#### Acceptance Criteria

1. WHEN a logged-in user submits comment THEN the Blog_System SHALL save comment with user information
2. WHERE anonymous comments are enabled WHEN a visitor submits comment THEN the Blog_System SHALL save comment with IP address
3. WHEN comment moderation is enabled THEN the Blog_System SHALL set new comments to pending status
4. WHEN a user replies to comment THEN the Blog_System SHALL create nested comment with parent reference
5. WHEN new comment is submitted THEN the Blog_System SHALL send email notification to administrator
6. WHEN user receives reply THEN the Blog_System SHALL send email notification to the original commenter
7. WHEN administrator approves comment THEN the Blog_System SHALL change comment status to approved

### Requirement 6: 分类与标签系统

**User Story:** As an author, I want to organize articles with categories and tags, so that readers can easily find related content.

#### Acceptance Criteria

1. WHEN administrator creates category THEN the Blog_System SHALL save category with unique name
2. WHEN administrator updates category THEN the Blog_System SHALL update category name
3. WHEN administrator deletes category THEN the Blog_System SHALL remove category and set articles to uncategorized
4. WHEN user adds new tag to article THEN the Blog_System SHALL create tag if not exists
5. WHEN displaying category THEN the Blog_System SHALL show article count for that category
6. WHEN displaying tag THEN the Blog_System SHALL show article count for that tag

### Requirement 7: 搜索与归档

**User Story:** As a reader, I want to search and browse archived articles, so that I can find content I'm interested in.

#### Acceptance Criteria

1. WHEN a user searches with keyword THEN the Blog_System SHALL return articles matching title or content
2. WHEN displaying search results THEN the Blog_System SHALL highlight matching keywords
3. WHEN a user views archive page THEN the Blog_System SHALL display articles grouped by year and month
4. WHEN a user clicks archive month THEN the Blog_System SHALL show all articles from that month

### Requirement 8: 文件上传系统

**User Story:** As a user, I want to upload files with flexible storage options, so that I can manage media content efficiently.

#### Acceptance Criteria

1. WHEN storage provider is set to local THEN the Blog_System SHALL store files in configured local directory
2. WHEN storage provider is set to cloud THEN the Blog_System SHALL upload files to configured cloud storage
3. WHEN user uploads file THEN the Blog_System SHALL validate file type against allowed types
4. WHEN user uploads file THEN the Blog_System SHALL validate file size against maximum limit
5. WHEN file upload succeeds THEN the Blog_System SHALL return accessible URL for the file
6. IF file validation fails THEN the Blog_System SHALL reject upload and return specific error message

### Requirement 9: 后台管理系统

**User Story:** As an administrator, I want a comprehensive admin panel, so that I can manage all aspects of the blog system.

#### Acceptance Criteria

1. WHEN administrator accesses dashboard THEN the Blog_System SHALL display statistics (article count, comment count, user count, view count)
2. WHEN administrator manages articles THEN the Blog_System SHALL provide list, edit, delete, and status change functions
3. WHEN administrator manages comments THEN the Blog_System SHALL provide approve, reject, and delete functions
4. WHEN administrator manages users THEN the Blog_System SHALL provide list, edit role, and disable functions
5. WHEN administrator updates site config THEN the Blog_System SHALL save and apply settings immediately
6. WHEN site config includes SEO settings THEN the Blog_System SHALL render meta tags in page headers

### Requirement 10: 前台展示页面

**User Story:** As a visitor, I want a well-designed blog frontend, so that I can browse and read content comfortably.

#### Acceptance Criteria

1. WHEN visitor accesses homepage THEN the Blog_System SHALL display paginated article list with latest first
2. WHEN visitor accesses category page THEN the Blog_System SHALL display articles filtered by category
3. WHEN visitor accesses tag page THEN the Blog_System SHALL display articles filtered by tag
4. WHEN visitor accesses article detail THEN the Blog_System SHALL display full content with comments
5. WHEN visitor accesses about page THEN the Blog_System SHALL display configured about content
6. WHEN visitor accesses friend links page THEN the Blog_System SHALL display configured friend links
