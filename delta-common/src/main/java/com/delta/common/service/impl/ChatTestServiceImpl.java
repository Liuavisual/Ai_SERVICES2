package com.delta.common.service.impl;

import com.delta.common.dto.ChatTestSendDTO;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.ChatTestService;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.vo.ChatTestReplyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatTestServiceImpl extends BaseMessageProcessService implements ChatTestService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatTestReplyVO sendMessage(ChatTestSendDTO sendDTO) {
        String content = sendDTO.getContent();
        String platform = sendDTO.getPlatform();
        String customerNickname = sendDTO.getCustomerNickname();
        Long csUserId = sendDTO.getCsUserId();

        ChatTestReplyVO replyVO = new ChatTestReplyVO();
        replyVO.setAiReply(false);
        replyVO.setKeywordTriggered(false);

        try {
            User user = getOrCreateUser(platform, customerNickname, csUserId);
            Message inMessage = saveIncomingMessage(user.getId(), content);

            if (detectServiceCompletion(user.getId(), content)) {
                log.info("【检测到服务完成信号】userId={}", user.getId());
                boolean completed = handleServiceCompletion(user.getId(), content);
                if (completed) {
                    log.info("【自动完成订单+触发评价】userId={}", user.getId());
                }
            }

            HandoffState handoffState = getHandoffState(user.getId());

            if (handoffState == HandoffState.IN_SERVICE) {
                log.info("【客服服务中】userId={}，AI完全静默", user.getId());
                saveOutgoingMessage(user.getId(), IN_SERVICE_REPLY, false);
                replyVO.setReplyContent(IN_SERVICE_REPLY);
                replyVO.setAiReply(false);
                replyVO.setResponseSource("HANDOFF_IN_SERVICE");
                return replyVO;
            }

            if (handoffState == HandoffState.WAITING) {
                log.info("【等待客服接手】userId={}，回复等待提示", user.getId());
                String waitingReply = getWaitingReplyWithQueueInfo();
                saveOutgoingMessage(user.getId(), waitingReply, false);
                replyVO.setReplyContent(waitingReply);
                replyVO.setAiReply(false);
                replyVO.setResponseSource("HANDOFF_WAITING");
                return replyVO;
            }

            List<String> matchedKeywords = keywordMatcherService.matchKeywords(content);
            String matchedKeyword = null;
            if (!matchedKeywords.isEmpty()) {
                matchedKeyword = matchedKeywords.get(0);
                replyVO.setKeywordTriggered(true);
                inMessage.setKeywordTriggered(true);
            }

            String orderIntent = checkOrderIntent(content);
            String humanExplicit = checkHumanExplicit(content);
            String negativeEmotion = checkNegativeEmotion(content);
            boolean aiConsecutiveFailure = checkAiConsecutiveFailure(user.getId());
            boolean needsHumanHandoff = false;
            String handoffReason = null;

            if (humanExplicit != null) {
                log.info("【客户明确要求人工】关键词: {}", humanExplicit);
                needsHumanHandoff = true;
                matchedKeyword = humanExplicit;
                handoffReason = humanExplicit;
            } else if (negativeEmotion != null) {
                log.info("【检测到负面情绪】关键词: {}", negativeEmotion);
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = negativeEmotion;
                handoffReason = negativeEmotion;
            } else if (orderIntent != null) {
                log.info("【检测到下单/预约意图】关键词: {}", orderIntent);
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = orderIntent;
                handoffReason = orderIntent;
            } else if (aiConsecutiveFailure) {
                log.info("【AI连续未解决】自动触发转人工");
                needsHumanHandoff = true;
                if (matchedKeyword == null) matchedKeyword = "AI连续未解决";
                handoffReason = "AI连续未解决";
            }

            String replyContent = null;
            boolean isAiReply = false;
            ResponseSource responseSource = ResponseSource.UNKNOWN;

            if (needsHumanHandoff) {
                replyContent = getHandoffReply(handoffReason, negativeEmotion, aiConsecutiveFailure);
                isAiReply = false;
                responseSource = ResponseSource.HANDOFF_REPLY;
                resetAiConsecutiveFailure(user.getId());
            } else if (matchedKeyword != null) {
                String directReply = tryDirectKeywordReply(matchedKeyword, content);
                if (directReply != null) {
                    replyContent = directReply;
                    isAiReply = false;
                    responseSource = ResponseSource.KEYWORD_DIRECT;
                    resetAiConsecutiveFailure(user.getId());
                }
            }

            if (replyContent == null) {
                String contextHint = buildContextHint(matchedKeywords, needsHumanHandoff, orderIntent, humanExplicit);
                String aiReply = tryDeepSeekAI(user.getId(), content, contextHint);
                if (aiReply != null) {
                    replyContent = aiReply;
                    isAiReply = true;
                    responseSource = ResponseSource.AI_REPLY;
                    trackAiConsecutiveFailure(user.getId());
                }
            }

            if (replyContent == null) {
                replyContent = AiCustomerServiceConstants.DEFAULT_FALLBACK_REPLY;
                isAiReply = false;
                responseSource = ResponseSource.DEFAULT_FALLBACK;
            }

            messageMapper.updateById(inMessage);
            saveOutgoingMessage(user.getId(), replyContent, isAiReply);

            if (needsHumanHandoff) {
                try {
                    String contextSummary = buildContextSummaryForHandoff(user.getId(), content, handoffReason);
                    boolean created = pendingMessageService.createPendingMessage(inMessage.getId(), user.getId(), matchedKeyword, content, sendDTO.getPlatform(), contextSummary);
                    replyVO.setPendingMessageCreated(created);
                    if (created) {
                        log.info("待办消息创建成功: userId={}", user.getId());
                    } else {
                        replyVO.setPendingFailureReason("用户已有待处理工单");
                        log.info("待办消息跳过创建(已有工单): userId={}", user.getId());
                    }
                } catch (BusinessException be) {
                    log.warn("待办消息业务校验未通过: userId={}, reason={}", user.getId(), be.getMessage());
                    replyVO.setPendingMessageCreated(false);
                    replyVO.setPendingFailureReason(be.getMessage());
                } catch (Exception e) {
                    log.error("【告警】待办消息创建系统异常: userId={}", user.getId(), e);
                    replyVO.setPendingMessageCreated(false);
                    replyVO.setPendingFailureReason("系统异常: " + e.getMessage());
                }
                try {
                    boolean isEmotion = handoffReason != null && (handoffReason.contains("情绪") || handoffReason.contains("不满") || handoffReason.contains("投诉"));
                    boolean isOrderIntent = handoffReason != null && (handoffReason.contains("下单") || handoffReason.contains("预约") || handoffReason.contains("点单"));
                    customerProfileService.recordHandoffEvent(user.getId(), handoffReason, isEmotion, isOrderIntent);
                } catch (Exception e) {
                    log.warn("记录转人工事件失败: userId={}", user.getId(), e);
                }
            }

            replyVO.setReplyContent(replyContent);
            replyVO.setAiReply(isAiReply);
            replyVO.setMatchedKeyword(matchedKeyword);
            replyVO.setMessageId(inMessage.getId());
            replyVO.setResponseSource(responseSource.name());

        } catch (Exception e) {
            log.error("消息处理异常", e);
            replyVO.setReplyContent(AiCustomerServiceConstants.CHAT_TEST_ERROR_REPLY);
            replyVO.setAiReply(false);
            replyVO.setResponseSource(ResponseSource.DEFAULT_FALLBACK.name());
        }

        return replyVO;
    }

    private User getOrCreateUser(String platform, String nickname, Long assignedCsUserId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>();
        wrapper.eq(User::getPlatform, platform);
        wrapper.eq(User::getPlatformUserId, "test_" + platform + "_" + nickname);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            user = new User();
            user.setPlatform(platform);
            user.setPlatformUserId("test_" + platform + "_" + nickname);
            user.setNickname(nickname);
            user.setAiEnabled(true);
            user.setAssignedCsUserId(assignedCsUserId);
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
        return user;
    }
}
