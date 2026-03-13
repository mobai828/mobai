package com.example.blog.service;

import com.example.blog.entity.SiteConfig;
import com.example.blog.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 后台管理服务
 */
@Service
public class AdminService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteConfigRepository siteConfigRepository;

    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("articleCount", articleRepository.count());
        stats.put("commentCount", commentRepository.count());
        stats.put("userCount", userRepository.count());
        
        // 计算总浏览量
        Long totalViews = articleRepository.findAll().stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
        stats.put("totalViews", totalViews);
        
        return stats;
    }

    /**
     * 获取网站配置
     */
    public Map<String, String> getSiteConfig() {
        Map<String, String> config = new HashMap<>();
        siteConfigRepository.findAll().forEach(c -> 
            config.put(c.getConfigKey(), c.getConfigValue())
        );
        return config;
    }

    /**
     * 更新网站配置
     */
    public void updateSiteConfig(String key, String value) {
        Optional<SiteConfig> existing = siteConfigRepository.findByConfigKey(key);
        SiteConfig config;
        if (existing.isPresent()) {
            config = existing.get();
            config.setConfigValue(value);
        } else {
            config = new SiteConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
        }
        siteConfigRepository.save(config);
    }
}
