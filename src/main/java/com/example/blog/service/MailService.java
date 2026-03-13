package com.example.blog.service;

import com.example.blog.entity.CaptchaType;
import com.example.blog.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private MailUtil mailUtil;

    // 简单的内存存储验证码，实际项目中应该使用Redis等缓存
    private Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private Map<String, Long> codeExpirations = new ConcurrentHashMap<>();

    public void sendVerificationCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        
        // 设置5分钟过期时间
        long expiration = System.currentTimeMillis() + 5 * 60 * 1000;
        
        verificationCodes.put(email, code);
        codeExpirations.put(email, expiration);
        
        // 发送邮件
        mailUtil.sendVerificationCode(email, code);
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        Long expiration = codeExpirations.get(email);
        
        if (storedCode != null && expiration != null) {
            // 检查验证码是否正确且未过期
            if (storedCode.equals(code) && System.currentTimeMillis() < expiration) {
                // 验证成功后清除验证码
                verificationCodes.remove(email);
                codeExpirations.remove(email);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 发送验证码邮件（新增方法）
     */
    public void sendCaptchaEmail(String email, String captcha, String subject, CaptchaType type) {
        String content = buildCaptchaEmailContent(captcha, type);
        try {
            mailUtil.sendHtmlMail(email, subject, content);
            logger.info("验证码邮件发送成功: email={}, type={}", email, type);
        } catch (Exception e) {
            logger.error("验证码邮件发送失败: email={}, type={}", email, type, e);
            // 降级为简单文本邮件
            mailUtil.sendVerificationCode(email, captcha);
        }
    }
    
    /**
     * 发送评论通知邮件给管理员
     */
    public void sendCommentNotification(String adminEmail, String articleTitle, 
            String commenterName, String commentContent) {
        String subject = "新评论通知 - " + articleTitle;
        String content = String.format(
            "<h3>您的文章收到了新评论</h3>" +
            "<p><strong>文章：</strong>%s</p>" +
            "<p><strong>评论者：</strong>%s</p>" +
            "<p><strong>评论内容：</strong></p>" +
            "<blockquote>%s</blockquote>",
            articleTitle, commenterName, commentContent
        );
        try {
            mailUtil.sendHtmlMail(adminEmail, subject, content);
            logger.info("评论通知邮件发送成功: adminEmail={}", adminEmail);
        } catch (Exception e) {
            logger.error("评论通知邮件发送失败", e);
        }
    }
    
    /**
     * 发送回复通知邮件给用户
     */
    public void sendReplyNotification(String userEmail, String articleTitle,
            String replierName, String replyContent) {
        String subject = "您的评论收到了回复 - " + articleTitle;
        String content = String.format(
            "<h3>您的评论收到了新回复</h3>" +
            "<p><strong>文章：</strong>%s</p>" +
            "<p><strong>回复者：</strong>%s</p>" +
            "<p><strong>回复内容：</strong></p>" +
            "<blockquote>%s</blockquote>",
            articleTitle, replierName, replyContent
        );
        try {
            mailUtil.sendHtmlMail(userEmail, subject, content);
            logger.info("回复通知邮件发送成功: userEmail={}", userEmail);
        } catch (Exception e) {
            logger.error("回复通知邮件发送失败", e);
        }
    }
    
    /**
     * 构建验证码邮件内容
     */
    private String buildCaptchaEmailContent(String captcha, CaptchaType type) {
        String action = getActionDescription(type);
        return String.format(
            "<div style='padding: 20px; background-color: #f5f5f5;'>" +
            "<h2 style='color: #333;'>验证码</h2>" +
            "<p>您正在进行<strong>%s</strong>操作，验证码为：</p>" +
            "<p style='font-size: 24px; color: #007bff; font-weight: bold;'>%s</p>" +
            "<p style='color: #666;'>验证码有效期为5分钟，请勿泄露给他人。</p>" +
            "<p style='color: #999; font-size: 12px;'>如非本人操作，请忽略此邮件。</p>" +
            "</div>",
            action, captcha
        );
    }
    
    private String getActionDescription(CaptchaType type) {
        switch (type) {
            case REGISTER: return "账号注册";
            case LOGIN: return "邮箱登录";
            case EMAIL_CHANGE: return "邮箱变更";
            case PASSWORD_RESET: return "密码重置";
            default: return "身份验证";
        }
    }
}