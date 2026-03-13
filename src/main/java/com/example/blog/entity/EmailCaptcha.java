package com.example.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 邮箱验证码实体
 */
@Data
@Entity
@Table(name = "email_captcha")
public class EmailCaptcha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String captcha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaptchaType type;

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(columnDefinition = "TINYINT default 0")
    private Boolean used = false;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    /**
     * 检查验证码是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
