package com.example.blog.controller;

import com.example.blog.dto.JwtResponseDto;
import com.example.blog.dto.UserLoginDto;
import com.example.blog.dto.UserProfileDto;
import com.example.blog.dto.UserRegisterDto;
import com.example.blog.entity.User;
import com.example.blog.service.AuthService;
import com.example.blog.service.MailService;
import com.example.blog.service.StorageService;
import com.example.blog.service.UserService;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MailService mailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StorageService storageService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDto registerDto) {
        String username = registerDto.getUsername();
        String email = registerDto.getEmail();
        String password = registerDto.getPassword();
        String captcha = registerDto.getCaptcha();
        
        // 检查用户名是否已存在
        if (userService.checkUsernameExists(username)) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "用户名已存在"));
        }

        // 检查邮箱是否已存在
        if (userService.checkEmailExists(email)) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "邮箱已被注册"));
        }

        // 验证验证码
        if (!mailService.verifyCode(email, captcha)) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "验证码错误或已过期"));
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        User savedUser = userService.registerUser(user);

        // 生成JWT Token
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getRole());

        JwtResponseDto response = new JwtResponseDto(token, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
        return ResponseEntity.ok(MapUtil.of("success", true, "data", response));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Map<String, String> loginRequest) {
        String emailOrUsername = loginRequest.get("emailOrUsername");
        String password = loginRequest.get("password");
        
        String token = authService.authenticateUser(emailOrUsername, password);

        if (token != null) {
            // 获取用户信息
            User user = userService.getUserByUsername(emailOrUsername);
            if (user == null) {
                user = userService.getUserByEmail(emailOrUsername);
            }

            JwtResponseDto response = new JwtResponseDto(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
            return ResponseEntity.ok(MapUtil.of("success", true, "data", response));
        } else {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "用户名或密码错误"));
        }
    }
    
    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login/email")
    public ResponseEntity<?> loginByEmailCode(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String code = loginRequest.get("code");
        
        // 验证验证码
        if (!mailService.verifyCode(email, code)) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "验证码错误或已过期"));
        }
        
        // 获取用户信息
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "该邮箱未注册"));
        }
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        
        JwtResponseDto response = new JwtResponseDto(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(MapUtil.of("success", true, "data", response));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @Valid @RequestBody UserProfileDto profileDto) {
        // 如果只更新头像，nickname 可能为 null，需要从数据库获取当前用户信息
        if (profileDto.getNickname() == null) {
            User currentUser = userService.getUserById(userId);
            if (currentUser != null) {
                profileDto.setNickname(currentUser.getNickname());
            }
        }
        
        User user = userService.updateUserProfile(userId, profileDto.getNickname(), profileDto.getIntro(), profileDto.getAvatar());
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authorization) {
        try {
            // 从Authorization头中提取token
            String token = authorization.substring(7); // 移除"Bearer "前缀
            
            // 使用JWT工具类验证token并获取用户ID
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            // 根据用户ID获取用户信息
            User user = userService.getUserById(userId);
            
            if (user != null) {
                // 构建响应数据
                Map<String, Object> userInfo = MapUtil.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                    "nickname", user.getNickname() != null ? user.getNickname() : ""
                );
                
                return ResponseEntity.ok(MapUtil.of("success", true, "data", userInfo));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(MapUtil.of("success", false, "message", "无效的token"));
        }
    }

    @PostMapping("/mail/send-captcha")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        mailService.sendVerificationCode(email);
        return ResponseEntity.ok(MapUtil.of("success", true, "message", "验证码已发送"));
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !isValidImageType(contentType)) {
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, 
                    "message", "只支持 JPG/PNG/GIF 格式的图片"));
            }
            
            // 验证文件大小（最大10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, 
                    "message", "图片大小不能超过10MB"));
            }
            
            // 获取用户ID
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            // 使用 StorageService 保存文件，返回唯一文件名
            String storedFileName = storageService.storeFile(file);
            // 构造头像访问 URL，基于 /uploads/ 资源映射
            String avatarUrl = "/uploads/" + storedFileName;

            // 更新用户头像
            User user = userService.updateAvatar(userId, avatarUrl);
            if (user != null) {
                return ResponseEntity.ok(MapUtil.of("success", true, "data", MapUtil.of("avatarUrl", avatarUrl)));
            } else {
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                return ResponseEntity.ok(MapUtil.of("success", true, "message", "密码修改成功"));
            } else {
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "旧密码不正确"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 变更邮箱
     */
    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        try {
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            String newEmail = request.get("newEmail");
            String captcha = request.get("captcha");
            
            boolean success = userService.changeEmail(userId, newEmail, captcha);
            if (success) {
                return ResponseEntity.ok(MapUtil.of("success", true, "message", "邮箱变更成功"));
            } else {
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "邮箱变更失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/gif");
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".jpg";
    }
}

