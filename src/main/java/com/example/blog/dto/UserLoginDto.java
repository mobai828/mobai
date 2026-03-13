package com.example.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginDto {

    @NotBlank(message = "登录凭证不能为空")
    private String credential;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe = false;
}