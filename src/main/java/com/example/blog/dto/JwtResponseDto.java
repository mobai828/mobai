package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String role;
}