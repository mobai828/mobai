package com.example.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserProfileDto {

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Size(max = 255, message = "个人简介长度不能超过255个字符")
    private String intro;

    private String avatar;
}