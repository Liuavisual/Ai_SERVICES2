package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.vo.OrderVO;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderVO getOrderById(Long id);

    OrderVO createOrder(Long userId, Long companionId, String serviceType,
                        LocalDateTime scheduledStart, LocalDateTime scheduledEnd,
                        String remark);

    void confirmOrder(Long id);

    void startService(Long id);

    void completeOrder(Long id);

    void cancelOrder(Long id, String reason);

    List<OrderVO> getActiveOrdersByUserId(Long userId);

    List<OrderVO> getOrdersByCompanionId(Long companionId);

    List<OrderVO> queryOrders(OrderQueryDTO queryDTO);

    Page<OrderVO> getOrderPage(Integer page, Integer size, Long userId, Long companionId, String orderStatus, String paymentStatus, String orderNo);
}
