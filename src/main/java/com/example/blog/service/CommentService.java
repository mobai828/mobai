package com.example.blog.service;

import com.example.blog.entity.Comment;
import com.example.blog.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MailService mailService;
    
    // 是否启用评论审核
    @Value("${blog.comment.moderation:true}")
    private boolean moderationEnabled;
    
    // 是否允许匿名评论
    @Value("${blog.comment.anonymous:false}")
    private boolean anonymousEnabled;
    
    // 管理员邮箱
    @Value("${blog.admin.email:admin@example.com}")
    private String adminEmail;

    /**
     * 创建评论（登录用户）
     */
    public Comment createComment(Comment comment) {
        // 如果启用审核，新评论默认为待审核状态
        if (moderationEnabled) {
            comment.setStatus(0); // 待审核
        } else {
            comment.setStatus(1); // 已通过
        }
        comment.setIsAnonymous(false);
        
        // 自动填充用户昵称和邮箱
        if (comment.getUserId() != null) {
            com.example.blog.entity.User user = userService.getUserById(comment.getUserId());
            if (user != null) {
                comment.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                comment.setEmail(user.getEmail());
            }
        }
        
        Comment saved = commentRepository.save(comment);
        logger.info("评论创建成功: commentId={}, articleId={}", saved.getId(), saved.getArticleId());
        return saved;
    }
    
    /**
     * 创建匿名评论
     */
    public Comment createAnonymousComment(Long articleId, Long parentId, String content, 
            String nickname, String email, String ip) {
        if (!anonymousEnabled) {
            throw new RuntimeException("不允许匿名评论");
        }
        
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setNickname(nickname);
        comment.setEmail(email);
        comment.setIp(ip);
        comment.setIsAnonymous(true);
        
        // 匿名评论默认需要审核
        comment.setStatus(0);
        
        Comment saved = commentRepository.save(comment);
        logger.info("匿名评论创建成功: commentId={}, articleId={}", saved.getId(), articleId);
        return saved;
    }
    
    /**
     * 审核评论（通过/拒绝）
     */
    public Comment moderateComment(Long commentId, int status) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null) {
            comment.setStatus(status);
            commentRepository.save(comment);
            logger.info("评论审核完成: commentId={}, status={}", commentId, status);
            return comment;
        }
        return null;
    }

    /**
     * 更新评论
     */
    public Comment updateComment(Long id, Comment comment) {
        comment.setId(id);
        return commentRepository.save(comment);
    }

    /**
     * 根据ID获取评论
     */
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    /**
     * 分页获取文章评论列表（仅审核通过的）
     */
    public Page<Comment> getCommentsByArticle(Long articleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createTime"));
        return commentRepository.findByArticleIdAndStatusOrderByCreateTimeAsc(articleId, 1, pageable);
    }

    /**
     * 获取文章所有评论（仅审核通过的，用于构建评论树）
     */
    public List<Comment> getAllCommentsByArticle(Long articleId) {
        return commentRepository.findByArticleIdAndStatusOrderByCreateTimeAsc(articleId, 1);
    }

    /**
     * 根据父评论ID获取子评论列表
     */
    public List<Comment> getCommentsByParent(Long parentId) {
        return commentRepository.findByParentIdOrderByCreateTimeAsc(parentId);
    }

    /**
     * 分页获取所有评论（管理员审核用）
     */
    public Page<Comment> getAllCommentsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        // 管理员应该能看到所有状态的评论，不仅仅是审核通过的
        Page<Comment> comments = commentRepository.findAll(pageable);
        
        // 填充缺失的昵称信息
        for (Comment comment : comments) {
            if (comment.getNickname() == null && comment.getUserId() != null) {
                com.example.blog.entity.User user = userService.getUserById(comment.getUserId());
                if (user != null) {
                    comment.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    // 这里我们不保存回数据库，只是为了显示
                }
            }
        }
        
        return comments;
    }

    /**
     * 删除评论
     */
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    /**
     * 审核评论
     */
    public Comment approveComment(Long id) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setStatus(1); // 设置为审核通过
            return commentRepository.save(comment);
        }
        return null;
    }
    
    /**
     * 拒绝评论
     */
    public Comment rejectComment(Long id) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setStatus(2); // 设置为已拒绝
            return commentRepository.save(comment);
        }
        return null;
    }

    /**
     * 获取评论树结构
     */
    public List<Comment> getCommentTree(Long articleId) {
        // 获取所有评论（包括子评论）
        List<Comment> allComments = getAllCommentsByArticle(articleId);
        
        // 填充用户信息
        for (Comment comment : allComments) {
            populateUserInfo(comment);
        }
        
        // 构建评论树
        return buildCommentTree(allComments);
    }

    /**
     * 填充评论的用户信息（昵称、头像）
     */
    private void populateUserInfo(Comment comment) {
        if (comment.getUserId() != null) {
            com.example.blog.entity.User user = userService.getUserById(comment.getUserId());
            if (user != null) {
                // 如果评论没有昵称（可能是旧数据），使用用户昵称
                if (comment.getNickname() == null) {
                    comment.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
                // 设置头像
                comment.setAvatar(user.getAvatar());
            }
        }
    }

    /**
     * 递归构建评论树
     */
    private List<Comment> buildCommentTree(List<Comment> comments) {
        List<Comment> tree = new ArrayList<>();
        
        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                // 一级评论
                comment.setChildComments(getChildComments(comment.getId(), comments));
                tree.add(comment);
            }
        }
        
        return tree;
    }

    /**
     * 获取子评论
     */
    private List<Comment> getChildComments(Long parentId, List<Comment> allComments) {
        List<Comment> children = new ArrayList<>();
        
        for (Comment comment : allComments) {
            if (parentId.equals(comment.getParentId())) {
                comment.setChildComments(getChildComments(comment.getId(), allComments));
                children.add(comment);
            }
        }
        
        return children;
    }

    /**
     * 统计文章评论数
     */
    public Long countCommentsByArticle(Long articleId) {
        return commentRepository.countByArticleIdAndStatus(articleId);
    }
}