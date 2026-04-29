package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.*;
import com.delta.common.vo.Result;
import com.delta.common.service.WorkOrderService;
import com.delta.common.vo.WorkOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
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
    public Result<WorkOrderVO> getWorkOrderDetail(@PathVariable String id, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        WorkOrderVO vo = workOrderService.getWorkOrderDetail(decodedId, currentUserId, currentUserRole);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> createWorkOrder(@Valid @RequestBody WorkOrderCreateDTO dto) {
        Long id = workOrderService.createWorkOrder(dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> updateWorkOrder(@PathVariable String id, @Valid @RequestBody WorkOrderCreateDTO dto, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.updateWorkOrder(decodedId, dto, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/accept")
    public Result<Void> acceptWorkOrder(@PathVariable String id, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserName = getCurrentUserName(request);
        workOrderService.acceptWorkOrder(decodedId, currentUserId, currentUserName);
        return Result.success();
    }

    @PutMapping("/{id}/submit")
    public Result<Void> submitWorkOrder(@PathVariable String id, @Valid @RequestBody WorkOrderSubmitDTO dto, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.submitWorkOrder(decodedId, dto, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirmWorkOrder(@PathVariable String id, @Valid @RequestBody WorkOrderConfirmDTO dto) {
        Long decodedId = decodeId(id);
        workOrderService.confirmWorkOrder(decodedId, dto);
        return Result.success();
    }

    @PutMapping("/{id}/close")
    public Result<Void> closeWorkOrder(@PathVariable String id, @RequestParam(required = false) String closeReason, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.closeWorkOrder(decodedId, closeReason, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelWorkOrder(@PathVariable String id, @RequestParam String cancelReason, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        workOrderService.cancelWorkOrder(decodedId, cancelReason, currentUserId, currentUserRole);
        return Result.success();
    }

    @PutMapping("/{id}/reopen")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> reopenWorkOrder(@PathVariable String id, @RequestParam String reopenReason, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        workOrderService.reopenWorkOrder(decodedId, reopenReason, currentUserId);
        return Result.success();
    }

    @PostMapping("/{id}/records")
    public Result<Void> addRecord(@PathVariable String id, @Valid @RequestBody WorkOrderRecordDTO dto, HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long operatorId = getCurrentUserId(request);
        String operatorName = getCurrentUserName(request);
        String operatorRole = getCurrentUserRole(request);
        workOrderService.addRecord(decodedId, dto, operatorId, operatorName, operatorRole);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/book")
    public Result<Void> bookServiceTrack(@PathVariable String id, @Valid @RequestBody ServiceTrackBookDTO dto) {
        Long decodedId = decodeId(id);
        workOrderService.bookServiceTrack(decodedId, dto);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/start")
    public Result<Void> startServiceTrack(@PathVariable String id, @RequestParam String companionId, @RequestParam String companionName) {
        Long decodedId = decodeId(id);
        Long decodedCompanionId = decodeId(companionId);
        workOrderService.startServiceTrack(decodedId, decodedCompanionId, companionName);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/end")
    public Result<Void> endServiceTrack(@PathVariable String id, @Valid @RequestBody ServiceTrackEndDTO dto) {
        Long decodedId = decodeId(id);
        workOrderService.endServiceTrack(decodedId, dto);
        return Result.success();
    }

    @PutMapping("/{id}/service-track/confirm")
    public Result<Void> confirmServiceTrack(@PathVariable String id, @RequestParam Integer customerRating, @RequestParam(required = false) String customerFeedback) {
        Long decodedId = decodeId(id);
        workOrderService.confirmServiceTrack(decodedId, customerRating, customerFeedback);
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
