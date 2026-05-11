package com.delta.admin.controller;

import com.delta.common.annotation.AuditLog;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "订单管理")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/orders")
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
public class OrderController extends BaseController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<Page<OrderVO>> getOrderPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "companionId", required = false) String companionId,
            @RequestParam(name = "orderStatus", required = false) String orderStatus,
            @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(name = "orderNo", required = false) String orderNo) {
        Long decodedUserId = userId != null ? decodeId(userId) : null;
        Long decodedCompanionId = companionId != null ? decodeId(companionId) : null;
        Page<OrderVO> pageResult = orderService.getOrderPage(page, size, decodedUserId, decodedCompanionId, orderStatus, paymentStatus, orderNo);
        return Result.success(pageResult);
    }

    @Operation(summary = "提交订单评价")
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<OrderVO> submitReview(@PathVariable Long id,
                                         @RequestParam Integer rating,
                                         @RequestParam(required = false) String reviewContent,
                                         @RequestParam Long reviewerId) {
        return Result.success(orderService.submitReview(id, rating, reviewContent, reviewerId));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable String id) {
        return Result.success(orderService.getOrderById(decodeId(id)));
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
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    @AuditLog(module = "订单管理", action = "陪玩师接单")
    public Result<OrderVO> acceptOrder(@PathVariable String id, @RequestParam Long companionId) {
        return Result.success(orderService.acceptOrder(decodeId(id), companionId));
    }

    @Operation(summary = "陪玩师拒单")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    @AuditLog(module = "订单管理", action = "陪玩师拒单")
    public Result<OrderVO> rejectOrder(@PathVariable String id, @RequestParam Long companionId, @RequestParam(required = false) String reason) {
        return Result.success(orderService.rejectOrder(decodeId(id), companionId, reason));
    }

    @Operation(summary = "获取陪玩师待处理订单")
    @GetMapping("/companion/{companionId}/pending")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<OrderVO>> getPendingOrdersByCompanionId(@PathVariable String companionId) {
        return Result.success(orderService.getPendingOrdersByCompanionId(decodeId(companionId)));
    }

    @Operation(summary = "确认订单")
    @PutMapping("/{id}/confirm")
    @AuditLog(module = "订单管理", action = "更新订单状态")
    public Result<Void> confirmOrder(@PathVariable String id) {
        orderService.confirmOrder(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "开始服务")
    @PutMapping("/{id}/start")
    public Result<Void> startService(@PathVariable String id) {
        orderService.startService(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "完成服务(触发评价)")
    @PutMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable String id) {
        orderService.completeOrder(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable String id, @RequestParam(required = false) String reason) {
        orderService.cancelOrder(decodeId(id), reason);
        return Result.success();
    }

    @Operation(summary = "查询用户活跃订单")
    @GetMapping("/active/user/{userId}")
    public Result<List<OrderVO>> getActiveOrdersByUserId(@PathVariable String userId) {
        return Result.success(orderService.getActiveOrdersByUserId(decodeId(userId)));
    }

    @Operation(summary = "查询陪玩师所有订单")
    @GetMapping("/companion/{companionId}")
    public Result<List<OrderVO>> getOrdersByCompanionId(@PathVariable String companionId) {
        return Result.success(orderService.getOrdersByCompanionId(decodeId(companionId)));
    }

    @Operation(summary = "条件查询订单")
    @GetMapping("/query")
    public Result<List<OrderVO>> queryOrders(OrderQueryDTO queryDTO) {
        return Result.success(orderService.queryOrders(queryDTO));
    }
}
