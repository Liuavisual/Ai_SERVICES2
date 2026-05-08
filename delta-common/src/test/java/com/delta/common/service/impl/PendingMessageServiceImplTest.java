package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.User;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.RedisService;
import com.delta.common.vo.PendingMessageVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "null"})
@ExtendWith(MockitoExtension.class)
class PendingMessageServiceImplTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), PendingMessage.class);
    }

    @Mock
    private PendingMessageMapper pendingMessageMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RedisService redisService;

    private static final String PENDING_COUNT_KEY = "delta:pending:count";

    @InjectMocks
    private PendingMessageServiceImpl pendingMessageService;

    @Test
    @DisplayName("分页查询待处理消息 - 无过滤条件返回分页结果")
    void getPendingMessagePage_noFilter_shouldReturnPagedResults() {
        Page<PendingMessage> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(pendingMessageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<PendingMessageVO> result = pendingMessageService.getPendingMessagePage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(pendingMessageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("处理待处理消息 - 消息不存在抛出异常")
    void handlePendingMessage_notExist_shouldThrow() {
        when(pendingMessageMapper.selectById(999L)).thenReturn(null);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(999L);
        dto.setStatus("processing");

        assertThrows(RuntimeException.class,
                () -> pendingMessageService.handlePendingMessage(dto));
    }

    @Test
    @DisplayName("处理待处理消息 - 正常从pending转到processing，设置处理人")
    void handlePendingMessage_pendingToProcessing_shouldSetHandler() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setStatus("PENDING");
        pm.setUserId(10L);
        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);
        when(pendingMessageMapper.updateById(any(PendingMessage.class))).thenReturn(1);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(1L);
        dto.setStatus("processing");
        dto.setRemark("开始处理");
        dto.setHandledBy(100L);

        pendingMessageService.handlePendingMessage(dto);

        ArgumentCaptor<PendingMessage> captor = ArgumentCaptor.forClass(PendingMessage.class);
        verify(pendingMessageMapper).updateById(captor.capture());
        PendingMessage updated = captor.getValue();
        assertEquals("PROCESSING", updated.getStatus());
        assertEquals("开始处理", updated.getRemark());
        assertEquals(Long.valueOf(100L), updated.getAssignedCsUserId());
        assertEquals(Long.valueOf(100L), updated.getHandledBy(), "处理人ID应持久化");
        assertNotNull(updated.getHandledAt(), "处理时间应设置");
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("处理待处理消息 - resolved状态正常处理")
    void handlePendingMessage_resolved_shouldSucceed() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setStatus("PROCESSING");
        pm.setUserId(10L);
        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);
        when(pendingMessageMapper.updateById(any(PendingMessage.class))).thenReturn(1);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(1L);
        dto.setStatus("resolved");
        dto.setRemark("已处理完成");
        dto.setHandledBy(100L);

        pendingMessageService.handlePendingMessage(dto);

        ArgumentCaptor<PendingMessage> captor = ArgumentCaptor.forClass(PendingMessage.class);
        verify(pendingMessageMapper).updateById(captor.capture());
        assertEquals("RESOLVED", captor.getValue().getStatus());
        assertEquals(Long.valueOf(100L), captor.getValue().getAssignedCsUserId());
        assertEquals(Long.valueOf(100L), captor.getValue().getHandledBy(), "处理人ID应持久化");
        assertNotNull(captor.getValue().getHandledAt(), "处理时间应设置");
    }

    @Test
    @DisplayName("创建待处理消息 - 用户不存在返回false")
    void createPendingMessage_userNotExist_shouldReturnFalse() {
        when(userMapper.selectById(999L)).thenReturn(null);

        boolean result = pendingMessageService.createPendingMessage(1L, 999L, "人工", "帮我一下", "wechat");

        assertFalse(result);
    }

    @Test
    @DisplayName("创建待处理消息 - 用户已有待处理工单返回false")
    void createPendingMessage_alreadyHasPending_shouldReturnFalse() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        boolean result = pendingMessageService.createPendingMessage(1L, 1L, "人工", "帮我一下", "wechat");

        assertFalse(result);
    }

    @Test
    @DisplayName("创建待处理消息 - 正常创建含deadline字段")
    void createPendingMessage_normal_shouldContainDeadline() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(pendingMessageMapper.insert(any(PendingMessage.class))).thenReturn(1);

        boolean result = pendingMessageService.createPendingMessage(1L, 1L, "人工", "帮我一下", "wechat");

        assertTrue(result);
        ArgumentCaptor<PendingMessage> captor = ArgumentCaptor.forClass(PendingMessage.class);
        verify(pendingMessageMapper).insert(captor.capture());
        PendingMessage saved = captor.getValue();
        assertNotNull(saved.getDeadline(), "deadline不能为空");
        assertNotNull(saved.getEscalationLevel(), "escalationLevel不能为空");
        assertEquals(Integer.valueOf(0), saved.getEscalationLevel());
        assertNotNull(saved.getReminderCount(), "reminderCount不能为空");
        assertEquals(Integer.valueOf(0), saved.getReminderCount());
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("创建待处理消息 - 无关键词时pendingReason为NEED_HUMAN")
    void createPendingMessage_noKeyword_shouldUseNeedHuman() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(pendingMessageMapper.insert(any(PendingMessage.class))).thenReturn(1);

        pendingMessageService.createPendingMessage(1L, 1L, null, "帮我一下", "wechat");

        ArgumentCaptor<PendingMessage> captor = ArgumentCaptor.forClass(PendingMessage.class);
        verify(pendingMessageMapper).insert(captor.capture());
        assertEquals("NEED_HUMAN", captor.getValue().getPendingReason());
    }

    @Test
    @DisplayName("获取待处理数量 - 管理员角色从缓存获取")
    void getPendingCount_adminRole_shouldReturnCachedValue() {
        when(redisService.get(PENDING_COUNT_KEY + ":1")).thenReturn(5L);

        Long count = pendingMessageService.getPendingCount(1L, "SYS_ADMIN");

        assertEquals(5L, count);
        verify(pendingMessageMapper, never()).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取待处理数量 - 管理员角色缓存未命中从数据库查询")
    void getPendingCount_adminRole_cacheMiss_shouldQueryDatabase() {
        when(redisService.get(PENDING_COUNT_KEY + ":1")).thenReturn(null);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        Long count = pendingMessageService.getPendingCount(1L, "SYS_ADMIN");

        assertEquals(3L, count);
        verify(redisService).set(eq(PENDING_COUNT_KEY + ":1"), eq(3L), anyLong(), any());
    }

    @Test
    @DisplayName("获取待处理数量 - 普通客服角色按分配过滤")
    void getPendingCount_csStaffRole_shouldFilterByAssignment() {
        when(redisService.get(PENDING_COUNT_KEY + ":100")).thenReturn(null);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        Long count = pendingMessageService.getPendingCount(100L, "CS_STAFF");

        assertEquals(2L, count);
        ArgumentCaptor<LambdaQueryWrapper<PendingMessage>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(pendingMessageMapper).selectCount(captor.capture());
        String sql = captor.getValue().getCustomSqlSegment();
        assertTrue(sql.contains("assigned_cs_user_id"), "应包含按客服分配过滤条件");
    }

    @Test
    @DisplayName("根据ID查询待处理消息 - 存在返回VO")
    void getPendingMessageById_exist_shouldReturnVO() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setUserId(10L);
        pm.setContent("人工");
        pm.setStatus("PENDING");
        pm.setPendingReason("KEYWORD_ESCALATE");
        pm.setKeyword("人工");
        pm.setDeadline(LocalDateTime.now().plusMinutes(30));
        pm.setEscalationLevel(0);
        pm.setReminderCount(0);

        User user = new User();
        user.setId(10L);
        user.setNickname("测试用户");
        user.setPlatform("test");

        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);
        when(userMapper.selectById(10L)).thenReturn(user);

        PendingMessageVO vo = pendingMessageService.getPendingMessageById(1L);

        assertNotNull(vo);
        assertEquals("人工", vo.getMessageContent());
        assertEquals("人工", vo.getKeyword(), "关键词应正确传递");
        assertEquals("测试用户", vo.getUserNickname());
        assertNotNull(vo.getDeadline());
        assertEquals("待处理", vo.getStatusDesc());
        assertEquals("关键词触发", vo.getInterventionTypeDesc());
    }

    @Test
    @DisplayName("根据ID查询待处理消息 - 不存在返回null")
    void getPendingMessageById_notExist_shouldReturnNull() {
        when(pendingMessageMapper.selectById(999L)).thenReturn(null);

        PendingMessageVO vo = pendingMessageService.getPendingMessageById(999L);

        assertNull(vo);
    }

    @Test
    @DisplayName("分页查询 - 客服角色只看到分配给自己或未分配的消息")
    void getPendingMessagePage_csStaffRole_shouldFilterByAssignment() {
        Page<PendingMessage> mockPage = new Page<>(1, 10);
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setUserId(10L);
        pm.setContent("人工");
        pm.setStatus("PENDING");
        pm.setPendingReason("KEYWORD_ESCALATE");
        pm.setKeyword("人工");
        pm.setDeadline(LocalDateTime.now().plusMinutes(30));
        pm.setEscalationLevel(0);
        pm.setReminderCount(0);
        mockPage.setRecords(Collections.singletonList(pm));
        mockPage.setTotal(1);

        User user = new User();
        user.setId(10L);
        user.setNickname("测试用户");
        user.setPlatform("test");

        when(pendingMessageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        when(userMapper.selectById(10L)).thenReturn(user);

        Page<PendingMessageVO> result = pendingMessageService.getPendingMessagePage(1, 10, null, null, null, 100L, "CS_STAFF");

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("人工", result.getRecords().get(0).getMessageContent());
        verify(pendingMessageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("创建待处理消息 - 正常创建含keyword持久化")
    void createPendingMessage_normal_shouldPersistKeyword() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(pendingMessageMapper.insert(any(PendingMessage.class))).thenReturn(1);

        boolean result = pendingMessageService.createPendingMessage(1L, 1L, "人工", "帮我一下", "wechat");

        assertTrue(result);
        ArgumentCaptor<PendingMessage> captor = ArgumentCaptor.forClass(PendingMessage.class);
        verify(pendingMessageMapper).insert(captor.capture());
        assertEquals("人工", captor.getValue().getKeyword(), "关键词应持久化");
    }
}
