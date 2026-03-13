package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章归档服务
 */
@Service
public class ArchiveService {

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 获取文章归档（按年月分组）
     */
    public Map<Integer, Map<Integer, List<Article>>> getArchive() {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createTime"));
        List<Article> articles = articleRepository.findByStatus(
                Article.STATUS_PUBLISHED, pageable).getContent();
        
        // 按年月分组
        return articles.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreateTime().getYear(),
                        TreeMap::new,
                        Collectors.groupingBy(
                                a -> a.getCreateTime().getMonthValue(),
                                TreeMap::new,
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 获取指定年月的文章
     */
    public List<Article> getArticlesByYearMonth(int year, int month) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createTime"));
        return articleRepository.findByStatus(
                Article.STATUS_PUBLISHED, pageable).getContent().stream()
                .filter(a -> a.getCreateTime().getYear() == year 
                        && a.getCreateTime().getMonthValue() == month)
                .collect(Collectors.toList());
    }
}
