package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.service.CompanionService;
import com.delta.common.vo.CompanionVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "陪玩师管理", description = "陪玩师管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/companions")
@RequiredArgsConstructor
@PermAuth("companion:view")
public class CompanionController {

    private final CompanionService companionService;

    @Operation(summary = "分页查询陪玩师")
    @GetMapping("/page")
    public Result<Page<CompanionVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "levelId", required = false) @DecodeId(required = false) Long levelId,
            @RequestParam(name = "nickname", required = false) String nickname,
            @RequestParam(name = "enabled", required = false) Integer enabled) {
        Page<CompanionVO> pageResult = companionService.getPage(page, size, levelId, nickname, enabled);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取所有启用的陪玩师")
    @GetMapping("/all")
    public Result<List<CompanionVO>> getAllEnabled() {
        List<CompanionVO> list = companionService.getAllEnabled();
        return Result.success(list);
    }

    @Operation(summary = "获取指定日期和等级的可用陪玩师")
    @GetMapping("/available")
    public Result<List<CompanionVO>> getAvailable(
            @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(name = "levelId", required = false) @DecodeId(required = false) Long levelId) {
        List<CompanionVO> list = companionService.getAvailableByDateAndLevel(date, levelId);
        return Result.success(list);
    }

    @Operation(summary = "获取陪玩师详情")
    @GetMapping("/{id}")
    public Result<CompanionVO> getById(@PathVariable("id") @DecodeId Long id) {
        CompanionVO vo = companionService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "根据用户ID查找关联陪玩师信息")
    @GetMapping("/by-user/{userId}")
    public Result<CompanionVO> getByUserId(@PathVariable Long userId) {
        return Result.success(companionService.getByUserId(userId));
    }

    @Operation(summary = "创建陪玩师")
    @PostMapping
    @PermAuth("companion:edit")
    @AuditLog(module = "陪玩师管理", action = "创建陪玩师")
    public Result<Void> create(@Valid @RequestBody CompanionDTO dto) {
        companionService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新陪玩师")
    @PutMapping
    @PermAuth("companion:edit")
    @AuditLog(module = "陪玩师管理", action = "编辑陪玩师")
    public Result<Void> update(@Valid @RequestBody CompanionDTO dto) {
        companionService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除陪玩师")
    @DeleteMapping("/{id}")
    @PermAuth("companion:edit")
    @AuditLog(module = "陪玩师管理", action = "删除陪玩师")
    public Result<Void> delete(@PathVariable("id") @DecodeId Long id) {
        companionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "导出陪玩师Excel")
    @GetMapping("/export")
    @PermAuth("companion:export")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "levelId", required = false) @DecodeId(required = false) Long levelId,
                            @RequestParam(name = "nickname", required = false) String nickname,
                            @RequestParam(name = "enabled", required = false) Integer enabled) {
        companionService.exportCompanions(response, levelId, nickname, enabled);
    }

    @Operation(summary = "导入陪玩师Excel")
    @PostMapping("/import")
    @PermAuth("companion:import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = companionService.importCompanions(file);
        return Result.success(result.toMap());
    }

    @Operation(summary = "获取陪玩师综合评分数据看板")
    @GetMapping("/ratings/dashboard/{companionId}")
    @PermAuth("companion:rating")
    public Result<Map<String, Object>> getRatingDashboard(@PathVariable("companionId") @DecodeId Long companionId) {
        return Result.success(companionService.getRatingDashboard(companionId));
    }

    @Operation(summary = "获取所有陪玩师综合评分排名")
    @GetMapping("/ratings/all")
    @PermAuth("companion:rating")
    public Result<List<Map<String, Object>>> getAllCompanionRatings() {
        return Result.success(companionService.getAllCompanionRatings());
    }
}
