package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.PricingPlanService;
import com.delta.common.vo.PricingPlanVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "定价方案管理", description = "SaaS定价方案管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/pricing-plans")
@RequiredArgsConstructor
public class PricingPlanController extends BaseController {

    private final PricingPlanService pricingPlanService;

    @Operation(summary = "分页查询定价方案")
    @GetMapping("/page")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Page<PricingPlanVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "planCode", required = false) String planCode,
            @RequestParam(name = "status", required = false) Integer status) {
        return Result.success(pricingPlanService.getPage(page, size, planCode, status));
    }

    @Operation(summary = "获取定价方案详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<PricingPlanVO> getById(@PathVariable("id") String id) {
        return Result.success(pricingPlanService.getById(decodeId(id)));
    }

    @Operation(summary = "创建定价方案")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "定价方案管理", action = "创建定价方案")
    public Result<Void> create(@Valid @RequestBody PricingPlanVO vo) {
        pricingPlanService.create(vo);
        return Result.success();
    }

    @Operation(summary = "更新定价方案")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "定价方案管理", action = "更新定价方案")
    public Result<Void> update(@Valid @RequestBody PricingPlanVO vo) {
        pricingPlanService.update(vo);
        return Result.success();
    }

    @Operation(summary = "删除定价方案")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "定价方案管理", action = "删除定价方案")
    public Result<Void> delete(@PathVariable("id") String id) {
        pricingPlanService.delete(decodeId(id));
        return Result.success();
    }
}
