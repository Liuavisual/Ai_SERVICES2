package com.delta.admin.config;

import com.delta.common.config.JwtConfig;
import com.delta.common.service.impl.TokenBlacklistService;
import com.delta.common.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.delta.common.constant.ExportConstants;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器，拦截请求验证Token有效性并设置安全上下文
 *
 * <p>安全机制：</p>
 * <ul>
 *   <li>拒绝refreshToken作为accessToken使用，防止权限提升</li>
 *   <li>单次解析JWT Claims，避免重复解析开销</li>
 *   <li>role为null时拒绝认证，防止创建ROLE_null权限</li>
 * </ul>
 *
 * @author delta
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!jwtConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (tokenBlacklistService.isBlacklisted(jwt)) {
                    log.warn("检测到已登出的Token，已拒绝: ip={}", request.getRemoteAddr());
                    filterChain.doFilter(request, response);
                    return;
                }

                Claims claims = jwtUtils.parseToken(jwt);
                String tokenType = claims.get("type", String.class);

                if ("refresh".equals(tokenType)) {
                    log.warn("检测到使用refreshToken访问API，已拒绝: ip={}", request.getRemoteAddr());
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                if (role == null) {
                    log.warn("JWT中role为null，拒绝认证: userId={}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (username != null && !jwtUtils.isTokenExpired(jwt)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    request.setAttribute("role", role);

                    log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, role);
                }
            }
        } catch (Exception ex) {
            log.error("JWT认证失败", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // 优先从Cookie获取Token（httpOnly Cookie方案）
        String tokenFromCookie = getTokenFromCookie(request);
        if (StringUtils.hasText(tokenFromCookie)) {
            return tokenFromCookie;
        }
        // 其次从Authorization Header获取Token（兼容旧模式）
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(ExportConstants.BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    /**
     * 从httpOnly Cookie中获取访问令牌
     *
     * @param request HTTP请求
     * @return Cookie中的Token值，不存在则返回null
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
}
