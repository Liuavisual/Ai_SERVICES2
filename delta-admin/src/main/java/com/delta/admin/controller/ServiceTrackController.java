package com.delta.admin.controller;

import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.ServiceTrackBookDTO;
import com.delta.common.dto.ServiceTrackEndDTO;
import com.delta.common.service.ServiceTrackService;
import com.delta.common.vo.ServiceTrackVO;
import com.delta.common.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/service-tracks")
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
public class ServiceTrackController extends BaseController {

    private final ServiceTrackService serviceTrackService;

    @GetMapping("/{id}")
    public Result<ServiceTrackVO> getById(@PathVariable String id) {
        return Result.success(serviceTrackService.getById(decodeId(id)));
    }

    @PostMapping
    public Result<ServiceTrackVO> createConsult(@RequestParam String userId,
                                                 @RequestParam(required = false) String workOrderId,
                                                 @RequestParam(required = false) String consultContent) {
        Long decodedUserId = decodeId(userId);
        Long decodedWorkOrderId = workOrderId != null ? decodeId(workOrderId) : null;
        return Result.success(serviceTrackService.createConsult(decodedUserId, decodedWorkOrderId, consultContent));
    }

    @PutMapping("/{id}/book")
    public Result<Void> bookService(@PathVariable String id,
                                    @RequestParam String userId,
                                    @Valid @RequestBody ServiceTrackBookDTO bookDTO) {
        serviceTrackService.bookService(decodeId(id), decodeId(userId), bookDTO);
        return Result.success();
    }

    @PutMapping("/{id}/start")
    public Result<Void> startService(@PathVariable String id,
                                     @RequestParam String companionId,
                                     @RequestParam String companionName) {
        serviceTrackService.startService(decodeId(id), decodeId(companionId), companionName);
        return Result.success();
    }

    @PutMapping("/{id}/end")
    public Result<Void> endService(@PathVariable String id,
                                   @Valid @RequestBody ServiceTrackEndDTO endDTO) {
        serviceTrackService.endService(decodeId(id), endDTO);
        return Result.success();
    }

    @PutMapping("/{id}/rating")
    public Result<Void> submitRating(@PathVariable String id,
                                     @RequestParam Integer rating,
                                     @RequestParam(required = false) String feedback) {
        serviceTrackService.submitRating(decodeId(id), rating, feedback);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result<List<ServiceTrackVO>> listByUserId(@PathVariable String userId) {
        return Result.success(serviceTrackService.listByUserId(decodeId(userId)));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<ServiceTrackVO>> listByOrderId(@PathVariable String orderId) {
        return Result.success(serviceTrackService.listByOrderId(decodeId(orderId)));
    }
}
