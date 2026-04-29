package com.delta.common.service.impl;

import com.delta.common.dto.LoginDTO;
import com.delta.common.dto.RegisterDTO;
import com.delta.common.entity.SysUser;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.service.RedisService;
import com.delta.common.util.JwtUtils;
import com.delta.common.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("认证服务单元测试")
public class AuthServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisService redisService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser activeUser;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        activeUser = new SysUser();
        activeUser.setId(1L);
        activeUser.setUsername("testuser");
        activeUser.setPassword("encoded_password");
        activeUser.setRealName("测试用户");
        activeUser.setRole("CS_STAFF");
        activeUser.setStatus("ACTIVE");
        activeUser.setDeleted(0);

        when(jwtUtils.generateToken(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtils.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");
        when(jwtUtils.getExpirationFromNow()).thenReturn(7200000L);
        when(redisService.hasKey(anyString())).thenReturn(false);
        when(redisService.increment(anyString())).thenReturn(1L);
        when(redisService.getExpire(anyString())).thenReturn(900L);
    }

    @Test
    @DisplayName("登录成功应返回双Token和用户信息")
    void testLoginSuccess() {
        when(sysUserMapper.selectOne(any())).thenReturn(activeUser);
        when(passwordEncoder.matches("Test1234", "encoded_password")).thenReturn(true);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");

        LoginVO result = authService.login(dto);

        assertNotNull(result);
        assertEquals("access_token", result.getToken());
        assertEquals("refresh_token", result.getRefreshToken());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("CS_STAFF", result.getRole());
    }

    @Test
    @DisplayName("用户名不存在应抛出异常")
    void testLoginUserNotFound() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("Test1234");

        assertThrows(Exception.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("密码错误应抛出异常")
    void testLoginWrongPassword() {
        when(sysUserMapper.selectOne(any())).thenReturn(activeUser);
        when(passwordEncoder.matches("Wrong123", "encoded_password")).thenReturn(false);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Wrong123");

        assertThrows(Exception.class, () -> authService.login(dto));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("连续5次登录失败后应锁定15分钟")
    void testLoginLockAfterMaxAttempts() {
        when(sysUserMapper.selectOne(any())).thenReturn(activeUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        when(redisService.increment(anyString())).thenAnswer(invocation -> {
            return 5L;
        });
        when(redisService.hasKey(startsWith("login:lock:"))).thenReturn(true);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Wrong1234");
        dto.setClientIp("127.0.0.1");

        Exception exception = assertThrows(Exception.class, () -> authService.login(dto));
        assertTrue(exception.getMessage().contains("分钟后再试"));
    }

    @Test
    @DisplayName("注册时密码不符合强度要求应抛出异常")
    void testRegisterWeakPassword() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123");
        dto.setRealName("新用户");

        assertThrows(Exception.class, () -> authService.register(dto));
    }

    @Test
    @DisplayName("注册时用户名已存在应抛出异常")
    void testRegisterDuplicateUsername() {
        when(sysUserMapper.selectOne(any())).thenReturn(activeUser);

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test1234");
        dto.setRealName("新用户");

        assertThrows(Exception.class, () -> authService.register(dto));
    }

    @Test
    @DisplayName("注册成功应插入新用户")
    void testRegisterSuccess() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_new_password");

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("Newuser123");
        dto.setRealName("新用户");

        assertDoesNotThrow(() -> authService.register(dto));
        verify(sysUserMapper).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("Token刷新成功应返回新的双Token")
    void testRefreshTokenSuccess() {
        when(jwtUtils.isRefreshToken("refresh_token")).thenReturn(true);
        when(jwtUtils.isTokenExpired("refresh_token")).thenReturn(false);
        when(jwtUtils.getUserIdFromToken("refresh_token")).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken("refresh_token")).thenReturn("testuser");
        when(sysUserMapper.selectById(1L)).thenReturn(activeUser);

        LoginVO result = authService.refreshToken("refresh_token");

        assertNotNull(result);
        assertEquals("access_token", result.getToken());
        assertEquals("refresh_token", result.getRefreshToken());
    }

    @Test
    @DisplayName("过期的refreshToken应抛出异常")
    void testRefreshTokenExpired() {
        when(jwtUtils.isRefreshToken("expired_token")).thenReturn(true);
        when(jwtUtils.isTokenExpired("expired_token")).thenReturn(true);

        assertThrows(Exception.class, () -> authService.refreshToken("expired_token"));
    }

    @Test
    @DisplayName("非refreshToken类型应抛出异常")
    void testRefreshTokenWrongType() {
        when(jwtUtils.isRefreshToken("access_token")).thenReturn(false);

        assertThrows(Exception.class, () -> authService.refreshToken("access_token"));
    }
}
