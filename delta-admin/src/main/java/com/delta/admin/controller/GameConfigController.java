package com.delta.admin.controller;

import com.delta.common.dto.GameConfigDTO;
import com.delta.common.service.GameConfigService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.GameConfigVO;
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

@Tag(name = "游戏配置管理", description = "游戏配置管理接口")
@RestController
@RequestMapping("/game-configs")
public class GameConfigController {

    @Autowired
    private GameConfigService gameConfigService;

    @Operation(summary = "获取俱乐部的游戏配置")
    @GetMapping("/club/{clubConfigId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<List<GameConfigVO>> getByClubId(@PathVariable("clubConfigId") Long clubConfigId) {
        return Result.success(gameConfigService.getByClubId(clubConfigId));
    }

    @Operation(summary = "获取游戏配置详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<GameConfigVO> getById(@PathVariable("id") Long id) {
        return Result.success(gameConfigService.getById(id));
    }

    @Operation(summary = "新增游戏配置")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> create(@Valid @RequestBody GameConfigDTO dto) {
        gameConfigService.create(dto);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新游戏配置")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> update(@Valid @RequestBody GameConfigDTO dto) {
        gameConfigService.update(dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除游戏配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> delete(@PathVariable("id") Long id) {
        gameConfigService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "导出游戏配置Excel")
    @GetMapping("/club/{clubConfigId}/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @PathVariable("clubConfigId") Long clubConfigId) throws IOException {
        List<GameConfigVO> list = gameConfigService.getByClubId(clubConfigId);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("gameName", "游戏名称");
        headers.put("gameCode", "游戏编码");
        headers.put("gameType", "游戏类型");
        headers.put("enabled", "是否启用");
        headers.put("sortOrder", "排序");
        headers.put("description", "描述");
        headers.put("createdAt", "创建时间");
        ExcelUtils.export(response, "游戏配置", headers, list, item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("gameName", item.getGameName());
            map.put("gameCode", item.getGameCode());
            map.put("gameType", item.getGameType());
            map.put("enabled", item.getEnabled() != null && item.getEnabled() == 1 ? "启用" : "禁用");
            map.put("sortOrder", item.getSortOrder());
            map.put("description", item.getDescription());
            map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
            return map;
        });
    }

    @Operation(summary = "导入游戏配置Excel")
    @PostMapping("/club/{clubConfigId}/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@PathVariable("clubConfigId") Long clubConfigId,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                GameConfigDTO dto = new GameConfigDTO();
                dto.setClubConfigId(clubConfigId);
                dto.setGameName(row.getOrDefault("游戏名称", row.getOrDefault("gameName", "")));
                dto.setGameCode(row.getOrDefault("游戏编码", row.getOrDefault("gameCode", "")));
                dto.setGameType(row.getOrDefault("游戏类型", row.getOrDefault("gameType", "")));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled("启用".equals(enabledStr) || "1".equals(enabledStr) ? 1 : 0);
                dto.setSortOrder(Integer.parseInt(row.getOrDefault("排序", row.getOrDefault("sortOrder", "0"))));
                dto.setDescription(row.getOrDefault("描述", row.getOrDefault("description", "")));
                gameConfigService.create(dto);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
