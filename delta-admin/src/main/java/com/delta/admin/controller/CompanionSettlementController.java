package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.CompanionSettlementService;
import com.delta.common.vo.CompanionSettlementVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "陪玩师结算管理", description = "陪玩师收益结算管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/settlements")
@RequiredArgsConstructor
@PermAuth("companion_settlement:view")
public class CompanionSettlementController extends BaseController {

    private final CompanionSettlementService companionSettlementService;

    @Operation(summary = "分页查询结算记录")
    @GetMapping("/page")
    public Result<Page<CompanionSettlementVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "companionId", required = false) String companionId,
            @RequestParam(name = "settlementStatus", required = false) String settlementStatus,
            @RequestParam(name = "confirmStatus", required = false) String confirmStatus) {
        Long decodedCompanionId = companionId != null ? decodeId(companionId) : null;
        return Result.success(companionSettlementService.getPage(page, size, decodedCompanionId, settlementStatus, confirmStatus));
    }

    @Operation(summary = "获取结算记录详情")
    @GetMapping("/{id}")
    public Result<CompanionSettlementVO> getById(@PathVariable("id") String id) {
        return Result.success(companionSettlementService.getById(decodeId(id)));
    }

    @Operation(summary = "陪玩师确认结算")
    @PutMapping("/{id}/confirm")
    @PermAuth("companion_settlement:edit")
    @AuditLog(module = "结算管理", action = "确认结算")
    public Result<Void> confirm(@PathVariable("id") String id, @RequestBody Map<String, String> params) {
        companionSettlementService.confirm(decodeId(id), decodeId(params.get("companionId")));
        return Result.success();
    }

    @Operation(summary = "陪玩师申诉结算")
    @PutMapping("/{id}/dispute")
    @AuditLog(module = "结算管理", action = "结算申诉")
    public Result<Void> dispute(@PathVariable("id") String id, @RequestBody Map<String, String> params) {
        companionSettlementService.dispute(decodeId(id), decodeId(params.get("companionId")), params.get("disputeContent"));
        return Result.success();
    }

    @Operation(summary = "执行结算（管理员操作）")
    @PutMapping("/{id}/settle")
    @PermAuth("companion_settlement:edit")
    @AuditLog(module = "结算管理", action = "执行结算")
    public Result<Void> settle(@PathVariable("id") String id) {
        companionSettlementService.settle(decodeId(id));
        return Result.success();
    }
}
