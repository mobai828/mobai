package com.example.blog.config;

import com.example.blog.entity.Category;
import com.example.blog.entity.User;
import com.example.blog.service.CategoryService;
import com.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;

    @Override
    public void run(String... args) throws Exception {
        // 创建管理员用户
        User admin = userService.getUserByUsername("test");
        if (admin == null) {
            User adminUser = new User();
            adminUser.setUsername("test");
            adminUser.setEmail("test@example.com");
            adminUser.setPassword("password123");
            adminUser.setNickname("管理员");
            adminUser.setIntro("系统管理员");
            // 使用专门的注册管理员方法
            userService.registerAdmin(adminUser);
            System.out.println("管理员用户创建成功：用户名=test，密码=password123");
        } else {
            // 检查并修复管理员权限
            if (!"ADMIN".equals(admin.getRole()) && !"ROLE_ADMIN".equals(admin.getRole())) {
                userService.updateUserRole(admin.getId(), "ADMIN");
                System.out.println("修复管理员用户权限：test -> ADMIN");
            }
        }
        
        // 初始化默认分类
        initCategories();
    }
    
    private void initCategories() {
        String[] defaultCategories = {"Java", "Spring Boot", "前端开发", "随笔杂谈", "系统架构"};
        
        for (String categoryName : defaultCategories) {
            if (!categoryService.existsByName(categoryName)) {
                Category category = new Category();
                category.setName(categoryName);
                categoryService.createCategory(category);
                System.out.println("创建默认分类: " + categoryName);
            }
        }
    }
}