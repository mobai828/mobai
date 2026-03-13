package com.example.blog.service;

import com.example.blog.entity.CaptchaType;
import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.PasswordEncoderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(PasswordEncoderUtil.encode("oldpassword"));
    }

    @Test
    void resetPassword_ShouldSucceed_WhenCaptchaIsValidAndUserExists() {
        String email = "test@example.com";
        String captcha = "123456";
        String newPassword = "newpassword";

        when(captchaService.verify(eq(email), eq(captcha), eq(CaptchaType.PASSWORD_RESET))).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authService.resetPassword(email, captcha, newPassword));

        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_ShouldThrowException_WhenCaptchaIsInvalid() {
        String email = "test@example.com";
        String captcha = "wrong";
        String newPassword = "newpassword";

        when(captchaService.verify(eq(email), eq(captcha), eq(CaptchaType.PASSWORD_RESET))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.resetPassword(email, captcha, newPassword));
    }

    @Test
    void resetPassword_ShouldThrowException_WhenUserDoesNotExist() {
        String email = "nonexistent@example.com";
        String captcha = "123456";
        String newPassword = "newpassword";

        when(captchaService.verify(eq(email), eq(captcha), eq(CaptchaType.PASSWORD_RESET))).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.resetPassword(email, captcha, newPassword));
    }
}
