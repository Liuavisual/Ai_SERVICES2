package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
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
            @RequestParam(name = "clubConfigId", required = false) @DecodeId(required = false) Long clubConfigId,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(clubSubscriptionService.getPage(page, size, clubConfigId, status));
    }

    @Operation(summary = "获取订阅详情")
    @GetMapping("/{id}")
    public Result<ClubSubscriptionVO> getById(@PathVariable("id") @DecodeId Long id) {
        return Result.success(clubSubscriptionService.getById(id));
    }

    @Operation(summary = "获取俱乐部当前订阅")
    @GetMapping("/by-club/{clubConfigId}")
    public Result<ClubSubscriptionVO> getByClubConfigId(@PathVariable("clubConfigId") @DecodeId Long clubConfigId) {
        return Result.success(clubSubscriptionService.getByClubConfigId(clubConfigId));
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
    public Result<Void> cancel(@PathVariable("id") @DecodeId Long id) {
        clubSubscriptionService.cancelSubscription(id);
        return Result.success();
    }

    @Operation(summary = "续费订阅")
    @PutMapping("/{id}/renew")
    @PermAuth("subscription:edit")
    @AuditLog(module = "订阅管理", action = "续费订阅")
    public Result<Void> renew(@PathVariable("id") @DecodeId Long id, @RequestBody Map<String, Integer> params) {
        clubSubscriptionService.renewSubscription(id, params.getOrDefault("months", 1));
        return Result.success();
    }
}
