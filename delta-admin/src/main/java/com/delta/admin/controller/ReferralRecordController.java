package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.ReferralRecordService;
import com.delta.common.vo.ReferralRecordVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "裂变推荐管理", description = "裂变推荐记录管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/referrals")
@RequiredArgsConstructor
public class ReferralRecordController extends BaseController {

    private final ReferralRecordService referralRecordService;

    @Operation(summary = "分页查询推荐记录")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Page<ReferralRecordVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "campaignId", required = false) String campaignId,
            @RequestParam(name = "referrerUserId", required = false) String referrerUserId,
            @RequestParam(name = "conversionStatus", required = false) String conversionStatus,
            @RequestParam(name = "rewardStatus", required = false) String rewardStatus) {
        Long decodedCampaignId = campaignId != null ? decodeId(campaignId) : null;
        Long decodedReferrerUserId = referrerUserId != null ? decodeId(referrerUserId) : null;
        return Result.success(referralRecordService.getPage(page, size, decodedCampaignId, decodedReferrerUserId, conversionStatus, rewardStatus));
    }

    @Operation(summary = "获取推荐记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<ReferralRecordVO> getById(@PathVariable("id") String id) {
        return Result.success(referralRecordService.getById(decodeId(id)));
    }

    @Operation(summary = "发放推荐奖励")
    @PutMapping("/{id}/issue-reward")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "裂变推荐", action = "发放奖励")
    public Result<Void> issueReward(@PathVariable("id") String id) {
        referralRecordService.issueReward(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "取消推荐奖励")
    @PutMapping("/{id}/cancel-reward")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "裂变推荐", action = "取消奖励")
    public Result<Void> cancelReward(@PathVariable("id") String id) {
        referralRecordService.cancelReward(decodeId(id));
        return Result.success();
    }
}
