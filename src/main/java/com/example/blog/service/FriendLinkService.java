package com.example.blog.service;

import com.example.blog.entity.FriendLink;
import com.example.blog.repository.FriendLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 友情链接服务
 */
@Service
public class FriendLinkService {

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    /**
     * 获取所有启用的友链
     */
    public List<FriendLink> getAllEnabledLinks() {
        return friendLinkRepository.findAllEnabled();
    }

    /**
     * 分页获取所有友链（管理员）
     */
    public Page<FriendLink> getAllLinks(int page, int size) {
        return friendLinkRepository.findAll(
            PageRequest.of(page, size, Sort.by("sortOrder").ascending())
        );
    }

    /**
     * 根据ID获取友链
     */
    public FriendLink getLinkById(Long id) {
        return friendLinkRepository.findById(id).orElse(null);
    }

    /**
     * 创建友链
     */
    public FriendLink createLink(FriendLink link) {
        return friendLinkRepository.save(link);
    }

    /**
     * 更新友链
     */
    public FriendLink updateLink(Long id, FriendLink link) {
        FriendLink existing = friendLinkRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(link.getName());
            existing.setUrl(link.getUrl());
            existing.setLogo(link.getLogo());
            existing.setDescription(link.getDescription());
            existing.setSortOrder(link.getSortOrder());
            existing.setStatus(link.getStatus());
            return friendLinkRepository.save(existing);
        }
        return null;
    }

    /**
     * 删除友链
     */
    public void deleteLink(Long id) {
        friendLinkRepository.deleteById(id);
    }
}
