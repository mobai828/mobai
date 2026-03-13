package com.example.blog.entity;

/**
 * 验证码类型枚举
 */
public enum CaptchaType {
    REGISTER,       // 注册验证
    LOGIN,          // 登录验证
    EMAIL_CHANGE,   // 邮箱变更
    PASSWORD_RESET  // 密码重置
}
