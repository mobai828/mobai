package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.entity.Comment;
import com.example.blog.entity.FriendLink;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.service.AdminService;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CategoryService;
import com.example.blog.service.CommentService;
import com.example.blog.service.FriendLinkService;
import com.example.blog.service.TagService;
import com.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private FriendLinkService friendLinkService;

    // ==================== 仪表盘 ====================
    
    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = adminService.getDashboardStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取统计数据（兼容前端 /api/admin/statistics 调用）
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getDashboardStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取网站配置
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSiteConfig() {
        Map<String, String> config = adminService.getSiteConfig();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", config);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新网站配置
     */
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateSiteConfig(@RequestBody Map<String, String> configMap) {
        configMap.forEach((key, value) -> adminService.updateSiteConfig(key, value));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "配置更新成功");
        return ResponseEntity.ok(response);
    }
    
    // ==================== 评论管理 ====================
    
    /**
     * 分页获取评论列表（管理员）
     */
    @GetMapping("/comments")
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Comment> comments = commentService.getAllCommentsForAdmin(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comments.getContent());
        response.put("currentPage", comments.getNumber());
        response.put("totalPages", comments.getTotalPages());
        response.put("totalElements", comments.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 审核评论
     */
    @PutMapping("/comments/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveComment(@PathVariable Long id) {
        Comment comment = commentService.approveComment(id);
        
        Map<String, Object> response = new HashMap<>();
        if (comment != null) {
            response.put("success", true);
            response.put("message", "评论审核通过");
            response.put("data", comment);
        } else {
            response.put("success", false);
            response.put("message", "评论不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 拒绝评论
     */
    @PutMapping("/comments/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectComment(@PathVariable Long id) {
        Comment comment = commentService.rejectComment(id);
        
        Map<String, Object> response = new HashMap<>();
        if (comment != null) {
            response.put("success", true);
            response.put("message", "评论已拒绝");
            response.put("data", comment);
        } else {
            response.put("success", false);
            response.put("message", "评论不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "评论删除成功");
        return ResponseEntity.ok(response);
    }

    // ==================== 用户管理 ====================
    
    /**
     * 分页获取用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<User> users = userService.getUsers(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalPages", users.getTotalPages());
        response.put("totalElements", users.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("success", true);
            response.put("data", user);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusMap) {
        Integer status = statusMap.get("status");
        User updatedUser = userService.updateUserStatus(id, status);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedUser != null) {
            response.put("success", true);
            response.put("message", "用户状态更新成功");
            response.put("data", updatedUser);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户角色
     */
    @PostMapping("/users/update-role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@RequestParam Long userId, @RequestParam String role) {
        User updatedUser = userService.updateUserRole(userId, role);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedUser != null) {
            response.put("success", true);
            response.put("message", "用户角色更新成功");
            response.put("data", updatedUser);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 切换用户状态
     */
    @PostMapping("/users/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Integer currentStatus = Integer.valueOf(request.get("currentStatus").toString());
        Integer newStatus = currentStatus == 1 ? 0 : 1;
        
        User updatedUser = userService.updateUserStatus(userId, newStatus);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedUser != null) {
            response.put("success", true);
            response.put("message", "用户状态切换成功");
            response.put("data", updatedUser);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 文章管理 ====================
    
    /**
     * 分页获取文章列表
     */
    @GetMapping("/articles")
    public ResponseEntity<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Page<Article> articles;
        if (keyword != null && !keyword.trim().isEmpty()) {
            articles = articleService.searchArticles(keyword.trim(), page, size, status);
        } else {
            articles = articleService.getArticlesByPage(page, size, status);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", articles.getContent());
        response.put("currentPage", articles.getNumber());
        response.put("totalPages", articles.getTotalPages());
        response.put("totalElements", articles.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取文章
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> getArticleById(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (article != null) {
            response.put("success", true);
            response.put("data", article);
        } else {
            response.put("success", false);
            response.put("message", "文章不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新文章状态
     */
    @PutMapping("/articles/{id}/status")
    public ResponseEntity<Map<String, Object>> updateArticleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusMap) {
        Integer status = statusMap.get("status");
        Article updatedArticle = articleService.updateArticleStatus(id, status);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedArticle != null) {
            response.put("success", true);
            response.put("message", "文章状态更新成功");
            response.put("data", updatedArticle);
        } else {
            response.put("success", false);
            response.put("message", "文章不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        Map<String, Object> response = new HashMap<>();
        try {
            Article updatedArticle = articleService.updateArticle(id, article, article.getTagIds());
            response.put("success", true);
            response.put("message", "文章更新成功");
            response.put("data", updatedArticle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新文章失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 删除文章（软删除）
     */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> deleteArticle(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            articleService.deleteArticle(id);
            response.put("success", true);
            response.put("message", "文章已移至回收站");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除文章失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 还原文章
     */
    @PostMapping("/articles/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreArticle(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            articleService.restoreArticle(id);
            response.put("success", true);
            response.put("message", "文章已还原");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "还原文章失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 永久删除文章
     */
    @DeleteMapping("/articles/{id}/permanent")
    public ResponseEntity<Map<String, Object>> deleteArticlePermanently(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            articleService.deleteArticlePermanently(id);
            response.put("success", true);
            response.put("message", "文章已永久删除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "永久删除文章失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // ==================== 分类管理 ====================
    
    /**
     * 分页获取分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Category> categories = categoryService.getCategoriesByPage(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categories.getContent());
        response.put("currentPage", categories.getNumber());
        response.put("totalPages", categories.getTotalPages());
        response.put("totalElements", categories.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取分类
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (category != null) {
            response.put("success", true);
            response.put("data", category);
        } else {
            response.put("success", false);
            response.put("message", "分类不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建分类
     */
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryService.createCategory(category);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "分类创建成功");
        response.put("data", savedCategory);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedCategory != null) {
            response.put("success", true);
            response.put("message", "分类更新成功");
            response.put("data", updatedCategory);
        } else {
            response.put("success", false);
            response.put("message", "分类不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "分类删除成功");
        return ResponseEntity.ok(response);
    }

    // ==================== 标签管理 ====================
    
    /**
     * 分页获取标签列表
     */
    @GetMapping("/tags")
    public ResponseEntity<Map<String, Object>> getTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Tag> tags = tagService.getTagsByPage(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tags.getContent());
        response.put("currentPage", tags.getNumber());
        response.put("totalPages", tags.getTotalPages());
        response.put("totalElements", tags.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取标签
     */
    @GetMapping("/tags/{id}")
    public ResponseEntity<Map<String, Object>> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (tag != null) {
            response.put("success", true);
            response.put("data", tag);
        } else {
            response.put("success", false);
            response.put("message", "标签不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建标签
     */
    @PostMapping("/tags")
    public ResponseEntity<Map<String, Object>> createTag(@RequestBody Tag tag) {
        Tag savedTag = tagService.createTag(tag);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "标签创建成功");
        response.put("data", savedTag);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新标签
     */
    @PutMapping("/tags/{id}")
    public ResponseEntity<Map<String, Object>> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        Tag updatedTag = tagService.updateTag(id, tag);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedTag != null) {
            response.put("success", true);
            response.put("message", "标签更新成功");
            response.put("data", updatedTag);
        } else {
            response.put("success", false);
            response.put("message", "标签不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除标签
     */
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Map<String, Object>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "标签删除成功");
        return ResponseEntity.ok(response);
    }

    // ==================== 友链管理 ====================
    
    /**
     * 分页获取友链列表
     */
    @GetMapping("/links")
    public ResponseEntity<Map<String, Object>> getLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<FriendLink> links = friendLinkService.getAllLinks(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", links.getContent());
        response.put("currentPage", links.getNumber());
        response.put("totalPages", links.getTotalPages());
        response.put("totalElements", links.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建友链
     */
    @PostMapping("/links")
    public ResponseEntity<Map<String, Object>> createLink(@RequestBody FriendLink link) {
        FriendLink savedLink = friendLinkService.createLink(link);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "友链创建成功");
        response.put("data", savedLink);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新友链
     */
    @PutMapping("/links/{id}")
    public ResponseEntity<Map<String, Object>> updateLink(@PathVariable Long id, @RequestBody FriendLink link) {
        FriendLink updatedLink = friendLinkService.updateLink(id, link);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedLink != null) {
            response.put("success", true);
            response.put("message", "友链更新成功");
            response.put("data", updatedLink);
        } else {
            response.put("success", false);
            response.put("message", "友链不存在");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除友链
     */
    @DeleteMapping("/links/{id}")
    public ResponseEntity<Map<String, Object>> deleteLink(@PathVariable Long id) {
        friendLinkService.deleteLink(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "友链删除成功");
        return ResponseEntity.ok(response);
    }
}