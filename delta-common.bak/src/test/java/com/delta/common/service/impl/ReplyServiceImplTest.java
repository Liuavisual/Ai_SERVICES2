package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ReplyDTO;
import com.delta.common.entity.Reply;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ReplyMapper;
import com.delta.common.service.RedisService;
import com.delta.common.vo.ReplyVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class ReplyServiceImplTest {

    @Mock
    private ReplyMapper replyMapper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ReplyServiceImpl replyService;

    @Test
    @DisplayName("分页查询回复 - 无过滤条件返回分页结果")
    void getReplyPage_noFilter_shouldReturnPagedResults() {
        Page<Reply> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(replyMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<ReplyVO> result = replyService.getReplyPage(1, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询回复 - 按触发类型过滤")
    void getReplyPage_withTriggerType_shouldApplyFilter() {
        Page<Reply> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(replyMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<ReplyVO> result = replyService.getReplyPage(1, 10, "keyword");

        assertNotNull(result);
        verify(replyMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询回复 - 不存在抛出异常")
    void getReplyById_notExist_shouldThrow() {
        when(replyMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> replyService.getReplyById(999L));
    }

    @Test
    @DisplayName("根据ID查询回复 - 正常返回")
    void getReplyById_exist_shouldReturnReply() {
        Reply reply = new Reply();
        reply.setId(1L);
        reply.setTriggerType("keyword");
        reply.setTriggerKey("退款");
        reply.setContent("关于退款问题...");
        when(replyMapper.selectById(1L)).thenReturn(reply);

        ReplyVO result = replyService.getReplyById(1L);

        assertNotNull(result);
        assertEquals("退款", result.getTriggerKey());
    }

    @Test
    @DisplayName("创建回复 - 重复触发类型和键抛出异常")
    void createReply_duplicate_shouldThrow() {
        ReplyDTO dto = new ReplyDTO();
        dto.setTriggerType("keyword");
        dto.setTriggerKey("退款");
        dto.setContent("退款回复");
        dto.setEnabled(true);
        when(replyMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> replyService.createReply(dto));
    }

    @Test
    @DisplayName("创建回复 - 正常创建并清除缓存")
    void createReply_normal_shouldInsertAndClearCache() {
        ReplyDTO dto = new ReplyDTO();
        dto.setTriggerType("keyword");
        dto.setTriggerKey("退款");
        dto.setContent("退款回复");
        dto.setEnabled(true);
        when(replyMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(replyMapper.insert(any(Reply.class))).thenReturn(1);

        replyService.createReply(dto);

        verify(replyMapper).insert(any(Reply.class));
        verify(redisService).delete("delta:replies:welcome");
        verify(redisService).delete("delta:replies:default");
        verify(redisService).deleteByPattern("delta:keyword:reply:*");
    }

    @Test
    @DisplayName("更新回复 - 不存在抛出异常")
    void updateReply_notExist_shouldThrow() {
        ReplyDTO dto = new ReplyDTO();
        dto.setId(999L);
        when(replyMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> replyService.updateReply(dto));
    }

    @Test
    @DisplayName("删除回复 - 不存在抛出异常")
    void deleteReply_notExist_shouldThrow() {
        when(replyMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> replyService.deleteReply(999L));
    }

    @Test
    @DisplayName("删除回复 - 正常删除并清除缓存")
    void deleteReply_normal_shouldDeleteAndClearCache() {
        Reply reply = new Reply();
        reply.setId(1L);
        when(replyMapper.selectById(1L)).thenReturn(reply);
        when(replyMapper.deleteById(1L)).thenReturn(1);

        replyService.deleteReply(1L);

        verify(replyMapper).deleteById(1L);
        verify(redisService).delete("delta:replies:welcome");
    }

    @Test
    @DisplayName("获取欢迎语 - 缓存命中返回缓存值")
    void getWelcomeReply_cached_shouldReturnCachedValue() {
        when(redisService.get("delta:replies:welcome")).thenReturn("欢迎光临！");

        String result = replyService.getWelcomeReply();

        assertEquals("欢迎光临！", result);
        verify(replyMapper, never()).selectPage(any(), any());
    }

    @Test
    @DisplayName("获取欢迎语 - 缓存未命中从数据库查询")
    void getWelcomeReply_cacheMiss_shouldQueryDatabase() {
        when(redisService.get("delta:replies:welcome")).thenReturn(null);

        Reply reply = new Reply();
        reply.setContent("欢迎咨询！");
        Page<Reply> page = new Page<>(1, 1);
        page.setRecords(List.of(reply));
        when(replyMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        String result = replyService.getWelcomeReply();

        assertEquals("欢迎咨询！", result);
        verify(redisService).set("delta:replies:welcome", "欢迎咨询！");
    }

    @Test
    @DisplayName("获取欢迎语 - 数据库无记录返回默认值")
    void getWelcomeReply_noRecord_shouldReturnDefault() {
        when(redisService.get("delta:replies:welcome")).thenReturn(null);

        Page<Reply> page = new Page<>(1, 1);
        page.setRecords(Collections.emptyList());
        when(replyMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        String result = replyService.getWelcomeReply();

        assertEquals("您好！欢迎咨询！", result);
    }

    @Test
    @DisplayName("获取默认回复 - 缓存命中返回缓存值")
    void getDefaultReply_cached_shouldReturnCachedValue() {
        when(redisService.get("delta:replies:default")).thenReturn("请稍候...");

        String result = replyService.getDefaultReply();

        assertEquals("请稍候...", result);
    }

    @Test
    @DisplayName("获取关键词回复 - 缓存命中返回缓存值")
    void getKeywordReply_cached_shouldReturnCachedValue() {
        when(redisService.get("delta:keyword:reply:退款")).thenReturn("退款回复内容");

        String result = replyService.getKeywordReply("退款");

        assertEquals("退款回复内容", result);
    }

    @Test
    @DisplayName("获取关键词回复 - 数据库无记录返回null")
    void getKeywordReply_noRecord_shouldReturnNull() {
        when(redisService.get("delta:keyword:reply:退款")).thenReturn(null);

        Page<Reply> page = new Page<>(1, 1);
        page.setRecords(Collections.emptyList());
        when(replyMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        String result = replyService.getKeywordReply("退款");

        assertNull(result);
    }
}
