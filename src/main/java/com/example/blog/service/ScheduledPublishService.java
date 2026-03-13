package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时发布服务
 */
@Service
public class ScheduledPublishService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledPublishService.class);

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 每分钟检查待发布的文章
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    @Transactional
    public void publishScheduledArticles() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查找所有定时发布时间已到的草稿文章
        List<Article> articlesToPublish = articleRepository
                .findByStatusAndScheduledTimeLessThanEqual(Article.STATUS_DRAFT, now);
        
        for (Article article : articlesToPublish) {
            article.setStatus(Article.STATUS_PUBLISHED);
            article.setScheduledTime(null); // 清除定时发布时间
            articleRepository.save(article);
            logger.info("定时发布文章成功: articleId={}, title={}", article.getId(), article.getTitle());
        }
        
        if (!articlesToPublish.isEmpty()) {
            logger.info("本次定时发布了 {} 篇文章", articlesToPublish.size());
        }
    }
}
