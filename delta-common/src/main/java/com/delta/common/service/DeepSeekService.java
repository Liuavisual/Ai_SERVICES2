package com.delta.common.service;

import java.util.List;

/**
 * DeepSeek AI服务接口，封装AI对话API调用
 *
 * @author 刘建国
 */
public interface DeepSeekService {

    /**
     * 调用 DeepSeek API 获取 AI 回复
     * @param userMessage 用户消息
     * @return AI 回复内容
     */
    String getChatReply(String userMessage);

    /**
     * 调用 DeepSeek API 获取 AI 回复（带历史对话）
     * @param userMessage 用户消息
     * @param conversationHistory 历史对话列表，格式：[{"role":"user","content":"..."}, {"role":"assistant","content":"..."}]
     * @return AI 回复内容
     */
    String getChatReplyWithHistory(String userMessage, List<ChatMessage> conversationHistory);

    /**
     * 检查 DeepSeek 是否启用
     * @return 是否启用
     */
    boolean isEnabled();

    /**
     * 对话消息类
     */
    class ChatMessage {
        private String role; // "user" 或 "assistant"
        private String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
