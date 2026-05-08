package com.delta.common.constant;

/**
 * 消息方向常量
 * <p>
 * 定义消息表中 direction 字段的取值，用于区分消息是来自客户还是系统发出。
 * </p>
 *
 * @author 刘建国
 */
public final class MessageConstants {

    /** 消息方向：接收（客户发送） */
    public static final String DIRECTION_IN = "in";

    /** 消息方向：发出（系统/客服发送） */
    public static final String DIRECTION_OUT = "out";

    /** AI 回复角色标识（用于对话历史构建） */
    public static final String ROLE_USER = "user";

    /** AI 回复角色标识（用于对话历史构建） */
    public static final String ROLE_ASSISTANT = "assistant";

    private MessageConstants() {
    }
}
