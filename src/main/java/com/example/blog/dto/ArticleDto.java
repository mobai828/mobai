package com.example.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ArticleDto {

    private Long id;

    @NotNull(message = "作者ID不能为空")
    private Long userId;

    private Long categoryId;

    @NotBlank(message = "文章标题不能为空")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String htmlContent;

    private String cover;

    private Integer isTop = 0;

    private Integer status = 1;

    private List<Long> tagIds;
}