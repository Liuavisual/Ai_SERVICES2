package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.service.CustomerService;
import com.delta.common.util.IdObfuscateUtils;
import com.delta.common.vo.CustomerVO;
import com.delta.common.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CustomerController customerController;

    private void mockRequestAttributes(Long userId, String role, String username) {
        when(request.getAttribute("userId")).thenReturn(userId);
        when(request.getAttribute("role")).thenReturn(role);
        when(request.getAttribute("username")).thenReturn(username);
    }

    @Test
    @DisplayName("分页查询客户 - 成功返回分页数据")
    void getCustomerPage_shouldReturnPagedData() {
        mockRequestAttributes(1L, "CS_LEADER", "leader1");

        CustomerVO vo = new CustomerVO();
        vo.setId(1L);

        Page<CustomerVO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(vo));

        when(customerService.getCustomerPage(anyInt(), anyInt(), any(), any(), any(), any())).thenReturn(page);

        Result<Page<CustomerVO>> result = customerController.getCustomerPage(1, 10, null, null, null, null, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
    }

    @Test
    @DisplayName("分页查询客户 - 带平台和关键词过滤")
    void getCustomerPage_withFilters_shouldReturnFilteredData() {
        mockRequestAttributes(1L, "CS_LEADER", "leader1");

        Page<CustomerVO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());

        when(customerService.getCustomerPage(anyInt(), anyInt(), eq("WECHAT"), any(), any(), eq("测试"))).thenReturn(page);

        Result<Page<CustomerVO>> result = customerController.getCustomerPage(1, 10, "WECHAT", null, null, "测试", request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    @DisplayName("分页查询客户 - CS_STAFF角色自动使用当前用户ID")
    void getCustomerPage_withCSStaffRole_shouldUseCurrentUserId() {
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        Page<CustomerVO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());

        when(customerService.getCustomerPage(anyInt(), anyInt(), any(), any(), eq(1L), any())).thenReturn(page);

        Result<Page<CustomerVO>> result = customerController.getCustomerPage(1, 10, null, null, null, null, request);

        assertEquals(200, result.getCode());
        verify(customerService).getCustomerPage(anyInt(), anyInt(), any(), any(), eq(1L), any());
    }

    @Test
    @DisplayName("获取客户详情 - 成功返回客户信息")
    void getCustomerById_shouldReturnCustomerInfo() {
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        CustomerVO vo = new CustomerVO();
        vo.setId(1L);

        when(customerService.getCustomerById(eq(1L), eq(1L), eq("CS_STAFF"))).thenReturn(vo);

        Result<CustomerVO> result = customerController.getCustomerById(1L, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    @DisplayName("分配客户 - 成功分配")
    void assignCustomer_withValidData_shouldReturnSuccess() {
        CustomerController.AssignCustomerDTO dto = new CustomerController.AssignCustomerDTO();
        dto.setCsUserId(IdObfuscateUtils.encode(2L));
        dto.setAssignType("MANUAL");
        dto.setRemark("手动分配");

        doNothing().when(customerService).assignCustomer(eq(1L), eq(2L), eq("MANUAL"), eq("手动分配"));

        Result<Void> result = customerController.assignCustomer(1L, dto);

        assertEquals(200, result.getCode());
        verify(customerService).assignCustomer(1L, 2L, "MANUAL", "手动分配");
    }

    @Test
    @DisplayName("切换AI启用状态 - 成功切换")
    void toggleAiEnabled_withValidData_shouldReturnSuccess() {
        CustomerController.ToggleAiEnabledDTO dto = new CustomerController.ToggleAiEnabledDTO();
        dto.setAiEnabled(true);

        doNothing().when(customerService).toggleAiEnabled(eq(1L), eq(true));

        Result<Void> result = customerController.toggleAiEnabled(1L, dto);

        assertEquals(200, result.getCode());
        verify(customerService).toggleAiEnabled(1L, true);
    }
}
