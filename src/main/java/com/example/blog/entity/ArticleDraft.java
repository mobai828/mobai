package com.example.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 文章草稿实体（用于自动保存）
 */
@Data
@Entity
@Table(name = "article_draft")
public class ArticleDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id")
    private Long articleId; // null 表示新文章

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "category_id")
    private Long categoryId;

    private String cover;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
