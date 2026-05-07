package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.User;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.PendingMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PendingMessageServiceImpl implements PendingMessageService {

    private static final Logger log = LoggerFactory.getLogger(PendingMessageServiceImpl.class);

    private final PendingMessageMapper pendingMessageMapper;

    private final UserMapper userMapper;

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
        return getPendingMessagePage(page, size, status, platform, keyword);
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
        pendingMessage.setStatus(handleDTO.getStatus());
        pendingMessage.setRemark(handleDTO.getRemark());
        pendingMessageMapper.updateById(pendingMessage);
        log.info("处理待处理消息成功: id={}", handleDTO.getId());
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
        if (pendingMessageMapper.selectCount(existWrapper) > 0) {
            log.info("用户已有待处理消息，跳过创建: userId={}", userId);
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
        pendingMessage.setContextSummary(contextSummary);

        pendingMessageMapper.insert(pendingMessage);
        log.info("创建待处理消息成功: id={}, platform={}", pendingMessage.getId(), platform);

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
        return getPendingCount();
    }

    private List<PendingMessageVO> buildVOList(List<PendingMessage> records) {
        List<PendingMessageVO> result = new ArrayList<>(records.size());
        for (PendingMessage pm : records) {
            PendingMessageVO vo = BeanUtil.copyProperties(pm, PendingMessageVO.class);

            User user = userMapper.selectById(pm.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserPlatform(user.getPlatform());
            }

            result.add(vo);
        }
        return result;
    }
}
