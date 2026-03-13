package com.example.blog.repository;

import com.example.blog.entity.FriendLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendLinkRepository extends JpaRepository<FriendLink, Long> {
    
    /**
     * 按排序和状态查询友链
     */
    List<FriendLink> findAllByStatusOrderBySortOrderAsc(Integer status);
    
    /**
     * 查询所有启用的友链
     */
    default List<FriendLink> findAllEnabled() {
        return findAllByStatusOrderBySortOrderAsc(1);
    }
}
