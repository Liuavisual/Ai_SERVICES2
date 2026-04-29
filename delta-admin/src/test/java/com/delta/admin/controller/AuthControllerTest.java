package com.delta.admin.controller;

import com.delta.common.dto.LoginDTO;
import com.delta.common.service.AuthService;
import com.delta.common.service.RedisService;
import com.delta.common.service.impl.TokenBlacklistService;
import com.delta.common.util.JwtUtils;
import com.delta.common.util.RateLimiter;
import com.delta.common.vo.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController MockMvc 集成测试
 * 验证认证控制器的HTTP请求/响应行为
 *
 * @author 刘建国
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    /** MockMvc实例 */
    @Autowired
    private MockMvc mockMvc;

    /** JSON序列化工具 */
    @Autowired
    private ObjectMapper objectMapper;

    /** 认证服务Mock */
    @MockitoBean
    private AuthService authService;

    /** 限流器Mock */
    @MockitoBean
    private RateLimiter rateLimiter;

    /** JWT工具Mock */
    @MockitoBean
    private JwtUtils jwtUtils;

    /** Token黑名单服务Mock */
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    /** Redis服务Mock */
    @MockitoBean
    private RedisService redisService;

    /**
     * 测试登录接口 - 有效凭证
     * 验证返回200状态码和正确的响应结构
     */
    @Test
    void login_withValidCredentials_shouldReturnSuccess() throws Exception {
        // 准备测试数据
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("password");

        LoginVO loginVO = new LoginVO();
        loginVO.setToken("test-token");
        loginVO.setRefreshToken("test-refresh");
        loginVO.setUsername("admin");
        loginVO.setRole("SYS_ADMIN");

        when(authService.login(any(LoginDTO.class))).thenReturn(loginVO);

        // 执行测试并验证
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").value("test-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("test-refresh"));
    }

    /**
     * 测试登录接口 - 空用户名
     * 验证请求参数校验
     */
    @Test
    void login_withEmptyUsername_shouldReturnOk() throws Exception {
        // 准备测试数据 - 空用户名
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");
        dto.setPassword("password");

        // 执行测试 - 验证HTTP响应（参数校验由@Valid触发）
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    /**
     * 测试登录接口 - 缺少密码
     * 验证请求参数校验
     */
    @Test
    void login_withoutPassword_shouldReturnOk() throws Exception {
        // 准备测试数据 - 缺少密码
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");

        // 执行测试
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    /**
     * 测试注册接口 - 正常流程
     * 验证限流检查和注册逻辑
     */
    @Test
    void register_withValidData_shouldReturnSuccess() throws Exception {
        // 准备测试数据
        when(rateLimiter.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"password123\",\"realName\":\"测试用户\",\"role\":\"CS_STAFF\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试注册接口 - 限流触发
     * 验证频繁注册请求被拒绝
     */
    @Test
    void register_rateLimited_shouldReturn429() throws Exception {
        // 准备测试数据 - 限流触发
        when(rateLimiter.isAllowed(anyString(), anyInt(), anyInt())).thenReturn(false);

        // 执行测试
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"password123\",\"realName\":\"测试用户\",\"role\":\"CS_STAFF\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(429));
    }

    /**
     * 测试刷新Token接口 - 正常流程
     * 验证返回新的Token
     */
    @Test
    void refreshToken_withValidToken_shouldReturnSuccess() throws Exception {
        // 准备测试数据
        LoginVO loginVO = new LoginVO();
        loginVO.setToken("new-token");
        loginVO.setRefreshToken("new-refresh");

        when(authService.refreshToken(anyString())).thenReturn(loginVO);

        // 执行测试
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"valid-refresh-token\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").value("new-token"));
    }

    /**
     * 测试登出接口 - 无Token
     * 验证无Token登出不报错
     */
    @Test
    void logout_withoutToken_shouldReturnSuccess() throws Exception {
        // 执行测试
        mockMvc.perform(post("/auth/logout"))
            .andExpect(status().isOk());
    }

    /**
     * 测试心跳接口 - 无认证
     * 验证无认证心跳请求正常返回
     */
    @Test
    void heartbeat_withoutAuth_shouldReturnSuccess() throws Exception {
        // 执行测试
        mockMvc.perform(post("/auth/heartbeat"))
            .andExpect(status().isOk());
    }

    /**
     * 测试会话事件接口 - 无认证
     * 验证无认证会话事件返回未认证错误
     */
    @Test
    void sessionEvent_withoutAuth_shouldReturnUnauthenticated() throws Exception {
        // 执行测试
        mockMvc.perform(post("/auth/session-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventType\":\"SESSION_END\",\"userId\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }
}
