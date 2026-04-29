package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CsUserCustomerDTO;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.vo.CsUserCustomerVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsUserCustomerServiceImplTest {

    @Mock
    private CsUserCustomerMapper csUserCustomerMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CsUserCustomerServiceImpl csUserCustomerService;

    @Test
    @DisplayName("分页查询分配关系 - 无过滤条件返回分页结果")
    void getPage_noFilter_shouldReturnPagedResults() {
        Page<CsUserCustomer> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(csUserCustomerMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CsUserCustomerVO> result = csUserCustomerService.getPage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询分配关系 - 填充客服和客户名称")
    void getPage_withData_shouldPopulateNames() {
        CsUserCustomer entity = new CsUserCustomer();
        entity.setId(1L);
        entity.setCsUserId(100L);
        entity.setCustomerUserId(200L);
        entity.setAssignType(BusinessStatusConstants.ASSIGN_TYPE_MANUAL);
        entity.setStatus(BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        entity.setAssignedBy(300L);

        Page<CsUserCustomer> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(entity));
        mockPage.setTotal(1);
        when(csUserCustomerMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        SysUser csUser = new SysUser();
        csUser.setId(100L);
        csUser.setRealName("客服张三");
        when(sysUserMapper.selectByIds(anyList())).thenReturn(List.of(csUser));

        User customer = new User();
        customer.setId(200L);
        customer.setNickname("客户李四");
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(customer));

        Page<CsUserCustomerVO> result = csUserCustomerService.getPage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("客服张三", result.getRecords().get(0).getCsUserName());
        assertEquals("客户李四", result.getRecords().get(0).getCustomerUserName());
        assertEquals("手动分配", result.getRecords().get(0).getAssignTypeDesc());
        assertEquals("有效", result.getRecords().get(0).getStatusDesc());
    }

    @Test
    @DisplayName("分页查询分配关系 - 系统分配和无效状态描述")
    void getPage_systemAssign_inactive_shouldShowCorrectDesc() {
        CsUserCustomer entity = new CsUserCustomer();
        entity.setId(1L);
        entity.setCsUserId(100L);
        entity.setCustomerUserId(200L);
        entity.setAssignType(BusinessStatusConstants.ASSIGN_TYPE_SYSTEM);
        entity.setStatus(BusinessStatusConstants.ASSIGN_STATUS_INACTIVE);

        Page<CsUserCustomer> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(entity));
        mockPage.setTotal(1);
        when(csUserCustomerMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        SysUser csUser = new SysUser();
        csUser.setId(100L);
        csUser.setRealName("客服张三");
        when(sysUserMapper.selectByIds(anyList())).thenReturn(List.of(csUser), Collections.emptyList());

        User customer = new User();
        customer.setId(200L);
        customer.setNickname("客户李四");
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(customer));

        Page<CsUserCustomerVO> result = csUserCustomerService.getPage(1, 10, null, null, null);

        assertEquals("系统分配", result.getRecords().get(0).getAssignTypeDesc());
        assertEquals("无效", result.getRecords().get(0).getStatusDesc());
    }

    @Test
    @DisplayName("根据ID查询分配关系 - 不存在抛出异常")
    void getById_notExist_shouldThrow() {
        when(csUserCustomerMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> csUserCustomerService.getById(999L));
    }

    @Test
    @DisplayName("根据ID查询分配关系 - 正常返回")
    void getById_exist_shouldReturnVO() {
        CsUserCustomer entity = new CsUserCustomer();
        entity.setId(1L);
        entity.setCsUserId(100L);
        entity.setCustomerUserId(200L);
        when(csUserCustomerMapper.selectById(1L)).thenReturn(entity);

        CsUserCustomerVO result = csUserCustomerService.getById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getCsUserId());
        assertEquals(200L, result.getCustomerUserId());
    }

    @Test
    @DisplayName("创建分配关系 - dto为null抛出异常")
    void create_nullDto_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> csUserCustomerService.create(null));
    }

    @Test
    @DisplayName("创建分配关系 - 已存在分配关系抛出异常")
    void create_alreadyExists_shouldThrow() {
        CsUserCustomer existing = new CsUserCustomer();
        existing.setId(1L);
        when(csUserCustomerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        CsUserCustomerDTO dto = new CsUserCustomerDTO();
        dto.setCsUserId(100L);
        dto.setCustomerUserId(200L);

        assertThrows(BusinessException.class,
                () -> csUserCustomerService.create(dto));
    }

    @Test
    @DisplayName("创建分配关系 - 正常创建默认手动分配和有效状态")
    void create_normal_shouldInsertWithDefaults() {
        when(csUserCustomerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(csUserCustomerMapper.insert(any(CsUserCustomer.class))).thenReturn(1);

        CsUserCustomerDTO dto = new CsUserCustomerDTO();
        dto.setCsUserId(100L);
        dto.setCustomerUserId(200L);

        csUserCustomerService.create(dto);

        ArgumentCaptor<CsUserCustomer> captor = ArgumentCaptor.forClass(CsUserCustomer.class);
        verify(csUserCustomerMapper).insert(captor.capture());
        assertEquals(BusinessStatusConstants.ASSIGN_TYPE_MANUAL, captor.getValue().getAssignType());
        assertEquals(BusinessStatusConstants.ASSIGN_STATUS_ACTIVE, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getAssignedAt());
    }

    @Test
    @DisplayName("更新分配关系 - dto为null抛出异常")
    void update_nullDto_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> csUserCustomerService.update(null));
    }

    @Test
    @DisplayName("更新分配关系 - 不存在抛出异常")
    void update_notExist_shouldThrow() {
        when(csUserCustomerMapper.selectById(999L)).thenReturn(null);

        CsUserCustomerDTO dto = new CsUserCustomerDTO();
        dto.setId(999L);

        assertThrows(BusinessException.class,
                () -> csUserCustomerService.update(dto));
    }

    @Test
    @DisplayName("更新分配关系 - 正常更新")
    void update_normal_shouldUpdate() {
        CsUserCustomer entity = new CsUserCustomer();
        entity.setId(1L);
        when(csUserCustomerMapper.selectById(1L)).thenReturn(entity);
        when(csUserCustomerMapper.updateById(any(CsUserCustomer.class))).thenReturn(1);

        CsUserCustomerDTO dto = new CsUserCustomerDTO();
        dto.setId(1L);
        dto.setStatus(BusinessStatusConstants.ASSIGN_STATUS_INACTIVE);

        csUserCustomerService.update(dto);

        verify(csUserCustomerMapper).updateById(any(CsUserCustomer.class));
    }

    @Test
    @DisplayName("删除分配关系 - 正常删除")
    void delete_normal_shouldDelete() {
        when(csUserCustomerMapper.deleteById(1L)).thenReturn(1);

        csUserCustomerService.delete(1L);

        verify(csUserCustomerMapper).deleteById(1L);
    }
}
