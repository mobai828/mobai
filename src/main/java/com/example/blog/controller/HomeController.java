package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.entity.Comment;
import com.example.blog.entity.FriendLink;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.service.ArchiveService;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CategoryService;
import com.example.blog.service.CommentService;
import com.example.blog.service.FriendLinkService;
import com.example.blog.service.TagService;
import com.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FriendLinkService friendLinkService;
    
    @Autowired
    private ArchiveService archiveService;
    
    /**
     * 添加公共属性到所有模型
     */
    @org.springframework.web.bind.annotation.ModelAttribute
    public void addCommonAttributes(Model model) {
        // 获取博主信息（管理员）
        User blogger = userService.getAdminUser();
        if (blogger == null) {
            // 如果没有管理员，创建一个默认的 dummy user 防止报错
            blogger = new User();
            blogger.setNickname("博主");
            blogger.setIntro("热爱技术，热爱生活");
            blogger.setAvatar("/images/default-avatar.svg");
        }
        model.addAttribute("blogger", blogger);
        
        // 获取已发布文章总数
        long articleCount = articleService.countPublishedArticles();
        model.addAttribute("articleCount", articleCount);
    }

    /**
     * 首页 - 显示文章列表
     */
    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Page<Article> articles = articleService.getPublishedArticles(page, size);
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        // 创建分类ID到分类名称的映射
        java.util.Map<Long, String> categoryMap = new java.util.HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }
        
        // 创建文章ID到标签列表的映射
        java.util.Map<Long, List<Tag>> articleTagsMap = new java.util.HashMap<>();
        for (Article article : articles) {
            List<Tag> articleTags = tagService.getTagsByArticleId(article.getId());
            articleTagsMap.put(article.getId(), articleTags);
        }
        
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", articles.getNumber());
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("articleTagsMap", articleTagsMap);
        
        return "index";
    }

    /**
     * 文章详情页
     */
    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        
        // 如果文章不存在，或者文章已删除且当前不是管理员，则返回404
        // 注意：这里需要从SecurityContext获取当前用户权限来判断是否是管理员
        // 简单起见，如果文章被标记为删除(status=2)，我们直接返回404，除非是管理员后台预览（通常走不同路径）
        if (article == null || (article.getStatus() != null && article.getStatus() == 2)) {
            return "error/404";
        }
        
        // 增加浏览量
        articleService.incrementViewCount(id);
        
        List<Category> categories = categoryService.getAllCategories();
        // 获取文章关联的标签，而不是所有标签
        List<Tag> tags = tagService.getTagsByArticleId(id);
        
        // 获取文章的评论树
        List<Comment> commentTree = commentService.getCommentTree(article.getId());
        
        model.addAttribute("article", article);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("comments", commentTree);
        
        return "article";
    }

    /**
     * 分类文章列表页
     */
    @GetMapping("/category/{id}")
    public String articlesByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Page<Article> articles = articleService.getPublishedArticlesByCategory(id, page, size);
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        Category category = categoryService.getCategoryById(id);
        
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", articles.getNumber());
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("currentCategory", category);
        
        return "category-articles";
    }

    /**
     * 标签文章列表页
     */
    @GetMapping("/tag/{id}")
    public String articlesByTag(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Page<Article> articles = articleService.getPublishedArticlesByTag(id, page, size);
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        Tag tag = tagService.getTagById(id);
        
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", articles.getNumber());
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("currentTag", tag);
        
        return "tag-articles";
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public String searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Page<Article> articles = articleService.searchPublishedArticles(keyword, page, size);
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", articles.getNumber());
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("keyword", keyword);
        
        return "search";
    }
    
    /**
     * 个人资料页
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        
        return "profile";
    }
    
    /**
     * 撰写文章页
     */
    @GetMapping("/write-article")
    public String writeArticle(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        
        return "write-article";
    }
    
    /**
     * 标签列表页
     */
    @GetMapping({"/tag", "/tag/"})
    public String tagPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        
        return "tag";
    }
    
    /**
     * 分类列表页
     */
    @GetMapping({"/category", "/category/"})
    public String categoryPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        
        return "category";
    }
    
    /**
     * 后台管理首页
     */
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        return "admin/index";
    }
    
    /**
     * 文章管理页面
     */
    @GetMapping("/admin/articles")
    public String adminArticles(Model model) {
        return "admin/articles";
    }
    
    /**
     * 分类管理页面
     */
    @GetMapping("/admin/categories")
    public String adminCategories(Model model) {
        return "admin/categories";
    }
    
    /**
     * 标签管理页面
     */
    @GetMapping("/admin/tags")
    public String adminTags(Model model) {
        return "admin/tags";
    }
    
    /**
     * 用户管理页面
     */
    @GetMapping("/admin/users")
    public String adminUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<User> usersPage = userService.getUsers(page, size);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        return "admin/users";
    }
    
    /**
     * 友链页面
     */
    @GetMapping("/links")
    public String linksPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        List<FriendLink> links = friendLinkService.getAllEnabledLinks();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("links", links);
        
        return "links";
    }
    
    /**
     * 归档页面
     */
    @GetMapping("/archive")
    public String archivePage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("archive", archiveService.getArchive());
        
        return "archive";
    }
    
    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String aboutPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Tag> tags = tagService.getAllTags();
        
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        
        return "about";
    }
    
    /**
     * 评论管理页面
     */
    @GetMapping("/admin/comments")
    public String adminComments(Model model) {
        return "admin/comments";
    }
    
    /**
     * 友链管理页面
     */
    @GetMapping("/admin/links")
    public String adminLinks(Model model) {
        return "admin/links";
    }
    
    /**
     * 系统设置页面
     */
    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        return "admin/settings";
    }
}