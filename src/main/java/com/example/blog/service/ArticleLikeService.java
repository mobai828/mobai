package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.repository.ArticleLikeRepository;
import com.example.blog.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 文章点赞服务
 */
@Service
public class ArticleLikeService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleLikeService.class);

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 切换点赞状态（点赞/取消点赞）
     * @return true 表示点赞，false 表示取消点赞
     */
    @Transactional
    public boolean toggleLike(Long articleId, Long userId) {
        Optional<ArticleLike> existingLike = articleLikeRepository
                .findByArticleIdAndUserId(articleId, userId);

        if (existingLike.isPresent()) {
            // 已点赞，取消点赞
            articleLikeRepository.delete(existingLike.get());
            updateArticleLikeCount(articleId, -1);
            logger.info("取消点赞: articleId={}, userId={}", articleId, userId);
            return false;
        } else {
            // 未点赞，添加点赞
            ArticleLike like = new ArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            articleLikeRepository.save(like);
            updateArticleLikeCount(articleId, 1);
            logger.info("点赞成功: articleId={}, userId={}", articleId, userId);
            return true;
        }
    }


    /**
     * 检查用户是否已点赞
     */
    public boolean hasUserLiked(Long articleId, Long userId) {
        return articleLikeRepository.existsByArticleIdAndUserId(articleId, userId);
    }

    /**
     * 获取文章点赞数
     */
    public Long getLikeCount(Long articleId) {
        return articleLikeRepository.countByArticleId(articleId);
    }

    /**
     * 更新文章点赞数
     */
    private void updateArticleLikeCount(Long articleId, int delta) {
        articleRepository.findById(articleId).ifPresent(article -> {
            int newCount = article.getLikeCount() + delta;
            article.setLikeCount(Math.max(0, newCount));
            articleRepository.save(article);
        });
    }
}
