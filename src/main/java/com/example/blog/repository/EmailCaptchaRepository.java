package com.example.blog.repository;

import com.example.blog.entity.CaptchaType;
import com.example.blog.entity.EmailCaptcha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailCaptchaRepository extends JpaRepository<EmailCaptcha, Long> {
    
    /**
     * 根据邮箱、类型查找未使用的验证码
     */
    Optional<EmailCaptcha> findByEmailAndTypeAndUsedFalse(String email, CaptchaType type);
    
    /**
     * 根据邮箱、验证码、类型查找未使用的验证码
     */
    Optional<EmailCaptcha> findByEmailAndCaptchaAndTypeAndUsedFalse(
            String email, String captcha, CaptchaType type);
    
    /**
     * 删除指定邮箱和类型的所有验证码
     */
    void deleteByEmailAndType(String email, CaptchaType type);
}
