package com.example.blog.repository;

import com.example.blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticleIdAndStatusOrderByCreateTimeAsc(Long articleId, Integer status, Pageable pageable);
    List<Comment> findByArticleIdAndStatusOrderByCreateTimeAsc(Long articleId, Integer status);
    List<Comment> findByParentIdOrderByCreateTimeAsc(Long parentId);
    Page<Comment> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    
    // 查询所有状态的评论（管理员用）
    Page<Comment> findAll(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.articleId = :articleId AND c.status = 1")
    Long countByArticleIdAndStatus(@Param("articleId") Long articleId);
}