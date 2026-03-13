package com.example.blog.config;

import com.example.blog.service.CustomUserDetailsService;
import com.example.blog.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.example.blog.repository.UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("Processing JWT authentication for request: {}", request.getRequestURI());
        
        // 跳过已认证的请求
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("Request is already authenticated, skipping JWT filter");
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头中提取Authorization头
        String authorizationHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authorizationHeader);

        String token = null;
        String userId = null;
        String role = null;

        // 检查Authorization头是否存在，并且格式为"Bearer {token}"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            logger.debug("Extracted token: {}", token);
            try {
                userId = jwtUtil.getUserIdFromToken(token);
                role = jwtUtil.getRoleFromToken(token);
                logger.debug("JWT token is valid, userId: {}, role: {}", userId, role);
            } catch (Exception e) {
                // 无效的token
                logger.error("Invalid JWT token: {}", e.getMessage());
            }
        } else {
            logger.debug("Authorization header is missing or invalid");
        }

        // 如果token有效，且用户未被认证
        if (userId != null) {
            // 检查用户状态
            Long uid = Long.parseLong(userId);
            com.example.blog.entity.User user = userRepository.findById(uid).orElse(null);
            
            if (user == null) {
                logger.warn("User not found: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }
            
            if (user.getStatus() != 1) {
                logger.warn("User is disabled: {}", userId);
                // 可以选择返回403，或者只是不设置认证（Security会处理）
                // response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is disabled");
                // return;
                
                // 这里我们不设置认证信息，后续的Security配置会拦截需要认证的请求
                filterChain.doFilter(request, response);
                return;
            }

            // 对于JWT认证，我们直接创建一个简单的认证对象
            try {
                // 如果role为空，默认设置为ADMIN角色
                if (role == null) {
                    role = "ROLE_ADMIN";
                    logger.debug("Role is null, setting default role: {}", role);
                }
                
                // 更新角色（以数据库为准，防止token中的角色过时）
                String dbRole = user.getRole();
                if (dbRole != null && !dbRole.isEmpty()) {
                    // 确保存储的角色格式正确（带ROLE_前缀或不带，视系统约定）
                    // 这里假设JWT中存储的是 user.getRole() 的值
                    role = dbRole;
                }
                
                // 创建认证对象，设置用户ID和角色
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("JWT authentication successful for user: {}, role: {}", userId, role);
            } catch (Exception e) {
                logger.error("Failed to authenticate user with JWT: {}", e.getMessage());
            }
        }

        // 继续处理请求
        filterChain.doFilter(request, response);
    }
}
