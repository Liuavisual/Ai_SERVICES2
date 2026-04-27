package com.delta.admin.controller;

import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.LoginDTO;
import com.delta.common.dto.RefreshTokenDTO;
import com.delta.common.dto.RegisterDTO;
import com.delta.common.service.AuthService;
import com.delta.common.service.impl.TokenBlacklistService;
import com.delta.common.util.JwtUtils;
import com.delta.common.util.RateLimiter;
import com.delta.common.vo.LoginVO;
import com.delta.common.vo.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * 注册接口最大请求次数
     */
    private static final int REGISTER_MAX_REQUESTS = 5;

    /**
     * 注册接口限流时间窗口（秒）
     */
    private static final int REGISTER_WINDOW_SECONDS = 3600;

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @param request  HTTP请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        // 设置客户端IP
        loginDTO.setClientIp(getClientIp(request));
        // 执行登录逻辑
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @param request     HTTP请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        // 获取客户端IP并构建限流Key
        String clientIp = getClientIp(request);
        String rateLimitKey = "register:" + clientIp;
        // 检查是否超过限流阈值
        if (!rateLimiter.isAllowed(rateLimitKey, REGISTER_MAX_REQUESTS, REGISTER_WINDOW_SECONDS)) {
            return Result.error(429, "注册请求过于频繁，请1小时后再试");
        }
        // 执行注册逻辑
        authService.register(registerDTO);
        return Result.success(null);
    }

    /**
     * 刷新访问令牌
     *
     * @param body 包含refreshToken的请求体
     * @return 新的登录信息
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        LoginVO loginVO = authService.refreshToken(dto.getRefreshToken());
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(ExportConstants.BEARER_PREFIX_LENGTH);
            try {
                Claims claims = jwtUtils.parseToken(jwt);
                Date expiration = claims.getExpiration();
                long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    tokenBlacklistService.blacklistToken(jwt, remainingMillis);
                }
            } catch (Exception e) {
                log.warn("登出时解析Token失败，忽略: {}", e.getMessage());
            }
        }
        String refreshToken = request.getParameter("refreshToken");
        if (StringUtils.hasText(refreshToken)) {
            try {
                Claims claims = jwtUtils.parseToken(refreshToken);
                Date expiration = claims.getExpiration();
                long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    tokenBlacklistService.blacklistToken(refreshToken, remainingMillis);
                }
            } catch (Exception e) {
                log.warn("登出时解析refreshToken失败，忽略: {}", e.getMessage());
            }
        }
        request.getSession().invalidate();
        response.setStatus(HttpServletResponse.SC_OK);
        return Result.success(null);
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        // 尝试从X-Forwarded-For头获取IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 尝试从X-Real-IP头获取IP
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 使用远程地址作为备选
            ip = request.getRemoteAddr();
        }
        // 如果存在多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
