package com.delta.admin.controller;

import com.delta.admin.config.TokenCookieService;
import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.LoginDTO;
import com.delta.common.dto.RefreshTokenDTO;
import com.delta.common.dto.RegisterDTO;
import com.delta.common.service.AuthService;
import com.delta.common.service.RedisService;
import com.delta.common.service.impl.TokenBlacklistService;
import com.delta.common.util.ClientIpUtils;
import com.delta.common.util.JwtUtils;
import com.delta.common.util.RateLimiter;
import com.delta.common.vo.LoginVO;
import com.delta.common.vo.Result;
import io.jsonwebtoken.Claims;
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

    private static final int LOGIN_MAX_REQUESTS = 10;

    private static final int LOGIN_WINDOW_SECONDS = 300;

    private final TokenCookieService tokenCookieService;

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
        // 登录接口限流保护，防止暴力破解
        String clientIp = ClientIpUtils.getClientIp(request);
        String loginRateLimitKey = "login:" + clientIp;
        if (!rateLimiter.isAllowed(loginRateLimitKey, LOGIN_MAX_REQUESTS, LOGIN_WINDOW_SECONDS)) {
            log.warn("登录请求过于频繁, IP={}", clientIp);
            return Result.error(429, "登录请求过于频繁，请5分钟后再试");
        }
        loginDTO.setClientIp(clientIp);
        LoginVO loginVO = authService.login(loginDTO);
        if (loginVO.getRequireTwoFactor() != null && loginVO.getRequireTwoFactor()) {
            return Result.success(loginVO);
        }
        // 将Token设置到httpOnly Cookie中，防止XSS攻击窃取
        tokenCookieService.setTokenCookie(response, loginVO.getToken(), loginVO.getRefreshToken(), loginVO.getExpiresIn());
        return Result.success(loginVO);
    }

    /**
     * 验证2FA验证码，签发正式JWT令牌
     * <p>
     * 在密码登录返回 requireTwoFactor=true 后调用，
     * 使用登录响应中的 twoFactorToken 和认证器生成的6位验证码完成2FA验证。
     * </p>
     *
     * @param body 包含 twoFactorToken 和 code 的请求体
     * @return 登录信息（含正式JWT令牌）
     */
    @PostMapping("/verify-2fa")
    @AuditLog(module = "认证", action = "2FA验证")
    public Result<LoginVO> verifyTwoFactor(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String twoFactorToken = body.get("twoFactorToken");
        String code = body.get("code");

        if (twoFactorToken == null || code == null) {
            return Result.error(400, "令牌和验证码不能为空");
        }

        LoginVO loginVO = authService.verifyTwoFactor(twoFactorToken, code);
        tokenCookieService.setTokenCookie(response, loginVO.getToken(), loginVO.getRefreshToken(), loginVO.getExpiresIn());
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
        String clientIp = ClientIpUtils.getClientIp(request);
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
        tokenCookieService.setTokenCookie(response, loginVO.getToken(), loginVO.getRefreshToken(), loginVO.getExpiresIn());
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
        tokenCookieService.clearTokenCookie(response);
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
}
