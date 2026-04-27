package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.service.CompanionService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.CompanionVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 陪玩师管理控制器，提供陪玩师CRUD接口
 *
 * @author delta
 */
@Tag(name = "陪玩师管理", description = "陪玩师管理接口")
@RestController
@RequestMapping("/companions")
public class CompanionController {

    @Autowired
    private CompanionService companionService;

    @Operation(summary = "分页查询陪玩师")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CompanionVO>> getPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "levelId", required = false) Long levelId,
            @RequestParam(name = "nickname", required = false) String nickname,
            @RequestParam(name = "enabled", required = false) Integer enabled) {
        Page<CompanionVO> page = companionService.getPage(pageNum, pageSize, levelId, nickname, enabled);
        return Result.success(page);
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
            @RequestParam(name = "levelId", required = false) Long levelId) {
        List<CompanionVO> list = companionService.getAvailableByDateAndLevel(date, levelId);
        return Result.success(list);
    }

    @Operation(summary = "获取陪玩师详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CompanionVO> getById(@PathVariable("id") Long id) {
        CompanionVO vo = companionService.getById(id);
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
    public Result<Void> delete(@PathVariable("id") Long id) {
        companionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "导出陪玩师Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "levelId", required = false) Long levelId,
                            @RequestParam(name = "nickname", required = false) String nickname,
                            @RequestParam(name = "enabled", required = false) Integer enabled) throws IOException {
        Page<CompanionVO> page = companionService.getPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, levelId, nickname, enabled);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("realName", "真实姓名");
        headers.put("nickname", "昵称");
        headers.put("phone", "手机号");
        headers.put("wechat", "微信");
        headers.put("levelName", "等级");
        headers.put("gameType", "游戏类型");
        headers.put("price", "价格");
        headers.put("enabled", "是否启用");
        ExcelUtils.export(response, "陪玩师列表", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("realName", item.getRealName());
            map.put("nickname", item.getNickname());
            map.put("phone", desensitize(item.getPhone()));
            map.put("wechat", desensitize(item.getWechat()));
            map.put("levelName", item.getLevelName());
            map.put("gameType", item.getGameType());
            map.put("price", item.getPrice());
            map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
            return map;
        });
    }

    @Operation(summary = "导入陪玩师Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                CompanionDTO dto = new CompanionDTO();
                dto.setRealName(row.getOrDefault("真实姓名", row.getOrDefault("realName", "")));
                dto.setNickname(row.getOrDefault("昵称", row.getOrDefault("nickname", "")));
                dto.setPhone(row.getOrDefault("手机号", row.getOrDefault("phone", "")));
                dto.setWechat(row.getOrDefault("微信", row.getOrDefault("wechat", "")));
                dto.setGameType(row.getOrDefault("游戏类型", row.getOrDefault("gameType", "")));
                String priceStr = row.getOrDefault("价格", row.getOrDefault("price", "0"));
                dto.setPrice(new java.math.BigDecimal(priceStr.isEmpty() ? "0" : priceStr));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled("启用".equals(enabledStr) || "true".equalsIgnoreCase(enabledStr));
                companionService.create(dto);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }

    private String desensitize(String value) {
        if (value == null || value.length() <= 3) {
            return value;
        }
        return value.substring(0, 1) + "***" + value.substring(value.length() - 1);
    }
}
