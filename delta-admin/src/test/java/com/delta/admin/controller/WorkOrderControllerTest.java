package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.WorkOrderConfirmDTO;
import com.delta.common.dto.WorkOrderCreateDTO;
import com.delta.common.dto.WorkOrderSubmitDTO;
import com.delta.common.service.WorkOrderService;
import com.delta.common.util.IdObfuscateUtils;
import com.delta.common.vo.Result;
import com.delta.common.vo.WorkOrderVO;
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
class WorkOrderControllerTest {

    @Mock
    private WorkOrderService workOrderService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private WorkOrderController workOrderController;

    private void mockRequestAttributes(Long userId, String role, String username) {
        when(request.getAttribute("userId")).thenReturn(userId);
        when(request.getAttribute("role")).thenReturn(role);
        when(request.getAttribute("username")).thenReturn(username);
    }

    @Test
    @DisplayName("分页查询工单 - 成功返回分页数据")
    void getWorkOrderPage_shouldReturnPagedData() {
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        WorkOrderVO vo = new WorkOrderVO();
        vo.setId(1L);

        Page<WorkOrderVO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(vo));

        when(workOrderService.getWorkOrderPage(anyInt(), anyInt(), any(), any(), any(), any(), any(), eq(1L), eq("CS_STAFF"))).thenReturn(page);

        Result<Page<WorkOrderVO>> result = workOrderController.getWorkOrderPage(1, 10, null, null, null, null, null, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
    }

    @Test
    @DisplayName("分页查询工单 - 带状态和类型过滤")
    void getWorkOrderPage_withFilters_shouldReturnFilteredData() {
        mockRequestAttributes(1L, "CS_LEADER", "leader1");

        Page<WorkOrderVO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());

        when(workOrderService.getWorkOrderPage(anyInt(), anyInt(), eq("PENDING"), eq("COMPLAINT"), any(), any(), any(), eq(1L), eq("CS_LEADER"))).thenReturn(page);

        Result<Page<WorkOrderVO>> result = workOrderController.getWorkOrderPage(1, 10, "PENDING", "COMPLAINT", null, null, null, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    @DisplayName("获取工单详情 - 成功返回详情")
    void getWorkOrderDetail_shouldReturnDetail() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        WorkOrderVO vo = new WorkOrderVO();
        vo.setId(1L);

        when(workOrderService.getWorkOrderDetail(eq(1L), eq(1L), eq("CS_STAFF"))).thenReturn(vo);

        Result<WorkOrderVO> result = workOrderController.getWorkOrderDetail(obfuscatedId, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    @DisplayName("创建工单 - 成功创建")
    void createWorkOrder_withValidData_shouldReturnSuccess() {
        WorkOrderCreateDTO dto = new WorkOrderCreateDTO();

        when(workOrderService.createWorkOrder(any(WorkOrderCreateDTO.class))).thenReturn(1L);

        Result<Long> result = workOrderController.createWorkOrder(dto);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData());
    }

    @Test
    @DisplayName("更新工单 - 成功更新")
    void updateWorkOrder_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_STAFF", "staff1");
        WorkOrderCreateDTO dto = new WorkOrderCreateDTO();

        doNothing().when(workOrderService).updateWorkOrder(eq(1L), any(WorkOrderCreateDTO.class), eq(1L), eq("CS_STAFF"));

        Result<Void> result = workOrderController.updateWorkOrder(obfuscatedId, dto, request);

        assertEquals(200, result.getCode());
        verify(workOrderService).updateWorkOrder(eq(1L), any(WorkOrderCreateDTO.class), eq(1L), eq("CS_STAFF"));
    }

    @Test
    @DisplayName("接手工单 - 成功接手")
    void acceptWorkOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        doNothing().when(workOrderService).acceptWorkOrder(eq(1L), eq(1L), eq("staff1"));

        Result<Void> result = workOrderController.acceptWorkOrder(obfuscatedId, request);

        assertEquals(200, result.getCode());
        verify(workOrderService).acceptWorkOrder(1L, 1L, "staff1");
    }

    @Test
    @DisplayName("提交处理 - 成功提交")
    void submitWorkOrder_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        WorkOrderSubmitDTO dto = new WorkOrderSubmitDTO();
        dto.setHandleResult("已与用户协商解决");

        doNothing().when(workOrderService).submitWorkOrder(eq(1L), any(WorkOrderSubmitDTO.class), eq(1L), eq("CS_STAFF"));

        Result<Void> result = workOrderController.submitWorkOrder(obfuscatedId, dto, request);

        assertEquals(200, result.getCode());
        verify(workOrderService).submitWorkOrder(eq(1L), any(WorkOrderSubmitDTO.class), eq(1L), eq("CS_STAFF"));
    }

    @Test
    @DisplayName("确认完成 - 成功确认")
    void confirmWorkOrder_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        WorkOrderConfirmDTO dto = new WorkOrderConfirmDTO();
        dto.setSatisfactionScore(5);
        dto.setSatisfactionRemark("服务很好");

        doNothing().when(workOrderService).confirmWorkOrder(eq(1L), any(WorkOrderConfirmDTO.class));

        Result<Void> result = workOrderController.confirmWorkOrder(obfuscatedId, dto);

        assertEquals(200, result.getCode());
        verify(workOrderService).confirmWorkOrder(eq(1L), any(WorkOrderConfirmDTO.class));
    }

    @Test
    @DisplayName("关闭工单 - 成功关闭")
    void closeWorkOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_LEADER", "leader1");

        doNothing().when(workOrderService).closeWorkOrder(eq(1L), eq("问题已解决"), eq(1L), eq("CS_LEADER"));

        Result<Void> result = workOrderController.closeWorkOrder(obfuscatedId, "问题已解决", request);

        assertEquals(200, result.getCode());
        verify(workOrderService).closeWorkOrder(1L, "问题已解决", 1L, "CS_LEADER");
    }

    @Test
    @DisplayName("取消工单 - 成功取消")
    void cancelWorkOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        doNothing().when(workOrderService).cancelWorkOrder(eq(1L), eq("用户主动取消"), eq(1L), eq("CS_STAFF"));

        Result<Void> result = workOrderController.cancelWorkOrder(obfuscatedId, "用户主动取消", request);

        assertEquals(200, result.getCode());
        verify(workOrderService).cancelWorkOrder(1L, "用户主动取消", 1L, "CS_STAFF");
    }

    @Test
    @DisplayName("重新打开工单 - 成功重新打开")
    void reopenWorkOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "SYS_ADMIN", "admin");

        doNothing().when(workOrderService).reopenWorkOrder(eq(1L), eq("需要重新处理"), eq(1L));

        Result<Void> result = workOrderController.reopenWorkOrder(obfuscatedId, "需要重新处理", request);

        assertEquals(200, result.getCode());
        verify(workOrderService).reopenWorkOrder(1L, "需要重新处理", 1L);
    }

    @Test
    @DisplayName("添加工单记录 - 成功添加")
    void addRecord_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        mockRequestAttributes(1L, "CS_STAFF", "staff1");
        com.delta.common.dto.WorkOrderRecordDTO dto = new com.delta.common.dto.WorkOrderRecordDTO();

        doNothing().when(workOrderService).addRecord(eq(1L), any(com.delta.common.dto.WorkOrderRecordDTO.class), eq(1L), eq("staff1"), eq("CS_STAFF"));

        Result<Void> result = workOrderController.addRecord(obfuscatedId, dto, request);

        assertEquals(200, result.getCode());
        verify(workOrderService).addRecord(eq(1L), any(com.delta.common.dto.WorkOrderRecordDTO.class), eq(1L), eq("staff1"), eq("CS_STAFF"));
    }

    @Test
    @DisplayName("预约服务跟踪 - 成功预约")
    void bookServiceTrack_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        com.delta.common.dto.ServiceTrackBookDTO dto = new com.delta.common.dto.ServiceTrackBookDTO();

        doNothing().when(workOrderService).bookServiceTrack(eq(1L), any(com.delta.common.dto.ServiceTrackBookDTO.class));

        Result<Void> result = workOrderController.bookServiceTrack(obfuscatedId, dto);

        assertEquals(200, result.getCode());
        verify(workOrderService).bookServiceTrack(eq(1L), any(com.delta.common.dto.ServiceTrackBookDTO.class));
    }

    @Test
    @DisplayName("开始服务跟踪 - 成功开始")
    void startServiceTrack_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        String obfuscatedCompanionId = IdObfuscateUtils.encode(2L);

        doNothing().when(workOrderService).startServiceTrack(eq(1L), eq(2L), eq("陪玩师A"));

        Result<Void> result = workOrderController.startServiceTrack(obfuscatedId, obfuscatedCompanionId, "陪玩师A");

        assertEquals(200, result.getCode());
        verify(workOrderService).startServiceTrack(1L, 2L, "陪玩师A");
    }

    @Test
    @DisplayName("结束服务跟踪 - 成功结束")
    void endServiceTrack_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        com.delta.common.dto.ServiceTrackEndDTO dto = new com.delta.common.dto.ServiceTrackEndDTO();

        doNothing().when(workOrderService).endServiceTrack(eq(1L), any(com.delta.common.dto.ServiceTrackEndDTO.class));

        Result<Void> result = workOrderController.endServiceTrack(obfuscatedId, dto);

        assertEquals(200, result.getCode());
        verify(workOrderService).endServiceTrack(eq(1L), any(com.delta.common.dto.ServiceTrackEndDTO.class));
    }

    @Test
    @DisplayName("确认服务跟踪 - 成功确认")
    void confirmServiceTrack_withValidData_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        doNothing().when(workOrderService).confirmServiceTrack(eq(1L), eq(5), eq("非常满意"));

        Result<Void> result = workOrderController.confirmServiceTrack(obfuscatedId, 5, "非常满意");

        assertEquals(200, result.getCode());
        verify(workOrderService).confirmServiceTrack(1L, 5, "非常满意");
    }

    @Test
    @DisplayName("获取待处理工单数 - 成功返回数量")
    void getPendingCount_shouldReturnCount() {
        mockRequestAttributes(1L, "CS_STAFF", "staff1");

        when(workOrderService.getPendingCount(eq(1L), eq("CS_STAFF"))).thenReturn(5L);

        Result<Long> result = workOrderController.getPendingCount(request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(5L, result.getData());
    }
}
