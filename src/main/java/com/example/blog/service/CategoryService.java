package com.example.blog.service;

import com.example.blog.entity.Category;
import com.example.blog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 创建分类
     */
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Long id, Category category) {
        category.setId(id);
        return categoryRepository.save(category);
    }

    /**
     * 根据ID获取分类
     */
    @Cacheable(value = "categories", key = "'category_'.concat(#id)")
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * 根据名称获取分类
     */
    @Cacheable(value = "categories", key = "'category_name_'.concat(#name)")
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }

    /**
     * 获取所有分类
     */
    @Cacheable(value = "categories", key = "'all_categories'")
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        // 为每个分类设置文章数量
        for (Category category : categories) {
            Long articleCount = categoryRepository.countArticlesByCategoryId(category.getId());
            category.setArticleCount(articleCount);
        }
        return categories;
    }

    /**
     * 分页获取分类
     */
    public Page<Category> getCategoriesByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return categoryRepository.findAll(pageable);
    }

    /**
     * 删除分类
     */
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    /**
     * 检查分类名称是否存在
     */
    @Cacheable(value = "categories", key = "'category_exists_'.concat(#name)")
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}