package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.constant.MessageConstants;
import com.delta.common.constant.PlatformConstants;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.entity.Message;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.enums.InterventionTypeEnum;
import com.delta.common.enums.PendingMessageStatusEnum;
import com.delta.common.event.PendingMessageCreatedEvent;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.NotificationVO;
import com.delta.common.vo.PendingMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 待处理消息服务实现，管理人工客服升级工单的创建、分配、处理和满意度评价
 *
 * @author delta
 */
@Service
public class PendingMessageServiceImpl implements PendingMessageService {

    private static final Logger log = LoggerFactory.getLogger(PendingMessageServiceImpl.class);

    private static final int DEADLINE_SECONDS = 300;

    @Autowired
    private PendingMessageMapper pendingMessageMapper;

    @Autowired
    private CsUserCustomerMapper csUserCustomerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RedisService redisService;

    private static final String PENDING_COUNT_KEY = "delta:pending:count";
    private static final long PENDING_COUNT_TTL = 5;

    @Override
    public Page<PendingMessageVO> getPendingMessagePage(Integer pageNum, Integer pageSize, String status, String platform, String keyword) {
        Page<PendingMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getStatus, status);
        }

        if (platform != null && !platform.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getPlatform, platform);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(PendingMessage::getKeyword, keyword);
        }

        wrapper.orderByDesc(PendingMessage::getCreatedAt);

        Page<PendingMessage> pendingMessagePage = pendingMessageMapper.selectPage(page, wrapper);
        List<PendingMessageVO> voList = buildVOList(pendingMessagePage.getRecords());

        Page<PendingMessageVO> resultPage = new Page<>(pendingMessagePage.getCurrent(), pendingMessagePage.getSize(), pendingMessagePage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePendingMessage(PendingMessageHandleDTO handleDTO) {
        handlePendingMessage(handleDTO, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePendingMessage(PendingMessageHandleDTO handleDTO, Long currentUserId, String currentUserRole) {
        PendingMessage pendingMessage = pendingMessageMapper.selectById(handleDTO.getId());
        if (pendingMessage == null) {
            throw new BusinessException("待处理消息不存在");
        }

        if (currentUserId != null && BusinessStatusConstants.ROLE_CS_STAFF.equals(currentUserRole)) {
            if (!isCsStaffAuthorizedForPendingMessage(pendingMessage, currentUserId)) {
                throw new BusinessException("您无权处理此待办事项，只能处理分配给您的客户工单");
            }
        }

        String newStatus = handleDTO.getStatus();
        String oldStatus = pendingMessage.getStatus();

        if (!isValidStatus(newStatus)) {
            throw new BusinessException("无效的处理状态: " + newStatus);
        }

        if (!isValidTransition(oldStatus, newStatus)) {
            throw new BusinessException("不允许从[" + oldStatus + "]转换到[" + newStatus + "]");
        }

        pendingMessage.setStatus(newStatus);
        pendingMessage.setRemark(handleDTO.getRemark());
        pendingMessage.setHandledAt(LocalDateTime.now());
        if (handleDTO.getHandledBy() != null) {
            pendingMessage.setHandledBy(handleDTO.getHandledBy());
        }

        pendingMessageMapper.updateById(pendingMessage);
        log.info("处理待处理消息成功: id={}, {}->{}", handleDTO.getId(), oldStatus, newStatus);

        if (BusinessStatusConstants.PENDING_STATUS_RESOLVED.equals(newStatus) && !BusinessStatusConstants.PENDING_STATUS_RESOLVED.equals(oldStatus)) {
            sendSatisfactionPrompt(pendingMessage);
        }

        redisService.delete(PENDING_COUNT_KEY);
    }

    private boolean isCsStaffAuthorizedForPendingMessage(PendingMessage pm, Long csUserId) {
        if (pm.getAssignedCsUserId() != null && pm.getAssignedCsUserId().equals(csUserId)) {
            return true;
        }

        LambdaQueryWrapper<CsUserCustomer> csWrapper = new LambdaQueryWrapper<>();
        csWrapper.eq(CsUserCustomer::getCsUserId, csUserId);
        csWrapper.eq(CsUserCustomer::getCustomerUserId, pm.getUserId());
        csWrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        csWrapper.eq(CsUserCustomer::getDeleted, BusinessStatusConstants.NOT_DELETED);
        if (csUserCustomerMapper.selectCount(csWrapper) > 0) {
            return true;
        }

        User user = userMapper.selectById(pm.getUserId());
        if (user != null && user.getAssignedCsUserId() != null && user.getAssignedCsUserId().equals(csUserId)) {
            return true;
        }

        return false;
    }

    private boolean isValidStatus(String status) {
        return BusinessStatusConstants.PENDING_STATUS_PENDING.equals(status)
                || BusinessStatusConstants.PENDING_STATUS_PROCESSING.equals(status)
                || BusinessStatusConstants.PENDING_STATUS_RESOLVED.equals(status);
    }

    private boolean isValidTransition(String from, String to) {
        if (from.equals(to)) return true;
        if (BusinessStatusConstants.PENDING_STATUS_PENDING.equals(from)) {
            return BusinessStatusConstants.PENDING_STATUS_PROCESSING.equals(to)
                    || BusinessStatusConstants.PENDING_STATUS_RESOLVED.equals(to);
        }
        if (BusinessStatusConstants.PENDING_STATUS_PROCESSING.equals(from)) {
            return BusinessStatusConstants.PENDING_STATUS_RESOLVED.equals(to)
                    || BusinessStatusConstants.PENDING_STATUS_PENDING.equals(to);
        }
        return false;
    }

    private void sendSatisfactionPrompt(PendingMessage pm) {
        try {
            Message satisfactionMsg = new Message();
            satisfactionMsg.setUserId(pm.getUserId());
            satisfactionMsg.setDirection(MessageConstants.DIRECTION_OUT);
            satisfactionMsg.setContent(AiCustomerServiceConstants.SATISFACTION_PROMPT);
            satisfactionMsg.setAi(false);
            satisfactionMsg.setKeywordTriggered(false);
            satisfactionMsg.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(satisfactionMsg);
            log.info("已发送满意度评价提示: userId={}", pm.getUserId());
        } catch (Exception e) {
            log.warn("发送满意度评价提示失败: userId={}", pm.getUserId(), e);
        }
    }

    @Override
    public void createPendingMessage(Long messageId, Long userId, String keyword, String messageContent) {
        createPendingMessage(messageId, userId, keyword, messageContent, PlatformConstants.WECHAT);
    }

    @Override
    public void createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform) {
        createPendingMessage(messageId, userId, keyword, messageContent, platform, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform, String contextSummary) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("创建待处理消息失败：用户不存在, userId={}", userId);
            throw new BusinessException("用户不存在，无法创建待处理消息");
        }

        LambdaQueryWrapper<PendingMessage> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(PendingMessage::getUserId, userId);
        existWrapper.in(PendingMessage::getStatus, BusinessStatusConstants.PENDING_STATUS_PENDING, BusinessStatusConstants.PENDING_STATUS_PROCESSING);
        if (pendingMessageMapper.selectCount(existWrapper) > 0) {
            log.info("用户已有待处理工单，跳过创建: userId={}", userId);
            return;
        }

        InterventionTypeEnum interventionType = InterventionTypeEnum.fromKeyword(keyword);

        String summary = contextSummary;
        if (summary == null || summary.isEmpty()) {
            summary = buildContextSummary(userId, messageContent, keyword);
        }

        PendingMessage pendingMessage = new PendingMessage();
        pendingMessage.setMessageId(messageId);
        pendingMessage.setUserId(userId);
        pendingMessage.setPlatform(platform != null ? platform : user.getPlatform());
        pendingMessage.setKeyword(keyword);
        pendingMessage.setInterventionType(interventionType.getCode());
        pendingMessage.setStatus(PendingMessageStatusEnum.PENDING.getCode());
        pendingMessage.setDeadline(LocalDateTime.now().plusSeconds(DEADLINE_SECONDS));
        pendingMessage.setEscalationLevel(0);
        pendingMessage.setReminderCount(0);
        pendingMessage.setContextSummary(summary);

        assignCsStaff(pendingMessage, userId);

        pendingMessageMapper.insert(pendingMessage);
        log.info("创建待处理消息成功: id={}, platform={}, type={}, deadline={}",
                pendingMessage.getId(), platform, interventionType.getDesc(), pendingMessage.getDeadline());

        String nickname = user.getNickname();

        NotificationVO notification = new NotificationVO();
        notification.setType("pending_message");
        notification.setPendingMessageId(pendingMessage.getId());
        notification.setUserId(userId);
        notification.setUserNickname(nickname);
        notification.setKeyword(keyword);
        notification.setInterventionType(interventionType.getDesc());
        notification.setPlatform(platform);
        notification.setMessageContent(messageContent);
        notification.setDeadline(pendingMessage.getDeadline().toString());
        notification.setContextSummary(summary);
        notification.setTimestamp(System.currentTimeMillis());

        eventPublisher.publishEvent(new PendingMessageCreatedEvent(this, notification));

        redisService.delete(PENDING_COUNT_KEY);
    }

    private String buildContextSummary(Long userId, String currentMessage, String keyword) {
        try {
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getUserId, userId);
            wrapper.orderByDesc(Message::getCreatedAt);
            Page<Message> page = new Page<>(1, ExportConstants.RECENT_MESSAGES_LIMIT);
            Page<Message> resultPage = messageMapper.selectPage(page, wrapper);
            List<Message> recentMessages = resultPage.getRecords();

            if (recentMessages.isEmpty()) {
                return "客户发送: \"" + currentMessage + "\"，触发关键词: " + keyword;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【近期对话】\n");

            for (int i = recentMessages.size() - 1; i >= 0; i--) {
                Message msg = recentMessages.get(i);
                String role = MessageConstants.DIRECTION_IN.equals(msg.getDirection()) ? "客户" : (msg.getAi() ? "AI" : "客服");
                String content = msg.getContent();
                if (content == null) {
                    content = "";
                } else if (content.length() > ExportConstants.PENDING_MESSAGE_CONTENT_TRUNCATION) {
                    content = content.substring(0, ExportConstants.PENDING_MESSAGE_CONTENT_TRUNCATION) + "...";
                }
                sb.append(role).append(": ").append(content).append("\n");
            }

            sb.append("【触发原因】关键词: ").append(keyword);
            return sb.toString();
        } catch (Exception e) {
            log.warn("生成上下文摘要失败: userId={}", userId, e);
            return "客户发送: \"" + currentMessage + "\"，触发关键词: " + keyword;
        }
    }

    private void assignCsStaff(PendingMessage pm, Long userId) {
        try {
            LambdaQueryWrapper<CsUserCustomer> csWrapper = new LambdaQueryWrapper<>();
            csWrapper.eq(CsUserCustomer::getCustomerUserId, userId);
            csWrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
            csWrapper.last("LIMIT 1");
            CsUserCustomer assignment = csUserCustomerMapper.selectOne(csWrapper);
            if (assignment != null) {
                pm.setAssignedCsUserId(assignment.getCsUserId());
            }
        } catch (Exception e) {
            log.debug("分配客服失败，使用默认分配: userId={}", userId, e);
        }
    }

    @Override
    public Long getPendingCount() {
        Object cached = redisService.get(PENDING_COUNT_KEY);
        if (cached != null) {
            try {
                return Long.parseLong(cached.toString());
            } catch (Exception e) {
                log.warn("解析缓存失败: {}", cached);
            }
        }

        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingMessage::getStatus, PendingMessageStatusEnum.PENDING.getCode());
        Long count = pendingMessageMapper.selectCount(wrapper);
        if (count == null) {
            count = 0L;
        }

        redisService.set(PENDING_COUNT_KEY, count, PENDING_COUNT_TTL, TimeUnit.MINUTES);
        return count;
    }

    @Override
    public Long getPendingCount(Long currentUserId, String currentUserRole) {
        if (currentUserId == null || currentUserRole == null) {
            return getPendingCount();
        }

        if (BusinessStatusConstants.ROLE_SYS_ADMIN.equals(currentUserRole) || BusinessStatusConstants.ROLE_CS_LEADER.equals(currentUserRole)) {
            return getPendingCount();
        }

        List<Long> customerIds = getCustomerIdsForCsStaff(currentUserId);

        if (customerIds.isEmpty()) return 0L;

        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingMessage::getStatus, PendingMessageStatusEnum.PENDING.getCode());
        wrapper.in(PendingMessage::getUserId, customerIds);
        return pendingMessageMapper.selectCount(wrapper);
    }

    @Override
    public Page<PendingMessageVO> getPendingMessagePage(Integer pageNum, Integer pageSize, String status, String platform, String keyword, Long currentUserId, String currentUserRole) {
        if (currentUserId == null || currentUserRole == null) {
            return getPendingMessagePage(pageNum, pageSize, status, platform, keyword);
        }

        if (BusinessStatusConstants.ROLE_SYS_ADMIN.equals(currentUserRole) || BusinessStatusConstants.ROLE_CS_LEADER.equals(currentUserRole)) {
            return getPendingMessagePage(pageNum, pageSize, status, platform, keyword);
        }

        List<Long> customerIds = getCustomerIdsForCsStaff(currentUserId);

        if (customerIds.isEmpty()) {
            Page<PendingMessageVO> emptyPage = new Page<>(pageNum, pageSize, 0);
            emptyPage.setRecords(List.of());
            return emptyPage;
        }

        Page<PendingMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PendingMessage::getUserId, customerIds);

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getStatus, status);
        }
        if (platform != null && !platform.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getPlatform, platform);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(PendingMessage::getKeyword, keyword);
        }
        wrapper.orderByDesc(PendingMessage::getCreatedAt);

        Page<PendingMessage> pendingMessagePage = pendingMessageMapper.selectPage(page, wrapper);
        List<PendingMessageVO> voList = buildVOList(pendingMessagePage.getRecords());

        Page<PendingMessageVO> resultPage = new Page<>(pendingMessagePage.getCurrent(), pendingMessagePage.getSize(), pendingMessagePage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    private List<Long> getCustomerIdsForCsStaff(Long csUserId) {
        LambdaQueryWrapper<CsUserCustomer> csWrapper = new LambdaQueryWrapper<>();
        csWrapper.eq(CsUserCustomer::getCsUserId, csUserId);
        csWrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        csWrapper.eq(CsUserCustomer::getDeleted, BusinessStatusConstants.NOT_DELETED);
        List<CsUserCustomer> assignments = csUserCustomerMapper.selectList(csWrapper);

        java.util.Set<Long> customerIdSet = assignments.stream()
                .map(CsUserCustomer::getCustomerUserId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getAssignedCsUserId, csUserId);
        userWrapper.select(User::getId);
        List<User> assignedUsers = userMapper.selectList(userWrapper);
        for (User user : assignedUsers) {
            customerIdSet.add(user.getId());
        }

        return new java.util.ArrayList<>(customerIdSet);
    }

    private List<PendingMessageVO> buildVOList(List<PendingMessage> records) {
        List<Long> userIds = records.stream()
                .map(PendingMessage::getUserId).distinct().collect(Collectors.toList());
        List<Long> messageIds = records.stream()
                .map(PendingMessage::getMessageId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> assignedCsIds = records.stream()
                .map(PendingMessage::getAssignedCsUserId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> handledByIds = records.stream()
                .map(PendingMessage::getHandledBy).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Message> messageMap = messageIds.isEmpty() ? Map.of() :
                messageMapper.selectBatchIds(messageIds).stream()
                        .collect(Collectors.toMap(Message::getId, m -> m));
        Map<Long, SysUser> csMap = assignedCsIds.isEmpty() ? Map.of() :
                sysUserMapper.selectBatchIds(assignedCsIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, SysUser> handledByMap = handledByIds.isEmpty() ? Map.of() :
                sysUserMapper.selectBatchIds(handledByIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        return records.stream().map(pm -> {
            PendingMessageVO vo = BeanUtil.copyProperties(pm, PendingMessageVO.class);

            User user = userMap.get(pm.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserPlatform(user.getPlatform());
            }

            Message msg = messageMap.get(pm.getMessageId());
            if (msg != null) {
                vo.setMessageContent(msg.getContent());
            }

            if (pm.getInterventionType() != null) {
                for (InterventionTypeEnum it : InterventionTypeEnum.values()) {
                    if (it.getCode().equals(pm.getInterventionType())) {
                        vo.setInterventionTypeDesc(it.getDesc());
                        break;
                    }
                }
            }

            if (pm.getStatus() != null) {
                switch (pm.getStatus()) {
                    case BusinessStatusConstants.PENDING_STATUS_PENDING: vo.setStatusDesc("待处理"); break;
                    case BusinessStatusConstants.PENDING_STATUS_PROCESSING: vo.setStatusDesc("处理中"); break;
                    case BusinessStatusConstants.PENDING_STATUS_RESOLVED: vo.setStatusDesc("已解决"); break;
                    default: vo.setStatusDesc(pm.getStatus()); break;
                }
            }

            if (pm.getAssignedCsUserId() != null) {
                SysUser csUser = csMap.get(pm.getAssignedCsUserId());
                if (csUser != null) {
                    vo.setAssignedCsUserName(csUser.getRealName());
                }
            }

            if (pm.getHandledBy() != null) {
                SysUser handler = handledByMap.get(pm.getHandledBy());
                if (handler != null) {
                vo.setHandledByName(handler.getRealName());
            }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
