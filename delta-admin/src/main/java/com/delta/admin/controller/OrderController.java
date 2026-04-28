package com.delta.admin.controller;

import com.delta.common.dto.OrderCreateDTO;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.service.OrderService;
import com.delta.common.vo.OrderVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(
            dto.getUserId(), dto.getCompanionId(), dto.getServiceType(),
            dto.getScheduledStart(), dto.getScheduledEnd(), dto.getRemark()
        ));
    }

    @Operation(summary = "确认订单")
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id) {
        orderService.confirmOrder(id);
        return Result.success();
    }

    @Operation(summary = "开始服务")
    @PutMapping("/{id}/start")
    public Result<Void> startService(@PathVariable Long id) {
        orderService.startService(id);
        return Result.success();
    }

    @Operation(summary = "完成服务(触发评价)")
    @PutMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestParam(required = false) String reason) {
        orderService.cancelOrder(id, reason);
        return Result.success();
    }

    @Operation(summary = "查询用户活跃订单")
    @GetMapping("/active/user/{userId}")
    public Result<List<OrderVO>> getActiveOrdersByUserId(@PathVariable Long userId) {
        return Result.success(orderService.getActiveOrdersByUserId(userId));
    }

    @Operation(summary = "查询陪玩师所有订单")
    @GetMapping("/companion/{companionId}")
    public Result<List<OrderVO>> getOrdersByCompanionId(@PathVariable Long companionId) {
        return Result.success(orderService.getOrdersByCompanionId(companionId));
    }

    @Operation(summary = "条件查询订单")
    @GetMapping("/query")
    public Result<List<OrderVO>> queryOrders(OrderQueryDTO queryDTO) {
        return Result.success(orderService.queryOrders(queryDTO));
    }
}
