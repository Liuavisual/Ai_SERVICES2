package com.delta.admin.controller;

import com.delta.common.dto.OrderCreateDTO;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.service.OrderService;
import com.delta.common.util.IdObfuscateUtils;
import com.delta.common.vo.OrderVO;
import com.delta.common.vo.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("获取订单详情 - 成功返回订单信息")
    void getOrderById_shouldReturnOrderInfo() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        OrderVO vo = new OrderVO();
        vo.setId(1L);

        when(orderService.getOrderById(1L)).thenReturn(vo);

        Result<OrderVO> result = orderController.getOrderById(obfuscatedId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    @DisplayName("创建订单 - 成功创建")
    void createOrder_withValidData_shouldReturnSuccess() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setUserId(IdObfuscateUtils.encode(1L));
        dto.setCompanionId(IdObfuscateUtils.encode(2L));
        dto.setServiceType("陪玩");
        dto.setScheduledStart(LocalDateTime.of(2026, 1, 1, 10, 0));
        dto.setScheduledEnd(LocalDateTime.of(2026, 1, 1, 12, 0));
        dto.setRemark("测试订单");
        dto.setTimeSource("SYSTEM");
        dto.setScheduleId(null);

        OrderVO createdOrder = new OrderVO();
        createdOrder.setId(1L);

        when(orderService.createOrder(eq(1L), eq(2L), eq("陪玩"), any(LocalDateTime.class), any(LocalDateTime.class), eq("测试订单"), eq("SYSTEM"), isNull())).thenReturn(createdOrder);

        Result<OrderVO> result = orderController.createOrder(dto);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    @DisplayName("确认订单 - 成功更新状态")
    void confirmOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        doNothing().when(orderService).confirmOrder(anyLong());

        Result<Void> result = orderController.confirmOrder(obfuscatedId);

        assertEquals(200, result.getCode());
        verify(orderService).confirmOrder(1L);
    }

    @Test
    @DisplayName("开始服务 - 成功更新状态")
    void startService_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        doNothing().when(orderService).startService(anyLong());

        Result<Void> result = orderController.startService(obfuscatedId);

        assertEquals(200, result.getCode());
        verify(orderService).startService(1L);
    }

    @Test
    @DisplayName("完成服务 - 成功更新状态")
    void completeOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        doNothing().when(orderService).completeOrder(anyLong());

        Result<Void> result = orderController.completeOrder(obfuscatedId);

        assertEquals(200, result.getCode());
        verify(orderService).completeOrder(1L);
    }

    @Test
    @DisplayName("取消订单 - 成功更新状态")
    void cancelOrder_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        doNothing().when(orderService).cancelOrder(anyLong(), anyString());

        Result<Void> result = orderController.cancelOrder(obfuscatedId, "用户主动取消");

        assertEquals(200, result.getCode());
        verify(orderService).cancelOrder(1L, "用户主动取消");
    }

    @Test
    @DisplayName("查询用户活跃订单 - 成功返回列表")
    void getActiveOrdersByUserId_shouldReturnOrderList() {
        String obfuscatedUserId = IdObfuscateUtils.encode(1L);
        OrderVO vo = new OrderVO();
        vo.setId(1L);

        when(orderService.getActiveOrdersByUserId(1L)).thenReturn(List.of(vo));

        Result<List<OrderVO>> result = orderController.getActiveOrdersByUserId(obfuscatedUserId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("查询陪玩师所有订单 - 成功返回列表")
    void getOrdersByCompanionId_shouldReturnOrderList() {
        String obfuscatedCompanionId = IdObfuscateUtils.encode(2L);
        OrderVO vo = new OrderVO();
        vo.setId(1L);

        when(orderService.getOrdersByCompanionId(2L)).thenReturn(List.of(vo));

        Result<List<OrderVO>> result = orderController.getOrdersByCompanionId(obfuscatedCompanionId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("条件查询订单 - 成功返回订单列表")
    void queryOrders_shouldReturnOrderList() {
        OrderVO vo = new OrderVO();
        vo.setId(1L);
        OrderQueryDTO queryDTO = new OrderQueryDTO();

        when(orderService.queryOrders(any(OrderQueryDTO.class))).thenReturn(List.of(vo));

        Result<List<OrderVO>> result = orderController.queryOrders(queryDTO);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }
}
