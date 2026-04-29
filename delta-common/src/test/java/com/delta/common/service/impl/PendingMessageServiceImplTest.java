package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.entity.Message;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.mapper.MessageMapper;
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
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingMessageServiceImplTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);
    }

    @Mock
    private PendingMessageMapper pendingMessageMapper;

    @Mock
    private CsUserCustomerMapper csUserCustomerMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RedisService redisService;

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
        dto.setStatus(BusinessStatusConstants.PENDING_STATUS_PROCESSING);

        assertThrows(BusinessException.class,
                () -> pendingMessageService.handlePendingMessage(dto));
    }

    @Test
    @DisplayName("处理待处理消息 - 无效状态抛出异常")
    void handlePendingMessage_invalidStatus_shouldThrow() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setStatus(BusinessStatusConstants.PENDING_STATUS_PENDING);
        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(1L);
        dto.setStatus("INVALID_STATUS");

        assertThrows(BusinessException.class,
                () -> pendingMessageService.handlePendingMessage(dto));
    }

    @Test
    @DisplayName("处理待处理消息 - 不允许的状态转换抛出异常")
    void handlePendingMessage_invalidTransition_shouldThrow() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setStatus(BusinessStatusConstants.PENDING_STATUS_RESOLVED);
        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(1L);
        dto.setStatus(BusinessStatusConstants.PENDING_STATUS_PENDING);

        assertThrows(BusinessException.class,
                () -> pendingMessageService.handlePendingMessage(dto));
    }

    @Test
    @DisplayName("处理待处理消息 - 正常从pending转到processing")
    void handlePendingMessage_pendingToProcessing_shouldSucceed() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setStatus(BusinessStatusConstants.PENDING_STATUS_PENDING);
        pm.setUserId(10L);
        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);
        when(pendingMessageMapper.updateById(any(PendingMessage.class))).thenReturn(1);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(1L);
        dto.setStatus(BusinessStatusConstants.PENDING_STATUS_PROCESSING);
        dto.setRemark("开始处理");

        pendingMessageService.handlePendingMessage(dto);

        ArgumentCaptor<PendingMessage> captor = ArgumentCaptor.forClass(PendingMessage.class);
        verify(pendingMessageMapper).updateById(captor.capture());
        assertEquals(BusinessStatusConstants.PENDING_STATUS_PROCESSING, captor.getValue().getStatus());
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("处理待处理消息 - 转为resolved时发送满意度提示")
    void handlePendingMessage_resolved_shouldSendSatisfactionPrompt() {
        PendingMessage pm = new PendingMessage();
        pm.setId(1L);
        pm.setStatus(BusinessStatusConstants.PENDING_STATUS_PROCESSING);
        pm.setUserId(10L);
        when(pendingMessageMapper.selectById(1L)).thenReturn(pm);
        when(pendingMessageMapper.updateById(any(PendingMessage.class))).thenReturn(1);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        PendingMessageHandleDTO dto = new PendingMessageHandleDTO();
        dto.setId(1L);
        dto.setStatus(BusinessStatusConstants.PENDING_STATUS_RESOLVED);

        pendingMessageService.handlePendingMessage(dto);

        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    @DisplayName("创建待处理消息 - 用户不存在抛出异常")
    void createPendingMessage_userNotExist_shouldThrow() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> pendingMessageService.createPendingMessage(1L, 999L, "人工", "帮我一下", "wechat"));
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
    @DisplayName("创建待处理消息 - 正常创建返回true")
    void createPendingMessage_normal_shouldReturnTrue() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(pendingMessageMapper.insert(any(PendingMessage.class))).thenReturn(1);
        when(csUserCustomerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(sysUserMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        boolean result = pendingMessageService.createPendingMessage(1L, 1L, "人工", "帮我一下", "wechat");

        assertTrue(result);
        verify(eventPublisher).publishEvent(any());
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("获取待处理数量 - 从缓存获取")
    void getPendingCount_cached_shouldReturnCachedValue() {
        when(redisService.get(anyString())).thenReturn(5L);

        Long count = pendingMessageService.getPendingCount();

        assertEquals(5L, count);
        verify(pendingMessageMapper, never()).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取待处理数量 - 缓存未命中从数据库查询")
    void getPendingCount_cacheMiss_shouldQueryDatabase() {
        when(redisService.get(anyString())).thenReturn(null);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        Long count = pendingMessageService.getPendingCount();

        assertEquals(3L, count);
        verify(redisService).set(anyString(), eq(3L), anyLong(), any());
    }

    @Test
    @DisplayName("获取待处理数量 - 管理员角色返回全部数量")
    void getPendingCount_adminRole_shouldReturnAllCount() {
        when(redisService.get(anyString())).thenReturn(null);
        when(pendingMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        Long count = pendingMessageService.getPendingCount(1L, BusinessStatusConstants.ROLE_SYS_ADMIN);

        assertEquals(10L, count);
    }

    @Test
    @DisplayName("获取待处理数量 - 客服角色无分配客户返回0")
    void getPendingCount_csStaffRole_noCustomers_shouldReturnZero() {
        when(csUserCustomerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Long count = pendingMessageService.getPendingCount(100L, BusinessStatusConstants.ROLE_CS_STAFF);

        assertEquals(0L, count);
    }
}
