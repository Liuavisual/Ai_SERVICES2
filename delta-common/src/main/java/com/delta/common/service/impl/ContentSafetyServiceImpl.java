package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.Keyword;
import com.delta.common.mapper.KeywordMapper;
import com.delta.common.service.ContentSafetyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
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
 * 基于敏感词库和正则模式匹配，对用户输入内容进行安全检查。
 * 采用 ConcurrentHashMap + volatile 模式缓存敏感词库，支持动态刷新。
 * 检查结果通过 Redis HyperLogLog 进行近似计数统计。
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

    /** 敏感词数据库类型前缀到安全类别的映射 */
    private static final Map<String, SafetyCategory> TYPE_TO_CATEGORY = new LinkedHashMap<>();

    static {
        TYPE_TO_CATEGORY.put("SENSITIVE_POLITICAL", SafetyCategory.POLITICAL);
        TYPE_TO_CATEGORY.put("SENSITIVE_ADULT", SafetyCategory.ADULT);
        TYPE_TO_CATEGORY.put("SENSITIVE_VIOLENCE", SafetyCategory.VIOLENCE);
        TYPE_TO_CATEGORY.put("SENSITIVE_FRAUD", SafetyCategory.FRAUD);
        TYPE_TO_CATEGORY.put("SENSITIVE_HARASSMENT", SafetyCategory.HARASSMENT);
    }

    /** 关键词数据访问层，用于从数据库加载敏感词 */
    private final KeywordMapper keywordMapper;

    /** Redis操作模板，用于HyperLogLog和计数器操作 */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 敏感词库缓存
     * <p>
     * 使用volatile保证可见性，ConcurrentHashMap保证并发安全。
     * key为敏感词类别，value为该类别下的敏感词集合。
     * 刷新时整体替换，避免并发修改问题。
     * </p>
     */
    private volatile Map<SafetyCategory, Set<String>> sensitiveWordsMap = new ConcurrentHashMap<>();

    /**
     * 服务初始化，在Spring容器启动后自动加载敏感词库
     */
    @PostConstruct
    public void init() {
        reloadSensitiveWords();
        log.info("内容安全过滤服务初始化完成");
    }

    /**
     * 内容安全检查
     * <p>
     * 检查流程：
     * 1. 参数校验（空文本直接放行）
     * 2. 敏感词匹配
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
        // 参数校验：空文本直接放行
        if (text == null || text.trim().isEmpty()) {
            return new ContentSafetyResult(true, text, Collections.emptyList(),
                    SafetyLevel.SAFE, "空内容，放行");
        }

        // 敏感词匹配
        List<String> matchedWords = matchSensitiveWords(text);
        // 正则模式匹配（手机号、身份证号、银行卡号）
        List<String> patternMatches = matchPatterns(text);

        // 合并所有匹配词
        List<String> allMatches = new ArrayList<>();
        allMatches.addAll(matchedWords);
        allMatches.addAll(patternMatches);

        // 判定安全等级
        SafetyLevel level;
        String reason;

        if (!allMatches.isEmpty()) {
            // 匹配到任何敏感词或敏感模式，直接BLOCK
            level = SafetyLevel.BLOCK;
            reason = "检测到敏感内容：" + String.join("、", allMatches);
        } else {
            level = SafetyLevel.SAFE;
            reason = "内容安全";
        }

        // 过滤文本中的敏感词
        String filteredText = filterMatchedWords(text, allMatches);

        // 构建结果
        ContentSafetyResult result = new ContentSafetyResult(
                level != SafetyLevel.BLOCK,
                filteredText,
                allMatches,
                level,
                reason
        );

        // 记录检查报告到Redis
        recordSafetyCheck(userId, level, allMatches);

        // 对BLOCK级别记录拦截日志
        if (level == SafetyLevel.BLOCK) {
            log.warn("【内容安全拦截】userId={} | 匹配词={} | 原始内容长度={}",
                    userId, allMatches, text.length());
        }

        return result;
    }

    /**
     * 判断文本是否包含敏感词
     * <p>
     * 快速检查，仅作布尔判断，不记录报告。
     * </p>
     *
     * @param text 待检查的文本内容
     * @return 包含敏感词返回true
     */
    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        // 先检查敏感词匹配
        if (!matchSensitiveWords(text).isEmpty()) {
            return true;
        }
        // 再检查正则模式匹配
        return !matchPatterns(text).isEmpty();
    }

    /**
     * 过滤内容中的敏感词
     * <p>
     * 将文本中所有匹配到的敏感词替换为"***"。
     * </p>
     *
     * @param text 待过滤的文本内容
     * @return 过滤后的文本
     */
    @Override
    public String filterContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 收集所有匹配到的敏感词
        List<String> allMatches = new ArrayList<>();
        allMatches.addAll(matchSensitiveWords(text));
        allMatches.addAll(matchPatterns(text));

        // 替换所有敏感词
        return filterMatchedWords(text, allMatches);
    }

    /**
     * 刷新敏感词库
     * <p>
     * 从数据库keywords表重新加载所有敏感词，按类型分类存储。
     * 使用整体替换方式，避免并发读写问题。
     * </p>
     */
    @Override
    public void reloadSensitiveWords() {
        log.info("开始刷新敏感词库...");

        try {
            // 查询所有启用的关键词
            LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Keyword::getEnabled, true);
            // 只查询敏感词类型的数据（按category字段筛选）
            wrapper.in(Keyword::getCategory, TYPE_TO_CATEGORY.keySet());

            List<Keyword> keywords = keywordMapper.selectList(wrapper);

            // 按类别分组构建新的敏感词Map
            Map<SafetyCategory, Set<String>> newMap = new ConcurrentHashMap<>();

            // 初始化所有类别为空集合
            for (SafetyCategory category : SafetyCategory.values()) {
                newMap.put(category, ConcurrentHashMap.newKeySet());
            }

            // 将关键词按category分类
            for (Keyword keyword : keywords) {
                String category = keyword.getCategory();
                if (category != null && TYPE_TO_CATEGORY.containsKey(category)) {
                    SafetyCategory safetyCategory = TYPE_TO_CATEGORY.get(category);
                    newMap.get(safetyCategory).add(keyword.getKeyword());
                }
            }

            // 整体替换，利用volatile保证可见性
            this.sensitiveWordsMap = newMap;

            int totalCount = keywords.size();
            log.info("敏感词库刷新完成，共加载 {} 个敏感词", totalCount);

            // 按类别输出统计日志
            for (Map.Entry<SafetyCategory, Set<String>> entry : newMap.entrySet()) {
                log.debug("  {} 类敏感词: {} 个", entry.getKey().name(), entry.getValue().size());
            }

        } catch (Exception e) {
            log.error("刷新敏感词库失败", e);
        }
    }

    /**
     * 生成安全报告
     * <p>
     * 从Redis中读取指定日期范围内的安全检查统计数据并汇总。
     * 使用HyperLogLog获取近似去重计数。
     * </p>
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

        // 汇总匹配词排行
        Map<String, Long> allWordsRank = new LinkedHashMap<>();
        // 汇总分时段统计
        Map<Integer, Long> allHourlyStats = new TreeMap<>();

        // 遍历日期范围，汇总每日数据
        LocalDate current = start;
        while (!current.isAfter(end)) {
            String dateKey = DATE_FORMATTER.format(current);

            // 读取各维度的HyperLogLog近似计数
            Long dayTotal = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_TOTAL);
            Long dayBlocked = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_BLOCKED);
            Long dayWarning = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_WARNING);
            Long daySafe = redisTemplate.opsForHyperLogLog().size(SAFETY_KEY_PREFIX + dateKey + KEY_SAFE);

            totalChecks += (dayTotal != null ? dayTotal : 0);
            blockedCount += (dayBlocked != null ? dayBlocked : 0);
            warningCount += (dayWarning != null ? dayWarning : 0);
            safeCount += (daySafe != null ? daySafe : 0);

            // 读取匹配词排行（Sorted Set），取Top 20
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

            // 读取分时段统计（Hash）
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

        // 高频匹配词按出现次数降序排列
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
     * 遍历所有敏感词类别，对每个类别的敏感词进行逐词匹配。
     * 对文本和敏感词均转为小写进行不区分大小写匹配。
     * </p>
     *
     * @param text 待匹配的文本
     * @return 匹配到的敏感词列表
     */
    private List<String> matchSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> matched = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 遍历所有敏感词类别
        for (Map.Entry<SafetyCategory, Set<String>> entry : sensitiveWordsMap.entrySet()) {
            for (String word : entry.getValue()) {
                if (lowerText.contains(word.toLowerCase())) {
                    matched.add(word);
                }
            }
        }

        return matched;
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

        // 检测手机号
        if (PHONE_PATTERN.matcher(text).find()) {
            patternMatches.add("手机号");
        }

        // 检测身份证号
        if (ID_CARD_PATTERN.matcher(text).find()) {
            patternMatches.add("身份证号");
        }

        // 检测银行卡号
        if (BANK_CARD_PATTERN.matcher(text).find()) {
            patternMatches.add("银行卡号");
        }

        return patternMatches;
    }

    /**
     * 过滤文本中匹配到的敏感词，替换为"***"
     * <p>
     * 按敏感词长度降序排列后替换，避免短词先替换导致长词无法匹配的问题。
     * </p>
     *
     * @param text         原始文本
     * @param matchedWords 匹配到的敏感词列表
     * @return 过滤后的文本
     */
    private String filterMatchedWords(String text, List<String> matchedWords) {
        if (text == null || matchedWords.isEmpty()) {
            return text;
        }

        String result = text;

        // 按敏感词长度降序排列，避免短词替换影响长词匹配
        List<String> sortedWords = matchedWords.stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .distinct()
                .collect(Collectors.toList());

        // 逐个替换敏感词为***
        for (String word : sortedWords) {
            // 使用正则替换，忽略大小写
            result = result.replaceAll("(?i)" + Pattern.quote(word), MASK_REPLACEMENT);
        }

        return result;
    }

    /**
     * 记录安全检查结果到Redis
     * <p>
     * 使用HyperLogLog进行去重近似计数，使用Sorted Set记录匹配词排行，
     * 使用Hash记录分时段统计。
     * </p>
     *
     * @param userId      用户ID
     * @param level       安全等级
     * @param matchedWords 匹配到的敏感词列表
     */
    private void recordSafetyCheck(String userId, SafetyLevel level, List<String> matchedWords) {
        try {
            // 获取当前日期键
            String dateKey = DATE_FORMATTER.format(LocalDate.now());

            // 使用HyperLogLog记录去重用户数
            String totalKey = SAFETY_KEY_PREFIX + dateKey + KEY_TOTAL;
            redisTemplate.opsForHyperLogLog().add(totalKey, userId);

            // 根据等级记录到对应HyperLogLog
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

            // 记录匹配词排行（Sorted Set，分值自增）
            if (!matchedWords.isEmpty()) {
                String wordsKey = SAFETY_KEY_PREFIX + dateKey + KEY_WORDS;
                for (String word : matchedWords) {
                    redisTemplate.opsForZSet().incrementScore(wordsKey, word, 1);
                }
            }

            // 记录分时段统计（Hash，每小时计数+1）
            int currentHour = LocalDateTime.now().getHour();
            String hourlyKey = SAFETY_KEY_PREFIX + dateKey + KEY_HOURLY;
            redisTemplate.opsForHash().increment(hourlyKey, String.valueOf(currentHour), 1);

        } catch (Exception e) {
            // Redis记录失败不影响主流程，仅记录日志
            log.error("记录安全检查数据到Redis失败 | userId={} | level={}", userId, level, e);
        }
    }
}
