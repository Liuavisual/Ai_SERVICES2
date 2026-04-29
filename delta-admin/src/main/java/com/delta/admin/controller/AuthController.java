package com.delta.admin.controller;

import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.constant.ExportConstants;
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
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static final int REGISTER_MAX_REQUESTS = 5;

    private static final int REGISTER_WINDOW_SECONDS = 3600;

    private final AuthService authService;

    private final RateLimiter rateLimiter;

    private final JwtUtils jwtUtils;

    private final TokenBlacklistService tokenBlacklistService;

    private final RedisService redisService;

    private static final String HEARTBEAT_KEY_PREFIX = "session:heartbeat:";
    private static final long HEARTBEAT_TTL_MINUTES = 5;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @param request  HTTP请求
     * @return 登录结果
     */
    @PostMapping("/login")
    @AuditLog(module = "认证", action = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        // 设置客户端IP
        loginDTO.setClientIp(getClientIp(request));
        // 执行登录逻辑
        LoginVO loginVO = authService.login(loginDTO);
        // 将Token设置到httpOnly Cookie中，防止XSS攻击窃取
        setTokenCookie(response, loginVO.getToken(), loginVO.getRefreshToken(), loginVO.getExpiresIn());
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
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto, HttpServletResponse response) {
        LoginVO loginVO = authService.refreshToken(dto.getRefreshToken());
        // 刷新Token时同步更新httpOnly Cookie
        setTokenCookie(response, loginVO.getToken(), loginVO.getRefreshToken(), loginVO.getExpiresIn());
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
    @AuditLog(module = "认证", action = "用户登出")
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
        // 清除httpOnly Cookie
        clearTokenCookie(response);
        response.setStatus(HttpServletResponse.SC_OK);
        return Result.success(null);
    }

    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            String bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                try {
                    String jwt = bearerToken.substring(ExportConstants.BEARER_PREFIX_LENGTH);
                    if (!tokenBlacklistService.isBlacklisted(jwt)) {
                        Claims claims = jwtUtils.parseToken(jwt);
                        userIdAttr = claims.get("userId", Long.class);
                    }
                } catch (Exception e) {
                    log.debug("心跳请求Token解析失败", e);
                }
            }
            if (userIdAttr == null) {
                return Result.success();
            }
        }
        Long userId = (Long) userIdAttr;
        String key = HEARTBEAT_KEY_PREFIX + userId;
        redisService.set(key, String.valueOf(System.currentTimeMillis()), HEARTBEAT_TTL_MINUTES, TimeUnit.MINUTES);

        Boolean forceLogout = redisService.hasKey("session:force_logout:" + userId);
        if (Boolean.TRUE.equals(forceLogout)) {
            redisService.delete("session:force_logout:" + userId);
            return Result.error(401, "账户已在其他设备登录，请重新登录");
        }

        return Result.success();
    }

    @PostMapping(value = "/session-event", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Void> sessionEvent(@RequestBody Map<String, Object> event, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            String bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                try {
                    String jwt = bearerToken.substring(ExportConstants.BEARER_PREFIX_LENGTH);
                    if (!tokenBlacklistService.isBlacklisted(jwt)) {
                        Claims claims = jwtUtils.parseToken(jwt);
                        userIdAttr = claims.get("userId", Long.class);
                    }
                } catch (Exception e) {
                    log.debug("session-event Token解析失败", e);
                }
            }
        }

        if (userIdAttr == null) {
            return Result.error(401, "未认证");
        }

        String eventType = (String) event.get("eventType");
        Object userIdObj = event.get("userId");
        if (userIdObj == null) {
            return Result.success();
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException e) {
            return Result.success();
        }

        Long authenticatedUserId = (Long) userIdAttr;
        if (!userId.equals(authenticatedUserId)) {
            log.warn("session-event用户ID不匹配: authenticated={}, event={}", authenticatedUserId, userId);
            return Result.error(403, "无权操作");
        }

        if ("SESSION_END".equals(eventType)) {
            String key = HEARTBEAT_KEY_PREFIX + userId;
            redisService.delete(key);
            log.info("会话结束事件: userId={}", userId);
        }

        return Result.success();
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

    /**
     * 将Token设置到httpOnly Cookie中，防止XSS攻击窃取
     *
     * @param response    HTTP响应
     * @param token       访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn   过期时间（秒）
     */
    private void setTokenCookie(HttpServletResponse response, String token, String refreshToken, Long expiresIn) {
        int maxAge = expiresIn != null ? expiresIn.intValue() : 7200;

        // 设置访问令牌Cookie
        Cookie tokenCookie = new Cookie("access_token", token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/api");
        tokenCookie.setMaxAge(maxAge);
        response.addCookie(tokenCookie);

        // 设置刷新令牌Cookie，路径限制为refresh接口，过期时间为访问令牌的两倍
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api" + ApiVersionConstants.V1 + "/auth/refresh");
        refreshCookie.setMaxAge(maxAge * 2);
        response.addCookie(refreshCookie);
    }

    /**
     * 清除httpOnly Cookie（登出时调用）
     *
     * @param response HTTP响应
     */
    private void clearTokenCookie(HttpServletResponse response) {
        // 清除访问令牌Cookie
        Cookie tokenCookie = new Cookie("access_token", "");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/api");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        // 清除刷新令牌Cookie
        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api" + ApiVersionConstants.V1 + "/auth/refresh");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}
