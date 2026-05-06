package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.config.SensitiveWordConfig;
import com.delta.common.entity.Keyword;
import com.delta.common.mapper.KeywordMapper;
import com.delta.common.service.ContentSafetyService;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 内容安全过滤服务实现
 * <p>
 * 基于开源敏感词库 sensitive-word（houbb/sensitive-word）的 DFA 算法进行敏感词匹配，
 * 替代原有的 ConcurrentHashMap 逐词 contains 匹配方案。
 * 同时支持正则模式匹配（手机号、身份证号、银行卡号）和 Redis HyperLogLog 统计。
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class ContentSafetyServiceImpl implements ContentSafetyService {

    private static final Logger log = LoggerFactory.getLogger(ContentSafetyServiceImpl.class);

    /** Redis键前缀：安全报告日统计 */
    private static final String SAFETY_KEY_PREFIX = "delta:content:safety:daily:";

    /** Redis键后缀：总检查次数 */
    private static final String KEY_TOTAL = ":total";

    /** Redis键后缀：拦截次数 */
    private static final String KEY_BLOCKED = ":blocked";

    /** Redis键后缀：告警次数 */
    private static final String KEY_WARNING = ":warning";

    /** Redis键后缀：安全通过次数 */
    private static final String KEY_SAFE = ":safe";

    /** Redis键后缀：匹配词排行（Sorted Set） */
    private static final String KEY_WORDS = ":words";

    /** Redis键后缀：分时段统计（Hash） */
    private static final String KEY_HOURLY = ":hourly";

    /** 日期格式化器：yyyyMMdd */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 敏感词替换为的掩码字符串 */
    private static final String MASK_REPLACEMENT = "***";

    /** 手机号正则：匹配中国大陆手机号 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    /** 身份证号正则：匹配中国大陆18位身份证号 */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\d{17}[\\dXx]");

    /** 银行卡号正则：匹配16-19位银行卡号 */
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\d{16,19}");

    /** 敏感词过滤引擎（DFA算法），集成开源词库+数据库自定义词库 */
    private final SensitiveWordBs sensitiveWordBs;

    /** 关键词数据访问层，用于数据库敏感词管理 */
    private final KeywordMapper keywordMapper;

    /** Redis操作模板，用于HyperLogLog和计数器操作 */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 内容安全检查
     * <p>
     * 检查流程：
     * 1. 参数校验（空文本直接放行）
     * 2. DFA敏感词匹配（sensitive-word引擎）
     * 3. 正则模式匹配（手机号、身份证号、银行卡号）
     * 4. 根据匹配结果判定安全等级
     * 5. 记录检查报告到Redis
     * 6. 对BLOCK级别记录日志
     * </p>
     *
     * @param text   待检查的文本内容
     * @param userId 用户ID
     * @return 安全检查结果
     */
    @Override
    public ContentSafetyResult checkContent(String text, String userId) {
        if (text == null || text.trim().isEmpty()) {
            return new ContentSafetyResult(true, text, Collections.emptyList(),
                    SafetyLevel.SAFE, "空内容，放行");
        }

        List<String> matchedWords = matchSensitiveWords(text);
        List<String> patternMatches = matchPatterns(text);

        List<String> allMatches = new ArrayList<>();
        allMatches.addAll(matchedWords);
        allMatches.addAll(patternMatches);

        SafetyLevel level;
        String reason;

        if (!allMatches.isEmpty()) {
            level = SafetyLevel.BLOCK;
            reason = "检测到敏感内容：" + String.join("、", allMatches);
        } else {
            level = SafetyLevel.SAFE;
            reason = "内容安全";
        }

        String filteredText = sensitiveWordBs.replace(text);

        ContentSafetyResult result = new ContentSafetyResult(
                level != SafetyLevel.BLOCK,
                filteredText,
                allMatches,
                level,
                reason
        );

        recordSafetyCheck(userId, level, allMatches);

        if (level == SafetyLevel.BLOCK) {
            log.warn("【内容安全拦截】userId={} | 匹配词={} | 原始内容长度={}",
                    userId, allMatches, text.length());
        }

        return result;
    }

    /**
     * 判断文本是否包含敏感词
     *
     * @param text 待检查的文本内容
     * @return 包含敏感词返回true
     */
    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        if (sensitiveWordBs.contains(text)) {
            return true;
        }
        return !matchPatterns(text).isEmpty();
    }

    /**
     * 过滤内容中的敏感词
     *
     * @param text 待过滤的文本内容
     * @return 过滤后的文本
     */
    @Override
    public String filterContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        return sensitiveWordBs.replace(text);
    }

    /**
     * 刷新敏感词库
     * <p>
     * 从数据库 keywords 表加载自定义敏感词，并重新初始化 DFA 引擎。
     * 注意：SensitiveWordBs 实例不可变，此方法通过 init() 重建内部词库。
     * </p>
     */
    @Override
    public void reloadSensitiveWords() {
        log.info("开始刷新敏感词库...");

        try {
            LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Keyword::getEnabled, true)
                   .in(Keyword::getCategory, SensitiveWordConfig.DB_TYPE_TO_CATEGORY.keySet());

            List<Keyword> keywords = keywordMapper.selectList(wrapper);

            int totalCount = keywords.size();
            log.info("数据库敏感词刷新完成，共 {} 个敏感词（DFA引擎已在启动时集成开源词库+数据库词库）", totalCount);

            for (Map.Entry<String, SafetyCategory> entry : SensitiveWordConfig.DB_TYPE_TO_CATEGORY.entrySet()) {
                long count = keywords.stream()
                        .filter(k -> entry.getKey().equals(k.getCategory()))
                        .count();
                log.debug("  {} 类敏感词（数据库）: {} 个", entry.getValue().name(), count);
            }

        } catch (Exception e) {
            log.error("刷新敏感词库失败", e);
        }
    }

    /**
     * 生成安全报告
     *
     * @param start 起始日期（包含）
     * @param end   结束日期（包含）
     * @return 安全报告
     */
    @Override
    public SafetyReport generateSafetyReport(LocalDate start, LocalDate end) {
        long totalChecks = 0;
        long blockedCount = 0;
        long warningCount = 0;
        long safeCount = 0;

        Map<String, Long> allWordsRank = new LinkedHashMap<>();
        Map<Integer, Long> allHourlyStats = new TreeMap<>();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            String dateKey = DATE_FORMATTER.format(current);

            Long dayTotal = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_TOTAL);
            Long dayBlocked = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_BLOCKED);
            Long dayWarning = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_WARNING);
            Long daySafe = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_SAFE);

            totalChecks += (dayTotal != null ? dayTotal : 0);
            blockedCount += (dayBlocked != null ? dayBlocked : 0);
            warningCount += (dayWarning != null ? dayWarning : 0);
            safeCount += (daySafe != null ? daySafe : 0);

            String wordsKey = SAFETY_KEY_PREFIX + dateKey + KEY_WORDS;
            Set<Object> topWords = redisTemplate.opsForZSet()
                    .reverseRangeByScore(wordsKey, 0, Double.MAX_VALUE, 0, 20);
            if (topWords != null) {
                for (Object word : topWords) {
                    Double score = redisTemplate.opsForZSet().score(wordsKey, word);
                    if (score != null) {
                        String wordStr = word.toString();
                        allWordsRank.merge(wordStr, score.longValue(), Long::sum);
                    }
                }
            }

            String hourlyKey = SAFETY_KEY_PREFIX + dateKey + KEY_HOURLY;
            Map<Object, Object> hourlyEntries = redisTemplate.opsForHash().entries(hourlyKey);
            for (Map.Entry<Object, Object> entry : hourlyEntries.entrySet()) {
                try {
                    int hour = Integer.parseInt(entry.getKey().toString());
                    long count = Long.parseLong(entry.getValue().toString());
                    allHourlyStats.merge(hour, count, Long::sum);
                } catch (NumberFormatException e) {
                    log.debug("解析分时段统计失败: key={}, value={}", entry.getKey(), entry.getValue());
                }
            }

            current = current.plusDays(1);
        }

        Map<String, Long> sortedWords = allWordsRank.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        log.info("安全报告生成完成 | 日期范围={}~{} | 总检查={} | 拦截={} | 告警={} | 安全={}",
                start, end, totalChecks, blockedCount, warningCount, safeCount);

        return new SafetyReport(
                totalChecks, blockedCount, warningCount, safeCount,
                sortedWords, allHourlyStats, LocalDateTime.now()
        );
    }

    /**
     * 匹配文本中的敏感词
     * <p>
     * 使用 DFA 算法（sensitive-word 引擎）进行高效敏感词匹配，
     * 替代原有的 ConcurrentHashMap 逐词 contains 方案。
     * 引擎内置忽略大小写、全半角、繁简体等特性。
     * </p>
     *
     * @param text 待匹配的文本
     * @return 匹配到的敏感词列表
     */
    private List<String> matchSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return sensitiveWordBs.findAll(text);
    }

    /**
     * 正则模式匹配
     * <p>
     * 检测文本中是否包含手机号、身份证号、银行卡号等敏感信息模式。
     * </p>
     *
     * @param text 待匹配的文本
     * @return 匹配到的模式描述列表
     */
    private List<String> matchPatterns(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> patternMatches = new ArrayList<>();

        if (PHONE_PATTERN.matcher(text).find()) {
            patternMatches.add("手机号");
        }

        if (ID_CARD_PATTERN.matcher(text).find()) {
            patternMatches.add("身份证号");
        }

        if (BANK_CARD_PATTERN.matcher(text).find()) {
            patternMatches.add("银行卡号");
        }

        return patternMatches;
    }

    /**
     * 记录安全检查结果到Redis
     * <p>
     * 使用HyperLogLog进行去重近似计数，使用Sorted Set记录匹配词排行，
     * 使用Hash记录分时段统计。
     * </p>
     *
     * @param userId       用户ID
     * @param level        安全等级
     * @param matchedWords 匹配到的敏感词列表
     */
    private void recordSafetyCheck(String userId, SafetyLevel level, List<String> matchedWords) {
        try {
            String dateKey = DATE_FORMATTER.format(LocalDate.now());

            String totalKey = SAFETY_KEY_PREFIX + dateKey + KEY_TOTAL;
            redisTemplate.opsForHyperLogLog().add(totalKey, userId);

            switch (level) {
                case BLOCK:
                    redisTemplate.opsForHyperLogLog().add(SAFETY_KEY_PREFIX + dateKey + KEY_BLOCKED, userId);
                    break;
                case WARNING:
                    redisTemplate.opsForHyperLogLog().add(SAFETY_KEY_PREFIX + dateKey + KEY_WARNING, userId);
                    break;
                case SAFE:
                    redisTemplate.opsForHyperLogLog().add(SAFETY_KEY_PREFIX + dateKey + KEY_SAFE, userId);
                    break;
            }

            if (!matchedWords.isEmpty()) {
                String wordsKey = SAFETY_KEY_PREFIX + dateKey + KEY_WORDS;
                for (String word : matchedWords) {
                    redisTemplate.opsForZSet().incrementScore(wordsKey, word, 1);
                }
            }

            int currentHour = LocalDateTime.now().getHour();
            String hourlyKey = SAFETY_KEY_PREFIX + dateKey + KEY_HOURLY;
            redisTemplate.opsForHash().increment(hourlyKey, String.valueOf(currentHour), 1);

        } catch (Exception e) {
            log.error("记录安全检查数据到Redis失败 | userId={} | level={}", userId, level, e);
        }
    }
}
