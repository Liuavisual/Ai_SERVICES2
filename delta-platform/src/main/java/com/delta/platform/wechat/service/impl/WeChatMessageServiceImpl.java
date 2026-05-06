package com.delta.platform.wechat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.PlatformConstants;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.ContentSafetyService;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.OrderService;
import com.delta.common.service.impl.BaseMessageProcessService;
import com.delta.platform.wechat.service.WeChatMessageService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeChatMessageServiceImpl extends BaseMessageProcessService implements WeChatMessageService {

    private final UserMapper userMapper;

    public WeChatMessageServiceImpl(
            com.delta.common.mapper.MessageMapper messageMapper,
            com.delta.common.mapper.PendingMessageMapper pendingMessageMapper,
            com.delta.common.service.PendingMessageService pendingMessageService,
            @Nullable DeepSeekService deepSeekService,
            com.delta.common.service.matcher.KeywordMatcherService keywordMatcherService,
            com.delta.common.service.ReplyService replyService,
            com.delta.common.service.RedisService redisService,
            com.delta.common.service.CustomerProfileService customerProfileService,
            com.delta.common.mapper.ClubConfigMapper clubConfigMapper,
            com.delta.common.mapper.ServiceItemMapper serviceItemMapper,
            com.delta.common.mapper.ActivityPackageMapper activityPackageMapper,
            com.delta.common.mapper.CompanionLevelMapper companionLevelMapper,
            com.delta.common.mapper.ServicePriceRuleMapper servicePriceRuleMapper,
            com.delta.common.mapper.CompanionMapper companionMapper,
            @Nullable OrderService orderService,
            ContentSafetyService contentSafetyService,
            UserMapper userMapper) {
        super(messageMapper, pendingMessageMapper, pendingMessageService,
                keywordMatcherService, replyService, redisService, customerProfileService,
                clubConfigMapper, serviceItemMapper, activityPackageMapper, companionLevelMapper,
                servicePriceRuleMapper, companionMapper, contentSafetyService);
        this.deepSeekService = deepSeekService;
        this.orderService = orderService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String processTextMessage(String fromUser, String content) {
        log.info("收到微信用户消息: fromUser={}", fromUser);

        try {
            User user = getOrCreateUser(fromUser);
            Message inMessage = saveIncomingMessage(user.getId(), content);

            if (detectServiceCompletion(user.getId(), content)) {
                log.info("【检测到服务完成信号(微信)】fromUser={}", fromUser);
                boolean completed = handleServiceCompletion(user.getId(), content);
                if (completed) {
                    log.info("【自动完成订单+触发评价(微信)】fromUser={}", fromUser);
                }
            }

            HandoffState handoffState = getHandoffState(user.getId());

            if (handoffState == HandoffState.IN_SERVICE) {
                saveOutgoingMessage(user.getId(), IN_SERVICE_REPLY, false);
                return IN_SERVICE_REPLY;
            }

            if (handoffState == HandoffState.WAITING) {
                String waitingReply = getWaitingReplyWithQueueInfo();
                saveOutgoingMessage(user.getId(), waitingReply, false);
                return waitingReply;
            }

            List<String> matchedKeywords = keywordMatcherService.matchKeywords(content);
            String matchedKeyword = null;
            if (!matchedKeywords.isEmpty()) {
                matchedKeyword = matchedKeywords.get(0);
                inMessage.setKeywordTriggered(true);
                messageMapper.updateById(inMessage);
            }

            String orderIntent = checkOrderIntent(content);
            String priceInquiry = checkPriceInquiry(content);
            String serviceInquiry = checkServiceInquiry(content);
            String scheduleInquiry = checkScheduleInquiry(content);
            String humanExplicit = checkHumanExplicit(content);
            String negativeEmotion = checkNegativeEmotion(content);
            boolean aiConsecutiveFailure = checkAiConsecutiveFailure(user.getId());
            boolean isVip = isVipCustomer(user.getId());
            boolean needsHumanHandoff = false;
            boolean isPriceInquiryOnly = false;
            boolean isServiceInquiryOnly = false;
            boolean isScheduleInquiryOnly = false;
            boolean isVipHandoff = false;
            String handoffReason = null;

            if (humanExplicit != null) {
                needsHumanHandoff = true;
                matchedKeyword = humanExplicit;
                handoffReason = humanExplicit;
            } else if (negativeEmotion != null) {
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = negativeEmotion;
                handoffReason = negativeEmotion;
            } else if (orderIntent != null) {
                log.info("【微信-预约/下单意图】关键词: {}，转人工确认时间", orderIntent);
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = orderIntent;
                handoffReason = orderIntent;
            } else if (priceInquiry != null && isPurePriceInquiry(content, priceInquiry)) {
                log.info("【微信-纯价格咨询】关键词: {}，走AI价格通道", priceInquiry);
                isPriceInquiryOnly = true;
                if (matchedKeyword == null) matchedKeyword = priceInquiry;
                resetAiConsecutiveFailure(user.getId());
            } else if (serviceInquiry != null) {
                log.info("【微信-服务咨询】关键词: {}，走AI服务介绍通道", serviceInquiry);
                isServiceInquiryOnly = true;
                if (matchedKeyword == null) matchedKeyword = serviceInquiry;
                resetAiConsecutiveFailure(user.getId());
            } else if (scheduleInquiry != null) {
                log.info("【微信-时间咨询】关键词: {}，走AI排班通道", scheduleInquiry);
                isScheduleInquiryOnly = true;
                if (matchedKeyword == null) matchedKeyword = scheduleInquiry;
                resetAiConsecutiveFailure(user.getId());
            } else if (isVip && aiConsecutiveFailure) {
                log.info("【微信-VIP客户+AI未解决】优先转人工");
                needsHumanHandoff = true;
                isVipHandoff = true;
                handoffReason = "VIP客户专属服务";
            } else if (aiConsecutiveFailure) {
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = AiCustomerServiceConstants.HANDOFF_REASON_AI_CONSECUTIVE;
                handoffReason = AiCustomerServiceConstants.HANDOFF_REASON_AI_CONSECUTIVE;
            }

            String replyContent = null;
            boolean isAiReply = false;

            if (needsHumanHandoff) {
                if (isVipHandoff) {
                    replyContent = AiCustomerServiceConstants.VIP_HANDOFF_REPLY;
                } else {
                    replyContent = getHandoffReply(handoffReason, negativeEmotion, aiConsecutiveFailure);
                }
                isAiReply = false;
                resetAiConsecutiveFailure(user.getId());
            } else if (isPriceInquiryOnly) {
                InquiryReplyResult result = handleInquiryWithAiChannel(
                        user.getId(), content, AiCustomerServiceConstants.PRICE_INQUIRY_CONTEXT_HINT,
                        priceInquiry, true);
                replyContent = result.replyContent;
                isAiReply = result.isAiReply;
            } else if (isServiceInquiryOnly) {
                InquiryReplyResult result = handleInquiryWithAiChannel(
                        user.getId(), content, AiCustomerServiceConstants.SERVICE_INQUIRY_CONTEXT_HINT,
                        serviceInquiry, false);
                replyContent = result.replyContent;
                isAiReply = result.isAiReply;
            } else if (isScheduleInquiryOnly) {
                InquiryReplyResult result = handleInquiryWithAiChannel(
                        user.getId(), content, AiCustomerServiceConstants.SCHEDULE_INQUIRY_CONTEXT_HINT,
                        scheduleInquiry, false);
                replyContent = result.replyContent;
                isAiReply = result.isAiReply;
            } else if (matchedKeyword != null) {
                String directReply = tryDirectKeywordReply(matchedKeyword, content);
                if (directReply != null) {
                    replyContent = directReply;
                    isAiReply = false;
                    resetAiConsecutiveFailure(user.getId());
                }
            }

            if (replyContent == null) {
                String contextHint = buildContextHint(matchedKeywords, needsHumanHandoff, orderIntent, humanExplicit);
                String aiReply = tryDeepSeekAI(user.getId(), content, contextHint);
                if (aiReply != null) {
                    replyContent = aiReply;
                    isAiReply = true;
                    resetAiConsecutiveFailure(user.getId());
                } else {
                    trackAiConsecutiveFailure(user.getId());
                }
            }

            if (replyContent == null) {
                replyContent = AiCustomerServiceConstants.DEFAULT_FALLBACK_REPLY;
                isAiReply = false;
            }

            saveOutgoingMessage(user.getId(), replyContent, isAiReply);

            if (needsHumanHandoff) {
                try {
                    String contextSummary = buildContextSummaryForHandoff(user.getId(), content, handoffReason);
                    boolean created = pendingMessageService.createPendingMessage(inMessage.getId(), user.getId(), matchedKeyword, content, PlatformConstants.WECHAT, contextSummary);
                    if (!created) {
                        log.info("微信待办消息跳过创建(已有工单): userId={}", user.getId());
                    }
                    boolean isEmotion = handoffReason != null && (handoffReason.contains("情绪") || handoffReason.contains("不满") || handoffReason.contains("投诉"));
                    boolean isOrderIntent = handoffReason != null && (handoffReason.contains("下单") || handoffReason.contains("预约") || handoffReason.contains("点单"));
                    customerProfileService.recordHandoffEvent(user.getId(), handoffReason, isEmotion, isOrderIntent);
                } catch (Exception e) {
                    log.error("【告警】创建微信待处理消息失败: userId={}", user.getId(), e);
                }
            }

            return replyContent;

        } catch (Exception e) {
            log.error("微信消息处理异常", e);
            return AiCustomerServiceConstants.WECHAT_ERROR_REPLY;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String processSubscribeEvent(String fromUser) {
        log.info("微信用户关注: fromUser={}", fromUser);
        try {
            User user = getOrCreateUser(fromUser);
            String welcomeReply = replyService.getWelcomeReply();
            if (welcomeReply == null) {
                welcomeReply = AiCustomerServiceConstants.WECHAT_WELCOME_REPLY;
            }
            saveOutgoingMessage(user.getId(), welcomeReply, true);
            return welcomeReply;
        } catch (Exception e) {
            log.error("处理关注事件异常", e);
            return AiCustomerServiceConstants.WECHAT_WELCOME_FALLBACK_REPLY;
        }
    }

    private User getOrCreateUser(String fromUser) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPlatform, PlatformConstants.WECHAT);
        wrapper.eq(User::getPlatformUserId, fromUser);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            user = new User();
            user.setPlatform(PlatformConstants.WECHAT);
            user.setPlatformUserId(fromUser);
            user.setNickname("微信用户" + fromUser.substring(Math.max(0, fromUser.length() - 6)));
            user.setAiEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
        return user;
    }
}
