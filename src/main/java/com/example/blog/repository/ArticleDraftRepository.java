package com.example.blog.repository;

import com.example.blog.entity.ArticleDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleDraftRepository extends JpaRepository<ArticleDraft, Long> {
    
    /**
     * 根据用户ID和文章ID查找草稿
     */
    Optional<ArticleDraft> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 根据用户ID查找所有草稿
     */
    List<ArticleDraft> findByUserIdOrderByUpdateTimeDesc(Long userId);
    
    /**
     * 根据用户ID查找新文章草稿（articleId为null）
     */
    List<ArticleDraft> findByUserIdAndArticleIdIsNullOrderByUpdateTimeDesc(Long userId);
    
    /**
     * 删除指定用户的指定文章草稿
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
}
