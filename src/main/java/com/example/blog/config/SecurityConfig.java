package com.example.blog.config;

import com.example.blog.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 配置使用CustomUserDetailsService和BCrypt密码加密器
        auth.userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用默认强度10，与PasswordEncoderUtil保持一致
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 添加JWT过滤器，在用户名密码认证过滤器之前执行
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
                // 静态资源完全公开
                .antMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                // 公开页面
                .antMatchers("/", "/register", "/login", "/forgot-password", "/article/**", "/category/**", "/tag/**", "/search/**", "/archive", "/links", "/about").permitAll()
                // 管理后台页面允许访问（前端会检查登录状态和权限）
                .antMatchers("/admin", "/admin/**").permitAll()
                // 需要登录的页面（前端会检查）
                .antMatchers("/profile", "/write-article").permitAll()
                // 公开API
                .antMatchers("/api/auth/**", "/api/user/register", "/api/user/mail/send-captcha", "/api/user/login/**").permitAll()
                .antMatchers("/api/categories", "/api/tags", "/api/articles/categories", "/api/articles/tags").permitAll()
                .antMatchers("/api/articles", "/api/articles/latest", "/api/articles/search", "/api/articles/archive/**").permitAll()
                .antMatchers("/api/articles/{id}", "/api/articles/{id}/stats").permitAll()
                .antMatchers("/api/comments/article/**", "/api/comments/tree/**", "/api/comments/count/**").permitAll()
                // 管理员API暂时公开（生产环境应改回 hasRole("ADMIN")）
                .antMatchers("/api/admin/**").permitAll()
                // 其他API需要认证
                .anyRequest().authenticated()
                .and()
            // 禁用表单登录，使用JWT认证
            .formLogin()
                // 登录页面
                .loginPage("/login")
                // 登录处理路径
                .loginProcessingUrl("/login")
                // 用户名参数名
                .usernameParameter("credential")
                // 密码参数名
                .passwordParameter("password")
                // 登录成功处理器
                .successHandler((request, response, authentication) -> {
                    // 检查用户是否有管理员权限
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ADMIN") || auth.getAuthority().equals("ROLE_ADMIN"));
                    if (isAdmin) {
                        response.sendRedirect("/admin/articles");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                // 仅允许表单登录用于网页，不影响API
                .permitAll()
                .and()
            // 禁用HTTP Basic认证
            .httpBasic().disable()
            // 启用CSRF保护，但对所有 /api/** 请求禁用
            .csrf()
                .ignoringAntMatchers("/api/**")
                .and()
            // 配置会话管理，对于API请求，我们不需要会话
            .sessionManagement()
                // 每个用户最多一个会话
                .maximumSessions(1)
                // 禁用旧会话
                .expiredUrl("/login?expired=true");
    }
}