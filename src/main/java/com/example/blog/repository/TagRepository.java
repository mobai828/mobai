package com.example.blog.repository;

import com.example.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    Boolean existsByName(String name);
    
    // 获取标签下的文章数量（只统计已发布的文章）
    @Query("SELECT COUNT(DISTINCT at.articleId) FROM ArticleTag at JOIN Article a ON at.articleId = a.id WHERE at.tagId = :tagId AND a.status = 1")
    Long countArticlesByTagId(@Param("tagId") Long tagId);
}