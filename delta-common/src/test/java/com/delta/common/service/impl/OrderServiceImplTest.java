package com.delta.common.service.impl;

import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.Order;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.OrderMapper;
import com.delta.common.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试
 * 验证订单服务的核心业务逻辑
 *
 * @author 刘建国
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    /** 订单Mapper */
    @Mock
    private OrderMapper orderMapper;

    /** 陪玩师Mapper */
    @Mock
    private CompanionMapper companionMapper;

    /** 被测服务实例 */
    @InjectMocks
    private OrderServiceImpl orderService;

    /**
     * 测试根据ID获取订单 - 订单不存在
     * 验证抛出BusinessException
     */
    @Test
    void getOrderById_notExist_shouldThrow() {
        // 准备模拟数据
        when(orderMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> orderService.getOrderById(999L),
                "不存在的订单应抛出BusinessException");
    }

    /**
     * 测试根据ID获取订单 - 正常流程
     * 验证返回正确的OrderVO
     */
    @Test
    void getOrderById_shouldReturnOrderVO() {
        // 准备模拟数据
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD20240101001");
        order.setUserId(100L);
        order.setCompanionId(200L);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_PENDING);
        order.setPaymentStatus(BusinessStatusConstants.PAYMENT_STATUS_UNPAID);
        when(orderMapper.selectById(1L)).thenReturn(order);

        // 执行测试
        OrderVO result = orderService.getOrderById(1L);

        // 验证结果
        assertNotNull(result, "返回结果不应为null");
        assertEquals("ORD20240101001", result.getOrderNo(), "订单号应匹配");
    }

    /**
     * 测试创建订单 - 陪玩师不存在
     * 验证抛出BusinessException
     */
    @Test
    void createOrder_companionNotExist_shouldThrow() {
        // 准备模拟数据
        when(companionMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, 999L, "GAME", start, end, "测试备注"),
                "陪玩师不存在应抛出BusinessException");
    }

    /**
     * 测试创建订单 - 陪玩师不可用
     * 验证抛出BusinessException
     */
    @Test
    void createOrder_companionDisabled_shouldThrow() {
        // 准备模拟数据
        Companion companion = new Companion();
        companion.setId(1L);
        companion.setEnabled(0); // 不可用
        when(companionMapper.selectById(1L)).thenReturn(companion);

        // 执行测试并验证异常
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, 1L, "GAME", start, end, "测试备注"),
                "不可用陪玩师应抛出BusinessException");
    }

    /**
     * 测试创建订单 - 开始时间晚于结束时间
     * 验证抛出BusinessException
     */
    @Test
    void createOrder_invalidTimeRange_shouldThrow() {
        // 准备模拟数据
        Companion companion = new Companion();
        companion.setId(1L);
        companion.setEnabled(1);
        companion.setPrice(new BigDecimal("100.00"));
        when(companionMapper.selectById(1L)).thenReturn(companion);

        // 执行测试并验证异常 - 开始时间晚于结束时间
        LocalDateTime start = LocalDateTime.now().plusHours(3);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, 1L, "GAME", start, end, "测试备注"),
                "开始时间晚于结束时间应抛出BusinessException");
    }

    /**
     * 测试创建订单 - 正常流程
     * 验证订单创建成功
     */
    @Test
    void createOrder_shouldSucceed() {
        // 准备模拟数据
        Companion companion = new Companion();
        companion.setId(1L);
        companion.setNickname("测试陪玩");
        companion.setEnabled(1);
        companion.setPrice(new BigDecimal("100.00"));
        when(companionMapper.selectById(1L)).thenReturn(companion);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        // 执行测试
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        OrderVO result = orderService.createOrder(1L, 1L, "GAME", start, end, "测试备注");

        // 验证结果
        assertNotNull(result, "创建结果不应为null");
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(captor.capture());
        Order inserted = captor.getValue();
        assertEquals(BusinessStatusConstants.ORDER_STATUS_PENDING, inserted.getOrderStatus(), "状态应为PENDING");
        assertEquals(BusinessStatusConstants.PAYMENT_STATUS_UNPAID, inserted.getPaymentStatus(), "支付状态应为UNPAID");
    }

    /**
     * 测试确认订单 - 正常流程
     * 验证订单状态从PENDING变为CONFIRMED
     */
    @Test
    void confirmOrder_shouldSucceed() {
        // 准备模拟数据
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_PENDING);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // 执行测试
        orderService.confirmOrder(1L);

        // 使用ArgumentCaptor验证状态更新
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(captor.capture());
        assertEquals(BusinessStatusConstants.ORDER_STATUS_CONFIRMED, captor.getValue().getOrderStatus(), "状态应为CONFIRMED");
    }

    /**
     * 测试确认订单 - 状态不正确
     * 验证抛出BusinessException
     */
    @Test
    void confirmOrder_wrongStatus_shouldThrow() {
        // 准备模拟数据
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_COMPLETED);
        when(orderMapper.selectById(1L)).thenReturn(order);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> orderService.confirmOrder(1L),
                "非PENDING状态确认应抛出BusinessException");
    }

    /**
     * 测试取消订单 - 正常流程
     * 验证订单状态变为CANCELLED
     */
    @Test
    void cancelOrder_shouldSucceed() {
        // 准备模拟数据
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_PENDING);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // 执行测试
        orderService.cancelOrder(1L, "客户取消");

        // 使用ArgumentCaptor验证状态更新
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(captor.capture());
        assertEquals(BusinessStatusConstants.ORDER_STATUS_CANCELLED, captor.getValue().getOrderStatus(), "状态应为CANCELLED");
    }

    /**
     * 测试取消订单 - 已完成订单不可取消
     * 验证抛出BusinessException
     */
    @Test
    void cancelOrder_completed_shouldThrow() {
        // 准备模拟数据
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_COMPLETED);
        when(orderMapper.selectById(1L)).thenReturn(order);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(1L, "尝试取消"),
                "已完成订单取消应抛出BusinessException");
    }

    /**
     * 测试查询订单 - 带过滤条件
     * 验证过滤条件被正确应用
     */
    @Test
    void queryOrders_withFilter_shouldApplyFilter() {
        // 准备模拟数据
        when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());

        // 执行测试
        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setUserId(1L);
        queryDTO.setOrderStatus("PENDING");
        List<OrderVO> result = orderService.queryOrders(queryDTO);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        verify(orderMapper).selectList(any());
    }

    /**
     * 测试获取客户活跃订单
     * 验证返回正确的订单列表
     */
    @Test
    void getActiveOrdersByUserId_shouldReturnOrders() {
        // 准备模拟数据
        when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());

        // 执行测试
        List<OrderVO> result = orderService.getActiveOrdersByUserId(1L);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        verify(orderMapper).selectList(any());
    }
}
