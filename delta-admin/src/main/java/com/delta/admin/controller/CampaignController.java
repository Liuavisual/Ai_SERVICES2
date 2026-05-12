package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.CampaignService;
import com.delta.common.vo.CampaignVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "营销活动管理", description = "营销增长活动管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/campaigns")
@RequiredArgsConstructor
@PermAuth("campaign:view")
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "分页查询营销活动")
    @GetMapping("/page")
    public Result<Page<CampaignVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "clubConfigId", required = false) @DecodeId(required = false) Long clubConfigId,
            @RequestParam(name = "campaignType", required = false) String campaignType,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(campaignService.getPage(page, size, clubConfigId, campaignType, status));
    }

    @Operation(summary = "获取营销活动详情")
    @GetMapping("/{id}")
    public Result<CampaignVO> getById(@PathVariable("id") @DecodeId Long id) {
        return Result.success(campaignService.getById(id));
    }

    @Operation(summary = "创建营销活动")
    @PostMapping
    @PermAuth("campaign:edit")
    @AuditLog(module = "营销活动", action = "创建活动")
    public Result<Void> create(@Valid @RequestBody CampaignVO vo) {
        campaignService.create(vo);
        return Result.success();
    }

    @Operation(summary = "更新营销活动")
    @PutMapping
    @PermAuth("campaign:edit")
    @AuditLog(module = "营销活动", action = "更新活动")
    public Result<Void> update(@Valid @RequestBody CampaignVO vo) {
        campaignService.update(vo);
        return Result.success();
    }

    @Operation(summary = "启动营销活动")
    @PutMapping("/{id}/start")
    @PermAuth("campaign:edit")
    @AuditLog(module = "营销活动", action = "启动活动")
    public Result<Void> start(@PathVariable("id") @DecodeId Long id) {
        campaignService.startCampaign(id);
        return Result.success();
    }

    @Operation(summary = "暂停营销活动")
    @PutMapping("/{id}/pause")
    @PermAuth("campaign:edit")
    @AuditLog(module = "营销活动", action = "暂停活动")
    public Result<Void> pause(@PathVariable("id") @DecodeId Long id) {
        campaignService.pauseCampaign(id);
        return Result.success();
    }

    @Operation(summary = "结束营销活动")
    @PutMapping("/{id}/end")
    @PermAuth("campaign:edit")
    @AuditLog(module = "营销活动", action = "结束活动")
    public Result<Void> end(@PathVariable("id") @DecodeId Long id) {
        campaignService.endCampaign(id);
        return Result.success();
    }

    @Operation(summary = "删除营销活动")
    @DeleteMapping("/{id}")
    @PermAuth("campaign:edit")
    @AuditLog(module = "营销活动", action = "删除活动")
    public Result<Void> delete(@PathVariable("id") @DecodeId Long id) {
        campaignService.delete(id);
        return Result.success();
    }
}
