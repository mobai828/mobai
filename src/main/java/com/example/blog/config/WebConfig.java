package com.example.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 登录页面
        registry.addViewController("/login").setViewName("login");
        
        // 注册页面
        registry.addViewController("/register").setViewName("register");
        
        // 用户资料页面
        registry.addViewController("/profile").setViewName("profile");
        
        // 后台管理首页
        registry.addViewController("/admin").setViewName("admin/index");
        
        // 后台文章管理
        registry.addViewController("/admin/articles").setViewName("admin/articles");
        
        // 后台分类管理
        registry.addViewController("/admin/categories").setViewName("admin/categories");
        
        // 后台标签管理
        registry.addViewController("/admin/tags").setViewName("admin/tags");
        
        // 后台用户管理
        registry.addViewController("/admin/users").setViewName("admin/users");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置CORS支持
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081", "http://localhost:8083", "http://127.0.0.1:8083", "http://localhost:9090", "http://127.0.0.1:9090")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到项目运行目录下的 uploads 物理目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}