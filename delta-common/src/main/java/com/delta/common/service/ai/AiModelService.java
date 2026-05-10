package com.delta.common.service.ai;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI模型服务统一接口
 * 抽象AI模型调用，支持DeepSeek、OpenAI、百川等多种模型适配
 *
 * @author 刘建国
 */
public interface AiModelService {

    /**
     * 同步AI对话
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param context      上下文参数（温度、最大Token等）
     * @return AI响应文本
     */
    String chat(String systemPrompt, String userMessage, Map<String, Object> context);

    /**
     * 异步AI对话（流式输出场景）
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param context      上下文参数
     * @return AI响应Future
     */
    CompletableFuture<String> chatAsync(String systemPrompt, String userMessage, Map<String, Object> context);

    /**
     * 文本嵌入向量
     *
     * @param text 待嵌入文本
     * @return 嵌入向量
     */
    float[] embedding(String text);

    /**
     * 情绪分析
     *
     * @param message 待分析消息
     * @return 情绪标签（POSITIVE/NEGATIVE/NEUTRAL）
     */
    String analyzeEmotion(String message);

    /**
     * 返回模型名称
     *
     * @return 模型标识
     */
    String getModelName();

    /**
     * 模型是否可用（健康检查）
     *
     * @return true表示可用
     */
    boolean isAvailable();
}