package com.example.blog.controller;

import com.example.blog.dto.UserLoginDto;
import com.example.blog.entity.CaptchaType;
import com.example.blog.entity.User;
import com.example.blog.service.AuthService;
import com.example.blog.service.CaptchaService;
import com.example.blog.service.GiteeOAuthService;
import com.example.blog.service.UserService;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private CaptchaService captchaService;
    
    @Autowired
    private GiteeOAuthService giteeOAuthService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // 显示注册页面
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // 处理注册请求
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        // 检查用户名是否已存在
        if (userService.checkUsernameExists(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "该用户名已被使用");
        }

        // 检查邮箱是否已存在
        if (userService.checkEmailExists(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "该邮箱已被注册");
        }

        // 如果有验证错误，返回注册页面
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // 注册用户
        userService.registerUser(user);

        // 注册成功后跳转到登录页面
        model.addAttribute("successMessage", "注册成功，请登录");
        return "redirect:/login?success";
    }

    // 显示登录页面
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("userLoginDto", new UserLoginDto());
        // 添加错误消息属性
        model.addAttribute("errorMessage", "");
        return "login";
    }

    // Spring Security的表单登录会自动处理POST /login请求，无需手动实现
    
    // 处理API登录请求
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody Map<String, String> loginRequest) {
        String emailOrUsername = loginRequest.get("credential");
        String password = loginRequest.get("password");
        
        try {
            // 认证用户
            String token = authService.authenticateUser(emailOrUsername, password);
            
            if (token != null) {
                // 获取用户信息
                User user;
                if (emailOrUsername.contains("@")) {
                    user = userService.getUserByEmail(emailOrUsername);
                } else {
                    user = userService.getUserByUsername(emailOrUsername);
                }
                
                // 返回JWT token和用户角色
                return ResponseEntity.ok(MapUtil.of("success", true, "data", MapUtil.of(
                    "token", token,
                    "role", user != null ? user.getRole() : "ROLE_USER"
                )));
            } else {
                // 登录失败
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "登录凭证或密码错误"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 显示忘记密码页面
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // 重置密码
    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String captcha = request.get("captcha");
        String password = request.get("password");
        
        try {
            authService.resetPassword(email, captcha, password);
            return ResponseEntity.ok(MapUtil.of("success", true, "message", "密码重置成功，请重新登录"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 发送登录验证码
    @PostMapping("/api/auth/send-captcha")
    @ResponseBody
    public ResponseEntity<?> sendCaptcha(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String type = request.get("type"); // LOGIN, REGISTER, EMAIL_CHANGE, PASSWORD_RESET
        
        try {
            CaptchaType captchaType = CaptchaType.valueOf(type.toUpperCase());
            captchaService.generateAndSend(email, captchaType);
            return ResponseEntity.ok(MapUtil.of("success", true, "message", "验证码已发送"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 邮箱验证码登录
    @PostMapping("/api/auth/login/captcha")
    @ResponseBody
    public ResponseEntity<?> loginByCaptcha(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String captcha = request.get("captcha");
        
        try {
            String token = authService.authenticateByEmailCaptcha(email, captcha);
            
            if (token != null) {
                User user = userService.getUserByEmail(email);
                return ResponseEntity.ok(MapUtil.of("success", true, "data", MapUtil.of(
                    "token", token,
                    "role", user != null ? user.getRole() : "ROLE_USER"
                )));
            } else {
                return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", "验证码无效或已过期"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Gitee OAuth 登录入口
    @GetMapping("/api/auth/login/gitee")
    public String giteeLogin() {
        return "redirect:" + giteeOAuthService.getAuthorizationUrl();
    }
    
    
// Gitee OAuth 回调
    @GetMapping("/api/auth/callback/gitee")
    public String giteeCallback(@RequestParam String code, Model model) {
        try {
            String token = giteeOAuthService.handleCallback(code);
            
            if (token != null) {
                // 登录成功，重定向到首页并携带token
                return "redirect:/?token=" + token;
            } else {
                // 登录失败
                model.addAttribute("errorMessage", "Gitee登录失败");
                return "login";
            }
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        }
    }
    
    // API注册（带验证码）
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String captcha = request.get("captcha");
        
        try {
            User user = userService.registerWithCaptcha(username, email, password, captcha);
            return ResponseEntity.ok(MapUtil.of("success", true, "message", "注册成功", "data", MapUtil.of(
                "userId", user.getId(),
                "username", user.getUsername()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapUtil.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 检查登录状态
    @GetMapping("/api/auth/check")
    @ResponseBody
    public ResponseEntity<?> checkAuth(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.ok(MapUtil.of("success", false, "message", "未登录"));
            }
            
            String token = authorization.substring(7);
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            Long userId = Long.parseLong(userIdStr);
            
            User user = userService.getUserById(userId);
            if (user != null) {
                return ResponseEntity.ok(MapUtil.of("success", true, "data", MapUtil.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                    "nickname", user.getNickname() != null ? user.getNickname() : ""
                )));
            } else {
                return ResponseEntity.ok(MapUtil.of("success", false, "message", "用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(MapUtil.of("success", false, "message", "token无效或已过期"));
        }
    }
    
    // 登出
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> logout() {
        // JWT是无状态的，服务端不需要做任何处理
        // 客户端只需要删除localStorage中的token即可
        return ResponseEntity.ok(MapUtil.of("success", true, "message", "退出成功"));
    }
}
