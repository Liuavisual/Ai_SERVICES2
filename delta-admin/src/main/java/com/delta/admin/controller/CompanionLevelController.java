package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.service.CompanionLevelService;
import com.delta.common.vo.CompanionLevelVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 陪玩师等级管理控制器
 *
 * @author delta
 */
@Tag(name = "陪玩师等级管理", description = "陪玩师等级管理接口")
@RestController
@RequestMapping("/companion-levels")
@RequiredArgsConstructor
public class CompanionLevelController extends BaseController {

    private final CompanionLevelService companionLevelService;

    @Operation(summary = "分页查询陪玩师等级")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CompanionLevelVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "levelName", required = false) String levelName) {
        Page<CompanionLevelVO> pageResult = companionLevelService.getPage(page, size, levelName);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取所有启用的陪玩师等级")
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<CompanionLevelVO>> getAllEnabled() {
        List<CompanionLevelVO> list = companionLevelService.getAllEnabled();
        return Result.success(list);
    }

    @Operation(summary = "获取陪玩师等级详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CompanionLevelVO> getById(@PathVariable("id") String id) {
        CompanionLevelVO vo = companionLevelService.getById(decodeId(id));
        return Result.success(vo);
    }

    @Operation(summary = "创建陪玩师等级")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> create(@Valid @RequestBody CompanionLevelDTO dto) {
        companionLevelService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新陪玩师等级")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> update(@Valid @RequestBody CompanionLevelDTO dto) {
        companionLevelService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除陪玩师等级")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> delete(@PathVariable("id") String id) {
        companionLevelService.delete(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "导出陪玩师等级Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "levelName", required = false) String levelName) {
        companionLevelService.exportCompanionLevels(response, levelName);
    }

    @Operation(summary = "导入陪玩师等级Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = companionLevelService.importCompanionLevels(file);
        return Result.success(result.toMap());
    }
}
