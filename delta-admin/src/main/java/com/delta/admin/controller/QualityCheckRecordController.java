package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.QualityCheckRecordService;
import com.delta.common.vo.QualityCheckRecordVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "质检记录管理", description = "AI全流程质检记录管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/quality-checks")
@RequiredArgsConstructor
@PermAuth("quality_check:view")
public class QualityCheckRecordController extends BaseController {

    private final QualityCheckRecordService qualityCheckRecordService;

    @Operation(summary = "分页查询质检记录")
    @GetMapping("/page")
    public Result<Page<QualityCheckRecordVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "companionId", required = false) String companionId,
            @RequestParam(name = "riskLevel", required = false) String riskLevel,
            @RequestParam(name = "handleStatus", required = false) String handleStatus) {
        Long decodedCompanionId = companionId != null ? decodeId(companionId) : null;
        return Result.success(qualityCheckRecordService.getPage(page, size, decodedCompanionId, riskLevel, handleStatus));
    }

    @Operation(summary = "获取质检记录详情")
    @GetMapping("/{id}")
    public Result<QualityCheckRecordVO> getById(@PathVariable("id") String id) {
        return Result.success(qualityCheckRecordService.getById(decodeId(id)));
    }

    @Operation(summary = "处理质检记录")
    @PutMapping("/{id}/handle")
    @PermAuth("quality_check:edit")
    @AuditLog(module = "质检管理", action = "处理质检记录")
    public Result<Void> handle(@PathVariable("id") String id, @RequestBody Map<String, String> params) {
        qualityCheckRecordService.handleCheck(
                decodeId(id),
                params.get("handleStatus"),
                params.get("handleRemark"),
                decodeId(params.get("handlerId"))
        );
        return Result.success();
    }
}