package com.delta.common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 内容安全过滤服务接口
 * <p>
 * 提供内容安全检查、敏感词过滤、敏感词库管理和安全报告生成功能。
 * 在消息处理入口调用此服务进行内容审核，对违规内容进行拦截或标记。
 * </p>
 *
 * @author 刘建国
 */
public interface ContentSafetyService {

    /**
     * 内容安全检查
     * <p>
     * 对用户输入的文本进行全面的安全检查，包括敏感词匹配和正则模式检测。
     * </p>
     *
     * @param text   待检查的文本内容
     * @param userId 用户ID，用于记录安全报告
     * @return 安全检查结果，包含是否通过、过滤后文本、匹配词等信息
     */
    ContentSafetyResult checkContent(String text, String userId);

    /**
     * 判断文本是否包含敏感词
     * <p>
     * 快速检查文本中是否包含任何敏感词，不记录报告。
     * </p>
     *
     * @param text 待检查的文本内容
     * @return 如果包含敏感词返回true，否则返回false
     */
    boolean containsSensitiveWord(String text);

    /**
     * 过滤内容中的敏感词
     * <p>
     * 将文本中的敏感词替换为"***"，返回过滤后的文本。
     * </p>
     *
     * @param text 待过滤的文本内容
     * @return 过滤后的文本（敏感词被替换为***）
     */
    String filterContent(String text);

    /**
     * 刷新敏感词库
     * <p>
     * 从数据库重新加载所有敏感词，更新内存中的敏感词缓存。
     * 用于敏感词配置变更后动态刷新，无需重启服务。
     * </p>
     */
    void reloadSensitiveWords();

    /**
     * 生成安全报告
     * <p>
     * 统计指定日期范围内的内容安全检查情况，包括检查总量、拦截量、
     * 告警量、高频匹配词和分时段统计数据。
     * </p>
     *
     * @param start 报告起始日期（包含）
     * @param end   报告结束日期（包含）
     * @return 安全报告数据
     */
    SafetyReport generateSafetyReport(LocalDate start, LocalDate end);

    /**
     * 安全等级枚举
     * <p>
     * 定义内容安全检查结果的安全等级：
     * SAFE - 内容安全，放行
     * WARNING - 内容疑似违规，需要人工审核
     * BLOCK - 内容明确违规，直接拦截
     * </p>
     */
    enum SafetyLevel {
        /** 安全，内容通过检查 */
        SAFE,
        /** 疑似违规，需人工审核 */
        WARNING,
        /** 明确违规，拦截 */
        BLOCK
    }

    /**
     * 敏感词类别枚举
     * <p>
     * 定义敏感词的五大类别，对应数据库keywords表中的type字段。
     * </p>
     */
    enum SafetyCategory {
        /** 政治敏感 */
        POLITICAL,
        /** 成人/色情内容 */
        ADULT,
        /** 暴力/恐怖内容 */
        VIOLENCE,
        /** 诈骗/欺诈内容 */
        FRAUD,
        /** 骚扰/辱骂内容 */
        HARASSMENT
    }

    /**
     * 内容安全检查结果
     * <p>
     * 封装单次内容安全检查的完整结果信息。
     * </p>
     */
    class ContentSafetyResult {

        /** 是否通过安全检查 */
        private final boolean passed;

        /** 过滤后的文本内容（敏感词已替换为***） */
        private final String filteredText;

        /** 匹配到的敏感词列表 */
        private final List<String> matchedWords;

        /** 安全等级 */
        private final SafetyLevel level;

        /** 拦截/告警原因说明 */
        private final String reason;

        /**
         * 构造安全检查结果
         *
         * @param passed       是否通过
         * @param filteredText 过滤后文本
         * @param matchedWords 匹配到的敏感词
         * @param level        安全等级
         * @param reason       原因说明
         */
        public ContentSafetyResult(boolean passed, String filteredText,
                                    List<String> matchedWords, SafetyLevel level, String reason) {
            this.passed = passed;
            this.filteredText = filteredText;
            this.matchedWords = matchedWords;
            this.level = level;
            this.reason = reason;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getFilteredText() {
            return filteredText;
        }

        public List<String> getMatchedWords() {
            return matchedWords;
        }

        public SafetyLevel getLevel() {
            return level;
        }

        public String getReason() {
            return reason;
        }
    }

    /**
     * 安全报告
     * <p>
     * 统计指定日期范围内的内容安全检查汇总数据。
     * </p>
     */
    class SafetyReport {

        /** 总检查次数 */
        private final long totalChecks;

        /** 拦截次数 */
        private final long blockedCount;

        /** 告警次数 */
        private final long warningCount;

        /** 安全通过次数 */
        private final long safeCount;

        /** 高频匹配词及出现次数 */
        private final Map<String, Long> topMatchedWords;

        /** 按小时统计的检查次数，key为小时(0-23)，value为次数 */
        private final Map<Integer, Long> checksByHour;

        /** 报告生成时间 */
        private final LocalDateTime generatedAt;

        /**
         * 构造安全报告
         *
         * @param totalChecks     总检查次数
         * @param blockedCount    拦截次数
         * @param warningCount    告警次数
         * @param safeCount       安全通过次数
         * @param topMatchedWords 高频匹配词
         * @param checksByHour    分时段统计
         * @param generatedAt     生成时间
         */
        public SafetyReport(long totalChecks, long blockedCount, long warningCount,
                            long safeCount, Map<String, Long> topMatchedWords,
                            Map<Integer, Long> checksByHour, LocalDateTime generatedAt) {
            this.totalChecks = totalChecks;
            this.blockedCount = blockedCount;
            this.warningCount = warningCount;
            this.safeCount = safeCount;
            this.topMatchedWords = topMatchedWords;
            this.checksByHour = checksByHour;
            this.generatedAt = generatedAt;
        }

        public long getTotalChecks() {
            return totalChecks;
        }

        public long getBlockedCount() {
            return blockedCount;
        }

        public long getWarningCount() {
            return warningCount;
        }

        public long getSafeCount() {
            return safeCount;
        }

        public Map<String, Long> getTopMatchedWords() {
            return topMatchedWords;
        }

        public Map<Integer, Long> getChecksByHour() {
            return checksByHour;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }
    }
}
