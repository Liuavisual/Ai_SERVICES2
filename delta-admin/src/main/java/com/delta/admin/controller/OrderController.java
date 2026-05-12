package com.delta.admin.controller;

import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.OrderCreateDTO;
import com.delta.common.dto.OrderQueryDTO;
import com.delta.common.service.OrderService;
import com.delta.common.vo.OrderVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "订单管理")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/orders")
@PermAuth("order:view")
public class OrderController extends BaseController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<Page<OrderVO>> getOrderPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "userId", required = false) @DecodeId(required = false) Long userId,
            @RequestParam(name = "companionId", required = false) @DecodeId(required = false) Long companionId,
            @RequestParam(name = "orderStatus", required = false) String orderStatus,
            @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(name = "orderNo", required = false) String orderNo) {
        Page<OrderVO> pageResult = orderService.getOrderPage(page, size, userId, companionId, orderStatus, paymentStatus, orderNo);
        return Result.success(pageResult);
    }

    @Operation(summary = "提交订单评价")
    @PostMapping("/{id}/review")
    @PermAuth("order:review")
    public Result<OrderVO> submitReview(@PathVariable @DecodeId Long id,
                                         @RequestParam Integer rating,
                                         @RequestParam(required = false) String reviewContent,
                                         @RequestParam Long reviewerId) {
        return Result.success(orderService.submitReview(id, rating, reviewContent, reviewerId));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable @DecodeId Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    @AuditLog(module = "订单管理", action = "创建订单")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(
            decodeId(dto.getUserId()), decodeId(dto.getCompanionId()), dto.getServiceType(),
            dto.getScheduledStart(), dto.getScheduledEnd(), dto.getRemark(),
            dto.getTimeSource(), dto.getScheduleId()
        ));
    }

    @Operation(summary = "陪玩师接单")
    @PutMapping("/{id}/accept")
    @PermAuth("order:edit")
    @AuditLog(module = "订单管理", action = "陪玩师接单")
    public Result<OrderVO> acceptOrder(@PathVariable @DecodeId Long id, @RequestParam Long companionId) {
        return Result.success(orderService.acceptOrder(id, companionId));
    }

    @Operation(summary = "陪玩师拒单")
    @PutMapping("/{id}/reject")
    @PermAuth("order:edit")
    @AuditLog(module = "订单管理", action = "陪玩师拒单")
    public Result<OrderVO> rejectOrder(@PathVariable @DecodeId Long id, @RequestParam Long companionId, @RequestParam(required = false) String reason) {
        if (reason == null || reason.isBlank()) {
            return Result.error(400, "拒单原因不能为空");
        }
        return Result.success(orderService.rejectOrder(id, companionId, reason));
    }

    @Operation(summary = "获取陪玩师待处理订单")
    @GetMapping("/companion/{companionId}/pending")
    public Result<List<OrderVO>> getPendingOrdersByCompanionId(@PathVariable @DecodeId Long companionId) {
        return Result.success(orderService.getPendingOrdersByCompanionId(companionId));
    }

    @Operation(summary = "确认订单")
    @PutMapping("/{id}/confirm")
    @PermAuth("order:edit")
    @AuditLog(module = "订单管理", action = "更新订单状态")
    public Result<Void> confirmOrder(@PathVariable @DecodeId Long id) {
        orderService.confirmOrder(id);
        return Result.success();
    }

    @Operation(summary = "开始服务")
    @PutMapping("/{id}/start")
    @PermAuth("order:edit")
    public Result<Void> startService(@PathVariable @DecodeId Long id) {
        orderService.startService(id);
        return Result.success();
    }

    @Operation(summary = "完成服务(触发评价)")
    @PutMapping("/{id}/complete")
    @PermAuth("order:edit")
    public Result<Void> completeOrder(@PathVariable @DecodeId Long id) {
        orderService.completeOrder(id);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    @PermAuth("order:edit")
    public Result<Void> cancelOrder(@PathVariable @DecodeId Long id, @RequestParam(required = false) String reason) {
        orderService.cancelOrder(id, reason);
        return Result.success();
    }

    @Operation(summary = "查询用户活跃订单")
    @GetMapping("/active/user/{userId}")
    public Result<List<OrderVO>> getActiveOrdersByUserId(@PathVariable @DecodeId Long userId) {
        return Result.success(orderService.getActiveOrdersByUserId(userId));
    }

    @Operation(summary = "查询陪玩师所有订单")
    @GetMapping("/companion/{companionId}")
    public Result<List<OrderVO>> getOrdersByCompanionId(@PathVariable @DecodeId Long companionId) {
        return Result.success(orderService.getOrdersByCompanionId(companionId));
    }

    @Operation(summary = "条件查询订单")
    @GetMapping("/query")
    public Result<List<OrderVO>> queryOrders(OrderQueryDTO queryDTO) {
        return Result.success(orderService.queryOrders(queryDTO));
    }
}