package com.example.blog.controller;

import com.example.blog.entity.Comment;
import com.example.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 创建评论
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(@Valid @RequestBody Comment comment) {
        Comment savedComment = commentService.createComment(comment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "评论创建成功");
        response.put("data", savedComment);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新评论
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateComment(@PathVariable Long id, @Valid @RequestBody Comment comment) {
        Comment updatedComment = commentService.updateComment(id, comment);
        
        Map<String, Object> response = new HashMap<>();
        if (updatedComment != null) {
            response.put("success", true);
            response.put("message", "评论更新成功");
            response.put("data", updatedComment);
        } else {
            response.put("success", false);
            response.put("message", "评论不存在");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取评论
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (comment != null) {
            response.put("success", true);
            response.put("data", comment);
        } else {
            response.put("success", false);
            response.put("message", "评论不存在");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 分页获取文章评论列表（仅审核通过的）
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<Map<String, Object>> getCommentsByArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Comment> comments = commentService.getCommentsByArticle(articleId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comments.getContent());
        response.put("currentPage", comments.getNumber());
        response.put("totalPages", comments.getTotalPages());
        response.put("totalElements", comments.getTotalElements());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取文章评论树（仅审核通过的）
     */
    @GetMapping("/tree/article/{articleId}")
    public ResponseEntity<Map<String, Object>> getCommentTreeByArticle(@PathVariable Long articleId) {
        List<Comment> commentTree = commentService.getCommentTree(articleId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", commentTree);
        return ResponseEntity.ok(response);
    }

    /**
     * 分页获取所有评论（管理员审核用）
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllCommentsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Comment> comments = commentService.getAllCommentsForAdmin(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comments.getContent());
        response.put("currentPage", comments.getNumber());
        response.put("totalPages", comments.getTotalPages());
        response.put("totalElements", comments.getTotalElements());
        return ResponseEntity.ok(response);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "评论删除成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 审核评论（管理员）
     */
    @PutMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approveComment(@PathVariable Long id) {
        Comment approvedComment = commentService.approveComment(id);
        
        Map<String, Object> response = new HashMap<>();
        if (approvedComment != null) {
            response.put("success", true);
            response.put("message", "评论审核通过");
            response.put("data", approvedComment);
        } else {
            response.put("success", false);
            response.put("message", "评论不存在");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 统计文章评论数
     */
    @GetMapping("/count/article/{articleId}")
    public ResponseEntity<Map<String, Object>> countCommentsByArticle(@PathVariable Long articleId) {
        Long count = commentService.countCommentsByArticle(articleId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", count);
        return ResponseEntity.ok(response);
    }
}