package com.example.blog.service;

import com.example.blog.entity.ArticleDraft;
import com.example.blog.repository.ArticleDraftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 草稿服务
 */
@Service
public class DraftService {

    private static final Logger logger = LoggerFactory.getLogger(DraftService.class);

    @Autowired
    private ArticleDraftRepository articleDraftRepository;

    /**
     * 保存草稿
     */
    public ArticleDraft saveDraft(Long userId, Long articleId, String title, String content, 
            Long categoryId, String cover) {
        // 查找现有草稿
        Optional<ArticleDraft> existingDraft = articleDraftRepository
                .findByUserIdAndArticleId(userId, articleId);
        
        ArticleDraft draft;
        if (existingDraft.isPresent()) {
            draft = existingDraft.get();
        } else {
            draft = new ArticleDraft();
            draft.setUserId(userId);
            draft.setArticleId(articleId);
        }
        
        draft.setTitle(title);
        draft.setContent(content);
        draft.setCategoryId(categoryId);
        draft.setCover(cover);
        
        ArticleDraft saved = articleDraftRepository.save(draft);
        logger.info("草稿保存成功: userId={}, articleId={}", userId, articleId);
        return saved;
    }

    /**
     * 获取草稿
     */
    public ArticleDraft getDraft(Long userId, Long articleId) {
        return articleDraftRepository.findByUserIdAndArticleId(userId, articleId).orElse(null);
    }

    /**
     * 获取用户所有草稿
     */
    public List<ArticleDraft> getUserDrafts(Long userId) {
        return articleDraftRepository.findByUserIdOrderByUpdateTimeDesc(userId);
    }

    /**
     * 删除草稿
     */
    public void deleteDraft(Long userId, Long articleId) {
        articleDraftRepository.deleteByUserIdAndArticleId(userId, articleId);
        logger.info("草稿删除成功: userId={}, articleId={}", userId, articleId);
    }
}
