package com.delta.admin.controller;

import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.ServiceTrackBookDTO;
import com.delta.common.dto.ServiceTrackEndDTO;
import com.delta.common.service.ServiceTrackService;
import com.delta.common.vo.ServiceTrackVO;
import com.delta.common.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/service-tracks")
@PermAuth("service_track:view")
public class ServiceTrackController {

    private final ServiceTrackService serviceTrackService;

    @GetMapping("/{id}")
    public Result<ServiceTrackVO> getById(@PathVariable @DecodeId Long id) {
        return Result.success(serviceTrackService.getById(id));
    }

    @PostMapping
    public Result<ServiceTrackVO> createConsult(@RequestParam @DecodeId Long userId,
                                                 @RequestParam(required = false) @DecodeId(required = false) Long workOrderId,
                                                 @RequestParam(required = false) String consultContent) {
        return Result.success(serviceTrackService.createConsult(userId, workOrderId, consultContent));
    }

    @PutMapping("/{id}/book")
    public Result<Void> bookService(@PathVariable @DecodeId Long id,
                                    @RequestParam @DecodeId Long userId,
                                    @Valid @RequestBody ServiceTrackBookDTO bookDTO) {
        serviceTrackService.bookService(id, userId, bookDTO);
        return Result.success();
    }

    @PutMapping("/{id}/start")
    public Result<Void> startService(@PathVariable @DecodeId Long id,
                                     @RequestParam @DecodeId Long companionId,
                                     @RequestParam String companionName) {
        serviceTrackService.startService(id, companionId, companionName);
        return Result.success();
    }

    @PutMapping("/{id}/end")
    public Result<Void> endService(@PathVariable @DecodeId Long id,
                                   @Valid @RequestBody ServiceTrackEndDTO endDTO) {
        serviceTrackService.endService(id, endDTO);
        return Result.success();
    }

    @PutMapping("/{id}/rating")
    public Result<Void> submitRating(@PathVariable @DecodeId Long id,
                                     @RequestParam Integer rating,
                                     @RequestParam(required = false) String feedback) {
        serviceTrackService.submitRating(id, rating, feedback);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result<List<ServiceTrackVO>> listByUserId(@PathVariable("userId") @DecodeId Long userId) {
        return Result.success(serviceTrackService.listByUserId(userId));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<ServiceTrackVO>> listByOrderId(@PathVariable("orderId") @DecodeId Long orderId) {
        return Result.success(serviceTrackService.listByOrderId(orderId));
    }
}
