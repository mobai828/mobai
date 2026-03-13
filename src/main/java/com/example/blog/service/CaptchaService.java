package com.example.blog.service;

import com.example.blog.entity.CaptchaType;
import com.example.blog.entity.EmailCaptcha;
import com.example.blog.repository.EmailCaptchaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * 验证码服务
 */
@Service
public class CaptchaService {
    
    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);
    private static final int CAPTCHA_LENGTH = 6;
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    
    @Autowired
    private EmailCaptchaRepository emailCaptchaRepository;
    
    @Autowired
    private MailService mailService;
    
    /**
     * 生成6位数字验证码
     */
    public String generateCaptcha() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 生成并发送验证码
     */
    @Transactional
    public void generateAndSend(String email, CaptchaType type) {
        // 生成验证码
        String captcha = generateCaptcha();
        
        // 删除该邮箱该类型的旧验证码
        emailCaptchaRepository.deleteByEmailAndType(email, type);
        
        // 创建新验证码记录
        EmailCaptcha emailCaptcha = new EmailCaptcha();
        emailCaptcha.setEmail(email);
        emailCaptcha.setCaptcha(captcha);
        emailCaptcha.setType(type);
        emailCaptcha.setExpireTime(LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRE_MINUTES));
        emailCaptcha.setUsed(false);
        
        emailCaptchaRepository.save(emailCaptcha);
        
        // 发送邮件
        String subject = getCaptchaSubject(type);
        mailService.sendCaptchaEmail(email, captcha, subject, type);
        
        logger.info("验证码已发送到邮箱: {}, 类型: {}", email, type);
    }
    
    /**
     * 验证验证码
     */
    @Transactional
    public boolean verify(String email, String captcha, CaptchaType type) {
        Optional<EmailCaptcha> optional = emailCaptchaRepository
                .findByEmailAndCaptchaAndTypeAndUsedFalse(email, captcha, type);
        
        if (!optional.isPresent()) {
            logger.warn("验证码不存在或已使用: email={}, type={}", email, type);
            return false;
        }
        
        EmailCaptcha emailCaptcha = optional.get();
        
        // 检查是否过期
        if (emailCaptcha.isExpired()) {
            logger.warn("验证码已过期: email={}, type={}", email, type);
            return false;
        }
        
        // 标记为已使用
        emailCaptcha.setUsed(true);
        emailCaptchaRepository.save(emailCaptcha);
        
        logger.info("验证码验证成功: email={}, type={}", email, type);
        return true;
    }
    
    /**
     * 获取验证码邮件主题
     */
    private String getCaptchaSubject(CaptchaType type) {
        switch (type) {
            case REGISTER:
                return "注册验证码";
            case LOGIN:
                return "登录验证码";
            case EMAIL_CHANGE:
                return "邮箱变更验证码";
            case PASSWORD_RESET:
                return "密码重置验证码";
            default:
                return "验证码";
        }
    }
}
