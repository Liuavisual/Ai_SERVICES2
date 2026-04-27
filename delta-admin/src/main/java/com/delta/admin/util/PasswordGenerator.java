package com.delta.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类，生成随机安全密码
 *
 * @author delta
 */
public class PasswordGenerator {
    /**
     * 主方法 - 用于演示BCrypt密码加密和验证功能
     *
     * 此方法展示如何使用Spring Security的BCryptPasswordEncoder对密码进行加密，
     * 并验证加密后的密码。BCrypt算法的特点是每次加密同一明文密码都会生成
     * 不同的密文，但验证时都能正确匹配。
     *
     * @param args 命令行参数（此程序不使用）
     */
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123";

        System.out.println("原始密码: " + rawPassword);
        System.out.println("----------------------------------------");
        System.out.println("BCrypt 加密结果（每次运行都不一样，但都能验证通过）:");
        System.out.println();

        // 连续生成5个加密结果，展示BCrypt加密的随机性特性
        for (int i = 0; i < 5; i++) {
            String encodedPassword = encoder.encode(rawPassword);
            System.out.println("结果 " + (i + 1) + ": " + encodedPassword);
        }

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("验证测试:");

        // 验证BCrypt加密后的密码能否正确匹配原始密码
        String testPassword = encoder.encode(rawPassword);
        System.out.println("测试加密: " + testPassword);
        System.out.println("验证结果: " + encoder.matches(rawPassword, testPassword));
    }
}
