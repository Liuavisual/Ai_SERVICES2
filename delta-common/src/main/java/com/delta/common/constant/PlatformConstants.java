package com.delta.common.constant;

/**
 * 平台相关常量
 * <p>
 * 定义系统支持的所有社交平台标识符，用于用户表和平台配置表中的 platform 字段。
 * 新增平台时必须在此处添加对应常量。
 * </p>
 *
 * @author 刘建国
 */
public final class PlatformConstants {

    public static final String WECHAT = "wechat";
    public static final String WEWORK = "wework";
    public static final String KOOK = "kook";
    public static final String YY = "yy";

    private PlatformConstants() {
    }
}
