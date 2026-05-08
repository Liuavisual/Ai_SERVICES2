package com.delta.common.util;

import com.delta.common.config.JwtConfig;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * JwtUtils 单元测试
 * <p>
 * 测试JWT工具类的核心功能，包括：
 * - accessToken 生成与解析
 * - refreshToken 生成与解析
 * - Token类型区分
 * - Token过期检测
 * - Token验证
 * </p>
 *
 * @author 刘建国
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JWT工具类单元测试")
public class JwtUtilsTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtUtils jwtUtils;

    private final String testSecret = "test-secret-key-for-jwt-utils-testing-2024-abcdefghijklmnopqrstuvwxyz123456";
    private final Long testExpiration = 3600000L;
    private final Long testRefreshExpiration = 604800000L;
    private final String testIssuer = "test-issuer";
    private final String testAudience = "test-audience";

    @BeforeEach
    public void setUp() {
        when(jwtConfig.getSecret()).thenReturn(testSecret);
        when(jwtConfig.getExpiration()).thenReturn(testExpiration);
        when(jwtConfig.getRefreshExpiration()).thenReturn(testRefreshExpiration);
        when(jwtConfig.getIssuer()).thenReturn(testIssuer);
        when(jwtConfig.getAudience()).thenReturn(testAudience);
    }

    @Test
    @DisplayName("生成accessToken应包含userId、username、role和type=access")
    public void testGenerateAccessToken() {
        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN");

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtUtils.parseToken(token);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals("access", claims.get("type", String.class));
    }

    @Test
    @DisplayName("生成refreshToken应包含userId、username和type=refresh，不含role")
    public void testGenerateRefreshToken() {
        String token = jwtUtils.generateRefreshToken(1L, "testuser");

        assertNotNull(token);

        Claims claims = jwtUtils.parseToken(token);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
        assertNull(claims.get("role"));
    }

    @Test
    @DisplayName("isRefreshToken应正确区分accessToken和refreshToken")
    public void testIsRefreshToken() {
        String accessToken = jwtUtils.generateToken(1L, "testuser", "ADMIN");
        String refreshToken = jwtUtils.generateRefreshToken(1L, "testuser");

        assertFalse(jwtUtils.isRefreshToken(accessToken));
        assertTrue(jwtUtils.isRefreshToken(refreshToken));
    }

    @Test
    @DisplayName("getTypeFromToken应正确返回Token类型")
    public void testGetTypeFromToken() {
        String accessToken = jwtUtils.generateToken(1L, "testuser", "ADMIN");
        String refreshToken = jwtUtils.generateRefreshToken(1L, "testuser");

        assertEquals("access", jwtUtils.getTypeFromToken(accessToken));
        assertEquals("refresh", jwtUtils.getTypeFromToken(refreshToken));
    }

    @Test
    @DisplayName("accessToken和refreshToken应使用不同的过期时间")
    public void testDifferentExpirationTimes() {
        String accessToken = jwtUtils.generateToken(1L, "testuser", "ADMIN");
        String refreshToken = jwtUtils.generateRefreshToken(1L, "testuser");

        Date accessExpiry = jwtUtils.getExpirationDateFromToken(accessToken);
        Date refreshExpiry = jwtUtils.getExpirationDateFromToken(refreshToken);

        assertTrue(refreshExpiry.after(accessExpiry),
                "refreshToken过期时间应晚于accessToken");
    }

    @Test
    @DisplayName("validateToken应正确验证Token有效性")
    public void testValidateToken() {
        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN");
        assertTrue(jwtUtils.validateToken(token, "testuser"));
        assertFalse(jwtUtils.validateToken(token, "wronguser"));
        assertFalse(jwtUtils.validateToken("invalid-token", "testuser"));
    }

    @Test
    @DisplayName("未过期的Token应返回isTokenExpired=false")
    public void testIsTokenExpired() {
        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN");
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    @DisplayName("无效Token应返回isTokenExpired=true")
    public void testIsTokenExpiredWithInvalidToken() {
        assertTrue(jwtUtils.isTokenExpired("invalid-token"));
    }

    @Test
    @DisplayName("getUserIdFromToken应正确提取userId")
    public void testGetUserIdFromToken() {
        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN");
        assertEquals(1L, jwtUtils.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("getRoleFromToken应正确提取role")
    public void testGetRoleFromToken() {
        String token = jwtUtils.generateToken(1L, "testuser", "CS_STAFF");
        assertEquals("CS_STAFF", jwtUtils.getRoleFromToken(token));
    }
}
