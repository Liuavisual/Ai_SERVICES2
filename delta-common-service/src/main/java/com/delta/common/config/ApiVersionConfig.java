package com.delta.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API接口版本管理配置
 * 支持多版本API共存，通过URL路径版本化（/api/v1/、/api/v2/）
 * 各版本Controller通过@RequestMapping("/api/v{n}")区分
 *
 * 当前活跃版本：
 * - v1: 当前生产版本（默认）
 * - v2: 下一版本规划中
 *
 * @author 刘建国
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ApiVersionConfig.class);

    /** 当前默认API版本 */
    public static final String DEFAULT_VERSION = "v1";

    /** 支持的API版本列表（逗号分隔） */
    public static final String SUPPORTED_VERSIONS = "v1";

    /** API版本废弃策略：版本进入DEPRECATED状态后，保留至少1个大版本周期 */
    public static final int DEPRECATION_GRACE_PERIOD_MONTHS = 6;

    public ApiVersionConfig() {
        log.info("【API版本】初始化API版本管理，当前版本: {}, 支持版本: {}", DEFAULT_VERSION, SUPPORTED_VERSIONS);
    }

    /**
     * 判断指定版本是否被支持
     *
     * @param version 版本号
     * @return true表示支持
     */
    public static boolean isSupported(String version) {
        return SUPPORTED_VERSIONS.contains(version);
    }

    /**
     * 获取API版本基础路径
     *
     * @param version 版本号
     * @return 路径前缀如 /api/v1
     */
    public static String getBasePath(String version) {
        return "/api/" + (version != null && isSupported(version) ? version : DEFAULT_VERSION);
    }
}