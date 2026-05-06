package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.GameKnowledge;
import com.delta.common.mapper.GameKnowledgeMapper;
import com.delta.common.service.GameKnowledgeService;
import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 游戏知识库服务实现
 * <p>
 * 提供三角洲行动（Delta Force）游戏知识库的完整服务实现，包括：
 * <ol>
 *   <li>关键词全文搜索 - 基于MyBatis Plus LambdaQueryWrapper + like模糊匹配</li>
 *   <li>分类搜索 - 按游戏规则/角色攻略/操作指南/故障排除分类查询</li>
 *   <li>AI提示词注入 - 智能匹配最多3条知识注入到AI系统提示词中，控制在500 tokens内</li>
 *   <li>Redis热点缓存 - 缓存热点知识减少数据库查询（delta:game:knowledge:hot:{keyword}，TTL 30分钟）</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class GameKnowledgeServiceImpl implements GameKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(GameKnowledgeServiceImpl.class);

    /** 游戏知识库Mapper */
    private final GameKnowledgeMapper gameKnowledgeMapper;

    /** Redis缓存服务 */
    private final RedisService redisService;

    /** Redis热点知识缓存Key前缀 */
    private static final String HOT_KEY_PREFIX = "delta:game:knowledge:hot:";

    /** 热点知识缓存TTL（分钟） */
    private static final long HOT_KEY_TTL_MINUTES = 30;

    /** AI提示词注入最大条目数 */
    private static final int MAX_INJECT_COUNT = 3;

    /** AI提示词注入最大Token估算长度（中文约1字符≈1.5 token，取保守值） */
    private static final int MAX_INJECT_CHARS = 350;

    /** 知识库分类常量 */
    public static final String CATEGORY_GAME_RULES = "GAME_RULES";
    public static final String CATEGORY_CHARACTER_GUIDES = "CHARACTER_GUIDES";
    public static final String CATEGORY_OPERATION_GUIDES = "OPERATION_GUIDES";
    public static final String CATEGORY_TROUBLESHOOTING = "TROUBLESHOOTING";

    /**
     * 基于关键词的全文搜索
     * <p>
     * 搜索策略（多级匹配）：
     * 1. 精确匹配keywords字段（权重最高）
     * 2. 模糊匹配title字段
     * 3. 模糊匹配content字段
     * 结果优先展示标题匹配项，按reliability降序排列。
     * 仅查询enabled=1的启用状态知识条目。
     * </p>
     *
     * @param query 搜索关键词
     * @return 匹配的知识条目列表，无结果返回空列表
     */
    @Override
    public List<GameKnowledge> searchKnowledge(String query) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("【知识库】搜索关键词为空，返回空列表");
            return Collections.emptyList();
        }

        String keyword = query.trim();
        log.debug("【知识库】开始搜索 | 关键词={}", keyword);

        // 构建查询条件：keywords like or title like or content like，且enabled=1
        LambdaQueryWrapper<GameKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(GameKnowledge::getKeywords, keyword)
                .or()
                .like(GameKnowledge::getTitle, keyword)
                .or()
                .like(GameKnowledge::getContent, keyword)
        );
        // 仅查询已启用的知识条目
        wrapper.eq(GameKnowledge::getEnabled, 1);
        // 按可靠性降序排列
        wrapper.orderByDesc(GameKnowledge::getReliability);

        List<GameKnowledge> results = gameKnowledgeMapper.selectList(wrapper);

        log.info("【知识库】搜索完成 | 关键词={} | 匹配数={}", keyword, results.size());
        return results;
    }

    /**
     * 按分类搜索知识库
     * <p>
     * 根据分类名称精确匹配，仅查询enabled=1的启用状态条目。
     * 结果按reliability降序排列。
     * </p>
     *
     * @param category 知识分类，如GAME_RULES、CHARACTER_GUIDES等
     * @return 该分类下的知识条目列表，无结果返回空列表
     */
    @Override
    public List<GameKnowledge> searchByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            log.debug("【知识库】分类参数为空，返回空列表");
            return Collections.emptyList();
        }

        log.debug("【知识库】按分类搜索 | 分类={}", category);

        // 构建查询条件：分类精确匹配 + 启用状态 + 可靠性排序
        LambdaQueryWrapper<GameKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameKnowledge::getCategory, category.trim());
        wrapper.eq(GameKnowledge::getEnabled, 1);
        wrapper.orderByDesc(GameKnowledge::getReliability);

        List<GameKnowledge> results = gameKnowledgeMapper.selectList(wrapper);

        log.info("【知识库】分类搜索完成 | 分类={} | 匹配数={}", category, results.size());
        return results;
    }

    /**
     * 获取游戏规则类知识
     * <p>
     * 先按分类筛选GAME_RULES，再根据游戏类型(gameType)匹配标题或关键词。
     * 仅查询enabled=1的启用状态条目。
     * </p>
     *
     * @param gameType 游戏类型，如"三角洲行动"
     * @return 游戏规则知识列表，无结果返回空列表
     */
    @Override
    public List<GameKnowledge> getGameRules(String gameType) {
        if (gameType == null || gameType.trim().isEmpty()) {
            log.debug("【知识库】游戏类型参数为空，返回空列表");
            return Collections.emptyList();
        }

        log.debug("【知识库】获取游戏规则 | 游戏类型={}", gameType);

        // 构建查询条件：分类=GAME_RULES + 标题/关键词包含游戏类型 + 启用状态
        LambdaQueryWrapper<GameKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameKnowledge::getCategory, CATEGORY_GAME_RULES);
        wrapper.and(w -> w
                .like(GameKnowledge::getTitle, gameType)
                .or()
                .like(GameKnowledge::getKeywords, gameType)
        );
        wrapper.eq(GameKnowledge::getEnabled, 1);
        wrapper.orderByDesc(GameKnowledge::getReliability);

        List<GameKnowledge> results = gameKnowledgeMapper.selectList(wrapper);

        log.info("【知识库】游戏规则查询完成 | 游戏类型={} | 匹配数={}", gameType, results.size());
        return results;
    }

    /**
     * 获取角色攻略类知识
     * <p>
     * 先按分类筛选CHARACTER_GUIDES，再根据游戏类型匹配标题或关键词。
     * 仅查询enabled=1的启用状态条目。
     * </p>
     *
     * @param gameType 游戏类型，如"三角洲行动"
     * @return 角色攻略知识列表，无结果返回空列表
     */
    @Override
    public List<GameKnowledge> getCharacterGuides(String gameType) {
        if (gameType == null || gameType.trim().isEmpty()) {
            log.debug("【知识库】游戏类型参数为空，返回空列表");
            return Collections.emptyList();
        }

        log.debug("【知识库】获取角色攻略 | 游戏类型={}", gameType);

        // 构建查询条件：分类=CHARACTER_GUIDES + 标题/关键词包含游戏类型 + 启用状态
        LambdaQueryWrapper<GameKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameKnowledge::getCategory, CATEGORY_CHARACTER_GUIDES);
        wrapper.and(w -> w
                .like(GameKnowledge::getTitle, gameType)
                .or()
                .like(GameKnowledge::getKeywords, gameType)
        );
        wrapper.eq(GameKnowledge::getEnabled, 1);
        wrapper.orderByDesc(GameKnowledge::getReliability);

        List<GameKnowledge> results = gameKnowledgeMapper.selectList(wrapper);

        log.info("【知识库】角色攻略查询完成 | 游戏类型={} | 匹配数={}", gameType, results.size());
        return results;
    }

    /**
     * 获取故障排除指南
     * <p>
     * 先按分类筛选TROUBLESHOOTING，再按关键词匹配标题或内容。
     * 仅查询enabled=1的启用状态条目。
     * </p>
     *
     * @param keyword 故障关键词，如"崩溃"、"连接失败"、"卡顿"
     * @return 故障排除指南列表，无结果返回空列表
     */
    @Override
    public List<GameKnowledge> getTroubleshooting(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("【知识库】故障关键词为空，返回空列表");
            return Collections.emptyList();
        }

        log.debug("【知识库】获取故障排除 | 关键词={}", keyword);

        // 构建查询条件：分类=TROUBLESHOOTING + 标题/关键词/内容模糊匹配 + 启用状态
        LambdaQueryWrapper<GameKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameKnowledge::getCategory, CATEGORY_TROUBLESHOOTING);
        wrapper.and(w -> w
                .like(GameKnowledge::getTitle, keyword)
                .or()
                .like(GameKnowledge::getKeywords, keyword)
                .or()
                .like(GameKnowledge::getContent, keyword)
        );
        wrapper.eq(GameKnowledge::getEnabled, 1);
        wrapper.orderByDesc(GameKnowledge::getReliability);

        List<GameKnowledge> results = gameKnowledgeMapper.selectList(wrapper);

        log.info("【知识库】故障排除查询完成 | 关键词={} | 匹配数={}", keyword, results.size());
        return results;
    }

    /**
     * 将相关知识注入AI提示词
     * <p>
     * 核心流程：
     * <ol>
     *   <li>提取用户消息中的关键词</li>
     *   <li>优先查询Redis热点缓存（delta:game:knowledge:hot:{keyword}）</li>
     *   <li>缓存未命中则从数据库匹配知识</li>
     *   <li>最多注入3条知识，总字符数控制在350以内（约500 tokens）</li>
     *   <li>将匹配结果写入Redis缓存（TTL 30分钟）</li>
     * </ol>
     * </p>
     *
     * @param userMessage 用户消息内容
     * @return 注入用的知识文本，无匹配返回空字符串
     */
    @Override
    public String injectKnowledgeToPrompt(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "";
        }

        log.debug("【知识库】开始AI提示词注入 | 用户消息长度={}", userMessage.length());

        // 第一步：提取用户消息中的关键词（用于缓存Key构建）
        String cacheKey = buildHotCacheKey(userMessage);

        // 第二步：尝试从Redis热点缓存获取
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                String cachedKnowledge = cached.toString();
                log.info("【知识库】热点缓存命中 | cacheKey={} | 知识长度={}", cacheKey, cachedKnowledge.length());
                return cachedKnowledge;
            }
        } catch (Exception e) {
            log.warn("【知识库】读取热点缓存失败 | cacheKey={} | error={}", cacheKey, e.getMessage());
        }

        // 第三步：从数据库搜索匹配的知识条目（使用关键词全文搜索）
        List<GameKnowledge> matched = searchKnowledge(userMessage);

        if (matched.isEmpty()) {
            log.debug("【知识库】无匹配知识条目");
            return "";
        }

        // 第四步：筛选最多3条，控制在350字符以内
        String injectText = buildInjectText(matched);

        // 第五步：写入Redis热点缓存
        if (!injectText.isEmpty()) {
            try {
                redisService.set(cacheKey, injectText, HOT_KEY_TTL_MINUTES, TimeUnit.MINUTES);
                log.info("【知识库】热点知识已缓存 | cacheKey={} | TTL={}分钟 | 知识长度={}",
                        cacheKey, HOT_KEY_TTL_MINUTES, injectText.length());
            } catch (Exception e) {
                log.warn("【知识库】写入热点缓存失败 | cacheKey={} | error={}", cacheKey, e.getMessage());
            }
        }

        return injectText;
    }

    /**
     * 获取所有知识分类列表
     * <p>
     * 从已启用的知识条目中提取去重的分类名称。
     * </p>
     *
     * @return 分类名称列表，无数据返回空列表
     */
    @Override
    public List<String> getAllCategories() {
        // 查询所有已启用的知识条目
        LambdaQueryWrapper<GameKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameKnowledge::getEnabled, 1);
        wrapper.select(GameKnowledge::getCategory);
        wrapper.groupBy(GameKnowledge::getCategory);

        List<GameKnowledge> list = gameKnowledgeMapper.selectList(wrapper);
        List<String> categories = list.stream()
                .map(GameKnowledge::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        log.debug("【知识库】获取分类列表 | 分类数={}", categories.size());
        return categories;
    }

    /**
     * 构建热点缓存Key
     * <p>
     * 对用户消息进行关键词提取，生成标准化缓存Key。
     * 策略：取消息中长度最长的有意义词汇作为关键词。
     * </p>
     *
     * @param userMessage 用户消息
     * @return Redis缓存Key
     */
    private String buildHotCacheKey(String userMessage) {
        // 提取核心关键词：去除标点符号，取前50个字符作为Key后缀
        String stripped = userMessage.replaceAll("[\\p{P}\\p{S}\\s]+", "");
        if (stripped.length() > 50) {
            stripped = stripped.substring(0, 50);
        }
        return HOT_KEY_PREFIX + stripped.hashCode();
    }

    /**
     * 构建AI注入知识文本
     * <p>
     * 从匹配的知识条目中选取最多3条，拼接为结构化文本。
     * 总长度控制在MAX_INJECT_CHARS以内，超出部分截断。
     * 优先选取reliability最高的条目。
     * </p>
     *
     * @param matched 匹配的知识条目列表
     * @return 注入知识文本
     */
    private String buildInjectText(List<GameKnowledge> matched) {
        // 按reliability降序排序后取前3条
        List<GameKnowledge> topKnowledge = matched.stream()
                .filter(k -> k.getContent() != null && !k.getContent().isEmpty())
                .sorted(Comparator.comparing(
                        k -> k.getReliability() != null ? k.getReliability() : "",
                        Comparator.reverseOrder()))
                .limit(MAX_INJECT_COUNT)
                .collect(Collectors.toList());

        if (topKnowledge.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## 游戏相关知识（来自知识库）\n\n");

        int currentLength = sb.length();

        for (int i = 0; i < topKnowledge.size(); i++) {
            GameKnowledge knowledge = topKnowledge.get(i);
            String title = knowledge.getTitle() != null ? knowledge.getTitle() : "";
            String content = knowledge.getContent() != null ? knowledge.getContent() : "";

            // 构建单条知识文本
            String entry = (i + 1) + ". " + title + "：" + content + "\n";

            // 检查是否超出字符限制
            if (currentLength + entry.length() > MAX_INJECT_CHARS + sb.length()) {
                // 超出限制，截断当前条目内容
                int remaining = MAX_INJECT_CHARS + sb.length() - currentLength;
                if (remaining > 50) {
                    String truncatedContent = content.substring(0, Math.min(remaining - title.length() - 10, content.length()));
                    entry = (i + 1) + ". " + title + "：" + truncatedContent + "...\n";
                    sb.append(entry);
                }
                break;
            }

            sb.append(entry);
            currentLength += entry.length();
        }

        log.debug("【知识库】注入文本构建完成 | 条目数={} | 文本长度={}", topKnowledge.size(), sb.length());

        return sb.toString();
    }
}
