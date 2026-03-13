package com.example.blog.dto;

import lombok.Data;
import javax.validation.constraints.Size;

/**
 * 用户资料更新请求DTO
 */
@Data
public class ProfileUpdateRequest {
    
    @Size(max = 50, message = "昵称最多50个字符")
    private String nickname;
    
    @Size(max = 500, message = "自我介绍最多500个字符")
    private String intro;
}
