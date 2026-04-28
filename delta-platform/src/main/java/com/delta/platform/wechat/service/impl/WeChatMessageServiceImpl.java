package com.delta.platform.wechat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.PlatformConstants;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.impl.BaseMessageProcessService;
import com.delta.platform.wechat.service.WeChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeChatMessageServiceImpl extends BaseMessageProcessService implements WeChatMessageService {

    @Autowired
    private UserMapper userMapper;

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
            String humanExplicit = checkHumanExplicit(content);
            String negativeEmotion = checkNegativeEmotion(content);
            boolean aiConsecutiveFailure = checkAiConsecutiveFailure(user.getId());
            boolean needsHumanHandoff = false;
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
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = orderIntent;
                handoffReason = orderIntent;
            } else if (aiConsecutiveFailure) {
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = AiCustomerServiceConstants.HANDOFF_REASON_AI_CONSECUTIVE;
                handoffReason = AiCustomerServiceConstants.HANDOFF_REASON_AI_CONSECUTIVE;
            }

            String replyContent = null;
            boolean isAiReply = false;

            if (needsHumanHandoff) {
                replyContent = getHandoffReply(handoffReason, negativeEmotion, aiConsecutiveFailure);
                isAiReply = false;
                resetAiConsecutiveFailure(user.getId());
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
