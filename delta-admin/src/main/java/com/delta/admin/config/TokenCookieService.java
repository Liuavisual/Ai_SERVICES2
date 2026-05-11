package com.delta.admin.config;

import com.delta.common.constant.ApiVersionConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * Token Cookie 服务组件
 * <p>
 * 将JWT令牌的Cookie操作封装为独立组件，解决AuthController职责过重的问题。
 * 支持httpOnly + Secure + SameSite 安全特性。
 * </p>
 *
 * @author 刘建国
 */
@Component
public class TokenCookieService {

    private static final String COOKIE_PATH_API = "/api";

    private static final int DEFAULT_MAX_AGE = 7200;

    /**
     * 将Token设置到httpOnly Cookie中，防止XSS攻击窃取
     *
     * @param response     HTTP响应
     * @param token        访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn    过期时间（秒）
     */
    public void setTokenCookie(HttpServletResponse response, String token, String refreshToken, Long expiresIn) {
        int maxAge = expiresIn != null ? expiresIn.intValue() : DEFAULT_MAX_AGE;

        Cookie tokenCookie = new Cookie("access_token", token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath(COOKIE_PATH_API);
        tokenCookie.setMaxAge(maxAge);
        response.addCookie(tokenCookie);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath(COOKIE_PATH_API + ApiVersionConstants.V1 + "/auth/refresh");
        refreshCookie.setMaxAge(maxAge * 2);
        response.addCookie(refreshCookie);
    }

    /**
     * 清除httpOnly Cookie（登出时调用）
     *
     * @param response HTTP响应
     */
    public void clearTokenCookie(HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("access_token", "");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath(COOKIE_PATH_API);
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath(COOKIE_PATH_API + ApiVersionConstants.V1 + "/auth/refresh");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}