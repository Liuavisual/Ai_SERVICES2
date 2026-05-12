package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.*;
import com.delta.common.vo.Result;
import com.delta.common.service.WorkOrderService;
import com.delta.common.vo.WorkOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiVersionConstants.V1 + "/work-orders")
@RequiredArgsConstructor
@PermAuth("work_order:view")
public class WorkOrderController extends BaseController {

    private final WorkOrderService workOrderService;

    @GetMapping("/page")
    public Result<Page<WorkOrderVO>> getWorkOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        Page<WorkOrderVO> pageResult = workOrderService.getWorkOrderPage(page, size, status, orderType, priority, platform, keyword, currentUserId, currentUserRole);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<WorkOrderVO> getWorkOrderDetail(@PathVariable @DecodeId Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        WorkOrderVO vo = workOrderService.getWorkOrderDetail(id, currentUserId, currentUserRole);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> createWorkOrder(@Valid @RequestBody WorkOrderCreateDTO dto) {
        Long id = workOrderService.createWorkOrder(dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> updateWorkOrder(@PathVariable @DecodeId Long id, @Valid @RequestBody WorkOrderCreateDTO dto, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.updateWorkOrder(id, dto, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/accept")
    @AuditLog(module = "工单管理", action = "接手工单")
    public Result<Void> acceptWorkOrder(@PathVariable @DecodeId Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserName = getCurrentUserName(request);
        workOrderService.acceptWorkOrder(id, currentUserId, currentUserName);
        return Result.success();
    }

    @PutMapping("/{id}/submit")
    @AuditLog(module = "工单管理", action = "提交处理")
    public Result<Void> submitWorkOrder(@PathVariable @DecodeId Long id, @Valid @RequestBody WorkOrderSubmitDTO dto, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.submitWorkOrder(id, dto, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @AuditLog(module = "工单管理", action = "确认完成")
    public Result<Void> confirmWorkOrder(@PathVariable @DecodeId Long id, @Valid @RequestBody WorkOrderConfirmDTO dto) {
        workOrderService.confirmWorkOrder(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/close")
    @AuditLog(module = "工单管理", action = "关闭工单")
    public Result<Void> closeWorkOrder(@PathVariable @DecodeId Long id, @RequestParam(required = false) String closeReason, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.closeWorkOrder(id, closeReason, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @AuditLog(module = "工单管理", action = "取消工单")
    public Result<Void> cancelWorkOrder(@PathVariable @DecodeId Long id, @RequestParam String cancelReason, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.cancelWorkOrder(id, cancelReason, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/reopen")
    @PermAuth("work_order:edit")
    public Result<Void> reopenWorkOrder(@PathVariable @DecodeId Long id, @RequestParam String reopenReason, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        workOrderService.reopenWorkOrder(id, reopenReason, currentUserId);
        return Result.success();
    }

    @PostMapping("/{id}/records")
    public Result<Void> addRecord(@PathVariable @DecodeId Long id, @Valid @RequestBody WorkOrderRecordDTO dto, HttpServletRequest request) {
        Long operatorId = getCurrentUserId(request);
        String operatorName = getCurrentUserName(request);
        String operatorRole = getCurrentUserRole(request);
        workOrderService.addRecord(id, dto, operatorId, operatorName, operatorRole);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/book")
    public Result<Void> bookServiceTrack(@PathVariable @DecodeId Long id, @Valid @RequestBody ServiceTrackBookDTO dto) {
        workOrderService.bookServiceTrack(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/start")
    public Result<Void> startServiceTrack(@PathVariable @DecodeId Long id, @RequestParam @DecodeId Long companionId, @RequestParam String companionName) {
        workOrderService.startServiceTrack(id, companionId, companionName);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/end")
    public Result<Void> endServiceTrack(@PathVariable @DecodeId Long id, @Valid @RequestBody ServiceTrackEndDTO dto) {
        workOrderService.endServiceTrack(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/confirm")
    public Result<Void> confirmServiceTrack(@PathVariable @DecodeId Long id, @RequestParam Integer customerRating, @RequestParam(required = false) String customerFeedback) {
        workOrderService.confirmServiceTrack(id, customerRating, customerFeedback);
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> getPendingCount(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        Long count = workOrderService.getPendingCount(currentUserId, currentUserRole);
        return Result.success(count);
    }
}
