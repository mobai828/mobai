package com.example.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CategoryDto {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;
}