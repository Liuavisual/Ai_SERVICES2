package com.delta.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类，提供BCrypt密码加密和验证功能
 *
 * @author delta
 */
public class PasswordGenerator {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 对原始密码进行BCrypt加密
     *
     * @param rawPassword 原始明文密码
     * @return BCrypt加密后的密文
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证原始密码是否与BCrypt密文匹配
     *
     * @param rawPassword     原始明文密码
     * @param encodedPassword BCrypt密文
     * @return 匹配返回true，否则false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
