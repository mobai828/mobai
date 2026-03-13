package com.example.blog.service;

import com.example.blog.entity.Tag;
import com.example.blog.repository.TagRepository;
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
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private com.example.blog.repository.ArticleTagRepository articleTagRepository;

    /**
     * 根据文章ID获取标签列表
     */
    public List<Tag> getTagsByArticleId(Long articleId) {
        List<com.example.blog.entity.ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
        List<Long> tagIds = articleTags.stream()
                .map(com.example.blog.entity.ArticleTag::getTagId)
                .collect(java.util.stream.Collectors.toList());
        return tagRepository.findAllById(tagIds);
    }

    /**
     * 创建标签
     */
    @CacheEvict(value = "tags", allEntries = true)
    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    /**
     * 批量创建标签
     */
    @CacheEvict(value = "tags", allEntries = true)
    public List<Tag> createTags(List<Tag> tags) {
        return tagRepository.saveAll(tags);
    }

    /**
     * 更新标签
     */
    @CacheEvict(value = "tags", allEntries = true)
    public Tag updateTag(Long id, Tag tag) {
        tag.setId(id);
        return tagRepository.save(tag);
    }

    /**
     * 根据ID获取标签
     */
    @Cacheable(value = "tags", key = "'tag_'.concat(#id)")
    public Tag getTagById(Long id) {
        return tagRepository.findById(id).orElse(null);
    }

    /**
     * 根据名称获取标签
     */
    @Cacheable(value = "tags", key = "'tag_name_'.concat(#name)")
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name).orElse(null);
    }

    /**
     * 获取所有标签
     */
    @Cacheable(value = "tags", key = "'all_tags'")
    public List<Tag> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        // 为每个标签设置文章数量
        for (Tag tag : tags) {
            Long articleCount = tagRepository.countArticlesByTagId(tag.getId());
            tag.setArticleCount(articleCount);
        }
        
        // 按文章数量倒序排序，如果数量相同按名称排序
        tags.sort((t1, t2) -> {
            int countCompare = t2.getArticleCount().compareTo(t1.getArticleCount());
            if (countCompare != 0) {
                return countCompare;
            }
            return t1.getName().compareTo(t2.getName());
        });
        
        return tags;
    }

    /**
     * 分页获取标签
     */
    public Page<Tag> getTagsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return tagRepository.findAll(pageable);
    }

    /**
     * 删除标签
     */
    @CacheEvict(value = "tags", allEntries = true)
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    /**
     * 检查标签名称是否存在
     */
    @Cacheable(value = "tags", key = "'tag_exists_'.concat(#name)")
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
}