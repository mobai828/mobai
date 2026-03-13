package com.example.blog.controller;

import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.service.CategoryService;
import com.example.blog.service.TagService;
import com.example.blog.util.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通用 API 控制器
 * 提供 /api/categories 和 /api/tags 等独立路由
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(MapUtil.of("success", true, "data", categories));
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(MapUtil.of("success", true, "data", category));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(MapUtil.of("success", true, "data", tags));
    }

    /**
     * 根据ID获取标签
     */
    @GetMapping("/tags/{id}")
    public ResponseEntity<?> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        if (tag != null) {
            return ResponseEntity.ok(MapUtil.of("success", true, "data", tag));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
