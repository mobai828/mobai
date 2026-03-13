package com.example.blog;

import com.example.blog.util.PasswordEncoderUtil;
import org.junit.jupiter.api.Test;

public class PasswordTest {
    @Test
    public void testPassword() {
        String password = "password123";
        String dbPassword = "$2a$10$GZOQ0g9UfYfsjK7EpOi9Ee53IRYuEGTx/J10YoXRwkG4JYAFqldze";
        
        System.out.println("密码: " + password);
        System.out.println("数据库密码哈希: " + dbPassword);
        System.out.println("密码匹配: " + PasswordEncoderUtil.matches(password, dbPassword));
        System.out.println("新生成的哈希: " + PasswordEncoderUtil.encode(password));
    }
}