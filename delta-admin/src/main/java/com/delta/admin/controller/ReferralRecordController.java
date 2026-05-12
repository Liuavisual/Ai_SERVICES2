package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.ReferralRecordService;
import com.delta.common.vo.ReferralRecordVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "裂变推荐管理", description = "裂变推荐记录管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/referrals")
@RequiredArgsConstructor
@PermAuth("referral:view")
public class ReferralRecordController {

    private final ReferralRecordService referralRecordService;

    @Operation(summary = "分页查询推荐记录")
    @GetMapping("/page")
    public Result<Page<ReferralRecordVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "campaignId", required = false) @DecodeId(required = false) Long campaignId,
            @RequestParam(name = "referrerUserId", required = false) @DecodeId(required = false) Long referrerUserId,
            @RequestParam(name = "conversionStatus", required = false) String conversionStatus,
            @RequestParam(name = "rewardStatus", required = false) String rewardStatus) {
        return Result.success(referralRecordService.getPage(page, size, campaignId, referrerUserId, conversionStatus, rewardStatus));
    }

    @Operation(summary = "获取推荐记录详情")
    @GetMapping("/{id}")
    public Result<ReferralRecordVO> getById(@PathVariable("id") @DecodeId Long id) {
        return Result.success(referralRecordService.getById(id));
    }

    @Operation(summary = "发放推荐奖励")
    @PutMapping("/{id}/issue-reward")
    @PermAuth("referral:edit")
    @AuditLog(module = "裂变推荐", action = "发放奖励")
    public Result<Void> issueReward(@PathVariable("id") @DecodeId Long id) {
        referralRecordService.issueReward(id);
        return Result.success();
    }

    @Operation(summary = "取消推荐奖励")
    @PutMapping("/{id}/cancel-reward")
    @PermAuth("referral:edit")
    @AuditLog(module = "裂变推荐", action = "取消奖励")
    public Result<Void> cancelReward(@PathVariable("id") @DecodeId Long id) {
        referralRecordService.cancelReward(id);
        return Result.success();
    }
}
