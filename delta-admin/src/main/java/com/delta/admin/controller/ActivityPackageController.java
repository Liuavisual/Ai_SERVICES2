package com.delta.admin.controller;

import com.delta.common.dto.ActivityPackageDTO;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.service.ActivityPackageService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.ActivityPackageVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "活动套餐管理", description = "活动套餐管理接口")
@RestController
@RequestMapping("/v1/activity-packages")
public class ActivityPackageController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ActivityPackageController.class);

    @Autowired
    private ActivityPackageService activityPackageService;

    @Operation(summary = "获取俱乐部的活动套餐")
    @GetMapping("/club/{clubConfigId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<ActivityPackageVO>> getByClubId(@PathVariable("clubConfigId") String clubConfigId) {
        return Result.success(activityPackageService.getByClubId(decodeId(clubConfigId)));
    }

    @Operation(summary = "获取当前有效的活动套餐")
    @GetMapping("/club/{clubConfigId}/active")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<ActivityPackageVO>> getActivePackages(@PathVariable("clubConfigId") String clubConfigId) {
        return Result.success(activityPackageService.getActivePackages(decodeId(clubConfigId)));
    }

    @Operation(summary = "获取活动套餐详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<ActivityPackageVO> getById(@PathVariable("id") String id) {
        return Result.success(activityPackageService.getById(decodeId(id)));
    }

    @Operation(summary = "新增活动套餐")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> create(@Valid @RequestBody ActivityPackageDTO dto) {
        activityPackageService.create(dto);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新活动套餐")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> update(@Valid @RequestBody ActivityPackageDTO dto) {
        activityPackageService.update(dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除活动套餐")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> delete(@PathVariable("id") String id) {
        activityPackageService.delete(decodeId(id));
        return Result.success("删除成功");
    }

    @Operation(summary = "导出活动套餐Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "clubConfigId", required = false) String clubConfigId) throws IOException {
        Long decodedClubConfigId = clubConfigId != null ? decodeId(clubConfigId) : null;
        List<ActivityPackageVO> list = decodedClubConfigId != null ? activityPackageService.getByClubId(decodedClubConfigId) : List.of();
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("title", "活动标题");
        headers.put("activityType", "活动类型");
        headers.put("gameName", "关联游戏");
        headers.put("packagePrice", "套餐价格");
        headers.put("originalPrice", "原价");
        headers.put("serviceItemNames", "包含服务");
        headers.put("enabled", "是否启用");
        headers.put("startTime", "开始时间");
        headers.put("endTime", "结束时间");
        headers.put("description", "活动描述");
        headers.put("terms", "使用条款");
        ExcelUtils.export(response, "活动套餐", headers, list, item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("title", item.getTitle());
            map.put("activityType", item.getActivityType());
            map.put("gameName", item.getGameName() != null ? item.getGameName() : "通用");
            map.put("packagePrice", item.getPackagePrice());
            map.put("originalPrice", item.getOriginalPrice());
            map.put("serviceItemNames", item.getServiceItemNames());
            map.put("enabled", item.getEnabled() != null && Integer.valueOf(BusinessStatusConstants.ENABLED_INT).equals(item.getEnabled()) ? "启用" : "禁用");
            map.put("startTime", item.getStartTime() != null ? item.getStartTime().toString() : "");
            map.put("endTime", item.getEndTime() != null ? item.getEndTime().toString() : "");
            map.put("description", item.getDescription());
            map.put("terms", item.getTerms());
            return map;
        });
    }

    @Operation(summary = "导入活动套餐Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(name = "clubConfigId", required = false) String clubConfigId) throws IOException {
        Long decodedClubConfigId = clubConfigId != null ? decodeId(clubConfigId) : null;
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                ActivityPackageDTO dto = new ActivityPackageDTO();
                if (decodedClubConfigId != null) {
                    dto.setClubConfigId(decodedClubConfigId);
                }
                dto.setTitle(row.getOrDefault("活动标题", row.getOrDefault("title", "")));
                dto.setActivityType(row.getOrDefault("活动类型", row.getOrDefault("activityType", "SEASON")));
                String priceStr = row.getOrDefault("套餐价格", row.getOrDefault("packagePrice", "0"));
                dto.setPackagePrice(new BigDecimal(priceStr));
                String origStr = row.getOrDefault("原价", row.getOrDefault("originalPrice", ""));
                if (!origStr.isEmpty()) dto.setOriginalPrice(new BigDecimal(origStr));
                dto.setDescription(row.getOrDefault("活动描述", row.getOrDefault("description", "")));
                dto.setTerms(row.getOrDefault("使用条款", row.getOrDefault("terms", "")));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabledInt(enabledStr));
                activityPackageService.create(dto);
                success++;
            } catch (Exception e) {
                fail++;
                log.warn("导入活动套餐失败: {}", e.getMessage());
            }
        }
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
