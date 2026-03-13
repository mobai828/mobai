# Implementation Plan

## Phase 1: 基础设施与数据模型

- [x] 1. 添加项目依赖和配置

  - [x] 1.1 在 pom.xml 中添加 jqwik 属性测试依赖


    - 添加 jqwik 1.8.2 版本依赖


    - _Requirements: Testing Strategy_

  - [x] 1.2 更新 application.yml 添加 Gitee OAuth 配置


    - 添加 gitee.client-id 和 gitee.client-secret 配置项


    - _Requirements: 1.6, 1.7_





- [x] 2. 创建新增数据实体


  - [x] 2.1 创建 EmailCaptcha 实体类


    - 包含 email, captcha, type, expireTime, used 字段

    - _Requirements: 1.3, 2.1_


  - [x] 2.2 创建 OAuthBinding 实体类


    - 包含 userId, provider, oauthId, oauthName, oauthAvatar 字段

    - _Requirements: 1.6, 1.7_


  - [x] 2.3 创建 ArticleLike 实体类


    - 包含 articleId, userId, createTime 字段


    - _Requirements: 4.6, 4.7_


  - [x] 2.4 创建 ArticleDraft 实体类

    - 包含 articleId, userId, title, content, updateTime 字段


    - _Requirements: 4.2_


  - [-] 2.5 创建 SiteConfig 实体类



    - 包含 configKey, configValue, description 字段
    - _Requirements: 9.5_
  - [x] 2.6 创建 FriendLink 实体类
    - 包含 name, url, logo, description, sortOrder, status 字段

    - _Requirements: 10.6_


- [x] 3. 扩展现有实体字段


  - [ ] 3.1 扩展 User 实体
    - 添加 emailVerified, lastLoginTime, lastLoginIp 字段


    - _Requirements: 2.1, 1.1_

  - [x] 3.2 扩展 Article 实体


    - 添加 scheduledTime, allowComment 字段

    - _Requirements: 4.3, 5.1_
  - [ ] 3.3 扩展 Comment 实体
    - 添加 nickname, email, isAnonymous 字段
    - _Requirements: 5.2_




- [x] 4. 创建 Repository 接口


  - [x] 4.1 创建 EmailCaptchaRepository

    - 添加 findByEmailAndTypeAndUsedFalse 方法



    - _Requirements: 1.3, 1.4_

  - [ ] 4.2 创建 OAuthBindingRepository
    - 添加 findByProviderAndOauthId 方法
    - _Requirements: 1.6, 1.7_

  - [x] 4.3 创建 ArticleLikeRepository


    - 添加 findByArticleIdAndUserId, countByArticleId 方法


    - _Requirements: 4.6, 4.7_

  - [ ] 4.4 创建 ArticleDraftRepository
    - 添加 findByUserIdAndArticleId 方法

    - _Requirements: 4.2_
  - [x] 4.5 创建 SiteConfigRepository

    - 添加 findByConfigKey 方法
    - _Requirements: 9.5_


  - [x] 4.6 创建 FriendLinkRepository
    - 添加 findAllByStatusOrderBySortOrder 方法
    - _Requirements: 10.6_




- [x] 5. Checkpoint - 确保数据模型正确

  - Ensure all tests pass, ask the user if questions arise.



## Phase 2: 验证码与邮件服务


- [ ] 6. 实现验证码服务
  - [x] 6.1 创建 CaptchaType 枚举


    - 定义 REGISTER, LOGIN, EMAIL_CHANGE, PASSWORD_RESET 类型

    - _Requirements: 1.3, 2.1, 3.5_
  - [x] 6.2 实现 CaptchaService 接口和实现类

    - 实现 generateAndSend, verify 方法
    - 验证码为6位数字，有效期5分钟
    - _Requirements: 1.3, 1.5, 2.1_
  - [ ] 6.3 编写属性测试：验证码格式和过期
    - **Property 2: Captcha Format and Expiration**

    - **Validates: Requirements 1.3, 1.5**


- [ ] 7. 增强邮件服务
  - [x] 7.1 扩展 MailService 添加验证码邮件模板

    - 实现 sendCaptchaEmail 方法
    - _Requirements: 1.3, 2.1_
  - [x] 7.2 添加评论通知邮件模板

    - 实现 sendCommentNotification, sendReplyNotification 方法
    - _Requirements: 5.5, 5.6_

## Phase 3: 认证系统增强



- [x] 8. 实现多方式登录

  - [-] 8.1 扩展 AuthService 添加邮箱验证码登录


    - 实现 loginByEmailCaptcha 方法
    - _Requirements: 1.4_

  - [ ] 8.2 编写属性测试：验证码认证往返
    - **Property 3: Captcha Authentication Round-Trip**
    - **Validates: Requirements 1.4, 2.2**
  - [ ] 8.3 实现 Gitee OAuth 登录
    - 创建 GiteeOAuthService 处理授权流程


    - _Requirements: 1.6, 1.7_
  - [x] 8.4 更新 AuthController 添加新登录端点

    - 添加 /api/auth/login/captcha, /api/auth/login/gitee 端点
    - _Requirements: 1.4, 1.6_
  - [x] 8.5 编写属性测试：凭证认证一致性


    - **Property 1: Credential Authentication Consistency**

    - **Validates: Requirements 1.1, 1.2, 1.8**


- [x] 9. 增强注册系统

  - [ ] 9.1 更新 UserService 注册流程
    - 添加邮箱验证码验证步骤
    - _Requirements: 2.1, 2.2_
  - [x] 9.2 更新 AuthController 注册端点


    - 添加 /api/auth/register, /api/auth/send-captcha 端点

    - _Requirements: 2.1, 2.2_
  - [ ] 9.3 编写属性测试：密码加密不变量
    - **Property 4: Password Encryption Invariant**
    - **Validates: Requirements 2.3**

  - [ ] 9.4 编写属性测试：注册唯一性约束
    - **Property 5: Registration Uniqueness Constraint**

    - **Validates: Requirements 2.4, 2.5**
  - [ ] 9.5 编写属性测试：默认角色分配
    - **Property 6: Default Role Assignment**
    - **Validates: Requirements 2.6**


- [ ] 10. Checkpoint - 确保认证功能正常
  - Ensure all tests pass, ask the user if questions arise.


## Phase 4: 用户资料管理

- [x] 11. 实现用户资料更新

  - [ ] 11.1 创建 ProfileUpdateRequest DTO
    - 包含 nickname, intro 字段
    - _Requirements: 3.1, 3.2_

  - [ ] 11.2 扩展 UserService 添加资料更新方法
    - 实现 updateProfile 方法，限制 intro 最大500字符
    - _Requirements: 3.1, 3.2_
  - [ ] 11.3 编写属性测试：资料更新持久化
    - **Property 7: Profile Update Persistence**

    - **Validates: Requirements 3.1, 3.2**
  - [x] 11.4 编写属性测试：简介长度约束

    - **Property 8: Introduction Length Constraint**
    - **Validates: Requirements 3.2**


- [x] 12. 实现头像上传

  - [ ] 12.1 扩展 UserService 添加头像上传方法
    - 实现 updateAvatar 方法，验证文件类型和大小

    - _Requirements: 3.3, 3.4_
  - [x] 12.2 更新 UserController 添加头像上传端点

    - 添加 POST /api/user/avatar 端点
    - _Requirements: 3.3, 3.4_

  - [ ] 12.3 编写属性测试：头像文件验证
    - **Property 9: Avatar File Validation**
    - **Validates: Requirements 3.3**



- [ ] 13. 实现邮箱和密码修改
  - [ ] 13.1 实现邮箱变更功能
    - 添加 changeEmail 方法，需验证码确认

    - _Requirements: 3.5, 3.6_
  - [ ] 13.2 实现密码修改功能
    - 添加 changePassword 方法，需验证旧密码
    - _Requirements: 3.7, 3.8_
  - [x] 13.3 编写属性测试：密码修改安全性

    - **Property 10: Password Change Security**
    - **Validates: Requirements 3.7, 3.8**


- [ ] 14. Checkpoint - 确保用户管理功能正常
  - Ensure all tests pass, ask the user if questions arise.


## Phase 5: 文章系统增强


- [x] 15. 实现点赞功能
  - [x] 15.1 创建 ArticleLikeService
    - 实现 toggleLike, hasUserLiked, getLikeCount 方法

    - _Requirements: 4.6, 4.7_
  - [x] 15.2 更新 ArticleController 添加点赞端点
    - 添加 POST /api/articles/{id}/like 端点

    - _Requirements: 4.6, 4.7_
  - [ ] 15.3 编写属性测试：点赞切换往返
    - **Property 11: Like Toggle Round-Trip**

    - **Validates: Requirements 4.6, 4.7**
  - [ ] 15.4 编写属性测试：浏览量递增
    - **Property 12: View Count Increment**

    - **Validates: Requirements 4.5**




- [x] 16. 实现草稿自动保存
  - [x] 16.1 创建 DraftService
    - 实现 saveDraft, getDraft, deleteDraft 方法
    - _Requirements: 4.2_

  - [x] 16.2 更新 ArticleController 添加草稿端点
    - 添加 POST /api/articles/draft 端点

    - _Requirements: 4.2_


- [x] 17. 实现定时发布
  - [x] 17.1 创建 ScheduledPublishService
    - 实现定时任务检查待发布文章
    - _Requirements: 4.3_

  - [x] 17.2 配置 Spring Scheduler
    - 添加 @EnableScheduling 和定时任务

    - _Requirements: 4.3_
  - [x] 17.3 编写属性测试：定时发布时机

    - **Property 13: Scheduled Publish Timing**
    - **Validates: Requirements 4.3**

## Phase 6: 评论系统增强


- [x] 18. 实现匿名评论

  - [x] 18.1 更新 CommentService 支持匿名评论
    - 添加 createAnonymousComment 方法

    - _Requirements: 5.2_
  - [x] 18.2 更新 CommentController 支持匿名评论
    - 修改 POST /api/comments 端点支持匿名
    - _Requirements: 5.2_

- [x] 19. 实现评论审核

  - [x] 19.1 添加评论审核功能
    - 实现 moderateComment 方法

    - _Requirements: 5.3, 5.7_
  - [ ] 19.2 编写属性测试：评论审核默认状态
    - **Property 15: Comment Moderation Default Status**
    - **Validates: Requirements 5.3**



- [x] 20. 实现嵌套评论
  - [x] 20.1 更新 CommentService 构建评论树
    - 实现 getCommentTree 方法


    - _Requirements: 5.4_
  - [ ] 20.2 编写属性测试：评论嵌套完整性
    - **Property 14: Comment Nesting Integrity**
    - **Validates: Requirements 5.4**


- [ ] 21. 实现评论通知
  - [ ] 21.1 集成评论邮件通知
    - 在评论创建后发送通知邮件
    - _Requirements: 5.5, 5.6_



- [ ] 22. Checkpoint - 确保评论功能正常
  - Ensure all tests pass, ask the user if questions arise.

## Phase 7: 分类标签与搜索

- [x] 23. 增强分类标签功能

  - [x] 23.1 实现分类文章数量统计



    - 更新 CategoryService 添加统计方法
    - _Requirements: 6.5_
  - [ ] 23.2 实现标签文章数量统计
    - 更新 TagService 添加统计方法
    - _Requirements: 6.6_
  - [ ] 23.3 编写属性测试：分类标签文章数量准确性
    - **Property 16: Taxonomy Article Count Accuracy**
    - **Validates: Requirements 6.5, 6.6**
  - [ ] 23.4 实现分类删除级联
    - 删除分类时将文章设为未分类
    - _Requirements: 6.3_
  - [ ] 23.5 编写属性测试：分类删除级联
    - **Property 17: Category Deletion Cascade**
    - **Validates: Requirements 6.3**
  - [ ] 23.6 实现标签自动创建
    - 添加新标签时自动创建
    - _Requirements: 6.4_
  - [ ] 23.7 编写属性测试：标签自动创建
    - **Property 18: Tag Auto-Creation**
    - **Validates: Requirements 6.4**


- [ ] 24. 增强搜索功能
  - [ ] 24.1 优化文章搜索
    - 更新 ArticleService 搜索方法
    - _Requirements: 7.1_
  - [ ] 24.2 编写属性测试：搜索结果相关性
    - **Property 19: Search Result Relevance**
    - **Validates: Requirements 7.1**

- [x] 25. 实现文章归档
  - [x] 25.1 创建 ArchiveService
    - 实现按年月分组文章
    - _Requirements: 7.3, 7.4_
  - [x] 25.2 添加归档 API 端点
    - 添加 GET /api/articles/archive 端点
    - _Requirements: 7.3, 7.4_
  - [ ] 25.3 编写属性测试：归档分组正确性
    - **Property 20: Archive Grouping Correctness**
    - **Validates: Requirements 7.3, 7.4**

## Phase 8: 文件存储系统

- [x] 26. 实现文件存储策略模式
  - [x] 26.1 创建 StorageProvider 接口
    - 定义 upload, delete, getAccessUrl 方法
    - _Requirements: 8.1, 8.2_
  - [x] 26.2 实现 LocalStorageProvider
    - 本地文件存储实现
    - _Requirements: 8.1_
  - [x] 26.3 实现 FileStorageService
    - 根据配置选择存储提供者
    - _Requirements: 8.1, 8.2_
  - [x] 26.4 添加文件验证逻辑
    - 验证文件类型和大小
    - _Requirements: 8.3, 8.4_
  - [ ] 26.5 编写属性测试：文件验证规则
    - **Property 21: File Validation Rules**
    - **Validates: Requirements 8.3, 8.4, 8.6**
  - [ ] 26.6 编写属性测试：文件上传URL生成
    - **Property 22: File Upload URL Generation**
    - **Validates: Requirements 8.5**

- [ ] 27. Checkpoint - 确保文件存储功能正常
  - Ensure all tests pass, ask the user if questions arise.


## Phase 9: 后台管理系统

- [x] 28. 实现管理仪表盘
  - [x] 28.1 创建 AdminService
    - 实现 getDashboardStats 方法
    - _Requirements: 9.1_
  - [x] 28.2 创建 AdminController
    - 添加 GET /api/admin/dashboard 端点
    - _Requirements: 9.1_
  - [ ] 28.3 编写属性测试：仪表盘统计准确性
    - **Property 23: Dashboard Statistics Accuracy**
    - **Validates: Requirements 9.1**

- [x] 29. 实现网站配置管理
  - [x] 29.1 创建 SiteConfigService (在 AdminService 中实现)
    - 实现 getConfig, updateConfig 方法
    - _Requirements: 9.5_
  - [x] 29.2 添加配置管理端点
    - 添加 GET/PUT /api/admin/config 端点
    - _Requirements: 9.5_
  - [ ] 29.3 编写属性测试：网站配置持久化
    - **Property 24: Site Config Persistence**
    - **Validates: Requirements 9.5**

- [x] 30. 实现后台管理功能
  - [x] 30.1 实现文章管理功能
    - 列表、编辑、删除、状态变更
    - _Requirements: 9.2_
  - [x] 30.2 实现评论管理功能
    - 审核、拒绝、删除
    - _Requirements: 9.3_
  - [x] 30.3 实现用户管理功能
    - 列表、角色编辑、禁用
    - _Requirements: 9.4_

## Phase 10: 前台页面

- [ ] 31. 更新前台页面模板
  - [ ] 31.1 更新首页模板
    - 显示分页文章列表
    - _Requirements: 10.1_
  - [ ] 31.2 编写属性测试：文章列表排序
    - **Property 26: Article List Ordering**
    - **Validates: Requirements 10.1**


- [ ] 32. 实现分类和标签页面
  - [ ] 32.1 创建分类页面控制器和模板
    - 显示分类列表和分类下文章
    - _Requirements: 10.2_
  - [ ] 32.2 创建标签页面控制器和模板
    - 显示标签列表和标签下文章
    - _Requirements: 10.3_
  - [ ] 32.3 编写属性测试：分类标签过滤正确性
    - **Property 25: Taxonomy Filtering Correctness**
    - **Validates: Requirements 10.2, 10.3**

- [x] 33. 实现其他页面
  - [x] 33.1 创建关于页面
    - 显示配置的关于内容
    - _Requirements: 10.5_
  - [x] 33.2 创建友链页面
    - 显示友情链接列表
    - _Requirements: 10.6_
  - [x] 33.3 创建归档页面
    - 显示按年月分组的文章
    - _Requirements: 7.3_

- [x] 34. 更新登录注册页面
  - [x] 34.1 更新登录页面支持多方式登录
    - 添加验证码登录和 Gitee 登录入口
    - _Requirements: 1.4, 1.6_
  - [x] 34.2 更新注册页面支持邮箱验证
    - 添加验证码发送和验证
    - _Requirements: 2.1, 2.2_

- [ ] 35. Final Checkpoint - 确保所有功能正常
  - Ensure all tests pass, ask the user if questions arise.
