package com.delta.platform.wework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.PlatformConstants;
import com.delta.common.constant.WeWorkConstants;
import com.delta.common.dto.WeWorkCallbackDTO;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.OrderService;
import com.delta.common.service.impl.BaseMessageProcessService;
import com.delta.platform.wework.service.WeWorkApiService;
import com.delta.platform.wework.service.WeWorkMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "wework", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WeWorkMessageServiceImpl extends BaseMessageProcessService implements WeWorkMessageService {

    private final UserMapper userMapper;

    private final WeWorkApiService weWorkApiService;

    public WeWorkMessageServiceImpl(
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
            UserMapper userMapper,
            WeWorkApiService weWorkApiService) {
        super(messageMapper, pendingMessageMapper, pendingMessageService,
                keywordMatcherService, replyService, redisService, customerProfileService,
                clubConfigMapper, serviceItemMapper, activityPackageMapper, companionLevelMapper,
                servicePriceRuleMapper, companionMapper);
        this.deepSeekService = deepSeekService;
        this.orderService = orderService;
        this.userMapper = userMapper;
        this.weWorkApiService = weWorkApiService;
    }

    @Override
    public void handleCallback(WeWorkCallbackDTO callbackDTO) {
        log.info("收到企业微信回调: msgType={}, eventType={}, fromUser={}",
                callbackDTO.getMsgType(), callbackDTO.getEventType(), callbackDTO.getFromUserName());

        String msgType = callbackDTO.getMsgType();
        if (WeWorkConstants.MSG_TYPE_TEXT.equals(msgType)) {
            handleTextMessage(callbackDTO.getFromUserName(), callbackDTO.getContent());
        } else if (WeWorkConstants.MSG_TYPE_IMAGE.equals(msgType)) {
            handleImageMessage(callbackDTO.getFromUserName(), callbackDTO.getContent());
        } else if (WeWorkConstants.MSG_TYPE_EVENT.equals(msgType)) {
            handleEvent(callbackDTO);
        } else {
            log.info("企业微信不支持的消息类型: {}", msgType);
        }
    }

    @Override
    public String handleTextMessage(String externalUserId, String content) {
        log.info("收到企业微信用户消息: externalUserId={}", externalUserId);

        try {
            User user = getOrCreateUser(externalUserId);
            Message inMessage = saveIncomingMessage(user.getId(), content);

            HandoffState handoffState = getHandoffState(user.getId());

            if (handoffState == HandoffState.IN_SERVICE) {
                saveOutgoingMessage(user.getId(), IN_SERVICE_REPLY, false);
                weWorkApiService.sendTextMessage(externalUserId, IN_SERVICE_REPLY);
                return IN_SERVICE_REPLY;
            }

            if (handoffState == HandoffState.WAITING) {
                String waitingReply = getWaitingReplyWithQueueInfo();
                saveOutgoingMessage(user.getId(), waitingReply, false);
                weWorkApiService.sendTextMessage(externalUserId, waitingReply);
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
                if (matchedKeyword == null) {
                    matchedKeyword = negativeEmotion;
                }
                handoffReason = negativeEmotion;
            } else if (orderIntent != null) {
                needsHumanHandoff = true;
                if (matchedKeyword == null) {
                    matchedKeyword = orderIntent;
                }
                handoffReason = orderIntent;
            } else if (aiConsecutiveFailure) {
                needsHumanHandoff = true;
                if (matchedKeyword == null) {
                    matchedKeyword = AiCustomerServiceConstants.HANDOFF_REASON_AI_CONSECUTIVE;
                }
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
                createHandoffPendingMessage(inMessage, user, matchedKeyword, content, handoffReason, negativeEmotion, orderIntent);
            }

            weWorkApiService.sendTextMessage(externalUserId, replyContent);
            return replyContent;

        } catch (Exception e) {
            log.error("企业微信消息处理异常: externalUserId={}", externalUserId, e);
            return AiCustomerServiceConstants.WECHAT_ERROR_REPLY;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAddExternalContact(String externalUserId, String userId) {
        log.info("企业微信客户添加: externalUserId={}, userId={}", externalUserId, userId);

        try {
            User user = getOrCreateUser(externalUserId);
            String welcomeReply = replyService.getWelcomeReply();
            if (welcomeReply == null) {
                welcomeReply = AiCustomerServiceConstants.WEWORK_WELCOME_REPLY;
            }
            saveOutgoingMessage(user.getId(), welcomeReply, true);
            weWorkApiService.sendWelcomeMessage(externalUserId, welcomeReply);
        } catch (Exception e) {
            log.error("处理企业微信添加客户事件异常: externalUserId={}", externalUserId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDelExternalContact(String externalUserId) {
        log.info("企业微信客户删除: externalUserId={}", externalUserId);

        try {
            User user = findUserByExternalId(externalUserId);
            if (user != null) {
                user.setAiEnabled(false);
                userMapper.updateById(user);
                log.info("已更新企业微信用户状态: userId={}", user.getId());
            }
        } catch (Exception e) {
            log.error("处理企业微信删除客户事件异常: externalUserId={}", externalUserId, e);
        }
    }

    private void handleImageMessage(String externalUserId, String picUrl) {
        log.info("收到企业微信图片消息: externalUserId={}", externalUserId);
        try {
            User user = getOrCreateUser(externalUserId);
            String imageContent = "[图片消息]";
            if (picUrl != null && !picUrl.isEmpty()) {
                imageContent = "[图片消息] " + picUrl;
            }
            saveIncomingMessage(user.getId(), imageContent);

            String reply = AiCustomerServiceConstants.WEWORK_IMAGE_REPLY;
            saveOutgoingMessage(user.getId(), reply, true);
            weWorkApiService.sendTextMessage(externalUserId, reply);
        } catch (Exception e) {
            log.error("处理企业微信图片消息异常: externalUserId={}", externalUserId, e);
        }
    }

    private void handleEvent(WeWorkCallbackDTO callbackDTO) {
        String eventType = callbackDTO.getEventType();
        if (eventType == null) {
            log.warn("企业微信事件消息缺少event字段");
            return;
        }

        switch (eventType) {
            case WeWorkConstants.EVENT_ADD_EXTERNAL_CONTACT:
                handleAddExternalContact(callbackDTO.getExternalUserId(), callbackDTO.getUserId());
                break;
            case WeWorkConstants.EVENT_DEL_EXTERNAL_CONTACT:
                handleDelExternalContact(callbackDTO.getExternalUserId());
                break;
            case WeWorkConstants.EVENT_CHANGE_EXTERNAL_CONTACT:
                log.info("企业微信外部联系人变更事件: externalUserId={}", callbackDTO.getExternalUserId());
                break;
            default:
                log.info("企业微信未处理的事件类型: {}", eventType);
                break;
        }
    }

    private void createHandoffPendingMessage(Message inMessage, User user, String matchedKeyword,
                                              String content, String handoffReason,
                                              String negativeEmotion, String orderIntent) {
        try {
            String contextSummary = buildContextSummaryForHandoff(user.getId(), content, handoffReason);
            boolean created = pendingMessageService.createPendingMessage(
                    inMessage.getId(), user.getId(), matchedKeyword, content,
                    PlatformConstants.WEWORK, contextSummary);
            if (!created) {
                log.info("企业微信待办消息跳过创建(已有工单): userId={}", user.getId());
            }

            boolean isEmotion = handoffReason != null
                    && (handoffReason.contains("情绪") || handoffReason.contains("不满") || handoffReason.contains("投诉"));
            boolean isOrderIntent = handoffReason != null
                    && (handoffReason.contains("下单") || handoffReason.contains("预约") || handoffReason.contains("点单"));
            customerProfileService.recordHandoffEvent(user.getId(), handoffReason, isEmotion, isOrderIntent);
        } catch (Exception e) {
            log.error("【告警】创建企业微信待处理消息失败: userId={}", user.getId(), e);
        }
    }

    private User getOrCreateUser(String externalUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPlatform, PlatformConstants.WEWORK);
        wrapper.eq(User::getPlatformUserId, externalUserId);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setPlatform(PlatformConstants.WEWORK);
            user.setPlatformUserId(externalUserId);
            user.setNickname("企微用户" + externalUserId.substring(Math.max(0, externalUserId.length() - 6)));
            user.setAiEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("创建企业微信新用户: externalUserId={}, userId={}", externalUserId, user.getId());
        }
        return user;
    }

    private User findUserByExternalId(String externalUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPlatform, PlatformConstants.WEWORK);
        wrapper.eq(User::getPlatformUserId, externalUserId);
        return userMapper.selectOne(wrapper);
    }
}
