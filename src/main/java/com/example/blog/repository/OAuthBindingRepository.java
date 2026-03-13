package com.example.blog.repository;

import com.example.blog.entity.OAuthBinding;
import com.example.blog.entity.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthBindingRepository extends JpaRepository<OAuthBinding, Long> {
    
    /**
     * 根据提供者和OAuth ID查找绑定
     */
    Optional<OAuthBinding> findByProviderAndOauthId(OAuthProvider provider, String oauthId);
    
    /**
     * 根据用户ID查找所有绑定
     */
    List<OAuthBinding> findByUserId(Long userId);
    
    /**
     * 根据用户ID和提供者查找绑定
     */
    Optional<OAuthBinding> findByUserIdAndProvider(Long userId, OAuthProvider provider);
    
    /**
     * 删除用户的指定提供者绑定
     */
    void deleteByUserIdAndProvider(Long userId, OAuthProvider provider);
}
