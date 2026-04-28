package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.Order;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.OrderMapper;
import com.delta.common.service.OrderService;
import com.delta.common.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CompanionMapper companionMapper;

    @Override
    public OrderVO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, Long companionId, String serviceType,
                                LocalDateTime scheduledStart, LocalDateTime scheduledEnd,
                                String remark) {
        Companion companion = companionMapper.selectById(companionId);
        if (companion == null || !companion.getDeleted().equals(BusinessStatusConstants.NOT_DELETED)) {
            throw new BusinessException("陪玩师不存在");
        }
        if (!Integer.valueOf(BusinessStatusConstants.ENABLED_INT).equals(companion.getEnabled())) {
            throw new BusinessException("该陪玩师当前不可用");
        }
        if (scheduledStart.isAfter(scheduledEnd)) {
            throw new BusinessException("预约开始时间不能晚于结束时间");
        }

        String orderNo = generateOrderNo();
        int durationMins = Math.toIntExact(Duration.between(scheduledStart, scheduledEnd).toMinutes());

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCompanionId(companionId);
        order.setCompanionName(companion.getNickname());
        order.setServiceType(serviceType);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_PENDING);
        order.setScheduledStart(scheduledStart);
        order.setScheduledEnd(scheduledEnd);
        order.setDurationMinutes(durationMins);
        order.setTotalAmount(calculateAmount(companion, durationMins));
        order.setPaymentStatus(BusinessStatusConstants.PAYMENT_STATUS_UNPAID);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setRemark(remark);

        orderMapper.insert(order);
        log.info("订单创建成功: orderNo={}, userId={}, companionId={}", orderNo, userId, companionId);
        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long id) {
        Order order = getOrderOrThrow(id);
        validateTransition(order.getOrderStatus(), BusinessStatusConstants.ORDER_STATUS_PENDING,
            BusinessStatusConstants.ORDER_STATUS_CONFIRMED);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_CONFIRMED);
        orderMapper.updateById(order);
        log.info("订单确认成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(Long id) {
        Order order = getOrderOrThrow(id);
        validateTransition(order.getOrderStatus(), BusinessStatusConstants.ORDER_STATUS_CONFIRMED,
            BusinessStatusConstants.ORDER_STATUS_IN_PROGRESS);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_IN_PROGRESS);
        order.setActualStart(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("服务开始: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long id) {
        Order order = getOrderOrThrow(id);
        validateTransition(order.getOrderStatus(), BusinessStatusConstants.ORDER_STATUS_IN_PROGRESS,
            BusinessStatusConstants.ORDER_STATUS_COMPLETED);
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_COMPLETED);
        order.setActualEnd(LocalDateTime.now());
        if (order.getActualStart() != null) {
            long actualMinutes = Duration.between(order.getActualStart(), LocalDateTime.now()).toMinutes();
            order.setDurationMinutes((int) Math.min(actualMinutes, Integer.MAX_VALUE));
        }
        orderMapper.updateById(order);
        log.info("服务完成: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id, String reason) {
        Order order = getOrderOrThrow(id);
        String currentStatus = order.getOrderStatus();
        if (BusinessStatusConstants.ORDER_STATUS_COMPLETED.equals(currentStatus)
                || BusinessStatusConstants.ORDER_STATUS_CANCELLED.equals(currentStatus)
                || BusinessStatusConstants.ORDER_STATUS_ARCHIVED.equals(currentStatus)) {
            throw new BusinessException("当前订单状态不允许取消");
        }
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_CANCELLED);
        if (reason != null && !reason.isEmpty()) {
            String existingRemark = order.getRemark();
            order.setRemark(existingRemark != null ? existingRemark + " | 取消原因:" + reason : "取消原因:" + reason);
        }
        orderMapper.updateById(order);
        log.info("订单取消: id={}, reason={}", id, reason);
    }

    @Override
    public List<OrderVO> getActiveOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.in(Order::getOrderStatus, BusinessStatusConstants.ORDER_STATUS_PENDING,
            BusinessStatusConstants.ORDER_STATUS_CONFIRMED, BusinessStatusConstants.ORDER_STATUS_IN_PROGRESS);
        wrapper.orderByDesc(Order::getScheduledStart);
        List<Order> orders = orderMapper.selectList(wrapper);
        return convertList(orders);
    }

    @Override
    public List<OrderVO> getOrdersByCompanionId(Long companionId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCompanionId, companionId);
        wrapper.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);
        return convertList(orders);
    }

    @Override
    public List<OrderVO> queryOrders(OrderQueryDTO queryDTO) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getUserId() != null) {
            wrapper.eq(Order::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getCompanionId() != null) {
            wrapper.eq(Order::getCompanionId, queryDTO.getCompanionId());
        }
        if (queryDTO.getOrderStatus() != null && !queryDTO.getOrderStatus().isEmpty()) {
            wrapper.eq(Order::getOrderStatus, queryDTO.getOrderStatus());
        }
        if (queryDTO.getPaymentStatus() != null && !queryDTO.getPaymentStatus().isEmpty()) {
            wrapper.eq(Order::getPaymentStatus, queryDTO.getPaymentStatus());
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);
        return convertList(orders);
    }

    private Order getOrderOrThrow(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void validateTransition(String currentStatus, String expectedFrom, String targetStatus) {
        if (!expectedFrom.equals(currentStatus)) {
            throw new BusinessException("当前订单状态为" + currentStatus + "，无法执行此操作");
        }
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    private BigDecimal calculateAmount(Companion companion, int durationMinutes) {
        if (companion.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        double hours = Math.ceil(durationMinutes / 60.0);
        return companion.getPrice().multiply(BigDecimal.valueOf(hours)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private List<OrderVO> convertList(List<Order> orders) {
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(convertToVO(order));
        }
        return result;
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        if (order == null) {
            return vo;
        }
        BeanUtils.copyProperties(order, vo);
        vo.setOrderStatusText(translateStatus(order.getOrderStatus()));
        vo.setPaymentStatusText(translatePaymentStatus(order.getPaymentStatus()));
        return vo;
    }

    private String translateStatus(String status) {
        switch (status) {
            case BusinessStatusConstants.ORDER_STATUS_PENDING: return "待确认";
            case BusinessStatusConstants.ORDER_STATUS_CONFIRMED: return "已确认";
            case BusinessStatusConstants.ORDER_STATUS_IN_PROGRESS: return "服务中";
            case BusinessStatusConstants.ORDER_STATUS_COMPLETED: return "已完成";
            case BusinessStatusConstants.ORDER_STATUS_CANCELLED: return "已取消";
            case BusinessStatusConstants.ORDER_STATUS_REFUNDED: return "已退款";
            default: return status;
        }
    }

    private String translatePaymentStatus(String status) {
        switch (status) {
            case BusinessStatusConstants.PAYMENT_STATUS_UNPAID: return "未支付";
            case BusinessStatusConstants.PAYMENT_STATUS_PARTIAL: return "部分支付";
            default: return "已支付";
        }
    }
}
