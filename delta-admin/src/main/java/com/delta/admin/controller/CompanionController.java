package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "陪玩师管理", description = "陪玩师管理接口")
@RestController
@RequestMapping("/companions")
@RequiredArgsConstructor
public class CompanionController extends BaseController {

    private final CompanionService companionService;

    @Operation(summary = "分页查询陪玩师")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CompanionVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "levelId", required = false) String levelId,
            @RequestParam(name = "nickname", required = false) String nickname,
            @RequestParam(name = "enabled", required = false) Integer enabled) {
        Long decodedLevelId = levelId != null ? decodeId(levelId) : null;
        Page<CompanionVO> pageResult = companionService.getPage(page, size, decodedLevelId, nickname, enabled);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取所有启用的陪玩师")
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<CompanionVO>> getAllEnabled() {
        List<CompanionVO> list = companionService.getAllEnabled();
        return Result.success(list);
    }

    @Operation(summary = "获取指定日期和等级的可用陪玩师")
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<CompanionVO>> getAvailable(
            @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(name = "levelId", required = false) String levelId) {
        Long decodedLevelId = levelId != null ? decodeId(levelId) : null;
        List<CompanionVO> list = companionService.getAvailableByDateAndLevel(date, decodedLevelId);
        return Result.success(list);
    }

    @Operation(summary = "获取陪玩师详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CompanionVO> getById(@PathVariable("id") String id) {
        CompanionVO vo = companionService.getById(decodeId(id));
        return Result.success(vo);
    }

    @Operation(summary = "创建陪玩师")
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> create(@Valid @RequestBody CompanionDTO dto) {
        companionService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新陪玩师")
    @PutMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> update(@Valid @RequestBody CompanionDTO dto) {
        companionService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除陪玩师")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> delete(@PathVariable("id") String id) {
        companionService.delete(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "导出陪玩师Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "levelId", required = false) String levelId,
                            @RequestParam(name = "nickname", required = false) String nickname,
                            @RequestParam(name = "enabled", required = false) Integer enabled) {
        Long decodedLevelId = levelId != null ? decodeId(levelId) : null;
        companionService.exportCompanions(response, decodedLevelId, nickname, enabled);
    }

    @Operation(summary = "导入陪玩师Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = companionService.importCompanions(file);
        return Result.success(result.toMap());
    }
}
