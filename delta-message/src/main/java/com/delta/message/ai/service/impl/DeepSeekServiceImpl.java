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
import com.delta.common.service.RedisService;
import com.delta.common.vo.CompanionLevelVO;
import com.delta.common.vo.ServiceItemVO;
import com.delta.common.vo.ServicePriceRuleVO;
import com.delta.message.ai.config.DeepSeekConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * </ol>
 * </p>
 *
 * @author delta
 */
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekServiceImpl.class);

    private static final String AI_REPLY_CACHE_PREFIX = "delta:ai:reply:";

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private RedisService redisService;

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
     * 3. 调用 API → 缓存结果
     * </p>
     */
    @Override
    public String getChatReplyWithHistory(String userMessage, List<DeepSeekService.ChatMessage> conversationHistory) {
        if (!isEnabled()) {
            log.debug("DeepSeek AI 未启用，返回 null");
            return null;
        }

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("DeepSeek API Key 未配置，返回 null");
            return null;
        }

        String cacheKey = buildCacheKey(userMessage);
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.info("【AI缓存命中】复用缓存回复，0 token消耗");
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("读取AI回复缓存失败，继续调用API", e);
        }

        try {
            JSONObject requestBody = buildRequestBody(userMessage, conversationHistory);
            log.info("调用 DeepSeek API，历史消息数: {}",
                    conversationHistory != null ? conversationHistory.size() : 0);

            String apiUrl = getApiUrl();
            long startTime = System.currentTimeMillis();

            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(AiCustomerServiceConstants.AI_TIMEOUT_MS)
                    .execute();

            long duration = System.currentTimeMillis() - startTime;
            log.info("DeepSeek API 耗时: {}ms", duration);

            String responseBody = response.body();

            if (response.isOk()) {
                String aiReply = parseResponse(responseBody);
                if (aiReply != null && !aiReply.trim().isEmpty()) {
                    try {
                        redisService.set(cacheKey, aiReply,
                                AiCustomerServiceConstants.AI_REPLY_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                        log.info("【AI回复已缓存】TTL={}分钟", AiCustomerServiceConstants.AI_REPLY_CACHE_TTL_MINUTES);
                    } catch (Exception e) {
                        log.warn("缓存AI回复失败", e);
                    }
                    return aiReply;
                }
            } else {
                log.error("DeepSeek API 调用失败: {}", responseBody);
            }
        } catch (Exception e) {
            log.error("调用 DeepSeek API 异常", e);
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
            log.warn("从数据库读取启用状态失败，默认启用DeepSeek", e);
        }
        return true;
    }

    private String getApiKey() {
        try {
            String apiKey = aiConfigService.getConfigValue("deepseek.api_key");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                return apiKey;
            }
        } catch (Exception e) {
            log.warn("从数据库读取API Key失败，使用配置默认值", e);
        }
        return deepSeekConfig.getApiKey();
    }

    private String getApiUrl() {
        try {
            String baseUrl = aiConfigService.getConfigValue("deepseek.base_url");
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                return baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
            }
        } catch (Exception e) {
            log.warn("从数据库读取API URL失败，使用配置默认值", e);
        }
        return deepSeekConfig.getApiUrl();
    }

    private String getModel() {
        try {
            String model = aiConfigService.getConfigValue("deepseek.model");
            if (model != null && !model.trim().isEmpty()) {
                return model;
            }
        } catch (Exception e) {
            log.warn("从数据库读取模型失败，使用配置默认值", e);
        }
        return deepSeekConfig.getModel();
    }

    private Double getTemperature() {
        try {
            String temp = aiConfigService.getConfigValue("deepseek.temperature");
            if (temp != null && !temp.trim().isEmpty()) {
                return Double.parseDouble(temp);
            }
        } catch (Exception e) {
            log.warn("解析温度参数失败，使用默认值", e);
        }
        return deepSeekConfig.getTemperature();
    }

    private Integer getMaxTokens() {
        try {
            String maxTokens = aiConfigService.getConfigValue("deepseek.max_tokens");
            if (maxTokens != null && !maxTokens.trim().isEmpty()) {
                return Integer.parseInt(maxTokens);
            }
        } catch (Exception e) {
            log.warn("解析最大Token参数失败，使用默认值", e);
        }
        return deepSeekConfig.getMaxTokens();
    }

    /**
     * 构建动态系统提示词（成本优化版）
     * <p>
     * 优化点：
     * 1. 基础提示词已压缩（~2500字→~800字，节省~500 tokens）
     * 2. FAQ不再全量注入，改为按关键词匹配注入最多2条（节省~1500 tokens）
     * </p>
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
        String priceLevelTwo = formatPrice(clubConfig != null ? clubConfig.getPriceLevelTwo() : null, "50");
        String priceLevelOne = formatPrice(clubConfig != null ? clubConfig.getPriceLevelOne() : null, "80");
        String priceTop = formatPrice(clubConfig != null ? clubConfig.getPriceTop() : null, "200");
        String priceStar = formatPrice(clubConfig != null ? clubConfig.getPriceStar() : null, "500");

        String dynamicPrompt = basePrompt;
        dynamicPrompt = dynamicPrompt.replace("{俱乐部名称}", clubName);
        dynamicPrompt = dynamicPrompt.replace("{主营游戏}", mainGames);
        dynamicPrompt = dynamicPrompt.replace("{俱乐部特色}", clubFeatures);
        dynamicPrompt = dynamicPrompt.replace("{二品价格}", priceLevelTwo);
        dynamicPrompt = dynamicPrompt.replace("{一品价格}", priceLevelOne);
        dynamicPrompt = dynamicPrompt.replace("{顶尖价格}", priceTop);
        dynamicPrompt = dynamicPrompt.replace("{明星价格}", priceStar);

        String detailedPricing = buildDetailedPricingText();
        if (detailedPricing != null && !detailedPricing.isEmpty()) {
            dynamicPrompt += "\n\n" + detailedPricing;
        }

        String relevantFaq = buildRelevantFaq(userMessage);
        if (relevantFaq != null && !relevantFaq.isEmpty()) {
            dynamicPrompt += "\n\n## 相关FAQ（仅参考）\n" + relevantFaq;
        }

        return dynamicPrompt;
    }

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
            log.warn("构建详细定价文本失败", e);
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

            if (userMessage.contains(category)) {
                isRelevant = true;
            } else {
                String[] questionWords = question.replace("？", "").replace("?", "").split("[，,、]");
                for (String word : questionWords) {
                    if (word.length() >= 2 && userMessage.contains(word)) {
                        isRelevant = true;
                        break;
                    }
                }
            }

            if (isRelevant) {
                String answer = item.getAnswer()
                        .replace("{price_level_two}", formatPrice(null, "50"))
                        .replace("{price_level_one}", formatPrice(null, "80"))
                        .replace("{price_top}", formatPrice(null, "200"))
                        .replace("{price_star}", formatPrice(null, "500"))
                        .replace("{俱乐部名称}", "三角洲行动陪玩俱乐部")
                        .replace("{主营游戏}", "三角洲行动")
                        .replace("{俱乐部特色}", "专业陪玩服务");

                faqBuilder.append("Q: ").append(question).append("\n");
                faqBuilder.append("A: ").append(answer, 0, Math.min(answer.length(), ExportConstants.FAQ_ANSWER_TRUNCATION_LENGTH)).append("\n\n");
                count++;
            }
        }

        return faqBuilder.length() > 0 ? faqBuilder.toString() : null;
    }

    private String formatPrice(BigDecimal price, String defaultValue) {
        if (price != null) {
            return price.toString();
        }
        return defaultValue;
    }

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
            for (DeepSeekService.ChatMessage historyMsg : conversationHistory) {
                JSONObject historyMsgObj = new JSONObject();
                historyMsgObj.set("role", historyMsg.getRole());
                historyMsgObj.set("content", historyMsg.getContent());
                messages.add(historyMsgObj);
            }
        }

        JSONObject userMessageObj = new JSONObject();
        userMessageObj.set("role", "user");
        userMessageObj.set("content", userMessage);
        messages.add(userMessageObj);

        request.set("messages", messages);
        return request;
    }

    private String parseResponse(String responseBody) {
        try {
            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                if (message != null) {
                    return message.getStr("content", "");
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析 DeepSeek 响应失败", e);
            return null;
        }
    }

    /**
     * 构建AI回复缓存Key
     * <p>
     * 基于用户消息内容的MD5哈希，确保相同问题复用缓存。
     * </p>
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
            return AI_REPLY_CACHE_PREFIX + hexString.toString();
        } catch (Exception e) {
            return AI_REPLY_CACHE_PREFIX + userMessage.hashCode();
        }
    }
}
