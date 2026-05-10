package com.delta.common.constant;

/**
 * API版本常量
 * <p>
 * 定义API路径的版本前缀，用于Controller的@RequestMapping注解中，
 * 便于统一管理和升级API版本。
 * </p>
 *
 * @author 刘建国
 */
public final class ApiVersionConstants {

    /**
     * V1版本前缀
     */
    public static final String V1 = "/v1";

    /**
     * 私有构造函数，防止实例化
     */
    private ApiVersionConstants() {}
}
