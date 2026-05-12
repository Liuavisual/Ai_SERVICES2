package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.entity.Message;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.vo.CustomerVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"null", "unchecked"})
class CustomerServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CsUserCustomerMapper csUserCustomerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    @DisplayName("分页查询客户 - 无过滤条件返回分页结果")
    void getCustomerPage_noFilter_shouldReturnPagedResults() {
        Page<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CustomerVO> result = customerService.getCustomerPage(1, 10, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(userMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询客户 - 带过滤条件正确应用")
    void getCustomerPage_withFilters_shouldApplyFilters() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试客户");
        user.setPlatform("wechat");
        user.setAiEnabled(true);
        user.setAssignedCsUserId(100L);
        user.setCreatedAt(LocalDateTime.now());

        Page<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(user));
        mockPage.setTotal(1);
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Message msg = new Message();
        msg.setId(1L);
        msg.setUserId(1L);
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(msg));

        Page<CustomerVO> result = customerService.getCustomerPage(1, 10, "wechat", true, 100L, "测试");

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("根据ID查询客户 - 客户不存在返回null")
    void getCustomerById_notExist_shouldReturnNull() {
        when(userMapper.selectById(999L)).thenReturn(null);

        CustomerVO result = customerService.getCustomerById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("根据ID查询客户 - 正常返回客户信息")
    void getCustomerById_exist_shouldReturnCustomerVO() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试客户");
        user.setAssignedCsUserId(100L);
        when(userMapper.selectById(1L)).thenReturn(user);

        Page<Message> emptyMsgPage = new Page<>(1, 1);
        emptyMsgPage.setRecords(Collections.emptyList());
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(emptyMsgPage);
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        SysUser csUser = new SysUser();
        csUser.setId(100L);
        csUser.setRealName("客服张三");
        when(sysUserMapper.selectById(100L)).thenReturn(csUser);

        CustomerVO result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("测试客户", result.getNickname());
        assertEquals(5, result.getMessageCount());
        assertEquals("客服张三", result.getAssignedCsUserName());
    }

    @Test
    @DisplayName("根据ID查询客户 - 客服角色无权查看非分配客户抛出异常")
    void getCustomerById_csStaffUnauthorized_shouldThrow() {
        User user = new User();
        user.setId(1L);
        user.setAssignedCsUserId(200L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(csUserCustomerMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThrows(BusinessException.class,
                () -> customerService.getCustomerById(1L, 100L, BusinessStatusConstants.ROLE_CS_STAFF));
    }

    @Test
    @DisplayName("切换AI状态 - 客户不存在抛出异常")
    void toggleAiEnabled_customerNotExist_shouldThrow() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> customerService.toggleAiEnabled(999L, true));
    }

    @Test
    @DisplayName("切换AI状态 - 正常更新AI状态")
    void toggleAiEnabled_normal_shouldUpdate() {
        User user = new User();
        user.setId(1L);
        user.setAiEnabled(false);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        customerService.toggleAiEnabled(1L, true);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertTrue(captor.getValue().getAiEnabled());
    }

    @Test
    @DisplayName("分配客户 - 客户不存在抛出异常")
    void assignCustomer_customerNotExist_shouldThrow() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> customerService.assignCustomer(999L, 100L, "MANUAL", "测试"));
    }

    @Test
    @DisplayName("分配客户 - 客服不存在抛出异常")
    void assignCustomer_csUserNotExist_shouldThrow() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.selectById(100L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> customerService.assignCustomer(1L, 100L, "MANUAL", "测试"));
    }

    @Test
    @DisplayName("分配客户 - 正常分配并创建新绑定关系")
    void assignCustomer_normal_shouldCreateAssignment() {
        User user = new User();
        user.setId(1L);
        user.setAssignedCsUserId(null);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        SysUser csUser = new SysUser();
        csUser.setId(100L);
        when(sysUserMapper.selectById(100L)).thenReturn(csUser);
        when(csUserCustomerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(csUserCustomerMapper.insert(any(CsUserCustomer.class))).thenReturn(1);

        customerService.assignCustomer(1L, 100L, "MANUAL", "测试分配");

        verify(csUserCustomerMapper).insert(any(CsUserCustomer.class));
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("分配客户 - 重新激活已有绑定关系")
    void assignCustomer_existingInactiveAssignment_shouldReactivate() {
        User user = new User();
        user.setId(1L);
        user.setAssignedCsUserId(null);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        SysUser csUser = new SysUser();
        csUser.setId(100L);
        when(sysUserMapper.selectById(100L)).thenReturn(csUser);

        CsUserCustomer existing = new CsUserCustomer();
        existing.setId(10L);
        existing.setStatus(BusinessStatusConstants.ASSIGN_STATUS_INACTIVE);
        when(csUserCustomerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(csUserCustomerMapper.updateById(any(CsUserCustomer.class))).thenReturn(1);

        customerService.assignCustomer(1L, 100L, "MANUAL", "测试分配");

        ArgumentCaptor<CsUserCustomer> captor = ArgumentCaptor.forClass(CsUserCustomer.class);
        verify(csUserCustomerMapper).updateById(captor.capture());
        assertEquals(BusinessStatusConstants.ASSIGN_STATUS_ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("判断客户是否分配给客服 - null参数返回false")
    void isCustomerAssignedToCsStaff_nullParams_shouldReturnFalse() {
        assertFalse(customerService.isCustomerAssignedToCsStaff(null, 100L));
        assertFalse(customerService.isCustomerAssignedToCsStaff(1L, null));
    }

    @Test
    @DisplayName("判断客户是否分配给客服 - 用户直接分配返回true")
    void isCustomerAssignedToCsStaff_directlyAssigned_shouldReturnTrue() {
        User user = new User();
        user.setId(1L);
        user.setAssignedCsUserId(100L);
        when(userMapper.selectById(1L)).thenReturn(user);

        assertTrue(customerService.isCustomerAssignedToCsStaff(1L, 100L));
    }

    @Test
    @DisplayName("判断客户是否分配给客服 - 通过绑定表分配返回true")
    void isCustomerAssignedToCsStaff_viaAssignmentTable_shouldReturnTrue() {
        User user = new User();
        user.setId(1L);
        user.setAssignedCsUserId(null);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(csUserCustomerMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertTrue(customerService.isCustomerAssignedToCsStaff(1L, 100L));
    }
}
