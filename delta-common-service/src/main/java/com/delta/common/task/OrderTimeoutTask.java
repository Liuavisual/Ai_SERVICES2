package com.delta.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.entity.Order;
import com.delta.common.mapper.OrderMapper;
import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时自动取消定时任务
 * <p>
 * 每60秒巡检一次，扫描处于 PENDING 状态且超过 {@link BusinessStatusConstants#ORDER_TIMEOUT_CANCEL_MINUTES}
 * 分钟未支付确认的订单，自动标记为已取消状态。
 * 采用分页查询避免全表扫描，每次取消操作记录详细日志。
 * </p>
 *
 * @author 刘建国
 */
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    /** 订单Mapper */
    private final OrderMapper orderMapper;

    /** Redis服务 */
    private final RedisService redisService;

    /**
     * 定时扫描超时未支付订单并自动取消
     * <p>
     * 使用 Redis 分布式锁防止多实例并发执行。
     * 锁有效期 120 秒，确保任务有足够时间完成。
     * </p>
     * <p>
     * 取消条件：
     * 1. 订单状态为 PENDING
     * 2. 支付状态为 UNPAID
     * 3. 创建时间距今超过 ORDER_TIMEOUT_CANCEL_MINUTES 分钟
     * </p>
     */
    @Scheduled(fixedRate = 60000)
    public void execute() {
        String lockKey = "task:lock:order_timeout";
        Boolean locked = redisService.setIfAbsent(lockKey, "1", 120, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return;
        }
        try {
            scanAndCancelTimeoutOrders();
        } catch (Throwable t) {
            log.error("【订单超时】执行异常", t);
        } finally {
            redisService.delete(lockKey);
        }
    }

    /**
     * 扫描并取消超时订单
     */
    private void scanAndCancelTimeoutOrders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime timeoutThreshold = now.minusMinutes(BusinessStatusConstants.ORDER_TIMEOUT_CANCEL_MINUTES);

            int pageNum = 1;
            int cancelledCount = 0;

            while (true) {
                Page<Order> page = new Page<>(pageNum, BusinessStatusConstants.ORDER_TIMEOUT_BATCH_SIZE);
                LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Order::getOrderStatus, BusinessStatusConstants.ORDER_STATUS_PENDING);
                wrapper.eq(Order::getPaymentStatus, BusinessStatusConstants.PAYMENT_STATUS_UNPAID);
                wrapper.lt(Order::getCreatedAt, timeoutThreshold);
                wrapper.orderByAsc(Order::getCreatedAt);

                Page<Order> pageResult = orderMapper.selectPage(page, wrapper);
                List<Order> timeoutOrders = pageResult.getRecords();

                if (timeoutOrders.isEmpty()) {
                    break;
                }

                for (Order order : timeoutOrders) {
                    try {
                        cancelOrderAutomatically(order);
                        cancelledCount++;
                    } catch (Exception e) {
                        log.error("【订单超时】自动取消单条订单失败 | orderNo={} | error={}", order.getOrderNo(), e.getMessage());
                    }
                }

                if (!pageResult.hasNext()) {
                    break;
                }
                pageNum++;
            }

            if (cancelledCount > 0) {
                log.info("【订单超时】本次自动取消 {} 个超时未支付订单", cancelledCount);
            }

        } catch (Exception e) {
            log.error("【订单超时】扫描异常", e);
        }
    }

    /**
     * 自动取消单笔超时订单
     * <p>
     * 使用 LambdaUpdateWrapper 执行原子更新，避免先查后改的并发竞争。
     * 仅对当前仍为 PENDING 且 UNPAID 的订单执行取消。
     * </p>
     *
     * @param order 超时订单实体
     */
    private void cancelOrderAutomatically(Order order) {
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, order.getId());
        updateWrapper.eq(Order::getOrderStatus, BusinessStatusConstants.ORDER_STATUS_PENDING);
        updateWrapper.eq(Order::getPaymentStatus, BusinessStatusConstants.PAYMENT_STATUS_UNPAID);
        updateWrapper.set(Order::getOrderStatus, BusinessStatusConstants.ORDER_STATUS_CANCELLED);
        updateWrapper.set(Order::getRemark,
                (order.getRemark() != null ? order.getRemark() + " | " : "")
                        + "系统自动取消：超过" + BusinessStatusConstants.ORDER_TIMEOUT_CANCEL_MINUTES + "分钟未支付确认");

        int affected = orderMapper.update(null, updateWrapper);
        if (affected > 0) {
            log.warn("【订单超时】自动取消订单 | orderNo={} | userId={} | companionId={} | 创建时间={} | 超时分钟={}",
                    order.getOrderNo(), order.getUserId(), order.getCompanionId(),
                    order.getCreatedAt(), BusinessStatusConstants.ORDER_TIMEOUT_CANCEL_MINUTES);
        }
    }
}