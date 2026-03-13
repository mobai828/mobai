package com.example.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TagDto {

    private Long id;

    @NotBlank(message = "标签名称不能为空")
    private String name;
}