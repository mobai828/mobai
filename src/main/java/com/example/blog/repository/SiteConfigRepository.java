package com.example.blog.repository;

import com.example.blog.entity.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {
    
    /**
     * 根据配置键查找配置
     */
    Optional<SiteConfig> findByConfigKey(String configKey);
    
    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);
}
