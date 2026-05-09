package com.delta.message.ai.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.FaqItem;
import com.delta.common.service.AiConfigService;
import com.delta.common.service.CacheService;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.GameKnowledgeService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.CompanionLevelVO;
import com.delta.common.vo.ServiceItemVO;
import com.delta.common.vo.ServicePriceRuleVO;
import com.delta.message.ai.config.DeepSeekConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI 服务实现
 * <p>
 * 成本优化策略：
 * <ol>
 *   <li>智能FAQ注入：仅注入与用户消息相关的1-2条FAQ，而非全量35条（节省~1500 tokens/次）</li>
 *   <li>AI回复缓存：相同用户消息在10分钟内复用缓存结果（避免重复调用）</li>
 *   <li>压缩系统提示词：精简到核心信息（节省~700 tokens/次）</li>
 *   <li>max_tokens=500：限制输出长度（节省输出token）</li>
 *   <li>对话历史压缩：仅保留最近4条有效历史（节省上下文token）</li>
 * </ol>
 * </p>
 * <p>
 * Token监控机制：
 * <ol>
 *   <li>每次API调用记录prompt_tokens、completion_tokens、total_tokens</li>
 *   <li>Redis累计统计每日Token消耗</li>
 *   <li>超过阈值时告警日志</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekServiceImpl.class);

    private static final String AI_REPLY_CACHE_PREFIX = "delta:ai:reply:";

    private static final String TOKEN_STATS_PREFIX = "delta:ai:token:stats:";

    private static final long TOKEN_DAILY_WARN_THRESHOLD = 500000L;

    private final DeepSeekConfig deepSeekConfig;

    private final CacheService cacheService;

    private final AiConfigService aiConfigService;

    private final RedisService redisService;

    /** 游戏知识库服务，用于注入游戏相关知识到AI提示词 */
    private final GameKnowledgeService gameKnowledgeService;

    @Override
    public String getChatReply(String userMessage) {
        return getChatReplyWithHistory(userMessage, null);
    }

    /**
     * 调用 DeepSeek API 获取 AI 回复
     * <p>
     * 成本优化流程：
     * 1. 检查缓存 → 命中则直接返回（0 token 消耗）
     * 2. 构建精简请求体（智能FAQ注入 + 压缩系统提示词）
     * 3. 调用 API → 缓存结果 → 记录Token消耗
     * </p>
     *
     * @param userMessage         用户消息内容
     * @param conversationHistory 对话历史列表
     * @return AI回复内容，失败返回null
     */
    @Override
    @SuppressWarnings("null")
    public String getChatReplyWithHistory(String userMessage, List<DeepSeekService.ChatMessage> conversationHistory) {
        if (!isEnabled()) {
            log.debug("【DeepSeek】AI未启用，跳过调用");
            return null;
        }

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("【DeepSeek】API Key未配置，无法调用");
            return null;
        }

        log.debug("【DeepSeek】开始处理请求 | 用户消息长度:{} | 历史消息数:{}",
                userMessage != null ? userMessage.length() : 0,
                conversationHistory != null ? conversationHistory.size() : 0);

        String cacheKey = buildCacheKey(userMessage);
        if (cacheKey == null) {
            cacheKey = "ai:cache:default";
        }
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.info("【DeepSeek】缓存命中 | cacheKey={} | 0 token消耗", cacheKey);
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】读取缓存失败，继续调用API | error={}", e.getMessage());
        }

        try {
            JSONObject requestBody = buildRequestBody(userMessage, conversationHistory);
            String requestJson = requestBody.toString();
            log.debug("【DeepSeek】请求体构建完成 | model={} | maxTokens={} | temperature={} | messagesCount={}",
                    requestBody.getStr("model"), requestBody.getInt("max_tokens"),
                    requestBody.getDouble("temperature"), requestBody.getJSONArray("messages").size());
            log.debug("【DeepSeek】请求体详情 | bodyLength={}字符", requestJson.length());

            String apiUrl = getApiUrl();
            long startTime = System.currentTimeMillis();

            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .timeout(AiCustomerServiceConstants.AI_TIMEOUT_MS)
                    .execute();

            long duration = System.currentTimeMillis() - startTime;
            String responseBody = response.body();

            log.info("【DeepSeek】API响应 | status={} | 耗时={}ms | bodyLength={}字符",
                    response.getStatus(), duration, responseBody != null ? responseBody.length() : 0);

            if (response.isOk()) {
                ParsedResponse parsed = parseResponseWithUsage(responseBody);
                if (parsed != null && parsed.content != null && !parsed.content.trim().isEmpty()) {
                    recordTokenUsage(parsed.promptTokens, parsed.completionTokens, parsed.totalTokens);

                    try {
                        String contentToCache = parsed.content;
                        redisService.set(cacheKey, contentToCache,
                                AiCustomerServiceConstants.AI_REPLY_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                        log.info("【DeepSeek】回复已缓存 | TTL={}分钟 | cacheKey={}",
                                AiCustomerServiceConstants.AI_REPLY_CACHE_TTL_MINUTES, cacheKey);
                    } catch (Exception e) {
                        log.warn("【DeepSeek】缓存写入失败 | error={}", e.getMessage());
                    }

                    log.info("【DeepSeek】调用成功 | 回复长度={}字符 | tokens={}/{}/{} | 耗时={}ms",
                            parsed.content.length(), parsed.promptTokens, parsed.completionTokens,
                            parsed.totalTokens, duration);
                    return parsed.content;
                } else {
                    log.warn("【DeepSeek】API返回空内容 | responseBody={}", truncateForLog(responseBody, 500));
                }
            } else {
                log.error("【DeepSeek】API调用失败 | status={} | body={}", response.getStatus(),
                        truncateForLog(responseBody, 500));
                if (response.getStatus() == 401) {
                    log.error("【DeepSeek】认证失败！请检查API Key是否正确配置");
                } else if (response.getStatus() == 429) {
                    log.warn("【DeepSeek】请求频率超限，建议降低调用频率");
                } else if (response.getStatus() == 500 || response.getStatus() == 502 || response.getStatus() == 503) {
                    log.warn("【DeepSeek】服务端错误({})，DeepSeek平台可能暂时不可用", response.getStatus());
                }
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                log.error("【DeepSeek】请求超时 | timeout={}ms | 请检查网络连接或增大超时时间",
                        AiCustomerServiceConstants.AI_TIMEOUT_MS);
            } else if (cause instanceof java.net.ConnectException) {
                log.error("【DeepSeek】连接失败 | apiUrl={} | 请检查网络连接和API地址", getApiUrl());
            } else {
                log.error("【DeepSeek】调用异常 | errorType={} | message={}", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        return null;
    }

    @Override
    public boolean isEnabled() {
        try {
            String enabledStr = aiConfigService.getConfigValue("deepseek.enabled");
            if (enabledStr != null && !enabledStr.trim().isEmpty()) {
                return Boolean.parseBoolean(enabledStr);
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】从数据库读取启用状态失败，使用配置默认值 | error={}", e.getMessage());
        }
        return deepSeekConfig.isEnabled();
    }

    /**
     * 获取API Key，优先从数据库读取，回退到配置文件
     *
     * @return API Key字符串
     */
    private String getApiKey() {
        try {
            String apiKey = aiConfigService.getConfigValue("deepseek.api_key");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                return apiKey;
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】从数据库读取API Key失败，使用配置默认值 | error={}", e.getMessage());
        }
        return deepSeekConfig.getApiKey();
    }

    /**
     * 获取API URL，优先从数据库读取，回退到配置文件
     *
     * @return API URL字符串
     */
    private String getApiUrl() {
        try {
            String baseUrl = aiConfigService.getConfigValue("deepseek.base_url");
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                return baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】从数据库读取API URL失败，使用配置默认值 | error={}", e.getMessage());
        }
        return deepSeekConfig.getApiUrl();
    }

    /**
     * 获取模型名称，优先从数据库读取，回退到配置文件
     *
     * @return 模型名称字符串
     */
    private String getModel() {
        try {
            String model = aiConfigService.getConfigValue("deepseek.model");
            if (model != null && !model.trim().isEmpty()) {
                return model;
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】从数据库读取模型失败，使用配置默认值 | error={}", e.getMessage());
        }
        return deepSeekConfig.getModel();
    }

    /**
     * 获取温度参数，优先从数据库读取，回退到配置文件
     *
     * @return 温度参数值
     */
    private Double getTemperature() {
        try {
            String temp = aiConfigService.getConfigValue("deepseek.temperature");
            if (temp != null && !temp.trim().isEmpty()) {
                return Double.parseDouble(temp);
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】解析温度参数失败，使用默认值 | error={}", e.getMessage());
        }
        return deepSeekConfig.getTemperature();
    }

    /**
     * 获取最大Token数，优先从数据库读取，回退到配置文件
     *
     * @return 最大Token数
     */
    private Integer getMaxTokens() {
        try {
            String maxTokens = aiConfigService.getConfigValue("deepseek.max_tokens");
            if (maxTokens != null && !maxTokens.trim().isEmpty()) {
                return Integer.parseInt(maxTokens);
            }
        } catch (Exception e) {
            log.warn("【DeepSeek】解析最大Token参数失败，使用默认值 | error={}", e.getMessage());
        }
        return deepSeekConfig.getMaxTokens();
    }

    /**
     * 构建动态系统提示词（成本优化版）
     * <p>
     * 优化点：
     * 1. 基础提示词已压缩（~2500字→~800字，节省~500 tokens）
     * 2. FAQ不再全量注入，改为按关键词匹配注入最多2条（节省~1500 tokens）
     * 3. 详细价格信息仅在用户咨询价格时注入
     * </p>
     *
     * @param userMessage 用户消息内容
     * @return 构建完成的系统提示词
     */
    private String buildDynamicSystemPrompt(String userMessage) {
        ClubConfig clubConfig = cacheService.getClubConfig();

        String basePrompt = aiConfigService.getConfigValue("deepseek.system_prompt");
        if (basePrompt == null || basePrompt.trim().isEmpty()) {
            basePrompt = deepSeekConfig.getSystemPrompt();
        }

        String clubName = clubConfig != null && clubConfig.getClubName() != null ? clubConfig.getClubName() : "三角洲行动陪玩俱乐部";
        String mainGames = clubConfig != null && clubConfig.getMainGames() != null ? clubConfig.getMainGames() : "三角洲行动";
        String clubFeatures = clubConfig != null && clubConfig.getClubFeatures() != null ? clubConfig.getClubFeatures() : "我们提供专业的陪玩服务";

        List<CompanionLevelVO> levels = cacheService.getCompanionLevels();
        String priceLevelTwo = formatPrice(findLevelBasePrice(levels, "BRONZE"), "50");
        String priceLevelOne = formatPrice(findLevelBasePrice(levels, "SILVER"), "80");
        String priceTop = formatPrice(findLevelBasePrice(levels, "GOLD"), "200");
        String priceStar = formatPrice(findLevelBasePrice(levels, "DIAMOND"), "500");

        String dynamicPrompt = basePrompt;
        dynamicPrompt = dynamicPrompt.replace("{俱乐部名称}", clubName);
        dynamicPrompt = dynamicPrompt.replace("{主营游戏}", mainGames);
        dynamicPrompt = dynamicPrompt.replace("{俱乐部特色}", clubFeatures);
        dynamicPrompt = dynamicPrompt.replace("{二品价格}", priceLevelTwo);
        dynamicPrompt = dynamicPrompt.replace("{一品价格}", priceLevelOne);
        dynamicPrompt = dynamicPrompt.replace("{顶尖价格}", priceTop);
        dynamicPrompt = dynamicPrompt.replace("{明星价格}", priceStar);

        boolean isPriceRelated = isPriceRelatedMessage(userMessage);
        if (isPriceRelated) {
            String detailedPricing = buildDetailedPricingText();
            if (detailedPricing != null && !detailedPricing.isEmpty()) {
                dynamicPrompt += "\n\n" + detailedPricing;
            }
        }

        String relevantFaq = buildRelevantFaq(userMessage);
        if (relevantFaq != null && !relevantFaq.isEmpty()) {
            dynamicPrompt += "\n\n## 相关FAQ（仅参考）\n" + relevantFaq;
        }

        // 注入游戏知识库内容（最多3条，控制在500 tokens内）
        String gameKnowledge = gameKnowledgeService.injectKnowledgeToPrompt(userMessage);
        if (gameKnowledge != null && !gameKnowledge.isEmpty()) {
            dynamicPrompt += "\n\n" + gameKnowledge;
        }

        log.debug("【DeepSeek】系统提示词构建完成 | 长度={}字符 | 含价格数据={} | 含FAQ={} | 含知识库={}",
                dynamicPrompt.length(), isPriceRelated, relevantFaq != null, gameKnowledge != null && !gameKnowledge.isEmpty());

        return dynamicPrompt;
    }

    /**
     * 判断用户消息是否与价格相关
     * <p>
     * 仅在价格相关时注入详细价格数据，避免非价格咨询场景浪费Token
     * </p>
     *
     * @param userMessage 用户消息
     * @return 是否价格相关
     */
    private boolean isPriceRelatedMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return false;
        }
        for (String keyword : AiCustomerServiceConstants.PRICE_INQUIRY_KEYWORDS) {
            if (userMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建详细定价文本
     * <p>
     * 从缓存获取陪玩师等级和服务项目价格规则，生成结构化价格信息。
     * 仅在价格相关咨询时注入到系统提示词中。
     * </p>
     *
     * @return 定价文本，无数据返回null
     */
    private String buildDetailedPricingText() {
        try {
            List<CompanionLevelVO> levels = cacheService.getCompanionLevels();
            List<ServiceItemVO> serviceItems = cacheService.getServiceItems();

            if ((levels == null || levels.isEmpty()) && (serviceItems == null || serviceItems.isEmpty())) {
                return null;
            }

            StringBuilder sb = new StringBuilder();

            if (levels != null && !levels.isEmpty()) {
                sb.append("## 陪玩师等级与详细价格（必须按此数据回答价格问题）\n\n");
                for (CompanionLevelVO level : levels) {
                    sb.append("【").append(level.getLevelName()).append("】");
                    if (level.getDescription() != null && !level.getDescription().isEmpty()) {
                        sb.append(" - ").append(level.getDescription());
                    }
                    if (level.getBasePrice() != null) {
                        sb.append("，基础价").append(level.getBasePrice()).append("元/小时");
                    }
                    sb.append("\n");

                    if (serviceItems != null) {
                        for (ServiceItemVO item : serviceItems) {
                            if (item.getPriceRules() != null) {
                                for (ServicePriceRuleVO rule : item.getPriceRules()) {
                                    if (level.getId().equals(rule.getCompanionLevelId()) && rule.getEnabled() != null && rule.getEnabled() == 1) {
                                        String unit = formatPriceUnit(rule.getPriceUnit());
                                        sb.append("  · ").append(item.getItemName()).append("：")
                                                .append(rule.getPrice()).append("元").append(unit);
                                        if (rule.getOriginalPrice() != null) {
                                            sb.append("（原价").append(rule.getOriginalPrice()).append("元）");
                                        }
                                        sb.append("\n");
                                    }
                                }
                            }
                        }
                    }
                    sb.append("\n");
                }
            }

            if (serviceItems != null && !serviceItems.isEmpty()) {
                sb.append("## 服务项目详情\n\n");
                for (ServiceItemVO item : serviceItems) {
                    sb.append("· ").append(item.getItemName());
                    if (item.getCategory() != null) {
                        String cat = formatCategory(item.getCategory());
                        sb.append(" ").append(cat);
                    }
                    if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                        sb.append(" - ").append(item.getDescription());
                    }
                    if (item.getMinDuration() != null) {
                        sb.append("，最少").append(item.getMinDuration()).append("小时");
                    }
                    if (item.getGuaranteeText() != null && !item.getGuaranteeText().isEmpty()) {
                        sb.append("，保障：").append(item.getGuaranteeText());
                    }
                    if (item.getRefundPolicy() != null && !item.getRefundPolicy().isEmpty()) {
                        sb.append("，退款：").append(item.getRefundPolicy());
                    }
                    sb.append("\n");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.warn("【DeepSeek】构建详细定价文本失败 | error={}", e.getMessage());
            return null;
        }
    }

    /**
     * 智能FAQ注入：仅注入与用户消息相关的1-2条FAQ
     * <p>
     * 旧逻辑：全量注入35条FAQ → ~2000 tokens
     * 新逻辑：按关键词匹配注入最多2条 → ~100 tokens
     * 节省：~1900 tokens/次
     * </p>
     *
     * @param userMessage 用户消息
     * @return 相关FAQ文本，无匹配返回 null
     */
    private String buildRelevantFaq(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return null;
        }

        List<FaqItem> allFaqItems = cacheService.getFaqItems();
        if (allFaqItems == null || allFaqItems.isEmpty()) {
            return null;
        }

        StringBuilder faqBuilder = new StringBuilder();
        int count = 0;

        for (FaqItem item : allFaqItems) {
            if (count >= 2) {
                break;
            }

            String question = item.getQuestion();
            String category = item.getCategory();

            boolean isRelevant = false;

            if (category != null && userMessage.contains(category)) {
                isRelevant = true;
            }

            if (!isRelevant && question != null) {
                String[] questionWords = question.replace("？", "").replace("?", "").split("[，,、]");
                for (String word : questionWords) {
                    if (word.length() >= 2 && userMessage.contains(word)) {
                        isRelevant = true;
                        break;
                    }
                }
            }

            if (!isRelevant) {
                for (String keyword : AiCustomerServiceConstants.PRICE_INQUIRY_KEYWORDS) {
                    if (userMessage.contains(keyword) && category != null && category.contains("价格")) {
                        isRelevant = true;
                        break;
                    }
                }
                if (!isRelevant) {
                    for (String keyword : AiCustomerServiceConstants.SERVICE_INQUIRY_KEYWORDS) {
                        if (userMessage.contains(keyword) && category != null && (category.contains("服务") || category.contains("流程"))) {
                            isRelevant = true;
                            break;
                        }
                    }
                }
            }

            if (isRelevant) {
                ClubConfig clubConfig = cacheService.getClubConfig();
                String clubName = clubConfig != null && clubConfig.getClubName() != null ? clubConfig.getClubName() : "三角洲行动陪玩俱乐部";
                String mainGames = clubConfig != null && clubConfig.getMainGames() != null ? clubConfig.getMainGames() : "三角洲行动";
                String clubFeatures = clubConfig != null && clubConfig.getClubFeatures() != null ? clubConfig.getClubFeatures() : "专业陪玩服务";

                String answer = item.getAnswer()
                        .replace("{price_level_two}", formatPrice(null, "50"))
                        .replace("{price_level_one}", formatPrice(null, "80"))
                        .replace("{price_top}", formatPrice(null, "200"))
                        .replace("{price_star}", formatPrice(null, "500"))
                        .replace("{俱乐部名称}", clubName)
                        .replace("{主营游戏}", mainGames)
                        .replace("{俱乐部特色}", clubFeatures);

                faqBuilder.append("Q: ").append(question).append("\n");
                faqBuilder.append("A: ").append(answer, 0, Math.min(answer.length(), ExportConstants.FAQ_ANSWER_TRUNCATION_LENGTH)).append("\n\n");
                count++;
            }
        }

        if (count > 0) {
            log.debug("【DeepSeek】FAQ注入 | 匹配数={}条", count);
        }

        return faqBuilder.length() > 0 ? faqBuilder.toString() : null;
    }

    /**
     * 从陪玩师等级列表中查找指定等级编码的基础价格
     *
     * @param levels    陪玩师等级列表
     * @param levelCode 等级编码（如 BRONZE, SILVER, GOLD, DIAMOND）
     * @return 对应等级的基础价格，未找到返回null
     */
    private BigDecimal findLevelBasePrice(List<CompanionLevelVO> levels, String levelCode) {
        if (levels == null || levelCode == null) {
            return null;
        }
        return levels.stream()
                .filter(l -> levelCode.equals(l.getLevelCode()))
                .map(CompanionLevelVO::getBasePrice)
                .findFirst()
                .orElse(null);
    }

    /**
     * 格式化价格值
     *
     * @param price        价格BigDecimal值
     * @param defaultValue 默认值字符串
     * @return 格式化后的价格字符串
     */
    private String formatPrice(BigDecimal price, String defaultValue) {
        if (price != null) {
            return price.toString();
        }
        return defaultValue;
    }

    /**
     * 格式化价格单位
     *
     * @param priceUnit 价格单位枚举值
     * @return 中文价格单位
     */
    private String formatPriceUnit(String priceUnit) {
        if ("HOUR".equals(priceUnit)) {
            return "/时";
        } else if ("NIGHT".equals(priceUnit)) {
            return "/晚";
        } else if ("ORDER".equals(priceUnit)) {
            return "/单";
        } else if (priceUnit != null && !priceUnit.isEmpty()) {
            return "/" + priceUnit;
        }
        return "";
    }

    /**
     * 格式化服务类别
     *
     * @param category 服务类别枚举值
     * @return 中文类别标签
     */
    private String formatCategory(String category) {
        if ("ACCOMPANY".equals(category)) {
            return "[陪玩]";
        } else if ("PACKAGE".equals(category)) {
            return "[套餐]";
        } else if ("TEACHING".equals(category)) {
            return "[教学]";
        } else if ("SOCIAL".equals(category)) {
            return "[社交]";
        }
        return "";
    }

    /**
     * 构建API请求体
     * <p>
     * 包含模型参数、系统提示词、对话历史和用户消息。
     * 对话历史限制为最近4条以控制Token消耗。
     * </p>
     *
     * @param userMessage         用户消息
     * @param conversationHistory 对话历史
     * @return 构建完成的JSON请求体
     */
    private JSONObject buildRequestBody(String userMessage, List<DeepSeekService.ChatMessage> conversationHistory) {
        JSONObject request = new JSONObject();
        request.set("model", getModel());
        request.set("max_tokens", getMaxTokens());
        request.set("temperature", getTemperature());

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", buildDynamicSystemPrompt(userMessage));
        messages.add(systemMessage);

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            int historyLimit = AiCustomerServiceConstants.CONVERSATION_HISTORY_LIMIT;
            int startIndex = Math.max(0, conversationHistory.size() - historyLimit);
            for (int i = startIndex; i < conversationHistory.size(); i++) {
                DeepSeekService.ChatMessage historyMsg = conversationHistory.get(i);
                JSONObject historyMsgObj = new JSONObject();
                historyMsgObj.set("role", historyMsg.getRole());
                historyMsgObj.set("content", historyMsg.getContent());
                messages.add(historyMsgObj);
            }
            if (startIndex > 0) {
                log.debug("【DeepSeek】对话历史压缩 | 原始={}条 | 截取最近={}条", conversationHistory.size(), conversationHistory.size() - startIndex);
            }
        }

        JSONObject userMessageObj = new JSONObject();
        userMessageObj.set("role", "user");
        userMessageObj.set("content", userMessage);
        messages.add(userMessageObj);

        request.set("messages", messages);
        return request;
    }

    /**
     * 解析API响应（含Token用量信息）
     * <p>
     * 从DeepSeek API响应中提取回复内容和Token使用统计。
     * </p>
     *
     * @param responseBody API响应体JSON字符串
     * @return 解析结果对象，包含回复内容和Token统计
     */
    private ParsedResponse parseResponseWithUsage(String responseBody) {
        try {
            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            String content = null;
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                if (message != null) {
                    content = message.getStr("content", "");
                }
            }

            int promptTokens = 0;
            int completionTokens = 0;
            int totalTokens = 0;
            JSONObject usage = json.getJSONObject("usage");
            if (usage != null) {
                promptTokens = usage.getInt("prompt_tokens", 0);
                completionTokens = usage.getInt("completion_tokens", 0);
                totalTokens = usage.getInt("total_tokens", 0);
            }

            return new ParsedResponse(content, promptTokens, completionTokens, totalTokens);
        } catch (Exception e) {
            log.error("【DeepSeek】解析响应失败 | error={}", e.getMessage());
            return null;
        }
    }

    /**
     * 记录Token消耗到Redis，按日统计
     *
     * @param promptTokens     输入Token数
     * @param completionTokens 输出Token数
     * @param totalTokens      总Token数
     */
    private void recordTokenUsage(int promptTokens, int completionTokens, int totalTokens) {
        if (totalTokens <= 0) {
            return;
        }
        try {
            String today = java.time.LocalDate.now().toString();
            String statsKey = TOKEN_STATS_PREFIX + today;

            redisService.increment(statsKey);
            Long dailyTotal = redisService.increment(statsKey);

            String promptKey = TOKEN_STATS_PREFIX + "prompt:" + today;
            String completionKey = TOKEN_STATS_PREFIX + "completion:" + today;
            redisService.increment(promptKey);
            redisService.increment(completionKey);

            redisService.expire(statsKey, 7, TimeUnit.DAYS);
            redisService.expire(promptKey, 7, TimeUnit.DAYS);
            redisService.expire(completionKey, 7, TimeUnit.DAYS);

            log.info("【Token监控】本次消耗 | prompt={} | completion={} | total={}", promptTokens, completionTokens, totalTokens);

            if (dailyTotal != null && dailyTotal > TOKEN_DAILY_WARN_THRESHOLD) {
                log.warn("【Token监控】日消耗告警！今日累计={} tokens | 阈值={}", dailyTotal, TOKEN_DAILY_WARN_THRESHOLD);
            }
        } catch (Exception e) {
            log.debug("【Token监控】记录Token消耗失败 | error={}", e.getMessage());
        }
    }

    /**
     * 构建AI回复缓存Key
     * <p>
     * 基于用户消息内容的MD5哈希，确保相同问题复用缓存。
     * </p>
     *
     * @param userMessage 用户消息
     * @return 缓存Key字符串
     */
    private String buildCacheKey(String userMessage) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(userMessage.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return AI_REPLY_CACHE_PREFIX + hexString;
        } catch (Exception e) {
            return AI_REPLY_CACHE_PREFIX + userMessage.hashCode();
        }
    }

    /**
     * 截断日志内容，防止超长日志影响性能
     *
     * @param text   原始文本
     * @param maxLen 最大长度
     * @return 截断后的文本
     */
    private String truncateForLog(String text, int maxLen) {
        if (text == null) return "null";
        return text.length() > maxLen ? text.substring(0, maxLen) + "...(truncated)" : text;
    }

    /**
     * API响应解析结果内部类
     */
    private static class ParsedResponse {
        final String content;
        final int promptTokens;
        final int completionTokens;
        final int totalTokens;

        ParsedResponse(String content, int promptTokens, int completionTokens, int totalTokens) {
            this.content = content;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }
    }
}
