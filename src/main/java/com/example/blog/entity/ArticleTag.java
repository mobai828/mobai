package com.example.blog.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "article_tag")
public class ArticleTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}