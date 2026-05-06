package com.delta.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.Keyword;
import com.delta.common.mapper.KeywordMapper;
import com.delta.common.service.ContentSafetyService.SafetyCategory;
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.api.IWordTag;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.tag.WordTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 敏感词过滤配置类
 * <p>
 * 基于开源敏感词库 sensitive-word（houbb/sensitive-word），
 * 使用 DFA 算法实现高性能敏感词匹配。
 * 集成内置6万+词库，同时支持从数据库 keywords 表加载自定义敏感词。
 * 内置标签分类映射到系统 SafetyCategory 枚举。
 * </p>
 *
 * @author 刘建国
 */
@Configuration
public class SensitiveWordConfig {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordConfig.class);

    /**
     * 敏感词数字标签到 SafetyCategory 的映射
     * <p>
     * sensitive-word 系统内置标签：
     * 0-政治、1-毒品、2-色情、3-赌博、4-违法
     * </p>
     */
    private static final Map<String, SafetyCategory> TAG_TO_CATEGORY = new LinkedHashMap<>();

    static {
        TAG_TO_CATEGORY.put("0", SafetyCategory.POLITICAL);
        TAG_TO_CATEGORY.put("1", SafetyCategory.VIOLENCE);
        TAG_TO_CATEGORY.put("2", SafetyCategory.ADULT);
        TAG_TO_CATEGORY.put("3", SafetyCategory.FRAUD);
        TAG_TO_CATEGORY.put("4", SafetyCategory.FRAUD);
    }

    /**
     * 敏感词类别字符串到 SafetyCategory 的映射（用于数据库词库）
     */
    public static final Map<String, SafetyCategory> DB_TYPE_TO_CATEGORY = new LinkedHashMap<>();

    static {
        DB_TYPE_TO_CATEGORY.put("SENSITIVE_POLITICAL", SafetyCategory.POLITICAL);
        DB_TYPE_TO_CATEGORY.put("SENSITIVE_ADULT", SafetyCategory.ADULT);
        DB_TYPE_TO_CATEGORY.put("SENSITIVE_VIOLENCE", SafetyCategory.VIOLENCE);
        DB_TYPE_TO_CATEGORY.put("SENSITIVE_FRAUD", SafetyCategory.FRAUD);
        DB_TYPE_TO_CATEGORY.put("SENSITIVE_HARASSMENT", SafetyCategory.HARASSMENT);
    }

    /**
     * 根据数字标签获取对应的 SafetyCategory
     *
     * @param tag 数字标签字符串（"0"~"4"）
     * @return 对应的 SafetyCategory，未匹配返回null
     */
    public static SafetyCategory getCategoryByTag(String tag) {
        return TAG_TO_CATEGORY.get(tag);
    }

    /**
     * 配置 SensitiveWordBs Bean
     * <p>
     * 启用特性：
     * - 忽略大小写（ignoreCase）
     * - 忽略全角/半角（ignoreWidth）
     * - 忽略繁简体（ignoreChineseStyle）
     * - 忽略英文变体（ignoreEnglishStyle）
     * - 数字检测（enableNumCheck）
     * - 系统内置标签分类（wordTag）
     * - 默认词库 + 数据库自定义词库（wordDeny chains）
     * - 默认白名单 + 数据库白名单（wordAllow chains）
     * </p>
     *
     * @param keywordMapper 关键词数据访问层，用于加载数据库自定义敏感词
     * @return SensitiveWordBs 实例
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs(KeywordMapper keywordMapper) {
        // 数据库自定义敏感词（黑名单）
        IWordDeny dbWordDeny = () -> loadDbDenyWords(keywordMapper);
        // 数据库白名单
        IWordAllow dbWordAllow = () -> loadDbAllowWords(keywordMapper);

        SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance()
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .ignoreRepeat(false)
                .enableNumCheck(true)
                .enableEmailCheck(false)
                .enableUrlCheck(false)
                .enableWordCheck(true)
                .wordTag(WordTags.defaults())
                .wordDeny(WordDenys.chains(WordDenys.defaults(), dbWordDeny))
                .wordAllow(WordAllows.chains(WordAllows.defaults(), dbWordAllow))
                .init();

        log.info("敏感词过滤引擎初始化完成");
        return sensitiveWordBs;
    }

    /**
     * 从数据库加载黑名单敏感词
     * <p>
     * 查询 keywords 表中 action_type='BLOCK' 且 enabled=true 的关键词，
     * 排除已存在于开源词库中的重复词。
     * </p>
     *
     * @param keywordMapper 关键词Mapper
     * @return 敏感词列表
     */
    private List<String> loadDbDenyWords(KeywordMapper keywordMapper) {
        try {
            LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Keyword::getEnabled, true)
                   .eq(Keyword::getActionType, "BLOCK")
                   .orderByDesc(Keyword::getPriority);

            List<Keyword> keywords = keywordMapper.selectList(wrapper);

            List<String> words = keywords.stream()
                    .map(Keyword::getKeyword)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            log.info("从数据库加载黑名单敏感词: {} 条", words.size());
            return words;
        } catch (Exception e) {
            log.error("从数据库加载黑名单敏感词失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 从数据库加载白名单（放行词）
     * <p>
     * 查询 keywords 表中 action_type='ALLOW' 且 enabled=true 的关键词。
     * </p>
     *
     * @param keywordMapper 关键词Mapper
     * @return 白名单列表
     */
    private List<String> loadDbAllowWords(KeywordMapper keywordMapper) {
        try {
            LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Keyword::getEnabled, true)
                   .eq(Keyword::getActionType, "ALLOW");

            List<Keyword> keywords = keywordMapper.selectList(wrapper);

            List<String> words = keywords.stream()
                    .map(Keyword::getKeyword)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            log.info("从数据库加载白名单: {} 条", words.size());
            return words;
        } catch (Exception e) {
            log.error("从数据库加载白名单失败", e);
            return Collections.emptyList();
        }
    }
}
