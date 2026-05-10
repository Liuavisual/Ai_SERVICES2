package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.MessageService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    private final UserMapper userMapper;

    @Override
    public Page<MessageVO> getMessagePage(Integer page, Integer size, Long userId, String platform, String direction, Boolean isAi, Boolean keywordTriggered, String keyword) {
        Page<Message> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();

        if (platform != null && !platform.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getPlatform, platform);
            if (userId != null) {
                userWrapper.eq(User::getId, userId);
            }
            List<User> platformUsers = userMapper.selectList(userWrapper);
            Set<Long> platformUserIds = platformUsers.stream().map(User::getId).collect(Collectors.toSet());
            if (platformUserIds.isEmpty()) {
                Page<MessageVO> emptyPage = new Page<>(page, size, 0);
                emptyPage.setRecords(List.of());
                return emptyPage;
            }
            wrapper.in(Message::getUserId, platformUserIds);
        } else if (userId != null) {
            wrapper.eq(Message::getUserId, userId);
        }

        if (direction != null && !direction.isEmpty()) {
            wrapper.eq(Message::getDirection, direction);
        }

        if (isAi != null) {
            wrapper.eq(Message::getAi, isAi);
        }

        if (keywordTriggered != null) {
            wrapper.eq(Message::getKeywordTriggered, keywordTriggered);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Message::getContent, keyword);
        }

        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> messagePageResult = messageMapper.selectPage(pageObj, wrapper);

        List<Long> userIds = messagePageResult.getRecords().stream()
                .map(Message::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        List<MessageVO> voList = messagePageResult.getRecords().stream().map(msg -> {
            MessageVO vo = BeanUtil.copyProperties(msg, MessageVO.class);
            User user = userMap.get(msg.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserPlatform(user.getPlatform());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<MessageVO> resultPage = new Page<>(messagePageResult.getCurrent(), messagePageResult.getSize(), messagePageResult.getTotal());
        resultPage.setRecords(voList);
        VoUtils.setRowNumbers(resultPage);

        return resultPage;
    }
}
