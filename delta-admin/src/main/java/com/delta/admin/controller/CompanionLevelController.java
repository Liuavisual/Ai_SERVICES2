package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.service.CompanionLevelService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.CompanionLevelVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
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
public class CompanionLevelController {

    @Autowired
    private CompanionLevelService companionLevelService;

    @Operation(summary = "分页查询陪玩师等级")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CompanionLevelVO>> getPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "levelName", required = false) String levelName) {
        Page<CompanionLevelVO> page = companionLevelService.getPage(pageNum, pageSize, levelName);
        return Result.success(page);
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
    public Result<CompanionLevelVO> getById(@PathVariable("id") Long id) {
        CompanionLevelVO vo = companionLevelService.getById(id);
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
    public Result<Void> delete(@PathVariable("id") Long id) {
        companionLevelService.delete(id);
        return Result.success();
    }

    @Operation(summary = "导出陪玩师等级Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "levelName", required = false) String levelName) throws IOException {
        Page<CompanionLevelVO> page = companionLevelService.getPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, levelName);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("levelName", "等级名称");
        headers.put("levelCode", "等级编码");
        headers.put("sortOrder", "排序");
        headers.put("basePrice", "基础价格");
        headers.put("description", "描述");
        headers.put("enabled", "是否启用");
        ExcelUtils.export(response, "陪玩师等级", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("levelName", item.getLevelName());
            map.put("levelCode", item.getLevelCode());
            map.put("sortOrder", item.getSortOrder());
            map.put("basePrice", item.getBasePrice());
            map.put("description", item.getDescription());
            map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
            return map;
        });
    }

    @Operation(summary = "导入陪玩师等级Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                CompanionLevelDTO dto = new CompanionLevelDTO();
                dto.setLevelName(row.getOrDefault("等级名称", row.getOrDefault("levelName", "")));
                dto.setLevelCode(row.getOrDefault("等级编码", row.getOrDefault("levelCode", "")));
                dto.setSortOrder(Integer.parseInt(row.getOrDefault("排序", row.getOrDefault("sortOrder", "0"))));
                dto.setBasePrice(new java.math.BigDecimal(row.getOrDefault("基础价格", row.getOrDefault("basePrice", "0"))));
                dto.setDescription(row.getOrDefault("描述", row.getOrDefault("description", "")));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled("启用".equals(enabledStr) || "true".equalsIgnoreCase(enabledStr));
                companionLevelService.create(dto);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
