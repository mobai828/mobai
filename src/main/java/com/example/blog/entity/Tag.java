package com.example.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tag")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 非数据库字段，用于存储标签下的文章数量
    @Transient
    private Long articleCount = 0L;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}