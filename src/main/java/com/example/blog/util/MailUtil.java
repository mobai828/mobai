package com.example.blog.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
@ConditionalOnProperty(
    prefix = "spring.mail",
    name = "host"
)
public class MailUtil {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String to, String code) {
        if (mailSender == null) {
            System.out.println("邮件发送功能未配置，无法发送验证码到: " + to);
            return;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("博客系统验证码");
        message.setText("您的验证码是: " + code + "，有效期5分钟。");

        mailSender.send(message);
    }
    
    /**
     * 发送HTML格式邮件
     */
    public void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        if (mailSender == null) {
            System.out.println("邮件发送功能未配置，无法发送邮件到: " + to);
            return;
        }
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}