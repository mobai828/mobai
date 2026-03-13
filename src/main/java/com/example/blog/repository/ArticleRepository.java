package com.example.blog.repository;

import com.example.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByStatus(Integer status, Pageable pageable);
    Page<Article> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    Page<Article> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    
    // 新增方法：根据分类ID获取所有状态的文章
    Page<Article> findByCategoryId(Long categoryId, Pageable pageable);
    
    // 新增方法：根据用户ID获取所有状态的文章
    Page<Article> findByUserId(Long userId, Pageable pageable);
    
    // 查找不等于指定状态的文章
    Page<Article> findByStatusNot(Integer status, Pageable pageable);
    
    /**
     * 搜索文章，支持状态筛选和多字段搜索
     * @param keyword 搜索关键词
     * @param status 文章状态（可选）
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    @Query("SELECT a FROM Article a WHERE ((:status IS NULL AND a.status <> 2) OR (:status IS NOT NULL AND a.status = :status)) AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
    
    List<Article> findTop10ByStatusOrderByViewCountDesc(Integer status);
    List<Article> findTop10ByStatusOrderByLikeCountDesc(Integer status);
    
    // 根据ID列表和状态查询文章
    Page<Article> findAllByIdInAndStatus(List<Long> ids, Integer status, Pageable pageable);
    
    // 增加浏览量
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    // 查找定时发布时间已到的草稿文章
    List<Article> findByStatusAndScheduledTimeLessThanEqual(Integer status, LocalDateTime time);
    
    // 统计指定状态的文章数量
    long countByStatus(Integer status);
}