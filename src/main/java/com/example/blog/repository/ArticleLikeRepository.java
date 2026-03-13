package com.example.blog.repository;

import com.example.blog.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    
    /**
     * 根据文章ID和用户ID查找点赞记录
     */
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
    
    /**
     * 统计文章的点赞数
     */
    Long countByArticleId(Long articleId);
    
    /**
     * 检查用户是否已点赞
     */
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    
    /**
     * 删除点赞记录
     */
    void deleteByArticleIdAndUserId(Long articleId, Long userId);
}
