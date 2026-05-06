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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息处理服务抽象基类
 * <p>
 * 提供消息处理的核心流程：意图检测、AI调用、关键词回复、
 * 转人工处理、消息持久化等公共能力。
 * 子类（ChatTestServiceImpl、WeWorkMessageServiceImpl等）继承此类实现平台特定逻辑。
 * </p>
 *
 * @author 刘建国
 */
@RequiredArgsConstructor
public abstract class BaseMessageProcessService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String WAITING_REPLY = AiCustomerServiceConstants.WAITING_REPLY;
    protected static final String IN_SERVICE_REPLY = AiCustomerServiceConstants.IN_SERVICE_REPLY;
    protected static final String EMOTION_HANDOFF_REPLY = AiCustomerServiceConstants.EMOTION_HANDOFF_REPLY;
    protected static final String AI_FAILURE_HANDOFF_REPLY = AiCustomerServiceConstants.AI_FAILURE_HANDOFF_REPLY;

    protected final MessageMapper messageMapper;

    protected final PendingMessageMapper pendingMessageMapper;

    protected final PendingMessageService pendingMessageService;

    @Nullable
    protected DeepSeekService deepSeekService;

    protected final KeywordMatcherService keywordMatcherService;

    protected final ReplyService replyService;

    protected final RedisService redisService;

    protected final CustomerProfileService customerProfileService;

    protected final ClubConfigMapper clubConfigMapper;

    protected final ServiceItemMapper serviceItemMapper;

    protected final ActivityPackageMapper activityPackageMapper;

    protected final CompanionLevelMapper companionLevelMapper;

    protected final ServicePriceRuleMapper servicePriceRuleMapper;

    protected final CompanionMapper companionMapper;

    @Nullable
    protected OrderService orderService;

    /**
     * 获取用户当前转人工状态
     *
     * @param userId 用户ID
     * @return 转人工状态枚举（NONE/WAITING/IN_SERVICE）
     */
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
            log.warn("【消息处理】检查人工模式状态异常 | userId={}", userId, e);
            return HandoffState.NONE;
        }
    }

    /**
     * 获取带队列信息的等待回复
     *
     * @return 等待回复文本
     */
    protected String getWaitingReplyWithQueueInfo() {
        try {
            Long pendingCount = pendingMessageService.getPendingCount();
            if (pendingCount != null && pendingCount > 1) {
                return "正在为您安排客服，当前前方有" + (pendingCount - 1) + "位等待，请稍等片刻~ 🔔";
            }
        } catch (Exception e) {
            log.debug("【消息处理】获取等待队列信息失败", e);
        }
        return WAITING_REPLY;
    }

    /**
     * 根据转人工原因获取对应回复
     *
     * @param handoffReason        转人工原因关键词
     * @param negativeEmotion      负面情绪关键词
     * @param aiConsecutiveFailure 是否AI连续失败
     * @return 转人工回复文本
     */
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

    /**
     * 检测负面情绪关键词
     *
     * @param content 用户消息内容
     * @return 匹配到的负面情绪关键词，未匹配返回null
     */
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

    /**
     * 检测订单意图关键词
     *
     * @param content 用户消息内容
     * @return 匹配到的订单意图关键词，未匹配返回null
     */
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

    /**
     * 检测价格咨询关键词
     *
     * @param content 用户消息内容
     * @return 匹配到的价格咨询关键词，未匹配返回null
     */
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

    /**
     * 判断是否为纯价格咨询（不含订单意图和人工请求）
     *
     * @param content       用户消息内容
     * @param priceKeyword  已匹配的价格关键词
     * @return 是否纯价格咨询
     */
    protected boolean isPurePriceInquiry(String content, String priceKeyword) {
        if (content == null || priceKeyword == null) return false;
        String trimmed = content.trim();
        String orderIntent = checkOrderIntent(trimmed);
        if (orderIntent != null) return false;
        String humanExplicit = checkHumanExplicit(trimmed);
        if (humanExplicit != null) return false;
        return true;
    }

    /**
     * 检测服务咨询关键词
     *
     * @param content 用户消息内容
     * @return 匹配到的服务咨询关键词，未匹配返回null
     */
    protected String checkServiceInquiry(String content) {
        if (content == null || content.trim().isEmpty()) return null;
        for (String keyword : AiCustomerServiceConstants.SERVICE_INQUIRY_KEYWORDS) {
            if (content.contains(keyword)) return keyword;
        }
        return null;
    }

    /**
     * 检测排班咨询关键词
     *
     * @param content 用户消息内容
     * @return 匹配到的排班咨询关键词，未匹配返回null
     */
    protected String checkScheduleInquiry(String content) {
        if (content == null || content.trim().isEmpty()) return null;
        for (String keyword : AiCustomerServiceConstants.SCHEDULE_INQUIRY_KEYWORDS) {
            if (content.contains(keyword)) return keyword;
        }
        return null;
    }

    /**
     * 判断用户是否为VIP客户
     *
     * @param userId 用户ID
     * @return 是否VIP客户
     */
    protected boolean isVipCustomer(Long userId) {
        if (userId == null || customerProfileService == null) {
            return false;
        }
        try {
            var profile = customerProfileService.getProfileByUserId(userId);
            if (profile == null) {
                return false;
            }
            String memberLevel = profile.getMemberLevel();
            if (memberLevel == null) {
                return false;
            }
            return AiCustomerServiceConstants.VIP_MEMBER_LEVELS.contains(memberLevel);
        } catch (Exception e) {
            log.debug("【消息处理】VIP判定异常 | userId={}", userId, e);
            return false;
        }
    }

    /**
     * 为价格咨询回复追加转化引导后缀
     *
     * @param replyContent 原始回复内容
     * @return 追加后缀后的回复内容
     */
    protected String appendPriceConversionSuffix(String replyContent) {
        if (replyContent == null || replyContent.isEmpty()) return replyContent;
        return replyContent + AiCustomerServiceConstants.PRICE_INQUIRY_CONVERSION_SUFFIX;
    }

    /**
     * 咨询回复结果封装
     */
    protected static class InquiryReplyResult {
        public final String replyContent;
        public final boolean isAiReply;
        public final ResponseSource responseSource;

        InquiryReplyResult(String replyContent, boolean isAiReply, ResponseSource responseSource) {
            this.replyContent = replyContent;
            this.isAiReply = isAiReply;
            this.responseSource = responseSource;
        }
    }

    /**
     * 处理咨询类消息（价格/服务/排班），AI优先、关键词兜底
     *
     * @param userId               用户ID
     * @param content              用户消息内容
     * @param contextHint          上下文提示
     * @param keyword              匹配到的关键词
     * @param appendConversionSuffix 是否追加转化后缀
     * @return 咨询回复结果
     */
    protected InquiryReplyResult handleInquiryWithAiChannel(
            Long userId, String content, String contextHint,
            String keyword, boolean appendConversionSuffix) {
        String replyContent = null;
        boolean isAiReply = false;
        ResponseSource responseSource = ResponseSource.DEFAULT_FALLBACK;

        String aiReply = tryDeepSeekAI(userId, content, contextHint);
        if (aiReply != null) {
            String validatedReply = validateAiReply(aiReply, content);
            replyContent = appendConversionSuffix ? appendPriceConversionSuffix(validatedReply) : validatedReply;
            isAiReply = true;
            responseSource = ResponseSource.AI_REPLY;
            log.debug("【消息处理】AI咨询通道回复 | keyword={} | 回复长度={} | 来源=AI", keyword, replyContent.length());
        } else {
            String directReply = tryDirectKeywordReply(keyword, content);
            if (directReply != null) {
                replyContent = appendConversionSuffix ? appendPriceConversionSuffix(directReply) : directReply;
                isAiReply = false;
                responseSource = ResponseSource.KEYWORD_DIRECT;
                log.debug("【消息处理】关键词兜底回复 | keyword={} | 来源=KEYWORD", keyword);
            }
        }

        if (replyContent == null) {
            replyContent = AiCustomerServiceConstants.DEFAULT_FALLBACK_REPLY;
            responseSource = ResponseSource.DEFAULT_FALLBACK;
            log.debug("【消息处理】默认兜底回复 | keyword={}", keyword);
        }

        return new InquiryReplyResult(replyContent, isAiReply, responseSource);
    }

    /**
     * 检测明确要求人工服务关键词
     *
     * @param content 用户消息内容
     * @return 匹配到的人工请求关键词，未匹配返回null
     */
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

    /**
     * 检查AI连续失败次数是否达到阈值
     *
     * @param userId 用户ID
     * @return 是否达到连续失败阈值
     */
    protected boolean checkAiConsecutiveFailure(Long userId) {
        try {
            String key = AiCustomerServiceConstants.AI_CONSECUTIVE_KEY_PREFIX + userId;
            Object countObj = redisService.get(key);
            if (countObj != null) {
                int count = Integer.parseInt(countObj.toString());
                return count >= AiCustomerServiceConstants.AI_CONSECUTIVE_FAILURE_THRESHOLD;
            }
        } catch (Exception e) {
            log.debug("【消息处理】检查AI连续失败计数异常 | userId={}", userId, e);
        }
        return false;
    }

    /**
     * 记录AI连续失败计数+1
     *
     * @param userId 用户ID
     */
    protected void trackAiConsecutiveFailure(Long userId) {
        try {
            String key = AiCustomerServiceConstants.AI_CONSECUTIVE_KEY_PREFIX + userId;
            Long count = redisService.increment(key);
            if (count != null && count == 1) {
                redisService.expire(key, AiCustomerServiceConstants.AI_CONSECUTIVE_TTL_MINUTES, TimeUnit.MINUTES);
            }
            log.debug("【消息处理】AI连续失败计数 | userId={} | 当前计数={}", userId, count);
        } catch (Exception e) {
            log.debug("【消息处理】记录AI连续失败计数异常 | userId={}", userId, e);
        }
    }

    /**
     * 重置AI连续失败计数
     *
     * @param userId 用户ID
     */
    protected void resetAiConsecutiveFailure(Long userId) {
        try {
            String key = AiCustomerServiceConstants.AI_CONSECUTIVE_KEY_PREFIX + userId;
            redisService.delete(key);
        } catch (Exception e) {
            log.debug("【消息处理】重置AI连续失败计数异常 | userId={}", userId, e);
        }
    }

    /**
     * 构建转人工上下文摘要
     *
     * @param userId        用户ID
     * @param currentMessage 当前用户消息
     * @param handoffReason 转人工原因
     * @return 上下文摘要文本
     */
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
            log.warn("【消息处理】生成上下文摘要失败 | userId={}", userId, e);
            return "客户发送: \"" + truncate(currentMessage, ExportConstants.CONTEXT_TRUNCATION_LENGTH) + "\" | 触发原因: " + handoffReason;
        }
    }

    /**
     * 截断字符串到指定长度
     *
     * @param str   原始字符串
     * @param maxLen 最大长度
     * @return 截断后的字符串
     */
    protected String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }

    /**
     * 尝试获取关键词直接回复
     *
     * @param keyword 匹配到的关键词
     * @param content 用户消息内容
     * @return 关键词回复内容，无匹配返回null
     */
    protected String tryDirectKeywordReply(String keyword, String content) {
        if (!AiCustomerServiceConstants.DIRECT_REPLY_KEYWORDS.contains(keyword)) {
            return null;
        }
        try {
            String keywordReply = replyService.getKeywordReply(keyword);
            if (keywordReply != null && !keywordReply.trim().isEmpty()) {
                log.debug("【消息处理】关键词直接回复 | keyword={} | 回复长度={}", keyword, keywordReply.length());
                return keywordReply;
            }
        } catch (Exception e) {
            log.warn("【消息处理】获取关键词直接回复失败 | keyword={}", keyword, e);
        }
        return null;
    }

    /**
     * 构建上下文提示（用于AI调用时附加到用户消息后）
     *
     * @param matchedKeywords 匹配到的关键词列表
     * @param needsHandoff    是否需要转人工
     * @param orderIntent     订单意图关键词
     * @param humanExplicit   人工请求关键词
     * @return 上下文提示文本
     */
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

    /**
     * 调用DeepSeek AI获取回复
     * <p>
     * 流程：构建对话历史 → 构建人格提示词 → 附加上下文 → 调用API → 质量校验
     * </p>
     *
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @param contextHint 上下文提示
     * @return AI回复内容，失败返回null
     */
    @SuppressWarnings("null")
    protected String tryDeepSeekAI(Long userId, String userMessage, String contextHint) {
        if (deepSeekService == null || !deepSeekService.isEnabled()) {
            log.debug("【消息处理】DeepSeek未启用，跳过AI调用 | userId={}", userId);
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

            log.debug("【消息处理】调用DeepSeek AI | userId={} | 历史消息数={} | 消息长度={}",
                    userId, conversationHistory.size(), messageWithContext.length());

            String aiReply = deepSeekService.getChatReplyWithHistory(messageWithContext, conversationHistory);

            if (aiReply != null) {
                String validatedReply = validateAiReply(aiReply, userMessage);
                log.info("【消息处理】AI回复成功 | userId={} | 原始长度={} | 校验后长度={}",
                        userId, aiReply.length(), validatedReply.length());
                return validatedReply;
            }

            return null;
        } catch (Exception e) {
            log.error("【消息处理】调用DeepSeek AI失败 | userId={}", userId, e);
        }
        return null;
    }

    /**
     * 校验AI回复质量
     * <p>
     * 质量控制规则：
     * 1. 回复不能为空或纯空白
     * 2. 回复长度不能超过500字符（防止冗长回复浪费Token）
     * 3. 回复不能包含明显的AI模板语言
     * 4. 回复不能与用户问题完全不相关（基础相关性检查）
     * </p>
     *
     * @param aiReply     AI原始回复
     * @param userMessage 用户原始消息
     * @return 校验后的回复内容，不合格返回默认兜底回复
     */
    protected String validateAiReply(String aiReply, String userMessage) {
        if (aiReply == null || aiReply.trim().isEmpty()) {
            log.warn("【质量校验】AI回复为空，使用兜底回复");
            return AiCustomerServiceConstants.DEFAULT_FALLBACK_REPLY;
        }

        String trimmed = aiReply.trim();

        if (trimmed.length() > 500) {
            log.warn("【质量校验】AI回复过长({}字符)，截断到500字符", trimmed.length());
            trimmed = trimmed.substring(0, 500);
        }

        String[] forbiddenPatterns = {
                "作为AI", "作为人工智能", "我是一个AI", "我是一个人工智能",
                "我无法", "我不能提供", "我无法提供", "我的知识截止",
                "请注意，我是", "免责声明", "以上内容仅供参考"
        };
        for (String pattern : forbiddenPatterns) {
            if (trimmed.contains(pattern)) {
                log.warn("【质量校验】AI回复包含禁止模板语言「{}」，使用兜底回复", pattern);
                return AiCustomerServiceConstants.DEFAULT_FALLBACK_REPLY;
            }
        }

        return trimmed;
    }

    /** 人格提示词Redis缓存Key */
    private static final String PERSONALITY_PROMPT_CACHE_KEY = "delta:ai:personality_prompt";

    /** 人格提示词缓存TTL（秒） */
    private static final long PERSONALITY_PROMPT_CACHE_TTL_SECONDS = 600L;

    /**
     * 构建AI人格提示词（带Redis缓存，TTL 10分钟）
     * <p>
     * 从俱乐部配置获取人格风格，结合服务项目、活动套餐、
     * 陪玩等级和趣味玩法生成完整的系统提示词。
     * 缓存完整提示词避免每次消息处理5-7次DB查询。
     * </p>
     *
     * @return 人格提示词文本
     */
    protected String buildPersonalityPrompt() {
        try {
            // 先查缓存
            Object cached = redisService.get(PERSONALITY_PROMPT_CACHE_KEY);
            if (cached != null) {
                log.debug("【消息处理】人格提示词命中缓存 | key={}", PERSONALITY_PROMPT_CACHE_KEY);
                return cached.toString();
            }

            // 缓存未命中，构建提示词
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

            String result = systemPrompt + awarenessPrompt;

            // 缓存结果，TTL 10分钟
            redisService.set(PERSONALITY_PROMPT_CACHE_KEY, result, PERSONALITY_PROMPT_CACHE_TTL_SECONDS, TimeUnit.SECONDS);

            log.debug("【消息处理】人格提示词构建完成并缓存 | personality={} | 提示词长度={}", personality, result.length());

            return result;
        } catch (Exception e) {
            log.debug("【消息处理】构建AI人格提示词失败", e);
            return "";
        }
    }

    /**
     * 构建用户活跃订单上下文
     *
     * @param userId 用户ID
     * @return 订单上下文文本，无订单返回null
     */
    @SuppressWarnings("null")
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
            log.debug("【消息处理】构建订单上下文失败 | userId={}", userId, e);
            return null;
        }
    }

    /**
     * 检测服务完成信号
     *
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @return 是否检测到服务完成信号
     */
    protected boolean detectServiceCompletion(Long userId, String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) return false;
        String trimmed = userMessage.trim();
        for (String keyword : AiCustomerServiceConstants.SERVICE_COMPLETE_KEYWORDS) {
            if (trimmed.contains(keyword)) return true;
        }
        return false;
    }

    /**
     * 处理服务完成（自动完成进行中的订单）
     *
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @return 是否成功处理了服务完成
     */
    @SuppressWarnings("null")
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
                        log.info("【消息处理】服务完成(自动检测) | orderId={} | companion={}", order.getId(), order.getCompanionName());
                    } catch (Exception e) {
                        log.warn("【消息处理】自动完成订单失败 | orderId={}", order.getId(), e);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("【消息处理】处理服务完成异常 | userId={}", userId, e);
            return false;
        }
    }

    /**
     * 构建服务项目文本
     *
     * @param clubConfigId 俱乐部配置ID
     * @return 服务项目文本
     */
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
                sb.append("- ").append(item.getServiceName());
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

    /**
     * 构建陪玩师等级文本（含各服务项目价格）
     *
     * @return 陪玩师等级文本
     */
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
                serviceItemMapper.selectByIds(serviceItemIds).forEach(
                        item -> serviceItemNameMap.put(item.getId(), item.getServiceName()));
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
            log.debug("【消息处理】构建陪玩等级文本失败", e);
            return "";
        }
    }

    /**
     * 格式化价格单位
     *
     * @param priceUnit 价格单位枚举值
     * @return 中文价格单位
     */
    protected String formatPriceUnit(String priceUnit) {
        if (BusinessStatusConstants.PRICE_UNIT_HOUR.equals(priceUnit)) return "/时";
        if (BusinessStatusConstants.PRICE_UNIT_ORDER.equals(priceUnit)) return "/单";
        if (BusinessStatusConstants.PRICE_UNIT_NIGHT.equals(priceUnit)) return "/晚";
        return "/" + priceUnit;
    }

    /**
     * 构建趣味玩法文本
     *
     * @return 趣味玩法文本
     */
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
            log.debug("【消息处理】构建趣味玩法文本失败", e);
            return "";
        }
    }

    /**
     * 构建活动套餐文本
     *
     * @param clubConfigId 俱乐部配置ID
     * @return 活动套餐文本
     */
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

    /**
     * 获取最近对话历史
     *
     * @param userId 用户ID
     * @param limit  最大条数
     * @return 对话历史消息列表
     */
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

    /**
     * 保存入站消息
     *
     * @param userId  用户ID
     * @param content 消息内容
     * @return 保存后的消息实体
     */
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
            log.warn("【消息处理】记录客户交互失败 | userId={}", userId, e);
        }
        return inMessage;
    }

    /**
     * 保存出站消息
     *
     * @param userId  用户ID
     * @param content 消息内容
     * @param isAi    是否AI回复
     */
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
            log.warn("【消息处理】记录客户交互失败 | userId={}", userId, e);
        }
    }

    /**
     * 转人工状态枚举
     */
    protected enum HandoffState {
        NONE,
        WAITING,
        IN_SERVICE
    }

    /**
     * 回复来源枚举
     */
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
