package com.example.blog.service;

import com.example.blog.entity.CaptchaType;
import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.PasswordEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CaptchaService captchaService;

    public User registerUser(User user) {
        user.setPassword(PasswordEncoderUtil.encode(user.getPassword()));
        user.setRole("USER"); // 确保默认角色为USER
        user.setEmailVerified(false);
        return userRepository.save(user);
    }

    public User registerAdmin(User user) {
        user.setPassword(PasswordEncoderUtil.encode(user.getPassword()));
        user.setRole("ADMIN");
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
    
    /**
     * 带验证码的注册
     */
    public User registerWithCaptcha(String username, String email, String password, String captcha) {
        // 验证验证码
        if (!captchaService.verify(email, captcha, CaptchaType.REGISTER)) {
            throw new RuntimeException("验证码无效或已过期");
        }
        
        // 检查用户名和邮箱
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(PasswordEncoderUtil.encode(password));
        user.setRole("USER");
        user.setEmailVerified(true); // 通过验证码验证的邮箱
        user.setStatus(1);
        
        User savedUser = userRepository.save(user);
        logger.info("用户注册成功: username={}, email={}", username, email);
        return savedUser;
    }
    
    /**
     * 发送注册验证码
     */
    public void sendRegisterCaptcha(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已被注册");
        }
        captchaService.generateAndSend(email, CaptchaType.REGISTER);
    }

    public User updateUserProfile(Long userId, String nickname, String intro, String avatar) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (nickname != null) {
                user.setNickname(nickname);
            }
            if (intro != null) {
                user.setIntro(intro);
            }
            if (avatar != null) {
                user.setAvatar(avatar);
            }
            return userRepository.save(user);
        }
        return null;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User getAdminUser() {
        return userRepository.findFirstByRole("ADMIN").orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 分页获取用户列表
     */
    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return userRepository.findAll(pageable);
    }

    /**
     * 更新用户状态
     */
    public User updateUserStatus(Long id, Integer status) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus(status);
            return userRepository.save(user);
        }
        return null;
    }
    
    /**
     * 更新用户角色
     */
    public User updateUserRole(Long id, String role) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setRole(role);
            return userRepository.save(user);
        }
        return null;
    }
    
    /**
     * 修改密码
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 验证旧密码
            if (!PasswordEncoderUtil.matches(oldPassword, user.getPassword())) {
                logger.warn("修改密码失败，旧密码不正确: userId={}", userId);
                return false;
            }
            // 更新密码
            user.setPassword(PasswordEncoderUtil.encode(newPassword));
            userRepository.save(user);
            logger.info("密码修改成功: userId={}", userId);
            return true;
        }
        return false;
    }
    
    /**
     * 变更邮箱
     */
    public boolean changeEmail(Long userId, String newEmail, String captcha) {
        // 验证验证码
        if (!captchaService.verify(newEmail, captcha, CaptchaType.EMAIL_CHANGE)) {
            throw new RuntimeException("验证码无效或已过期");
        }
        
        // 检查新邮箱是否已被使用
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("该邮箱已被使用");
        }
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmail(newEmail);
            user.setEmailVerified(true);
            userRepository.save(user);
            logger.info("邮箱变更成功: userId={}, newEmail={}", userId, newEmail);
            return true;
        }
        return false;
    }
    
    /**
     * 更新用户头像
     */
    public User updateAvatar(Long userId, String avatarUrl) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            logger.info("头像更新成功: userId={}", userId);
            return user;
        }
        return null;
    }
}