package com.example.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        // 转换为小写后再加密，实现不区分大小写的密码验证
        return encoder.encode(rawPassword.toLowerCase());
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        // 转换为小写后再验证，实现不区分大小写的密码验证
        return encoder.matches(rawPassword.toLowerCase(), encodedPassword);
    }
}