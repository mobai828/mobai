package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleDraft;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.service.ArchiveService;
import com.example.blog.service.ArticleLikeService;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CategoryService;
import com.example.blog.service.CommentService;
import com.example.blog.service.DraftService;
import com.example.blog.service.TagService;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;
    
    @Autowired
    private ArticleLikeService articleLikeService;
    
    @Autowired
    private DraftService draftService;
    
    @Autowired
    private ArchiveService archiveService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建文章
     */
    @PostMapping
    public ResponseEntity<?> createArticle(@RequestBody Article article) {
        // 添加日志记录接收到的文章对象和tagIds
        logger.info("接收到创建文章请求，文章对象: {}", article);
        if (article != null) {
            logger.info("文章标题: {}, tagIds: {}", article.getTitle(), article.getTagIds());
        }
        
        try {
            // 确保userId被设置，这里假设系统中只有一个管理员用户，ID为1
            // 更好的做法是从当前登录用户获取userId
            if (article.getUserId() == null) {
                logger.info("userId为空，设置默认值为1");
                article.setUserId(1L);
            }
            
            logger.info("调用ArticleService.createArticle方法");
            Article savedArticle = articleService.createArticle(article, article.getTagIds());
            logger.info("ArticleService.createArticle方法调用完成，返回文章ID: {}", savedArticle.getId());
            
            // 返回统一格式的JSON响应
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "message", "文章创建成功",
                "data", savedArticle
            ));
        } catch (Exception e) {
            logger.error("创建文章时发生错误: ", e);
            
            // 返回统一格式的JSON响应，包含错误信息
            return ResponseEntity.ok(MapUtil.of(
                "success", false,
                "message", "创建文章失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        try {
            Article updatedArticle = articleService.updateArticle(id, article, article.getTagIds());
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "message", "文章更新成功",
                "data", updatedArticle
            ));
        } catch (Exception e) {
            logger.error("更新文章时发生错误: ", e);
            return ResponseEntity.status(500).body(MapUtil.of(
                "success", false,
                "message", "更新文章失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取文章
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article != null) {
            // 增加浏览量
            articleService.incrementViewCount(id);
            // 获取评论数
            Long commentCount = commentService.countCommentsByArticle(id);
            // 返回包含统计信息的响应
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "data", article,
                "viewCount", article.getViewCount(),
                "commentCount", commentCount
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取文章统计信息
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getArticleStats(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article != null) {
            // 获取评论数
            Long commentCount = commentService.countCommentsByArticle(id);
            // 返回统计信息
            return ResponseEntity.ok(MapUtil.of(
                "viewCount", article.getViewCount(),
                "commentCount", commentCount,
                "likeCount", article.getLikeCount()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 分页获取文章列表，支持按状态筛选
     */
    @GetMapping
    public ResponseEntity<?> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Article> articles = articleService.getArticlesByPage(page, size, status);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles));
    }
    
    /**
     * 获取最新文章
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestArticles(
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Integer limit) {
        int actualSize = limit != null ? limit : size;
        Page<Article> articles = articleService.getArticlesByPage(0, actualSize, 1);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles.getContent()));
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularArticles(
            @RequestParam(defaultValue = "5") int limit) {
        List<Article> articles = articleService.getPopularArticles(limit);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles));
    }

    /**
     * 根据分类ID分页获取文章列表，支持按状态筛选
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Article> articles = articleService.getArticlesByCategory(categoryId, page, size, status);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles));
    }

    /**
     * 根据用户ID分页获取文章列表，支持按状态筛选
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getArticlesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Article> articles = articleService.getArticlesByUser(userId, page, size, status);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles));
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Article> articles = articleService.searchArticles(keyword, page, size, status);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles));
    }

    /**
     * 获取草稿文章列表
     */
    @GetMapping("/drafts")
    public ResponseEntity<?> getDraftArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Article> articles = articleService.getDraftArticles(page, size);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", articles));
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(MapUtil.of("success", true, "data", categories));
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(MapUtil.of("success", true, "data", category));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建分类
     */
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        // 检查分类名称是否已存在
        if (categoryService.existsByName(category.getName())) {
            return ResponseEntity.badRequest().body("分类名称已存在");
        }
        Category savedCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(savedCategory);
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(MapUtil.of("success", true, "data", tags));
    }

    /**
     * 根据ID获取标签
     */
    @GetMapping("/tags/{id}")
    public ResponseEntity<?> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        if (tag != null) {
            return ResponseEntity.ok(MapUtil.of("success", true, "data", tag));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建标签
     */
    @PostMapping("/tags")
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        // 检查标签名称是否已存在
        if (tagService.existsByName(tag.getName())) {
            // 如果已存在，直接返回已存在的标签
            Tag existingTag = tagService.getTagByName(tag.getName());
            return ResponseEntity.ok(MapUtil.of("success", true, "data", existingTag));
        }
        Tag savedTag = tagService.createTag(tag);
        return ResponseEntity.ok(MapUtil.of("success", true, "data", savedTag));
    }

    /**
     * 更新标签
     */
    @PutMapping("/tags/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        Tag updatedTag = tagService.updateTag(id, tag);
        return ResponseEntity.ok(updatedTag);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 点赞/取消点赞文章
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            boolean liked = articleLikeService.toggleLike(id, userId);
            Long likeCount = articleLikeService.getLikeCount(id);
            
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "liked", liked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 检查用户是否已点赞
     */
    @GetMapping("/{id}/like/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            boolean liked = articleLikeService.hasUserLiked(id, userId);
            Long likeCount = articleLikeService.getLikeCount(id);
            
            return ResponseEntity.ok(MapUtil.of(
                "liked", liked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // ==================== 草稿功能 ====================
    
    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(
            @RequestBody Map<String, Object> draftData,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            Long articleId = draftData.get("articleId") != null ? 
                    Long.parseLong(draftData.get("articleId").toString()) : null;
            String title = (String) draftData.get("title");
            String content = (String) draftData.get("content");
            Long categoryId = draftData.get("categoryId") != null ?
                    Long.parseLong(draftData.get("categoryId").toString()) : null;
            String cover = (String) draftData.get("cover");
            
            ArticleDraft draft = draftService.saveDraft(userId, articleId, title, content, categoryId, cover);
            
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "message", "草稿保存成功",
                "data", draft
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 获取草稿
     */
    @GetMapping("/draft")
    public ResponseEntity<?> getDraft(
            @RequestParam(required = false) Long articleId,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            ArticleDraft draft = draftService.getDraft(userId, articleId);
            
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "data", draft
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 获取用户所有草稿
     */
    @GetMapping("/drafts/user")
    public ResponseEntity<?> getUserDrafts(
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "data", draftService.getUserDrafts(userId)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 删除草稿
     */
    @DeleteMapping("/draft")
    public ResponseEntity<?> deleteDraft(
            @RequestParam(required = false) Long articleId,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            draftService.deleteDraft(userId, articleId);
            
            return ResponseEntity.ok(MapUtil.of(
                "success", true,
                "message", "草稿删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // ==================== 归档功能 ====================
    
    /**
     * 获取文章归档
     */
    @GetMapping("/archive")
    public ResponseEntity<?> getArchive() {
        return ResponseEntity.ok(MapUtil.of(
            "success", true,
            "data", archiveService.getArchive()
        ));
    }
    
    /**
     * 获取指定年月的文章
     */
    @GetMapping("/archive/{year}/{month}")
    public ResponseEntity<?> getArticlesByYearMonth(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(MapUtil.of(
            "success", true,
            "data", archiveService.getArticlesByYearMonth(year, month)
        ));
    }
}