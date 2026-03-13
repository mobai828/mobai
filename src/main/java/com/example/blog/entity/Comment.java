package com.example.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "comment", indexes = {
    @Index(name = "idx_comment_article_id", columnList = "article_id"),
    @Index(name = "idx_comment_status", columnList = "status"),
    @Index(name = "idx_comment_user_id", columnList = "user_id")
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 500)
    private String content;

    private String ip;

    @Column(columnDefinition = "TINYINT default 1")
    private Integer status = 1;

    // 匿名评论相关字段
    @Column(name = "nickname")
    private String nickname; // 匿名评论昵称

    @Column(name = "email")
    private String email; // 匿名评论邮箱（用于通知）

    @Column(name = "is_anonymous", columnDefinition = "TINYINT default 0")
    private Boolean isAnonymous = false;

    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 子评论列表，用于构建评论树结构，不持久化到数据库
    @Transient
    private List<Comment> childComments;
    
    // 用户头像，不持久化到数据库
    @Transient
    private String avatar;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}