package com.example.blog.service;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.PasswordEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String credential) throws UsernameNotFoundException {
        logger.info("尝试登录，凭证: {}", credential);
        User user = null;
        
        // 判断是邮箱还是用户名
        if (credential.contains("@")) {
            user = userRepository.findByEmail(credential)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        } else {
            user = userRepository.findByUsername(credential)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        }
        
        // 记录用户信息和密码验证结果
        logger.info("找到用户: {}, 角色: {}, 密码: {}", user.getUsername(), user.getRole(), user.getPassword());
        // 测试密码匹配
        logger.info("测试密码 'password123' 是否匹配: {}", PasswordEncoderUtil.matches("password123", user.getPassword()));
        
        // 创建UserDetails对象，使用数据库中存储的角色（已经包含ROLE_前缀）
        String role = user.getRole();
        logger.info("创建UserDetails对象，用户名: {}, 角色: {}", credential, role);
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        return new org.springframework.security.core.userdetails.User(
                credential, // 使用登录时的credential作为用户名
                user.getPassword(),
                enabled, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}