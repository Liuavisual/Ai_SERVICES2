package com.delta.common.service.impl;

import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.service.EmotionIntelligenceService;
import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 情绪智能服务实现
 * <p>
 * 通过Redis存储和追踪每个活跃会话的客户情绪状态。
 * 采用情绪渐变模型，综合关键词匹配、上下文分析和历史趋势判断。
 * </p>
 * <p>
 * Redis数据结构：
 * <ul>
 *   <li>情绪状态: delta:emotion:state:{userId} (JSON String, TTL=30分钟)</li>
 *   <li>情绪历史: delta:emotion:history:{userId} (List, TTL=7天, 最多100条)</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class EmotionIntelligenceServiceImpl implements EmotionIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(EmotionIntelligenceServiceImpl.class);

    /** 情绪状态Redis Key前缀 */
    private static final String EMOTION_STATE_KEY_PREFIX = "delta:emotion:state:";

    /** 情绪历史Redis Key前缀 */
    private static final String EMOTION_HISTORY_KEY_PREFIX = "delta:emotion:history:";

    /** 情绪状态TTL（分钟） */
    private static final long EMOTION_STATE_TTL_MINUTES = 30;

    /** 积极情绪关键词 */
    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList(
            "谢谢", "好的", "很好", "不错", "满意", "太棒了", "厉害",
            "nice", "牛", "666", "好评", "点赞", "辛苦了", "没问题",
            "完美", "舒服", "开心", "快乐", "nice", "good", "感谢",
            "太强了", "可以的", "给力", "稳", "行", "OK", "ok"
    );

    /** 轻度负面关键词（可能不满但不一定愤怒） */
    private static final List<String> SLIGHTLY_NEGATIVE_KEYWORDS = Arrays.asList(
            "等一下", "怎么还没", "能快点吗", "有点慢", "不太行",
            "不太满意", "有点问题", "帮我问问", "再确认下"
    );

    private final RedisService redisService;

    @Override
    public EmotionState analyzeEmotion(Long userId, String message, long waitDurationMs) {
        if (userId == null || message == null) {
            return new EmotionState(userId);
        }

        EmotionState state = getEmotionState(userId);

        // 分析当前消息的情绪
        int messageScore = scoreMessageEmotion(message);

        // 更新情绪强度（带惯性：新消息权重0.3，历史权重0.7）
        int oldIntensity = state.getMoodIntensity();
        int newIntensity = (int) (oldIntensity * 0.7 + messageScore * 10 * 0.3);
        newIntensity = Math.max(1, Math.min(10, newIntensity));
        state.setMoodIntensity(newIntensity);

        // 更新情绪状态
        String newMood = determineMoodLevel(newIntensity, message);
        state.setCurrentMood(newMood);

        // 追踪连续负面
        if ("NEGATIVE".equals(newMood) || "ANGRY".equals(newMood)) {
            state.setNegativeCount(state.getNegativeCount() + 1);
            state.setLastNegativeKeyword(findNegativeKeyword(message));
        } else if ("POSITIVE".equals(newMood)) {
            state.setNegativeCount(Math.max(0, state.getNegativeCount() - 1));
        }

        // 判断情绪趋势
        if (newIntensity < oldIntensity - 1) {
            state.setMoodTrend("DECLINING");
        } else if (newIntensity > oldIntensity + 1) {
            state.setMoodTrend("IMPROVING");
        } else {
            state.setMoodTrend("STABLE");
        }

        // 判断是否需要转人工
        if ((state.getNegativeCount() >= 3 || "ANGRY".equals(newMood)) && !state.isHandoffRecommended()) {
            state.setHandoffRecommended(true);
            log.warn("【情绪智能】建议转人工 | userId={} | mood={} | negativeCount={}", userId, newMood, state.getNegativeCount());
        }

        state.setLastUpdateTime(LocalDateTime.now());

        // 保存到Redis
        saveEmotionState(userId, state);
        saveEmotionHistory(userId, state, message);

        return state;
    }

    @Override
    public EmotionState getEmotionState(Long userId) {
        if (userId == null) {
            return new EmotionState(null);
        }

        String key = EMOTION_STATE_KEY_PREFIX + userId;
        try {
            Object cached = redisService.get(key);
            if (cached != null) {
                return parseEmotionState(userId, cached.toString());
            }
        } catch (Exception e) {
            log.debug("【情绪智能】读取情绪状态缓存失败 | userId={} | error={}", userId, e.getMessage());
        }

        return new EmotionState(userId);
    }

    @Override
    public boolean shouldHandoff(Long userId) {
        EmotionState state = getEmotionState(userId);
        if (state.isHandoffRecommended()) {
            return true;
        }
        if (state.getCurrentMood() != null) {
            return "ANGRY".equals(state.getCurrentMood())
                    || ("NEGATIVE".equals(state.getCurrentMood()) && state.getNegativeCount() >= 3);
        }
        return false;
    }

    @Override
    public String buildEmotionAwarePrompt(Long userId) {
        EmotionState state = getEmotionState(userId);
        if (state == null || "NEUTRAL".equals(state.getCurrentMood())) {
            return "";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("\n【客户情绪状态】");

        switch (state.getCurrentMood()) {
            case "POSITIVE":
                prompt.append("客户当前心情愉快，保持积极互动，可以适时引导消费或预约。");
                break;
            case "SLIGHTLY_NEGATIVE":
                prompt.append("客户有些不满或急切，请用温暖耐心的语气，优先解决当前问题，避免推销。");
                break;
            case "NEGATIVE":
                prompt.append("客户明显不满（连续").append(state.getNegativeCount()).append("次负面表达），请真诚道歉，表达重视和理解，不要推卸责任。如无法解决，建议转人工。");
                break;
            case "ANGRY":
                prompt.append("客户非常愤怒！请立即表达深刻歉意，告知正在安排人工客服优先处理，不要做过多解释或辩解。");
                break;
            default:
                break;
        }

        if ("DECLINING".equals(state.getMoodTrend())) {
            prompt.append("\n注意：客户情绪正在持续恶化，请格外谨慎回应。");
        }

        return prompt.toString();
    }

    @Override
    public List<EmotionTrendPoint> getEmotionHistory(Long userId, int days) {
        if (userId == null) {
            return new ArrayList<>();
        }

        String key = EMOTION_HISTORY_KEY_PREFIX + userId;
        List<EmotionTrendPoint> result = new ArrayList<>();

        try {
            Object cached = redisService.get(key);
            if (cached != null) {
                // 简单实现：记录为字符串列表
                String historyStr = cached.toString();
                for (String line : historyStr.split("\n")) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) {
                        result.add(new EmotionTrendPoint(
                                LocalDateTime.parse(parts[0]),
                                parts[1],
                                Integer.parseInt(parts[2]),
                                parts[3]
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("【情绪智能】读取情绪历史失败 | userId={} | error={}", userId, e.getMessage());
        }

        return result;
    }

    @Override
    public void resetEmotionState(Long userId) {
        if (userId == null) return;
        try {
            redisService.delete(EMOTION_STATE_KEY_PREFIX + userId);
            log.debug("【情绪智能】重置情绪状态 | userId={}", userId);
        } catch (Exception e) {
            log.warn("【情绪智能】重置情绪状态失败 | userId={} | error={}", userId, e.getMessage());
        }
    }

    /**
     * 对消息内容进行情绪评分
     *
     * @param message 消息内容
     * @return 情绪评分(-1=强烈负面, 0=中性, 1=积极)
     */
    private int scoreMessageEmotion(String message) {
        String lower = message.toLowerCase().trim();

        // 检查强烈负面（愤怒级别）
        for (String keyword : AiCustomerServiceConstants.NEGATIVE_EMOTION_KEYWORDS) {
            if (lower.contains(keyword)) {
                return -1; // 强烈负面
            }
        }

        // 检查轻度负面
        for (String keyword : SLIGHTLY_NEGATIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                double negativeFraction = AiCustomerServiceConstants.NEGATIVE_EMOTION_KEYWORDS.stream()
                        .filter(lower::contains).count() > 0 ? -0.8 : -0.3;
                return (int) (negativeFraction * 10) / 10;
            }
        }

        // 检查积极
        for (String keyword : POSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return 1; // 积极
            }
        }

        return 0; // 中性
    }

    /**
     * 根据强度评分确定情绪等级
     *
     * @param intensity 情绪强度(1-10)
     * @param message   原始消息（用于辅助判断）
     * @return 情绪等级字符串
     */
    private String determineMoodLevel(int intensity, String message) {
        if (intensity <= 2) return "ANGRY";
        if (intensity <= 4) return "NEGATIVE";
        if (intensity <= 5) return "SLIGHTLY_NEGATIVE";
        if (intensity <= 7) return "NEUTRAL";
        return "POSITIVE";
    }

    /**
     * 查找消息中的负面关键词
     *
     * @param message 消息内容
     * @return 匹配到的负面关键词，未找到返回null
     */
    private String findNegativeKeyword(String message) {
        for (String keyword : AiCustomerServiceConstants.NEGATIVE_EMOTION_KEYWORDS) {
            if (message.contains(keyword)) return keyword;
        }
        for (String keyword : SLIGHTLY_NEGATIVE_KEYWORDS) {
            if (message.contains(keyword)) return keyword;
        }
        return null;
    }

    /**
     * 保存情绪状态到Redis
     *
     * @param userId 用户ID
     * @param state  情绪状态
     */
    private void saveEmotionState(Long userId, EmotionState state) {
        try {
            String key = EMOTION_STATE_KEY_PREFIX + userId;
            String json = toJson(state);
            redisService.set(key, json, EMOTION_STATE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.debug("【情绪智能】保存情绪状态失败 | userId={} | error={}", userId, e.getMessage());
        }
    }

    /**
     * 保存情绪历史记录
     *
     * @param userId      用户ID
     * @param state       当前情绪状态
     * @param message     触发消息
     */
    private void saveEmotionHistory(Long userId, EmotionState state, String message) {
        try {
            String key = EMOTION_HISTORY_KEY_PREFIX + userId;
            String record = state.getLastUpdateTime().toString() + "|" +
                    state.getCurrentMood() + "|" +
                    state.getMoodIntensity() + "|" +
                    (message != null ? message.replace("|", " ").substring(0, Math.min(100, message.length())) : "");

            // 追加到Redis List
            Object existing = redisService.get(key);
            String newValue;
            if (existing != null) {
                newValue = existing.toString() + "\n" + record;
            } else {
                newValue = record;
            }

            // 限制最多200行
            String[] lines = newValue.split("\n");
            if (lines.length > 200) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 200; i < lines.length; i++) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(lines[i]);
                }
                newValue = sb.toString();
            }

            redisService.set(key, newValue, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.debug("【情绪智能】保存情绪历史失败 | userId={} | error={}", userId, e.getMessage());
        }
    }

    /**
     * 将情绪状态对象序列化为JSON字符串（简化版，避免引入Jackson依赖）
     *
     * @param state 情绪状态
     * @return JSON字符串
     */
    private String toJson(EmotionState state) {
        return String.format(
                "{\"userId\":%d,\"currentMood\":\"%s\",\"moodTrend\":\"%s\",\"negativeCount\":%d,\"lastNegativeKeyword\":\"%s\",\"handoffRecommended\":%b,\"lastUpdateTime\":\"%s\",\"moodIntensity\":%d}",
                state.getUserId(),
                state.getCurrentMood(),
                state.getMoodTrend(),
                state.getNegativeCount(),
                state.getLastNegativeKeyword() != null ? state.getLastNegativeKeyword() : "",
                state.isHandoffRecommended(),
                state.getLastUpdateTime().toString(),
                state.getMoodIntensity()
        );
    }

    /**
     * 从JSON字符串解析情绪状态
     *
     * @param userId    用户ID
     * @param jsonStr   JSON字符串
     * @return 情绪状态对象
     */
    private EmotionState parseEmotionState(Long userId, String jsonStr) {
        EmotionState state = new EmotionState(userId);
        try {
            if (jsonStr.contains("\"currentMood\":\"")) {
                state.setCurrentMood(extractJsonValue(jsonStr, "currentMood"));
            }
            if (jsonStr.contains("\"moodTrend\":\"")) {
                state.setMoodTrend(extractJsonValue(jsonStr, "moodTrend"));
            }
            if (jsonStr.contains("\"negativeCount\":")) {
                state.setNegativeCount(Integer.parseInt(extractJsonValue(jsonStr, "negativeCount")));
            }
            if (jsonStr.contains("\"handoffRecommended\":")) {
                state.setHandoffRecommended(Boolean.parseBoolean(extractJsonValue(jsonStr, "handoffRecommended")));
            }
            if (jsonStr.contains("\"moodIntensity\":")) {
                state.setMoodIntensity(Integer.parseInt(extractJsonValue(jsonStr, "moodIntensity")));
            }
            if (jsonStr.contains("\"lastNegativeKeyword\":\"")) {
                String keyword = extractJsonValue(jsonStr, "lastNegativeKeyword");
                if (keyword != null && !keyword.isEmpty()) {
                    state.setLastNegativeKeyword(keyword);
                }
            }
        } catch (Exception e) {
            log.debug("【情绪智能】解析情绪状态JSON失败 | error={}", e.getMessage());
        }
        return state;
    }

    /**
     * 从简单JSON中提取字段值（简化版，不依赖JSON库）
     *
     * @param json       JSON字符串
     * @param fieldName  字段名
     * @return 字段值
     */
    private String extractJsonValue(String json, String fieldName) {
        int start = json.indexOf("\"" + fieldName + "\":");
        if (start < 0) return "";
        start = json.indexOf(":", start) + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        int end;
        if (start < json.length() && json.charAt(start) == '"') {
            start++;
            end = json.indexOf("\"", start);
        } else {
            end = json.indexOf(",", start);
            if (end < 0) end = json.indexOf("}", start);
        }
        if (end < 0) end = json.length();
        return json.substring(start, end).trim();
    }
}
