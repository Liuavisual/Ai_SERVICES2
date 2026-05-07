package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.CompanionTrainingService;
import com.delta.common.vo.CompanionTrainingVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "陪玩师培训管理", description = "陪玩师标准化培训管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/trainings")
@RequiredArgsConstructor
public class CompanionTrainingController extends BaseController {

    private final CompanionTrainingService companionTrainingService;

    @Operation(summary = "分页查询培训记录")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Page<CompanionTrainingVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "companionId", required = false) String companionId,
            @RequestParam(name = "trainingStatus", required = false) String trainingStatus) {
        Long decodedCompanionId = companionId != null ? decodeId(companionId) : null;
        return Result.success(companionTrainingService.getPage(page, size, decodedCompanionId, trainingStatus));
    }

    @Operation(summary = "获取培训记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<CompanionTrainingVO> getById(@PathVariable("id") String id) {
        return Result.success(companionTrainingService.getById(decodeId(id)));
    }

    @Operation(summary = "创建培训课程")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "培训管理", action = "创建培训课程")
    public Result<Void> create(@Valid @RequestBody CompanionTrainingVO vo) {
        companionTrainingService.create(vo);
        return Result.success();
    }

    @Operation(summary = "更新培训课程")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "培训管理", action = "更新培训课程")
    public Result<Void> update(@Valid @RequestBody CompanionTrainingVO vo) {
        companionTrainingService.update(vo);
        return Result.success();
    }

    @Operation(summary = "开始学习培训")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    @AuditLog(module = "培训管理", action = "开始培训学习")
    public Result<Void> startTraining(@PathVariable("id") String id) {
        companionTrainingService.startTraining(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "完成培训学习")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    @AuditLog(module = "培训管理", action = "完成培训学习")
    public Result<Void> completeTraining(@PathVariable("id") String id, @RequestBody Map<String, Integer> params) {
        companionTrainingService.completeTraining(decodeId(id), params.getOrDefault("examScore", 0));
        return Result.success();
    }

    @Operation(summary = "删除培训课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "培训管理", action = "删除培训课程")
    public Result<Void> delete(@PathVariable("id") String id) {
        companionTrainingService.delete(decodeId(id));
        return Result.success();
    }
}
