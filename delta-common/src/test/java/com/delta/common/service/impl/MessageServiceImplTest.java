package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.vo.MessageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    @DisplayName("分页查询消息 - 无过滤条件返回分页结果")
    void getMessagePage_noFilter_shouldReturnPagedResults() {
        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<MessageVO> result = messageService.getMessagePage(1, 10, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询消息 - 按用户ID过滤")
    void getMessagePage_byUserId_shouldFilterByUserId() {
        Message msg = new Message();
        msg.setId(1L);
        msg.setUserId(100L);
        msg.setContent("你好");
        msg.setDirection("IN");
        msg.setCreatedAt(LocalDateTime.now());

        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(msg));
        mockPage.setTotal(1);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        User user = new User();
        user.setId(100L);
        user.setNickname("测试用户");
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(user));

        Page<MessageVO> result = messageService.getMessagePage(1, 10, 100L, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("测试用户", result.getRecords().get(0).getUserNickname());
    }

    @Test
    @DisplayName("分页查询消息 - 按平台过滤，平台无用户返回空页")
    void getMessagePage_byPlatform_noPlatformUsers_shouldReturnEmpty() {
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Page<MessageVO> result = messageService.getMessagePage(1, 10, null, "wechat", null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("分页查询消息 - 按平台过滤，有平台用户返回消息")
    void getMessagePage_byPlatform_hasPlatformUsers_shouldReturnMessages() {
        User platformUser = new User();
        platformUser.setId(100L);
        platformUser.setPlatform("wechat");
        platformUser.setNickname("微信用户");
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(platformUser));

        Message msg = new Message();
        msg.setId(1L);
        msg.setUserId(100L);
        msg.setContent("你好");
        msg.setDirection("IN");
        msg.setCreatedAt(LocalDateTime.now());

        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(msg));
        mockPage.setTotal(1);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(platformUser));

        Page<MessageVO> result = messageService.getMessagePage(1, 10, null, "wechat", null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("分页查询消息 - 按方向过滤")
    void getMessagePage_byDirection_shouldApplyFilter() {
        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<MessageVO> result = messageService.getMessagePage(1, 10, null, null, "IN", null, null, null);

        assertNotNull(result);
        verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询消息 - 按AI消息过滤")
    void getMessagePage_byAiFlag_shouldApplyFilter() {
        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<MessageVO> result = messageService.getMessagePage(1, 10, null, null, null, true, null, null);

        assertNotNull(result);
        verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询消息 - 按关键词搜索")
    void getMessagePage_byKeyword_shouldApplyFilter() {
        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<MessageVO> result = messageService.getMessagePage(1, 10, null, null, null, null, null, "退款");

        assertNotNull(result);
        verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询消息 - 填充用户昵称和平台信息")
    void getMessagePage_shouldPopulateUserNicknameAndPlatform() {
        Message msg = new Message();
        msg.setId(1L);
        msg.setUserId(100L);
        msg.setContent("测试消息");
        msg.setDirection("IN");
        msg.setCreatedAt(LocalDateTime.now());

        Page<Message> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(msg));
        mockPage.setTotal(1);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        User user = new User();
        user.setId(100L);
        user.setNickname("小明");
        user.setPlatform("wechat");
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(user));

        Page<MessageVO> result = messageService.getMessagePage(1, 10, 100L, null, null, null, null, null);

        assertNotNull(result);
        MessageVO vo = result.getRecords().get(0);
        assertEquals("小明", vo.getUserNickname());
        assertEquals("wechat", vo.getUserPlatform());
    }
}
