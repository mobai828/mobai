package com.example.blog.repository;

import com.example.blog.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    List<ArticleTag> findByArticleId(Long articleId);
    List<ArticleTag> findByTagId(Long tagId);
    void deleteByArticleId(Long articleId);
}