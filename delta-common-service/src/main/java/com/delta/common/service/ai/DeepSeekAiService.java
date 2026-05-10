package com.delta.common.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * DeepSeek AI模型适配器实现
 *
 * @author 刘建国
 */
@Service("deepseekAiService")
public class DeepSeekAiService implements AiModelService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekAiService.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String model;

    public DeepSeekAiService(
            RestTemplate restTemplate,
            @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}") String apiUrl,
            @Value("${deepseek.model:deepseek-chat}") String model) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Override
    @SuppressWarnings({"rawtypes", "null"})
    public String chat(String systemPrompt, String userMessage, Map<String, Object> context) {
        log.debug("【DeepSeek】同步对话请求, model: {}", model);
        try {
            Map<String, Object> requestBody = buildRequestBody(systemPrompt, userMessage, context);
            Map response = restTemplate.postForObject(apiUrl, requestBody, Map.class);
            return extractContent(response);
        } catch (Exception e) {
            log.error("【DeepSeek】对话请求失败", e);
            return "抱歉，AI服务暂时不可用，正在为您转接人工客服...";
        }
    }

    @Override
    public CompletableFuture<String> chatAsync(String systemPrompt, String userMessage, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> chat(systemPrompt, userMessage, context));
    }

    @Override
    public float[] embedding(String text) {
        log.debug("【DeepSeek】嵌入向量请求");
        return new float[0];
    }

    @Override
    public String analyzeEmotion(String message) {
        String prompt = "请分析以下用户消息的情绪，只回答 POSITIVE、NEGATIVE 或 NEUTRAL：\n" + message;
        String result = chat("你是一个情绪分析专家", prompt, Map.of("temperature", 0.0));
        if (result != null) {
            result = result.trim().toUpperCase();
            if (result.contains("POSITIVE")) return "POSITIVE";
            if (result.contains("NEGATIVE")) return "NEGATIVE";
        }
        return "NEUTRAL";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    @SuppressWarnings("null")
    public boolean isAvailable() {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", "ping")),
                    "max_tokens", 1
            );
            restTemplate.postForObject(apiUrl, requestBody, Map.class);
            return true;
        } catch (Exception e) {
            log.warn("【DeepSeek】健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建API请求体
     */
    private Map<String, Object> buildRequestBody(String systemPrompt, String userMessage, Map<String, Object> context) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> messages = new java.util.ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        body.put("messages", messages);

        if (context != null) {
            body.putAll(context);
        }
        body.putIfAbsent("temperature", 0.7);
        body.putIfAbsent("max_tokens", 2048);

        return body;
    }

    /**
     * 从响应中提取文本内容
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String extractContent(Map response) {
        if (response == null) return null;
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) return null;
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return null;
        return (String) message.get("content");
    }
}