package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.AiPersonalityConstants;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.constant.MessageConstants;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.Message;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.ServiceItem;
import com.delta.common.entity.ActivityPackage;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.entity.ServicePriceRule;
import com.delta.common.entity.Companion;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.mapper.ServiceItemMapper;
import com.delta.common.mapper.ActivityPackageMapper;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.ServicePriceRuleMapper;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.service.CustomerProfileService;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.OrderService;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.service.ReplyService;
import com.delta.common.service.matcher.KeywordMatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class BaseMessageProcessService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String WAITING_REPLY = AiCustomerServiceConstants.WAITING_REPLY;
    protected static final String IN_SERVICE_REPLY = AiCustomerServiceConstants.IN_SERVICE_REPLY;
    protected static final String EMOTION_HANDOFF_REPLY = AiCustomerServiceConstants.EMOTION_HANDOFF_REPLY;
    protected static final String AI_FAILURE_HANDOFF_REPLY = AiCustomerServiceConstants.AI_FAILURE_HANDOFF_REPLY;

    @Autowired
    protected MessageMapper messageMapper;

    @Autowired
    protected PendingMessageMapper pendingMessageMapper;

    @Autowired
    protected PendingMessageService pendingMessageService;

    @Autowired(required = false)
    protected DeepSeekService deepSeekService;

    @Autowired
    protected KeywordMatcherService keywordMatcherService;

    @Autowired
    protected ReplyService replyService;

    @Autowired
    protected RedisService redisService;

    @Autowired
    protected CustomerProfileService customerProfileService;

    @Autowired
    protected ClubConfigMapper clubConfigMapper;

    @Autowired
    protected ServiceItemMapper serviceItemMapper;

    @Autowired
    protected ActivityPackageMapper activityPackageMapper;

    @Autowired
    protected CompanionLevelMapper companionLevelMapper;

    @Autowired
    protected ServicePriceRuleMapper servicePriceRuleMapper;

    @Autowired
    protected CompanionMapper companionMapper;

    @Autowired(required = false)
    protected OrderService orderService;

    protected HandoffState getHandoffState(Long userId) {
        try {
            LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PendingMessage::getUserId, userId);
            wrapper.in(PendingMessage::getStatus, BusinessStatusConstants.PENDING_STATUS_PENDING, BusinessStatusConstants.PENDING_STATUS_PROCESSING);
            wrapper.orderByDesc(PendingMessage::getCreatedAt);
            Page<PendingMessage> page = pendingMessageMapper.selectPage(new Page<>(1, 1), wrapper);
            if (page.getRecords().isEmpty()) {
                return HandoffState.NONE;
            }
            PendingMessage pm = page.getRecords().get(0);
            if (BusinessStatusConstants.PENDING_STATUS_PROCESSING.equals(pm.getStatus())) {
                return HandoffState.IN_SERVICE;
            }
            return HandoffState.WAITING;
        } catch (Exception e) {
            log.warn("检查用户人工模式状态异常: userId={}", userId, e);
            return HandoffState.NONE;
        }
    }

    protected String getWaitingReplyWithQueueInfo() {
        try {
            Long pendingCount = pendingMessageService.getPendingCount();
            if (pendingCount != null && pendingCount > 1) {
                return "正在为您安排客服，当前前方有" + (pendingCount - 1) + "位等待，请稍等片刻~ 🔔";
            }
        } catch (Exception e) {
            log.debug("获取等待队列信息失败", e);
        }
        return WAITING_REPLY;
    }

    protected String getHandoffReply(String handoffReason, String negativeEmotion, boolean aiConsecutiveFailure) {
        if (negativeEmotion != null) {
            return EMOTION_HANDOFF_REPLY;
        }
        if (aiConsecutiveFailure) {
            return AI_FAILURE_HANDOFF_REPLY;
        }
        if (handoffReason != null && AiCustomerServiceConstants.HUMAN_EXPLICIT_KEYWORDS.contains(handoffReason)) {
            return AiCustomerServiceConstants.HUMAN_EXPLICIT_HANDOFF_REPLY;
        }
        return AiCustomerServiceConstants.ORDER_INTENT_HANDOFF_REPLY;
    }

    protected String checkNegativeEmotion(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        for (String keyword : AiCustomerServiceConstants.NEGATIVE_EMOTION_KEYWORDS) {
            if (content.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    protected String checkOrderIntent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        for (String keyword : AiCustomerServiceConstants.ORDER_INTENT_KEYWORDS) {
            if (content.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    protected String checkPriceInquiry(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        for (String keyword : AiCustomerServiceConstants.PRICE_INQUIRY_KEYWORDS) {
            if (content.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    protected boolean isPurePriceInquiry(String content, String priceKeyword) {
        if (content == null || priceKeyword == null) return false;
        String trimmed = content.trim();
        String orderIntent = checkOrderIntent(trimmed);
        if (orderIntent != null) return false;
        String humanExplicit = checkHumanExplicit(trimmed);
        if (humanExplicit != null) return false;
        return true;
    }

    protected String checkHumanExplicit(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        for (String keyword : AiCustomerServiceConstants.HUMAN_EXPLICIT_KEYWORDS) {
            if (content.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    protected boolean checkAiConsecutiveFailure(Long userId) {
        try {
            String key = AiCustomerServiceConstants.AI_CONSECUTIVE_KEY_PREFIX + userId;
            Object countObj = redisService.get(key);
            if (countObj != null) {
                int count = Integer.parseInt(countObj.toString());
                return count >= AiCustomerServiceConstants.AI_CONSECUTIVE_FAILURE_THRESHOLD;
            }
        } catch (Exception e) {
            log.debug("检查AI连续失败计数异常: userId={}", userId, e);
        }
        return false;
    }

    protected void trackAiConsecutiveFailure(Long userId) {
        try {
            String key = AiCustomerServiceConstants.AI_CONSECUTIVE_KEY_PREFIX + userId;
            Long count = redisService.increment(key);
            if (count != null && count == 1) {
                redisService.expire(key, AiCustomerServiceConstants.AI_CONSECUTIVE_TTL_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.debug("记录AI连续失败计数异常: userId={}", userId, e);
        }
    }

    protected void resetAiConsecutiveFailure(Long userId) {
        try {
            String key = AiCustomerServiceConstants.AI_CONSECUTIVE_KEY_PREFIX + userId;
            redisService.delete(key);
        } catch (Exception e) {
            log.debug("重置AI连续失败计数异常: userId={}", userId, e);
        }
    }

    protected String buildContextSummaryForHandoff(Long userId, String currentMessage, String handoffReason) {
        try {
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getUserId, userId);
            wrapper.orderByDesc(Message::getCreatedAt);
            Page<Message> page = new Page<>(1, ExportConstants.RECENT_MESSAGES_LIMIT);
            Page<Message> resultPage = messageMapper.selectPage(page, wrapper);
            List<Message> recentMessages = resultPage.getRecords();

            if (recentMessages.isEmpty()) {
                return "客户发送: \"" + truncate(currentMessage, ExportConstants.CONTEXT_TRUNCATION_LENGTH) + "\" | 触发原因: " + handoffReason;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【近期对话】\n");

            for (int i = recentMessages.size() - 1; i >= 0; i--) {
                Message msg = recentMessages.get(i);
                String role = MessageConstants.DIRECTION_IN.equals(msg.getDirection()) ? "客户" : (msg.getAi() ? "AI" : "客服");
                sb.append(role).append(": ").append(truncate(msg.getContent(), ExportConstants.CONTEXT_MESSAGE_TRUNCATION_LENGTH)).append("\n");
            }

            sb.append("【触发原因】").append(handoffReason);
            return sb.toString();
        } catch (Exception e) {
            log.warn("生成上下文摘要失败: userId={}", userId, e);
            return "客户发送: \"" + truncate(currentMessage, ExportConstants.CONTEXT_TRUNCATION_LENGTH) + "\" | 触发原因: " + handoffReason;
        }
    }

    protected String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }

    protected String tryDirectKeywordReply(String keyword, String content) {
        if (!AiCustomerServiceConstants.DIRECT_REPLY_KEYWORDS.contains(keyword)) {
            return null;
        }
        try {
            String keywordReply = replyService.getKeywordReply(keyword);
            if (keywordReply != null && !keywordReply.trim().isEmpty()) {
                return keywordReply;
            }
        } catch (Exception e) {
            log.warn("获取关键词直接回复失败: keyword={}", keyword, e);
        }
        return null;
    }

    protected String buildContextHint(List<String> matchedKeywords, boolean needsHandoff, String orderIntent, String humanExplicit) {
        if (!needsHandoff && (matchedKeywords == null || matchedKeywords.isEmpty())) {
            return null;
        }
        StringBuilder hint = new StringBuilder();
        if (humanExplicit != null) {
            hint.append("[客户要求转人工，告知正在转接]");
        } else if (orderIntent != null) {
            hint.append("[客户要预约/下单，告知正在安排人工客服处理]");
        }
        if (matchedKeywords != null && !matchedKeywords.isEmpty() && !needsHandoff) {
            String keywordsStr = matchedKeywords.stream().limit(ExportConstants.KEYWORD_HINT_LIMIT).collect(Collectors.joining("、"));
            hint.append("[命中关键词「").append(keywordsStr).append("」，结合含义自然回答]");
        }
        return hint.length() > 0 ? hint.toString() : null;
    }

    protected String tryDeepSeekAI(Long userId, String userMessage, String contextHint) {
        if (deepSeekService == null || !deepSeekService.isEnabled()) {
            return null;
        }
        try {
            List<DeepSeekService.ChatMessage> conversationHistory = getRecentConversationHistory(
                    userId, AiCustomerServiceConstants.CONVERSATION_HISTORY_LIMIT);

            String personalityPrompt = buildPersonalityPrompt();

            String messageWithContext = userMessage;
            if (contextHint != null && !contextHint.isEmpty()) {
                messageWithContext = userMessage + "\n\n" + contextHint;
            }

            String orderContext = buildOrderContextForUser(userId);
            if (orderContext != null && !orderContext.isEmpty()) {
                messageWithContext = messageWithContext + "\n\n" + orderContext;
            }

            if (!personalityPrompt.isEmpty()) {
                conversationHistory.add(0, new DeepSeekService.ChatMessage("system", personalityPrompt));
            }

            return deepSeekService.getChatReplyWithHistory(messageWithContext, conversationHistory);
        } catch (Exception e) {
            log.error("调用 DeepSeek AI 失败", e);
        }
        return null;
    }

    protected String buildPersonalityPrompt() {
        try {
            LambdaQueryWrapper<ClubConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.last("LIMIT 1");
            ClubConfig clubConfig = clubConfigMapper.selectOne(wrapper);
            if (clubConfig == null) return "";

            String personality = clubConfig.getAiPersonality() != null ? clubConfig.getAiPersonality() : AiPersonalityConstants.DEFAULT_PERSONALITY;
            String clubName = clubConfig.getClubName() != null ? clubConfig.getClubName() : AiPersonalityConstants.DEFAULT_CLUB_NAME_SUFFIX;

            String systemPrompt = AiPersonalityConstants.getSystemPrompt(personality, clubName);

            String serviceItems = buildServiceItemsText(clubConfig.getId());
            String activePackages = buildActivePackagesText(clubConfig.getId());
            String companionLevels = buildCompanionLevelsText();
            String funGameplay = buildFunGameplayText();

            String awarenessPrompt = AiPersonalityConstants.getServiceAwarenessPrompt(serviceItems, activePackages, companionLevels, funGameplay);

            return systemPrompt + awarenessPrompt;
        } catch (Exception e) {
            log.debug("构建AI人格提示词失败", e);
            return "";
        }
    }

    protected String buildOrderContextForUser(Long userId) {
        if (orderService == null || userId == null) return null;
        try {
            List<com.delta.common.vo.OrderVO> activeOrders = orderService.getActiveOrdersByUserId(userId);
            if (activeOrders == null || activeOrders.isEmpty()) return null;
            StringBuilder sb = new StringBuilder("\n【用户活跃订单信息】\n");
            for (com.delta.common.vo.OrderVO order : activeOrders) {
                String startStr = order.getScheduledStart() != null ? 
                    order.getScheduledStart().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "未设置";
                String endStr = order.getScheduledEnd() != null ? 
                    order.getScheduledEnd().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "未设置";
                sb.append(String.format("- 订单号:%s | 陪玩师:%s | 游戏:%s | 预约时间:%s~%s | 状态:%s | 金额:%s元\n",
                    order.getOrderNo(),
                    order.getCompanionName() != null ? order.getCompanionName() : "未知",
                    order.getGameType() != null ? order.getGameType() : "-",
                    startStr, endStr,
                    order.getOrderStatusText(),
                    order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0"));
            }
            return sb.toString();
        } catch (Exception e) {
            log.debug("构建订单上下文失败: userId={}", userId, e);
            return null;
        }
    }

    protected boolean detectServiceCompletion(Long userId, String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) return false;
        String trimmed = userMessage.trim();
        for (String keyword : AiCustomerServiceConstants.SERVICE_COMPLETE_KEYWORDS) {
            if (trimmed.contains(keyword)) return true;
        }
        return false;
    }

    protected boolean handleServiceCompletion(Long userId, String userMessage) {
        if (orderService == null) return false;
        try {
            List<com.delta.common.vo.OrderVO> inProgressOrders = new ArrayList<>();
            for (com.delta.common.vo.OrderVO vo : orderService.getActiveOrdersByUserId(userId)) {
                if (BusinessStatusConstants.ORDER_STATUS_IN_PROGRESS.equals(vo.getOrderStatus())) {
                    inProgressOrders.add(vo);
                }
            }
            if (!inProgressOrders.isEmpty()) {
                for (com.delta.common.vo.OrderVO order : inProgressOrders) {
                    try {
                        orderService.completeOrder(order.getId());
                        log.info("服务完成(自动检测): orderId={}, companion={}", order.getId(), order.getCompanionName());
                    } catch (Exception e) {
                        log.warn("自动完成订单失败: orderId={}", order.getId(), e);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("处理服务完成异常: userId={}", userId, e);
            return false;
        }
    }

    protected String buildServiceItemsText(Long clubConfigId) {
        try {
            LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceItem::getClubConfigId, clubConfigId);
            wrapper.eq(ServiceItem::getEnabled, BusinessStatusConstants.ENABLED_INT);
            wrapper.orderByAsc(ServiceItem::getSortOrder);
            List<ServiceItem> items = serviceItemMapper.selectList(wrapper);
            if (items.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (ServiceItem item : items) {
                sb.append("- ").append(item.getItemName());
                if (item.getBasePrice() != null) {
                    String unit = formatPriceUnit(item.getPriceUnit());
                    sb.append(" ¥").append(item.getBasePrice()).append(unit);
                }
                if (item.getGuaranteeText() != null && !item.getGuaranteeText().isEmpty()) {
                    sb.append(" (").append(item.getGuaranteeText()).append(")");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    protected String buildCompanionLevelsText() {
        try {
            LambdaQueryWrapper<CompanionLevel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CompanionLevel::getEnabled, BusinessStatusConstants.ENABLED_INT);
            wrapper.orderByAsc(CompanionLevel::getSortOrder);
            List<CompanionLevel> levels = companionLevelMapper.selectList(wrapper);
            if (levels.isEmpty()) return "";

            List<Long> levelIds = levels.stream().map(CompanionLevel::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ServicePriceRule> priceWrapper = new LambdaQueryWrapper<>();
            priceWrapper.in(ServicePriceRule::getCompanionLevelId, levelIds);
            priceWrapper.eq(ServicePriceRule::getEnabled, BusinessStatusConstants.ENABLED_INT);
            List<ServicePriceRule> allRules = servicePriceRuleMapper.selectList(priceWrapper);
            Map<Long, List<ServicePriceRule>> rulesByLevel = allRules.stream()
                    .collect(Collectors.groupingBy(ServicePriceRule::getCompanionLevelId));

            List<Long> serviceItemIds = allRules.stream()
                    .map(ServicePriceRule::getServiceItemId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> serviceItemNameMap = new HashMap<>();
            if (!serviceItemIds.isEmpty()) {
                serviceItemMapper.selectBatchIds(serviceItemIds).forEach(
                        item -> serviceItemNameMap.put(item.getId(), item.getItemName()));
            }

            StringBuilder sb = new StringBuilder();
            for (CompanionLevel level : levels) {
                sb.append("★ ").append(level.getLevelName());
                if (level.getDescription() != null && !level.getDescription().isEmpty()) {
                    sb.append("（").append(level.getDescription()).append("）");
                }
                if (level.getBasePrice() != null) {
                    sb.append("\n  基础价格：").append(level.getBasePrice()).append("元/小时");
                    List<ServicePriceRule> rules = rulesByLevel.getOrDefault(level.getId(), List.of());
                    if (!rules.isEmpty()) {
                        sb.append("\n  各服务项目价格：");
                        for (ServicePriceRule rule : rules) {
                            String unit = formatPriceUnit(rule.getPriceUnit());
                            String serviceName = serviceItemNameMap.getOrDefault(rule.getServiceItemId(), "服务");
                            sb.append("\n    - ").append(serviceName).append(" ¥").append(rule.getPrice()).append(unit);
                            if (rule.getOriginalPrice() != null) {
                                sb.append(" (原价¥").append(rule.getOriginalPrice()).append(")");
                            }
                        }
                    }
                }
                sb.append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.debug("构建陪玩等级文本失败", e);
            return "";
        }
    }

    protected String formatPriceUnit(String priceUnit) {
        if (BusinessStatusConstants.PRICE_UNIT_HOUR.equals(priceUnit)) return "/时";
        if (BusinessStatusConstants.PRICE_UNIT_ORDER.equals(priceUnit)) return "/单";
        if (BusinessStatusConstants.PRICE_UNIT_NIGHT.equals(priceUnit)) return "/晚";
        return "/" + priceUnit;
    }

    protected String buildFunGameplayText() {
        try {
            LambdaQueryWrapper<Companion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Companion::getEnabled, BusinessStatusConstants.ENABLED_INT);
            wrapper.last("LIMIT " + ExportConstants.COMPANION_QUERY_LIMIT);
            List<Companion> companions = companionMapper.selectList(wrapper);
            if (companions.isEmpty()) return "";

            java.util.Set<String> gameTypes = new java.util.LinkedHashSet<>();
            java.util.Set<String> tags = new java.util.LinkedHashSet<>();
            for (Companion c : companions) {
                if (c.getGameType() != null && !c.getGameType().isEmpty()) {
                    gameTypes.add(c.getGameType());
                }
                if (c.getSupportedGames() != null && !c.getSupportedGames().isEmpty()) {
                    for (String g : c.getSupportedGames().split("[,，、]")) {
                        g = g.trim();
                        if (!g.isEmpty()) tags.add(g);
                    }
                }
                if (c.getServiceTags() != null && !c.getServiceTags().isEmpty()) {
                    for (String t : c.getServiceTags().split("[,，、]")) {
                        t = t.trim();
                        if (!t.isEmpty()) tags.add(t);
                    }
                }
            }

            StringBuilder sb = new StringBuilder();

            if (!gameTypes.isEmpty()) {
                sb.append("🎮 热门游戏类型：").append(String.join("、", gameTypes)).append("\n\n");
            }

            if (!tags.isEmpty()) {
                sb.append("✨ 趣味玩法推荐：\n");
                int count = 0;
                for (String tag : tags) {
                    count++;
                    if (count > ExportConstants.FUN_GAMEPLAY_TAG_LIMIT) break;
                    sb.append("  • ").append(tag).append("\n");
                }
                sb.append("\n💡 小贴士：可以尝试不同类型的陪玩体验哦~ 比如想上分就选'带飞'模式，想放松就选'娱乐'模式，想社交就选'语音聊天'模式！\n");
            } else {
                sb.append("🎯 推荐玩法：\n");
                sb.append("  • 带飞上分 - 让大神带你冲击更高段位\n");
                sb.append("  • 娱乐开黑 - 轻松愉快地一起打游戏\n");
                sb.append("  • 语音聊天 - 聊天解压、分享日常\n");
                sb.append("  • 技术教学 - 学习高端操作和战术意识\n");
                sb.append("  • 包夜陪玩 - 通宵陪伴不孤单\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.debug("构建趣味玩法文本失败", e);
            return "";
        }
    }

    protected String buildActivePackagesText(Long clubConfigId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<ActivityPackage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ActivityPackage::getClubConfigId, clubConfigId);
            wrapper.eq(ActivityPackage::getEnabled, BusinessStatusConstants.ENABLED_INT);
            wrapper.le(ActivityPackage::getStartTime, now);
            wrapper.ge(ActivityPackage::getEndTime, now);
            List<ActivityPackage> packages = activityPackageMapper.selectList(wrapper);
            if (packages.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (ActivityPackage pkg : packages) {
                sb.append("- ").append(pkg.getTitle());
                sb.append(" ¥").append(pkg.getPackagePrice());
                if (pkg.getOriginalPrice() != null) {
                    sb.append(" (原价¥").append(pkg.getOriginalPrice()).append(")");
                }
                if (pkg.getDescription() != null && !pkg.getDescription().isEmpty()) {
                    sb.append(" - ").append(pkg.getDescription());
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    protected List<DeepSeekService.ChatMessage> getRecentConversationHistory(Long userId, int limit) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.orderByDesc(Message::getCreatedAt);
        Page<Message> page = new Page<>(1, limit * 2);
        Page<Message> resultPage = messageMapper.selectPage(page, wrapper);
        List<Message> messages = resultPage.getRecords();
        List<DeepSeekService.ChatMessage> history = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            String role = MessageConstants.DIRECTION_IN.equals(msg.getDirection())
                    ? MessageConstants.ROLE_USER : MessageConstants.ROLE_ASSISTANT;
            history.add(new DeepSeekService.ChatMessage(role, msg.getContent()));
        }
        return history;
    }

    protected Message saveIncomingMessage(Long userId, String content) {
        Message inMessage = new Message();
        inMessage.setUserId(userId);
        inMessage.setDirection(MessageConstants.DIRECTION_IN);
        inMessage.setContent(content);
        inMessage.setAi(false);
        inMessage.setKeywordTriggered(false);
        inMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(inMessage);
        try {
            customerProfileService.initProfileIfNeeded(userId);
            customerProfileService.recordInteraction(userId, false);
        } catch (Exception e) {
            log.warn("记录客户交互失败: userId={}", userId, e);
        }
        return inMessage;
    }

    protected void saveOutgoingMessage(Long userId, String content, boolean isAi) {
        Message outMessage = new Message();
        outMessage.setUserId(userId);
        outMessage.setDirection(MessageConstants.DIRECTION_OUT);
        outMessage.setContent(content);
        outMessage.setAi(isAi);
        outMessage.setKeywordTriggered(false);
        outMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(outMessage);
        try {
            customerProfileService.recordInteraction(userId, isAi);
        } catch (Exception e) {
            log.warn("记录客户交互失败: userId={}", userId, e);
        }
    }

    protected enum HandoffState {
        NONE,
        WAITING,
        IN_SERVICE
    }

    protected enum ResponseSource {
        KEYWORD_DIRECT,
        AI_REPLY,
        HANDOFF_REPLY,
        HANDOFF_WAITING,
        HANDOFF_IN_SERVICE,
        DEFAULT_FALLBACK,
        UNKNOWN
    }
}
