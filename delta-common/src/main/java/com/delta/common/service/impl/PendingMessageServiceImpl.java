package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.PendingMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PendingMessageServiceImpl implements PendingMessageService {

    private static final Logger log = LoggerFactory.getLogger(PendingMessageServiceImpl.class);

    private final PendingMessageMapper pendingMessageMapper;

    private final UserMapper userMapper;

    private final SysUserMapper sysUserMapper;

    private final RedisService redisService;

    private static final String PENDING_COUNT_KEY = "delta:pending:count";
    private static final long PENDING_COUNT_TTL = 5;

    @Override
    public Page<PendingMessageVO> getPendingMessagePage(Integer page, Integer size, String status, String platform, String keyword) {
        Page<PendingMessage> pendingPage = new Page<>(page, size);
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getStatus, status);
        }

        if (platform != null && !platform.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getPlatform, platform);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(PendingMessage::getContent, keyword);
        }

        wrapper.orderByDesc(PendingMessage::getCreatedAt);

        Page<PendingMessage> pendingPageResult = pendingMessageMapper.selectPage(pendingPage, wrapper);
        List<PendingMessageVO> voList = buildVOList(pendingPageResult.getRecords());

        Page<PendingMessageVO> resultPage = new Page<>(pendingPageResult.getCurrent(), pendingPageResult.getSize(), pendingPageResult.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public Page<PendingMessageVO> getPendingMessagePage(Integer page, Integer size, String status, String platform, String keyword, Long currentUserId, String currentUserRole) {
        Page<PendingMessage> pendingPage = new Page<>(page, size);
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getStatus, status);
        }

        if (platform != null && !platform.trim().isEmpty()) {
            wrapper.eq(PendingMessage::getPlatform, platform);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(PendingMessage::getContent, keyword);
        }

        if ("CS_STAFF".equals(currentUserRole)) {
            wrapper.and(w -> w.eq(PendingMessage::getAssignedCsUserId, currentUserId)
                    .or().isNull(PendingMessage::getAssignedCsUserId));
        }

        wrapper.orderByDesc(PendingMessage::getCreatedAt);

        Page<PendingMessage> pendingPageResult = pendingMessageMapper.selectPage(pendingPage, wrapper);
        List<PendingMessageVO> voList = buildVOList(pendingPageResult.getRecords());

        Page<PendingMessageVO> resultPage = new Page<>(pendingPageResult.getCurrent(), pendingPageResult.getSize(), pendingPageResult.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public PendingMessageVO getPendingMessageById(Long id) {
        PendingMessage pm = pendingMessageMapper.selectById(id);
        if (pm == null) {
            return null;
        }
        List<PendingMessage> list = Collections.singletonList(pm);
        List<PendingMessageVO> voList = buildVOList(list);
        return voList.isEmpty() ? null : voList.get(0);
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
            throw new RuntimeException("待处理消息不存在");
        }
        String targetStatus = handleDTO.getStatus().toUpperCase();
        pendingMessage.setStatus(targetStatus);
        pendingMessage.setRemark(handleDTO.getRemark());
        Long handlerUserId = handleDTO.getHandledBy() != null ? handleDTO.getHandledBy() : currentUserId;
        pendingMessage.setAssignedCsUserId(handlerUserId);
        pendingMessage.setHandledBy(handlerUserId);
        pendingMessage.setHandledAt(LocalDateTime.now());
        pendingMessageMapper.updateById(pendingMessage);
        log.info("处理待处理消息成功: id={}, status={}, handlerId={}", handleDTO.getId(), targetStatus, handlerUserId);
        redisService.delete(PENDING_COUNT_KEY);
    }

    @Override
    public boolean createPendingMessage(Long messageId, Long userId, String keyword, String messageContent) {
        return createPendingMessage(messageId, userId, keyword, messageContent, "WECHAT");
    }

    @Override
    public boolean createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform) {
        return createPendingMessage(messageId, userId, keyword, messageContent, platform, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform, String contextSummary) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("创建待处理消息失败：用户不存在, userId={}", userId);
            return false;
        }

        LambdaQueryWrapper<PendingMessage> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(PendingMessage::getUserId, userId);
        existWrapper.in(PendingMessage::getStatus, "PENDING", "PROCESSING");
        if (pendingMessageMapper.selectCount(existWrapper) > 0) {
            log.info("用户已有进行中的待处理消息，跳过创建: userId={}", userId);
            return false;
        }

        PendingMessage pendingMessage = new PendingMessage();
        pendingMessage.setUserId(userId);
        pendingMessage.setPlatform(platform != null ? platform : user.getPlatform());
        pendingMessage.setContent(messageContent);
        pendingMessage.setContentType("TEXT");
        pendingMessage.setPendingReason(keyword != null ? "KEYWORD_ESCALATE" : "NEED_HUMAN");
        pendingMessage.setPriority("NORMAL");
        pendingMessage.setMessageId(messageId);
        pendingMessage.setKeyword(keyword);
        pendingMessage.setStatus("PENDING");
        pendingMessage.setDeadline(LocalDateTime.now().plusMinutes(30));
        pendingMessage.setEscalationLevel(0);
        pendingMessage.setReminderCount(0);
        pendingMessage.setContextSummary(contextSummary);

        pendingMessageMapper.insert(pendingMessage);
        log.info("创建待处理消息成功: id={}, platform={}, deadline={}", pendingMessage.getId(), platform, pendingMessage.getDeadline());

        redisService.delete(PENDING_COUNT_KEY);
        return true;
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
        Long count = pendingMessageMapper.selectCount(wrapper);
        if (count == null) count = 0L;

        redisService.set(PENDING_COUNT_KEY, count, PENDING_COUNT_TTL, TimeUnit.MINUTES);
        return count;
    }

    @Override
    public Long getPendingCount(Long currentUserId, String currentUserRole) {
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
        if ("CS_STAFF".equals(currentUserRole)) {
            wrapper.and(w -> w.eq(PendingMessage::getAssignedCsUserId, currentUserId)
                    .or().isNull(PendingMessage::getAssignedCsUserId));
        }
        Object cached = redisService.get(PENDING_COUNT_KEY + ":" + currentUserId);
        if (cached != null) {
            try {
                return Long.parseLong(cached.toString());
            } catch (Exception e) {
                log.warn("解析缓存失败: {}", cached);
            }
        }
        Long count = pendingMessageMapper.selectCount(wrapper);
        if (count == null) count = 0L;
        redisService.set(PENDING_COUNT_KEY + ":" + currentUserId, count, PENDING_COUNT_TTL, TimeUnit.MINUTES);
        return count;
    }

    private List<PendingMessageVO> buildVOList(List<PendingMessage> records) {
        List<PendingMessageVO> result = new ArrayList<>(records.size());
        for (PendingMessage pm : records) {
            PendingMessageVO vo = BeanUtil.copyProperties(pm, PendingMessageVO.class);
            vo.setMessageContent(pm.getContent());
            vo.setInterventionType(pm.getPendingReason());
            if ("KEYWORD_ESCALATE".equals(pm.getPendingReason())) {
                vo.setInterventionTypeDesc("关键词触发");
            } else if ("NEED_HUMAN".equals(pm.getPendingReason())) {
                vo.setInterventionTypeDesc("人工请求");
            } else {
                vo.setInterventionTypeDesc(pm.getPendingReason());
            }

            if ("PENDING".equals(pm.getStatus())) {
                vo.setStatusDesc("待处理");
            } else if ("PROCESSING".equals(pm.getStatus())) {
                vo.setStatusDesc("处理中");
            } else if ("RESOLVED".equals(pm.getStatus())) {
                vo.setStatusDesc("已解决");
            } else {
                vo.setStatusDesc(pm.getStatus());
            }

            User user = userMapper.selectById(pm.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserPlatform(user.getPlatform());
            } else {
                vo.setUserNickname("未知用户");
                vo.setUserPlatform(pm.getPlatform());
            }

            if (pm.getAssignedCsUserId() != null) {
                SysUser csUser = sysUserMapper.selectById(pm.getAssignedCsUserId());
                if (csUser != null) {
                    vo.setAssignedCsUserName(csUser.getRealName() != null ? csUser.getRealName() : csUser.getUsername());
                }
            }

            if (pm.getHandledBy() != null) {
                SysUser handler = sysUserMapper.selectById(pm.getHandledBy());
                if (handler != null) {
                    vo.setHandledByName(handler.getRealName() != null ? handler.getRealName() : handler.getUsername());
                }
            }

            result.add(vo);
        }
        return result;
    }
}
