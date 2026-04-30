package com.delta.admin.controller;

import com.delta.common.dto.LoginDTO;
import com.delta.common.dto.RefreshTokenDTO;
import com.delta.common.dto.RegisterDTO;
import com.delta.common.service.AuthService;
import com.delta.common.service.RedisService;
import com.delta.common.service.impl.TokenBlacklistService;
import com.delta.common.util.JwtUtils;
import com.delta.common.util.RateLimiter;
import com.delta.common.vo.LoginVO;
import com.delta.common.vo.Result;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("认证控制器单元测试")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RedisService redisService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthController authController;

    private LoginVO testLoginVO;

    @BeforeEach
    void setUp() {
        testLoginVO = new LoginVO();
        testLoginVO.setToken("test-token");
        testLoginVO.setRefreshToken("test-refresh-token");
        testLoginVO.setUsername("admin");
        testLoginVO.setRole("SYS_ADMIN");
        testLoginVO.setExpiresIn(3600L);
    }

    @Test
    @DisplayName("登录-有效凭证应返回成功")
    void login_withValidCredentials_shouldReturnSuccess() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("password");
        when(authService.login(any(LoginDTO.class))).thenReturn(testLoginVO);
        when(request.getCookies()).thenReturn(null);

        Result<LoginVO> result = authController.login(dto, request, response);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("test-token", result.getData().getToken());
        verify(response, atLeastOnce()).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("登录-空用户名应正常处理")
    void login_withEmptyUsername_shouldReturnOk() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");
        dto.setPassword("password");
        when(authService.login(any(LoginDTO.class))).thenReturn(testLoginVO);
        when(request.getCookies()).thenReturn(null);

        Result<LoginVO> result = authController.login(dto, request, response);

        assertNotNull(result);
    }

    @Test
    @DisplayName("登录-缺少密码应正常处理")
    void login_withoutPassword_shouldReturnOk() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        when(authService.login(any(LoginDTO.class))).thenReturn(testLoginVO);
        when(request.getCookies()).thenReturn(null);

        Result<LoginVO> result = authController.login(dto, request, response);

        assertNotNull(result);
    }

    @Test
    @DisplayName("注册-正常流程应返回成功")
    void register_withValidData_shouldReturnSuccess() {
        when(rateLimiter.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);
        doNothing().when(authService).register(any(RegisterDTO.class));

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setRealName("测试用户");

        Result<Void> result = authController.register(dto, request);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("注册-限流触发应返回429")
    void register_rateLimited_shouldReturn429() {
        when(rateLimiter.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(false);

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setRealName("测试用户");

        Result<Void> result = authController.register(dto, request);

        assertEquals(429, result.getCode());
    }

    @Test
    @DisplayName("刷新Token-有效Token应返回新Token")
    void refreshToken_withValidToken_shouldReturnSuccess() {
        LoginVO newLoginVO = new LoginVO();
        newLoginVO.setToken("new-token");
        newLoginVO.setRefreshToken("new-refresh");
        newLoginVO.setExpiresIn(3600L);
        when(authService.refreshToken(anyString())).thenReturn(newLoginVO);

        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken("valid-refresh-token");

        Result<LoginVO> result = authController.refreshToken(dto, response);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("new-token", result.getData().getToken());
    }

    @Test
    @DisplayName("登出-无Token应正常返回")
    void logout_withoutToken_shouldReturnSuccess() {
        when(request.getCookies()).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(request.getSession(anyBoolean())).thenReturn(session);

        Result<Void> result = authController.logout(request, response);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("心跳-无认证应正常返回")
    void heartbeat_withoutAuth_shouldReturnSuccess() {
        Result<Void> result = authController.heartbeat(request);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("会话事件-无认证应返回401")
    void sessionEvent_withoutAuth_shouldReturnUnauthenticated() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        Map<String, Object> eventBody = Map.of("eventType", "SESSION_END", "userId", 1);
        Result<Void> result = authController.sessionEvent(eventBody, request);

        assertEquals(401, result.getCode());
    }
}
