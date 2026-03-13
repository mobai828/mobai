package com.example.blog.repository;

import com.example.blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Boolean existsByName(String name);
    
    // 获取分类下的文章数量（只统计已发布的文章）
    @Query("SELECT COUNT(a) FROM Article a WHERE a.categoryId = :categoryId AND a.status = 1")
    Long countArticlesByCategoryId(@Param("categoryId") Long categoryId);
}