package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.ClubSubscriptionService;
import com.delta.common.vo.ClubSubscriptionVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "俱乐部订阅管理", description = "俱乐部SaaS订阅管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/subscriptions")
@RequiredArgsConstructor
@PermAuth("subscription:view")
public class ClubSubscriptionController extends BaseController {

    private final ClubSubscriptionService clubSubscriptionService;

    @Operation(summary = "分页查询订阅记录")
    @GetMapping("/page")
    public Result<Page<ClubSubscriptionVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "clubConfigId", required = false) String clubConfigId,
            @RequestParam(name = "status", required = false) String status) {
        Long decodedClubConfigId = clubConfigId != null ? decodeId(clubConfigId) : null;
        return Result.success(clubSubscriptionService.getPage(page, size, decodedClubConfigId, status));
    }

    @Operation(summary = "获取订阅详情")
    @GetMapping("/{id}")
    public Result<ClubSubscriptionVO> getById(@PathVariable("id") String id) {
        return Result.success(clubSubscriptionService.getById(decodeId(id)));
    }

    @Operation(summary = "获取俱乐部当前订阅")
    @GetMapping("/by-club/{clubConfigId}")
    public Result<ClubSubscriptionVO> getByClubConfigId(@PathVariable("clubConfigId") String clubConfigId) {
        return Result.success(clubSubscriptionService.getByClubConfigId(decodeId(clubConfigId)));
    }

    @Operation(summary = "开通订阅")
    @PostMapping("/subscribe")
    @PermAuth("subscription:edit")
    @AuditLog(module = "订阅管理", action = "开通订阅")
    public Result<Void> subscribe(@RequestBody Map<String, String> params) {
        Long clubConfigId = decodeId(params.get("clubConfigId"));
        Long planId = decodeId(params.get("planId"));
        clubSubscriptionService.subscribe(clubConfigId, planId);
        return Result.success();
    }

    @Operation(summary = "开通试用")
    @PostMapping("/trial")
    @PermAuth("subscription:edit")
    @AuditLog(module = "订阅管理", action = "开通试用")
    public Result<Void> trial(@RequestBody Map<String, String> params) {
        Long clubConfigId = decodeId(params.get("clubConfigId"));
        clubSubscriptionService.trial(clubConfigId);
        return Result.success();
    }

    @Operation(summary = "取消订阅")
    @PutMapping("/{id}/cancel")
    @PermAuth("subscription:edit")
    @AuditLog(module = "订阅管理", action = "取消订阅")
    public Result<Void> cancel(@PathVariable("id") String id) {
        clubSubscriptionService.cancelSubscription(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "续费订阅")
    @PutMapping("/{id}/renew")
    @PermAuth("subscription:edit")
    @AuditLog(module = "订阅管理", action = "续费订阅")
    public Result<Void> renew(@PathVariable("id") String id, @RequestBody Map<String, Integer> params) {
        clubSubscriptionService.renewSubscription(decodeId(id), params.getOrDefault("months", 1));
        return Result.success();
    }
}
