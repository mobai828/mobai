package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Tag;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.ArticleTagRepository;
import com.example.blog.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ArticleTagRepository articleTagRepository;

    /**
     * 创建文章
     */
    @Transactional
    @CacheEvict(value = {"articles", "categories", "tags"}, allEntries = true)
    public Article createArticle(Article article, List<Long> tagIds) {
        logger.info("开始创建文章，文章标题: {}, tagIds: {}", article.getTitle(), tagIds);
        
        try {
            // 保存文章
            logger.info("准备保存文章...");
            Article savedArticle = articleRepository.save(article);
            logger.info("文章保存成功，文章ID: {}", savedArticle.getId());

            // 保存文章标签关联
            logger.info("开始处理标签关联，tagIds: {}", tagIds);
            if (tagIds != null && !tagIds.isEmpty()) {
                logger.info("处理 {} 个标签关联", tagIds.size());
                List<ArticleTag> articleTags = new ArrayList<>();
                for (Long tagId : tagIds) {
                    if (tagId != null) {  // 添加空值检查
                        logger.info("处理标签ID: {}", tagId);
                        ArticleTag articleTag = new ArticleTag();
                        articleTag.setArticleId(savedArticle.getId());
                        articleTag.setTagId(tagId);
                        articleTags.add(articleTag);
                    } else {
                        logger.warn("发现空的标签ID，跳过处理");
                    }
                }
                if (!articleTags.isEmpty()) {
                    logger.info("保存 {} 个标签关联", articleTags.size());
                    articleTagRepository.saveAll(articleTags);
                }
            } else {
                logger.info("没有标签需要处理");
            }

            logger.info("文章创建完成，文章ID: {}", savedArticle.getId());
            // 填充标签信息以便前端直接使用
            populateArticleTags(savedArticle);
            return savedArticle;
        } catch (Exception e) {
            logger.error("创建文章时发生错误: ", e);
            throw e;
        }
    }

    /**
     * 更新文章
     */
    @Transactional
    @CacheEvict(value = {"articles", "categories", "tags"}, allEntries = true)
    public Article updateArticle(Long articleId, Article article, List<Long> tagIds) {
        logger.info("开始更新文章，文章ID: {}, tagIds: {}", articleId, tagIds);
        
        // 添加空值检查
        if (article == null) {
            logger.error("文章对象为空，无法更新文章");
            throw new IllegalArgumentException("文章对象不能为空");
        }
        
        try {
            // 获取现有文章
            Article existingArticle = articleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId));

            // 更新允许修改的字段
            existingArticle.setTitle(article.getTitle());
            existingArticle.setContent(article.getContent());
            existingArticle.setHtmlContent(article.getHtmlContent());
            existingArticle.setCover(article.getCover());
            existingArticle.setCategoryId(article.getCategoryId());
            
            // 只有当这些字段不为null时才更新，或者根据业务需求决定是否允许更新为null
            if (article.getIsTop() != null) {
                existingArticle.setIsTop(article.getIsTop());
            }
            if (article.getStatus() != null) {
                existingArticle.setStatus(article.getStatus());
            }
            if (article.getAllowComment() != null) {
                existingArticle.setAllowComment(article.getAllowComment());
            }
            existingArticle.setScheduledTime(article.getScheduledTime());
            
            // 注意：不更新 userId, createTime, viewCount, likeCount 等统计/系统字段
            
            // 更新文章
            Article savedArticle = articleRepository.save(existingArticle);

            // 删除原有的标签关联
            articleTagRepository.deleteByArticleId(articleId);

            // 重新建立标签关联
            if (tagIds != null && !tagIds.isEmpty()) {
                List<ArticleTag> articleTags = new ArrayList<>();
                for (Long tagId : tagIds) {
                    // 添加空值检查
                    if (tagId != null) {
                        ArticleTag articleTag = new ArticleTag();
                        articleTag.setArticleId(articleId);
                        articleTag.setTagId(tagId);
                        articleTags.add(articleTag);
                    }
                }
                articleTagRepository.saveAll(articleTags);
            }

            logger.info("文章更新完成，文章ID: {}", savedArticle.getId());
            // 填充标签信息以便前端直接使用
            populateArticleTags(savedArticle);
            return savedArticle;
        } catch (Exception e) {
            logger.error("更新文章时发生错误: ", e);
            throw e;
        }
    }

    /**
     * 根据ID获取文章
     */
    @Cacheable(value = "articles", key = "'article_'.concat(#id)")
    public Article getArticleById(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        // 如果文章已删除，视为不存在
        if (article != null && Article.STATUS_DELETED.equals(article.getStatus())) {
            return null;
        }
        // 填充标签信息
        populateArticleTags(article);
        return article;
    }

    /**
     * 根据ID获取已发布的文章
     */
    @Cacheable(value = "articles", key = "'published_article_'.concat(#id)")
    public Article getPublishedArticleById(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article != null && Article.STATUS_PUBLISHED.equals(article.getStatus())) {
            // 填充标签信息
            populateArticleTags(article);
            return article;
        }
        return null;
    }

    /**
     * 分页获取文章列表，支持按状态筛选
     */
    public Page<Article> getArticlesByPage(int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createTime"));
        Page<Article> articles;
        if (status == null) {
            // 如果没有指定状态，返回除了已删除之外的所有文章
            articles = articleRepository.findByStatusNot(Article.STATUS_DELETED, pageable);
        } else {
            articles = articleRepository.findByStatus(status, pageable);
        }
        return populatePageWithTags(articles);
    }

    /**
     * 分页获取已发布的文章列表
     */
    @Cacheable(value = "articles", key = "'published_articles_'.concat(#page).concat('_').concat(#size)")
    public Page<Article> getPublishedArticles(int page, int size) {
        return getArticlesByPage(page, size, Article.STATUS_PUBLISHED);
    }

    /**
     * 分页获取草稿文章列表
     */
    public Page<Article> getDraftArticles(int page, int size) {
        return getArticlesByPage(page, size, Article.STATUS_DRAFT);
    }

    /**
     * 根据分类ID分页获取文章列表，支持按状态筛选
     */
    public Page<Article> getArticlesByCategory(Long categoryId, int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createTime"));
        Page<Article> articles;
        if (status == null) {
            // 如果没有指定状态，返回所有状态的文章
            articles = articleRepository.findByCategoryId(categoryId, pageable);
        } else {
            articles = articleRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        }
        return populatePageWithTags(articles);
    }

    /**
     * 根据分类ID分页获取已发布的文章列表
     */
    public Page<Article> getPublishedArticlesByCategory(Long categoryId, int page, int size) {
        return getArticlesByCategory(categoryId, page, size, Article.STATUS_PUBLISHED);
    }

    /**
     * 根据用户ID分页获取文章列表，支持按状态筛选
     */
    public Page<Article> getArticlesByUser(Long userId, int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createTime"));
        Page<Article> articles;
        if (status == null) {
            // 如果没有指定状态，返回所有状态的文章
            articles = articleRepository.findByUserId(userId, pageable);
        } else {
            articles = articleRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return populatePageWithTags(articles);
    }

    /**
     * 根据用户ID分页获取已发布的文章列表
     */
    public Page<Article> getPublishedArticlesByUser(Long userId, int page, int size) {
        return getArticlesByUser(userId, page, size, Article.STATUS_PUBLISHED);
    }

    /**
     * 根据用户ID分页获取草稿文章列表
     */
    public Page<Article> getDraftArticlesByUser(Long userId, int page, int size) {
        return getArticlesByUser(userId, page, size, Article.STATUS_DRAFT);
    }

    /**
     * 搜索文章，支持按状态筛选
     */
    public Page<Article> searchArticles(String keyword, int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createTime"));
        Page<Article> articles = articleRepository.searchByKeyword(keyword, status, pageable);
        return populatePageWithTags(articles);
    }

    /**
     * 获取热门文章（按浏览量倒序）
     */
    @Cacheable(value = "articles", key = "'popular_articles_'.concat(#limit)")
    public List<Article> getPopularArticles(int limit) {
        // 由于Repository中定义的是Top10，这里如果limit不是10，可能需要调整Repository或者手动截取
        // 简单起见，我们假设 limit <= 10，或者在Service层截取
        List<Article> articles = articleRepository.findTop10ByStatusOrderByViewCountDesc(Article.STATUS_PUBLISHED);
        if (articles.size() > limit) {
            return articles.subList(0, limit);
        }
        return articles;
    }

    /**
     * 搜索已发布的文章
     */
    public Page<Article> searchPublishedArticles(String keyword, int page, int size) {
        return searchArticles(keyword, page, size, Article.STATUS_PUBLISHED);
    }
    
    /**
     * 根据标签ID分页获取已发布的文章列表
     */
    public Page<Article> getPublishedArticlesByTag(Long tagId, int page, int size) {
        // 首先获取该标签下的所有文章ID
        List<ArticleTag> articleTags = articleTagRepository.findByTagId(tagId);
        if (articleTags.isEmpty()) {
            // 如果没有文章，返回空分页结果
            Pageable pageable = PageRequest.of(page, size);
            return Page.empty(pageable);
        }
        
        // 提取文章ID列表
        List<Long> articleIds = articleTags.stream()
                .map(ArticleTag::getArticleId)
                .collect(Collectors.toList());
        
        // 分页获取已发布的文章
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createTime"));
        Page<Article> articles = articleRepository.findAllByIdInAndStatus(articleIds, Article.STATUS_PUBLISHED, pageable);
        return populatePageWithTags(articles);
    }

    /**
     * 删除文章（软删除）
     */
    @Transactional
    @CacheEvict(value = {"articles", "categories", "tags"}, allEntries = true)
    public void deleteArticle(Long id) {
        // 检查文章是否存在
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            // 软删除：设置状态为已删除
            article.setStatus(Article.STATUS_DELETED);
            articleRepository.save(article);
            logger.info("文章已软删除，ID: {}", id);
        } else {
            logger.info("尝试删除不存在的文章，文章ID: {}", id);
        }
    }

    /**
     * 还原文章
     */
    @Transactional
    @CacheEvict(value = {"articles", "categories", "tags"}, allEntries = true)
    public void restoreArticle(Long id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            // 还原为草稿状态，以免直接发布未准备好的内容
            article.setStatus(Article.STATUS_DRAFT);
            articleRepository.save(article);
            logger.info("文章已还原，ID: {}", id);
        }
    }

    /**
     * 永久删除文章
     */
    @Transactional
    @CacheEvict(value = {"articles", "categories", "tags"}, allEntries = true)
    public void deleteArticlePermanently(Long id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            // 删除标签关联
            articleTagRepository.deleteByArticleId(id);
            // 删除文章
            articleRepository.deleteById(id);
            logger.info("文章已永久删除，ID: {}", id);
        }
    }

    /**
     * 获取文章的标签ID列表
     */
    public List<Long> getTagIdsByArticleId(Long articleId) {
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
        return articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
    }

    /**
     * 增加文章浏览量
     */
    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public void incrementViewCount(Long id) {
        articleRepository.incrementViewCount(id);
    }

    /**
     * 更新文章状态
     */
    @Transactional
    @CacheEvict(value = {"articles", "categories", "tags"}, allEntries = true)
    public Article updateArticleStatus(Long id, Integer status) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setStatus(status);
            return articleRepository.save(article);
        }
        return null;
    }
    
    /**
     * 统计已发布文章数量
     */
    @Cacheable(value = "dashboard_stats", key = "'count_published_articles'")
    public long countPublishedArticles() {
        return articleRepository.countByStatus(Article.STATUS_PUBLISHED);
    }

    /**
     * 填充文章标签信息
     */
    private void populateArticleTags(Article article) {
        if (article == null) {
            return;
        }
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(article.getId());
        if (articleTags != null && !articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());
            article.setTagIds(tagIds);
            
            List<Tag> tags = tagRepository.findAllById(tagIds);
            article.setTags(tags);
        }
    }
    
    /**
     * 批量填充文章标签信息
     */
    private Page<Article> populatePageWithTags(Page<Article> articles) {
        if (articles != null && articles.hasContent()) {
            articles.getContent().forEach(this::populateArticleTags);
        }
        return articles;
    }
}