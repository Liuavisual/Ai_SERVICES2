package com.delta.common.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 情绪智能服务接口
 * <p>
 * 提供客户交互过程中的情绪状态追踪、分析和自适应回复策略生成。
 * 通过Redis存储每个活跃会话的情绪状态，实现上下文感知的情绪智能。
 * </p>
 * <p>
 * 情绪状态模型：
 * <ul>
 *   <li>POSITIVE - 积极/满意</li>
 *   <li>NEUTRAL - 中性/正常</li>
 *   <li>SLIGHTLY_NEGATIVE - 轻微负面</li>
 *   <li>NEGATIVE - 明显负面</li>
 *   <li>ANGRY - 愤怒/极度不满</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
public interface EmotionIntelligenceService {

    /**
     * 分析用户消息并更新情绪状态
     * <p>
     * 综合考虑以下因素更新情绪评分：
     * <ol>
     *   <li>当前消息中的情绪关键词（正面/负面）</li>
     *   <li>历史情绪趋势（持续恶化→升级）</li>
     *   <li>客户画像特征（VIP客户更敏感）</li>
     *   <li>回复等待时间（超时→增加负面倾向）</li>
     * </ol>
     * </p>
     *
     * @param userId          用户ID
     * @param message         用户消息内容
     * @param waitDurationMs  等待时长（毫秒），用于评估响应延迟对情绪的影响
     * @return 更新后的情绪状态
     */
    EmotionState analyzeEmotion(Long userId, String message, long waitDurationMs);

    /**
     * 获取用户当前情绪状态
     *
     * @param userId 用户ID
     * @return 情绪状态对象，不存在返回默认NEUTRAL状态
     */
    EmotionState getEmotionState(Long userId);

    /**
     * 判断当前是否应该转人工处理
     * <p>
     * 判断条件：
     * <ol>
     *   <li>情绪等级达到NEGATIVE或ANGRY</li>
     *   <li>连续负面消息数超过阈值(3次)</li>
     *   <li>情绪趋势持续DECLINING超过3轮对话</li>
     * </ol>
     * </p>
     *
     * @param userId 用户ID
     * @return 是否应该转人工
     */
    boolean shouldHandoff(Long userId);

    /**
     * 构建情绪感知的AI提示词附加内容
     * <p>
     * 根据当前情绪状态，生成对应的AI行为指导文本，例如：
     * <ul>
     *   <li>ANGRY → "客户当前非常不满，请真诚道歉并表现出理解和重视"</li>
     *   <li>SLIGHTLY_NEGATIVE → "客户有些不满，请用温暖的语气积极解决问题"</li>
     *   <li>POSITIVE → "客户心情愉快，保持积极互动并适时引导消费"</li>
     * </ul>
     * </p>
     *
     * @param userId 用户ID
     * @return 情绪感知提示词文本，无特殊情绪状态返回空字符串
     */
    String buildEmotionAwarePrompt(Long userId);

    /**
     * 获取用户近期情绪变化趋势历史
     *
     * @param userId  用户ID
     * @param days    查询天数（1-30）
     * @return 情绪趋势数据列表，按时间升序排列
     */
    List<EmotionTrendPoint> getEmotionHistory(Long userId, int days);

    /**
     * 重置用户情绪状态（转人工后或会话结束后调用）
     *
     * @param userId 用户ID
     */
    void resetEmotionState(Long userId);

    /**
     * 情绪状态实体
     * <p>
     * 封装单个用户在AI对话中的实时情绪状态信息，
     * 存储在Redis中（Key: delta:emotion:{userId}，TTL: 30分钟）。
     * </p>
     */
    class EmotionState {

        /** 用户ID */
        private final Long userId;

        /** 当前情绪：POSITIVE / NEUTRAL / SLIGHTLY_NEGATIVE / NEGATIVE / ANGRY */
        private String currentMood;

        /** 情绪趋势：IMPROVING / STABLE / DECLINING */
        private String moodTrend;

        /** 连续负面消息计数 */
        private int negativeCount;

        /** 最近一次触发的负面情绪关键词 */
        private String lastNegativeKeyword;

        /** 是否已建议转人工 */
        private boolean handoffRecommended;

        /** 最近更新时间 */
        private LocalDateTime lastUpdateTime;

        /** 情绪强度评分(1-10)，综合当前情绪和趋势计算 */
        private int moodIntensity;

        /**
         * 构造初始情绪状态（默认NEUTRAL）
         *
         * @param userId 用户ID
         */
        public EmotionState(Long userId) {
            this.userId = userId;
            this.currentMood = "NEUTRAL";
            this.moodTrend = "STABLE";
            this.negativeCount = 0;
            this.handoffRecommended = false;
            this.lastUpdateTime = LocalDateTime.now();
            this.moodIntensity = 5;
        }

        public Long getUserId() {
            return userId;
        }

        public String getCurrentMood() {
            return currentMood;
        }

        public void setCurrentMood(String currentMood) {
            this.currentMood = currentMood;
        }

        public String getMoodTrend() {
            return moodTrend;
        }

        public void setMoodTrend(String moodTrend) {
            this.moodTrend = moodTrend;
        }

        public int getNegativeCount() {
            return negativeCount;
        }

        public void setNegativeCount(int negativeCount) {
            this.negativeCount = negativeCount;
        }

        public String getLastNegativeKeyword() {
            return lastNegativeKeyword;
        }

        public void setLastNegativeKeyword(String lastNegativeKeyword) {
            this.lastNegativeKeyword = lastNegativeKeyword;
        }

        public boolean isHandoffRecommended() {
            return handoffRecommended;
        }

        public void setHandoffRecommended(boolean handoffRecommended) {
            this.handoffRecommended = handoffRecommended;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public int getMoodIntensity() {
            return moodIntensity;
        }

        public void setMoodIntensity(int moodIntensity) {
            this.moodIntensity = moodIntensity;
        }

        /**
         * 判断是否处于需要关注的负面状态
         *
         * @return 是否负面（NEGATIVE或ANGRY）
         */
        public boolean isNegative() {
            return "NEGATIVE".equals(currentMood) || "ANGRY".equals(currentMood);
        }

        /**
         * 判断情绪是否在恶化
         *
         * @return 是否恶化趋势
         */
        public boolean isDeclining() {
            return "DECLINING".equals(moodTrend) && negativeCount >= 2;
        }
    }

    /**
     * 情绪趋势数据点
     * <p>
     * 记录某个时间点的情绪快照，用于绘制情绪变化曲线图。
     * </p>
     */
    class EmotionTrendPoint {

        /** 记录时间 */
        private final LocalDateTime timestamp;

        /** 情绪状态 */
        private final String mood;

        /** 情绪强度(1-10) */
        private final int intensity;

        /** 触发消息摘要 */
        private final String triggerMessage;

        /**
         * 构造情绪趋势点
         *
         * @param timestamp      时间戳
         * @param mood           情绪状态
         * @param intensity      情绪强度
         * @param triggerMessage 触发消息
         */
        public EmotionTrendPoint(LocalDateTime timestamp, String mood, int intensity, String triggerMessage) {
            this.timestamp = timestamp;
            this.mood = mood;
            this.intensity = intensity;
            this.triggerMessage = triggerMessage;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getMood() {
            return mood;
        }

        public int getIntensity() {
            return intensity;
        }

        public String getTriggerMessage() {
            return triggerMessage;
        }
    }
}
