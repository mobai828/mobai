package com.example.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "article", indexes = {
    @Index(name = "idx_article_status", columnList = "status"),
    @Index(name = "idx_article_user_id", columnList = "user_id"),
    @Index(name = "idx_article_category_id", columnList = "category_id"),
    @Index(name = "idx_article_create_time", columnList = "create_time")
})
public class Article {
    // 文章状态常量定义
    public static final Integer STATUS_DRAFT = 0; // 草稿
    public static final Integer STATUS_PUBLISHED = 1; // 已发布
    public static final Integer STATUS_DELETED = 2; // 已删除
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "html_content", columnDefinition = "LONGTEXT")
    private String htmlContent;

    private String cover;

    @Column(name = "is_top", columnDefinition = "TINYINT default 0")
    private Integer isTop = 0;

    @Column(columnDefinition = "TINYINT default 1")
    private Integer status = 1;

    @Column(name = "view_count", columnDefinition = "INT default 0")
    private Integer viewCount = 0;

    @Column(name = "like_count", columnDefinition = "INT default 0")
    private Integer likeCount = 0;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime; // 定时发布时间

    @Column(name = "allow_comment", columnDefinition = "TINYINT default 1")
    private Boolean allowComment = true; // 是否允许评论

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 非数据库字段，用于接收标签ID列表
    @Transient
    private List<Long> tagIds;

    // 非数据库字段，用于返回标签详情列表
    @Transient
    private List<Tag> tags;
    
    // 添加setter方法确保tagIds可以被正确设置，增加空值检查
    public void setTagIds(List<Long> tagIds) {
        if (tagIds != null) {
            this.tagIds = new ArrayList<>(tagIds);
        } else {
            this.tagIds = new ArrayList<>();
        }
    }
    
    // 添加getter方法确保tagIds可以被正确获取
    public List<Long> getTagIds() {
        if (this.tagIds == null) {
            this.tagIds = new ArrayList<>();
        }
        return this.tagIds;
    }

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