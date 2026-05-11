package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionNotification;
import com.delta.common.entity.CompanionSchedule;
import com.delta.common.entity.Order;
import com.delta.common.entity.OrderStatusHistory;
import com.delta.common.exception.BusinessException;
import com.delta.common.dto.WorkOrderCreateDTO;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionNotificationMapper;
import com.delta.common.mapper.CompanionScheduleMapper;
import com.delta.common.mapper.OrderMapper;
import com.delta.common.mapper.OrderStatusHistoryMapper;
import com.delta.common.service.OrderService;
import com.delta.common.service.WorkOrderService;
import com.delta.common.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderMapper orderMapper;

    private final CompanionMapper companionMapper;

    private final StringRedisTemplate redisTemplate;

    private final WorkOrderService workOrderService;

    private final CompanionScheduleMapper companionScheduleMapper;

    private final OrderStatusHistoryMapper orderStatusHistoryMapper;

    private final CompanionNotificationMapper companionNotificationMapper;

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
                                String remark, String timeSource, Long scheduleId) {
        Companion companion = companionMapper.selectById(companionId);
        if (companion == null) {
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
        order.setGameType(companion.getGameType());
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_PENDING);
        order.setScheduledStart(scheduledStart);
        order.setScheduledEnd(scheduledEnd);
        order.setDurationMinutes(durationMins);
        order.setTotalAmount(calculateAmount(companion, durationMins));
        order.setPaymentStatus(BusinessStatusConstants.PAYMENT_STATUS_UNPAID);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setRemark(remark);
        order.setTimeSource(timeSource);
        order.setScheduleId(scheduleId);

        orderMapper.insert(order);
        log.info("订单创建成功: orderNo={}, userId={}, companionId={}", orderNo, userId, companionId);

        if (scheduleId != null) {
            markScheduleAsBooked(scheduleId);
        }

        recordStatusHistory(order.getId(), null, BusinessStatusConstants.ORDER_STATUS_PENDING,
                userId, "系统", "SYSTEM", "订单创建");

        sendCompanionNotification(companionId, order.getId(),
                BusinessStatusConstants.NOTIFY_TYPE_NEW_ORDER,
                "新订单通知", "您有一个新的陪玩订单待处理，订单号：" + orderNo);

        createLinkedWorkOrder(order);

        return convertToVO(order);
    }

    /**
     * 订单创建后自动创建关联工单，实现订单-工单联动
     * <p>
     * 使用 try-catch 确保工单创建失败不影响订单核心流程，
     * 工单可在后续手动补建。
     * </p>
     *
     * @param order 已创建的订单实体
     */
    private void createLinkedWorkOrder(Order order) {
        try {
            WorkOrderCreateDTO workOrderDto = new WorkOrderCreateDTO();
            workOrderDto.setOrderType("BOOKING");
            workOrderDto.setPriority("NORMAL");
            workOrderDto.setPlatform("SYSTEM");
            workOrderDto.setUserId(order.getUserId());
            workOrderDto.setServiceType(order.getServiceType());
            workOrderDto.setProblemDetail("订单 " + order.getOrderNo() + " 自动创建的关联工单");
            workOrderDto.setContextSummary("订单编号：" + order.getOrderNo()
                    + "，陪玩师：" + order.getCompanionName()
                    + "，服务类型：" + order.getServiceType()
                    + "，预约时间：" + order.getScheduledStart() + " ~ " + order.getScheduledEnd());
            workOrderDto.setRelatedCompanionId(order.getCompanionId());

            Long workOrderId = workOrderService.createWorkOrder(workOrderDto);
            log.info("【订单-工单联动】自动创建关联工单成功 | orderNo={} | workOrderId={}",
                    order.getOrderNo(), workOrderId);
        } catch (Exception e) {
            log.warn("【订单-工单联动】自动创建工单失败，订单已正常创建 | orderNo={} | error={}",
                    order.getOrderNo(), e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO acceptOrder(Long orderId, Long companionId) {
        Order order = getOrderOrThrow(orderId);
        if (!BusinessStatusConstants.ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException("仅待确认状态的订单可以接单");
        }
        if (!companionId.equals(order.getCompanionId())) {
            throw new BusinessException("只能处理分配给您的订单");
        }
        String fromStatus = order.getOrderStatus();
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_CONFIRMED);
        orderMapper.updateById(order);

        recordStatusHistory(orderId, fromStatus, BusinessStatusConstants.ORDER_STATUS_CONFIRMED,
                companionId, order.getCompanionName(), "COMPANION", "陪玩师接单");

        sendCompanionNotification(companionId, orderId,
                BusinessStatusConstants.NOTIFY_TYPE_STATUS_CHANGE,
                "接单成功", "您已成功接单，订单号：" + order.getOrderNo());

        log.info("陪玩师接单成功: orderId={}, companionId={}", orderId, companionId);
        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO rejectOrder(Long orderId, Long companionId, String reason) {
        Order order = getOrderOrThrow(orderId);
        if (!BusinessStatusConstants.ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException("仅待确认状态的订单可以拒单");
        }
        if (!companionId.equals(order.getCompanionId())) {
            throw new BusinessException("只能处理分配给您的订单");
        }
        String fromStatus = order.getOrderStatus();
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_CANCELLED);
        order.setCancelReason(reason);
        orderMapper.updateById(order);

        recordStatusHistory(orderId, fromStatus, BusinessStatusConstants.ORDER_STATUS_CANCELLED,
                companionId, order.getCompanionName(), "COMPANION", "陪玩师拒单：" + (reason != null ? reason : ""));

        releaseSchedule(order);

        log.info("陪玩师拒单成功: orderId={}, companionId={}, reason={}", orderId, companionId, reason);
        return convertToVO(order);
    }

    @Override
    public List<OrderVO> getPendingOrdersByCompanionId(Long companionId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCompanionId, companionId);
        wrapper.eq(Order::getOrderStatus, BusinessStatusConstants.ORDER_STATUS_PENDING);
        wrapper.orderByAsc(Order::getScheduledStart);
        List<Order> orders = orderMapper.selectList(wrapper);
        return convertList(orders);
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
    public OrderVO submitReview(Long orderId, Integer rating, String reviewContent, Long reviewerId) {
        Order order = getOrderOrThrow(orderId);
        if (!BusinessStatusConstants.ORDER_STATUS_COMPLETED.equals(order.getOrderStatus())) {
            throw new BusinessException("仅已完成订单可进行评价");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }
        order.setOrderStatus(BusinessStatusConstants.ORDER_STATUS_PENDING_REVIEW);
        order.setRemark((order.getRemark() != null ? order.getRemark() : "") + " | 评分:" + rating + "星 评价:" + (reviewContent != null ? reviewContent : ""));
        orderMapper.updateById(order);
        log.info("订单评价提交成功: orderId={}, rating={}", orderId, rating);
        return convertToVO(order);
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
        order.setCancelReason(reason);
        if (reason != null && !reason.isEmpty()) {
            String existingRemark = order.getRemark();
            order.setRemark(existingRemark != null ? existingRemark + " | 取消原因:" + reason : "取消原因:" + reason);
        }
        orderMapper.updateById(order);

        releaseSchedule(order);

        log.info("订单取消: id={}, reason={}", id, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(String orderNo, String transactionId, BigDecimal paidAmount, LocalDateTime payTime) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            log.error("【支付确认】订单不存在 | orderNo={}", orderNo);
            throw new BusinessException("订单不存在: " + orderNo);
        }

        if (BusinessStatusConstants.PAYMENT_STATUS_PAID.equals(order.getPaymentStatus())) {
            log.warn("【支付确认】订单已支付，忽略重复回调 | orderNo={}", orderNo);
            return;
        }

        order.setPaymentStatus(BusinessStatusConstants.PAYMENT_STATUS_PAID);
        order.setTransactionId(transactionId);
        order.setPaidAmount(paidAmount);
        order.setPaymentTime(payTime);
        orderMapper.updateById(order);
        log.info("【支付确认】支付成功 | orderNo={} | transactionId={} | paidAmount={} | payTime={}",
                orderNo, transactionId, paidAmount, payTime);
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

    @Override
    public Page<OrderVO> getOrderPage(Integer page, Integer size, Long userId, Long companionId, String orderStatus, String paymentStatus, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (companionId != null) {
            wrapper.eq(Order::getCompanionId, companionId);
        }
        if (orderStatus != null && !orderStatus.isEmpty()) {
            wrapper.eq(Order::getOrderStatus, orderStatus);
        }
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            wrapper.eq(Order::getPaymentStatus, paymentStatus);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> entityPage = orderMapper.selectPage(new Page<>(page, size), wrapper);
        Page<OrderVO> voPage = new Page<>(page, size, entityPage.getTotal());
        voPage.setRecords(convertList(entityPage.getRecords()));
        return voPage;
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

    /**
     * 生成订单号
     * 使用 Redis 自增序列 + 日期前缀，避免高并发时时间戳碰撞
     * 格式：ORD + yyyyMMdd + 6位序列号（如 ORD20260508000001）
     */
    private String generateOrderNo() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = "order:seq:" + datePrefix;
        Long seq = redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
        return "ORD" + datePrefix + String.format("%06d", seq);
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

    /**
     * 将排班记录标记为已预约状态
     *
     * @param scheduleId 排班记录ID
     */
    private void markScheduleAsBooked(Long scheduleId) {
        CompanionSchedule schedule = companionScheduleMapper.selectById(scheduleId);
        if (schedule != null && BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE.equals(schedule.getStatus())) {
            schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_BOOKED);
            companionScheduleMapper.updateById(schedule);
            log.info("排班时段已标记为已预约: scheduleId={}", scheduleId);
        }
    }

    /**
     * 释放订单关联的时间资源
     * 将排班记录从已预约恢复为可用状态
     *
     * @param order 订单实体
     */
    private void releaseSchedule(Order order) {
        if (order.getScheduleId() != null) {
            CompanionSchedule schedule = companionScheduleMapper.selectById(order.getScheduleId());
            if (schedule != null && BusinessStatusConstants.SCHEDULE_STATUS_BOOKED.equals(schedule.getStatus())) {
                schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
                companionScheduleMapper.updateById(schedule);
                log.info("排班时段已释放: scheduleId={}, orderId={}", order.getScheduleId(), order.getId());
            }
        }
    }

    /**
     * 记录订单状态变更历史
     *
     * @param orderId      订单ID
     * @param fromStatus   变更前状态
     * @param toStatus     变更后状态
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     * @param operatorRole 操作人角色
     * @param reason       变更原因
     */
    private void recordStatusHistory(Long orderId, String fromStatus, String toStatus,
                                      Long operatorId, String operatorName,
                                      String operatorRole, String reason) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setOperatorRole(operatorRole);
        history.setReason(reason);
        history.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryMapper.insert(history);
    }

    /**
     * 向陪玩师发送通知消息
     *
     * @param companionId 陪玩师ID
     * @param orderId     订单ID
     * @param type        通知类型
     * @param title       通知标题
     * @param content     通知内容
     */
    private void sendCompanionNotification(Long companionId, Long orderId,
                                            String type, String title, String content) {
        CompanionNotification notification = new CompanionNotification();
        notification.setCompanionId(companionId);
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        companionNotificationMapper.insert(notification);
    }
}
