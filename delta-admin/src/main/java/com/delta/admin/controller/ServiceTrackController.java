package com.delta.admin.controller;

import com.delta.common.dto.ServiceTrackBookDTO;
import com.delta.common.dto.ServiceTrackEndDTO;
import com.delta.common.service.ServiceTrackService;
import com.delta.common.vo.ServiceTrackVO;
import com.delta.common.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-tracks")
public class ServiceTrackController extends BaseController {

    @Autowired
    private ServiceTrackService serviceTrackService;

    @GetMapping("/{id}")
    public Result<ServiceTrackVO> getById(@PathVariable Long id) {
        return Result.success(serviceTrackService.getById(id));
    }

    @PostMapping
    public Result<ServiceTrackVO> createConsult(@RequestParam Long userId,
                                                 @RequestParam(required = false) Long workOrderId,
                                                 @RequestParam(required = false) String consultContent) {
        return Result.success(serviceTrackService.createConsult(userId, workOrderId, consultContent));
    }

    @PutMapping("/{id}/book")
    public Result<Void> bookService(@PathVariable Long id,
                                    @RequestParam Long userId,
                                    @RequestBody ServiceTrackBookDTO bookDTO) {
        serviceTrackService.bookService(id, userId, bookDTO);
        return Result.success();
    }

    @PutMapping("/{id}/start")
    public Result<Void> startService(@PathVariable Long id,
                                     @RequestParam Long companionId,
                                     @RequestParam String companionName) {
        serviceTrackService.startService(id, companionId, companionName);
        return Result.success();
    }

    @PutMapping("/{id}/end")
    public Result<Void> endService(@PathVariable Long id,
                                   @RequestBody ServiceTrackEndDTO endDTO) {
        serviceTrackService.endService(id, endDTO);
        return Result.success();
    }

    @PutMapping("/{id}/rating")
    public Result<Void> submitRating(@PathVariable Long id,
                                     @RequestParam Integer rating,
                                     @RequestParam(required = false) String feedback) {
        serviceTrackService.submitRating(id, rating, feedback);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result<List<ServiceTrackVO>> listByUserId(@PathVariable Long userId) {
        return Result.success(serviceTrackService.listByUserId(userId));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<ServiceTrackVO>> listByOrderId(@PathVariable Long orderId) {
        return Result.success(serviceTrackService.listByOrderId(orderId));
    }
}
