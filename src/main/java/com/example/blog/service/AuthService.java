package com.example.blog.service;

import com.example.blog.entity.CaptchaType;
import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.PasswordEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CaptchaService captchaService;

    /**
     * 用户名/邮箱 + 密码登录
     */
    public String authenticateUser(String credential, String password) {
        User user = null;
        
        // 判断是邮箱还是用户名登录
        if (credential.contains("@")) {
            user = userRepository.findByEmail(credential).orElse(null);
        } else {
            user = userRepository.findByUsername(credential).orElse(null);
        }
        
        if (user != null && PasswordEncoderUtil.matches(password, user.getPassword())) {
            if (user.getStatus() != null && user.getStatus() != 1) {
                throw new RuntimeException("已被禁用");
            }
            return jwtUtil.generateToken(user.getId(), user.getRole());
        }
        
        return null;
    }
    
    /**
     * 邮箱验证码登录（无密码登录）
     */
    public String authenticateByEmailCaptcha(String email, String captcha) {
        // 验证验证码
        if (!captchaService.verify(email, captcha, CaptchaType.LOGIN)) {
            logger.warn("邮箱验证码登录失败，验证码无效: email={}", email);
            return null;
        }
        
        // 查找用户
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logger.warn("邮箱验证码登录失败，用户不存在: email={}", email);
            return null;
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            logger.warn("邮箱验证码登录失败，用户已禁用: email={}", email);
            throw new RuntimeException("已被禁用");
        }
        
        logger.info("邮箱验证码登录成功: email={}", email);
        return jwtUtil.generateToken(user.getId(), user.getRole());
    }
    
    /**
     * 发送登录验证码
     */
    public void sendLoginCaptcha(String email) {
        // 检查邮箱是否已注册
        if (!userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("该邮箱未注册");
        }
        captchaService.generateAndSend(email, CaptchaType.LOGIN);
    }
    
    /**
     * 重置密码
     */
    public void resetPassword(String email, String captcha, String newPassword) {
        // 验证验证码
        if (!captchaService.verify(email, captcha, CaptchaType.PASSWORD_RESET)) {
            throw new RuntimeException("验证码无效或已过期");
        }
        
        // 查找用户
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("该邮箱未注册"));
        
        // 更新密码
        user.setPassword(PasswordEncoderUtil.encode(newPassword));
        userRepository.save(user);
        
        logger.info("用户重置密码成功: email={}", email);
    }
    
    /**
     * 更新用户登录信息
     */
    public void updateLoginInfo(Long userId, String ip) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userRepository.save(user);
        });
    }
}