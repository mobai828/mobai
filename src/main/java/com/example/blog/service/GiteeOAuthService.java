package com.example.blog.service;

import com.example.blog.entity.OAuthBinding;
import com.example.blog.entity.OAuthProvider;
import com.example.blog.entity.User;
import com.example.blog.repository.OAuthBindingRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.PasswordEncoderUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Gitee OAuth 登录服务
 */
@Service
public class GiteeOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GiteeOAuthService.class);

    @Value("${gitee.client-id}")
    private String clientId;

    @Value("${gitee.client-secret}")
    private String clientSecret;

    @Value("${gitee.redirect-uri}")
    private String redirectUri;

    @Value("${gitee.authorization-url}")
    private String authorizationUrl;

    @Value("${gitee.token-url}")
    private String tokenUrl;

    @Value("${gitee.user-info-url}")
    private String userInfoUrl;

    @Autowired
    private OAuthBindingRepository oAuthBindingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;


    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取Gitee授权URL
     */
    public String getAuthorizationUrl() {
        return String.format("%s?client_id=%s&redirect_uri=%s&response_type=code",
                authorizationUrl, clientId, redirectUri);
    }

    /**
     * 处理Gitee回调，完成登录或注册
     */
    public String handleCallback(String code) {
        try {
            // 1. 用授权码换取access_token
            String accessToken = getAccessToken(code);
            if (accessToken == null) {
                logger.error("获取Gitee access_token失败");
                return null;
            }

            // 2. 获取用户信息
            Map<String, Object> userInfo = getUserInfo(accessToken);
            if (userInfo == null) {
                logger.error("获取Gitee用户信息失败");
                return null;
            }

            String giteeId = String.valueOf(userInfo.get("id"));
            String giteeName = (String) userInfo.get("name");
            String giteeAvatar = (String) userInfo.get("avatar_url");

            // 3. 查找是否已绑定
            Optional<OAuthBinding> bindingOpt = oAuthBindingRepository
                    .findByProviderAndOauthId(OAuthProvider.GITEE, giteeId);

            User user;
            if (bindingOpt.isPresent()) {
                // 已绑定，直接登录
                OAuthBinding binding = bindingOpt.get();
                user = userRepository.findById(binding.getUserId()).orElse(null);
                if (user == null) {
                    logger.error("绑定的用户不存在: userId={}", binding.getUserId());
                    return null;
                }
                
                // 检查用户状态
                if (user.getStatus() != 1) {
                    logger.warn("Gitee登录失败，用户已禁用: userId={}", user.getId());
                    throw new RuntimeException("已被禁用");
                }
                
                // 更新绑定信息
                binding.setAccessToken(accessToken);
                binding.setOauthName(giteeName);
                binding.setOauthAvatar(giteeAvatar);
                oAuthBindingRepository.save(binding);
            } else {
                // 未绑定，创建新用户
                user = createUserFromGitee(giteeId, giteeName, giteeAvatar, accessToken);
            }

            // 4. 生成JWT token
            return jwtUtil.generateToken(user.getId(), user.getRole());

        } catch (Exception e) {
            logger.error("Gitee OAuth登录失败", e);
            return null;
        }
    }

    private String getAccessToken(String code) {
        try {
            String url = String.format("%s?grant_type=authorization_code&code=%s&client_id=%s&redirect_uri=%s&client_secret=%s",
                    tokenUrl, code, clientId, redirectUri, clientSecret);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());
                return json.get("access_token").asText();
            }
        } catch (Exception e) {
            logger.error("获取Gitee access_token异常", e);
        }
        return null;
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        try {
            String url = userInfoUrl + "?access_token=" + accessToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            logger.error("获取Gitee用户信息异常", e);
        }
        return null;
    }

    private User createUserFromGitee(String giteeId, String giteeName, String giteeAvatar, String accessToken) {
        // 创建用户
        User user = new User();
        user.setUsername("gitee_" + giteeId);
        user.setEmail("gitee_" + giteeId + "@placeholder.com"); // 占位邮箱
        user.setPassword(PasswordEncoderUtil.encode(UUID.randomUUID().toString())); // 随机密码
        user.setNickname(giteeName);
        user.setAvatar(giteeAvatar);
        user.setRole("USER");
        user.setStatus(1);
        user = userRepository.save(user);

        // 创建绑定
        OAuthBinding binding = new OAuthBinding();
        binding.setUserId(user.getId());
        binding.setProvider(OAuthProvider.GITEE);
        binding.setOauthId(giteeId);
        binding.setOauthName(giteeName);
        binding.setOauthAvatar(giteeAvatar);
        binding.setAccessToken(accessToken);
        oAuthBindingRepository.save(binding);

        logger.info("Gitee用户创建成功: userId={}, giteeId={}", user.getId(), giteeId);
        return user;
    }
}
