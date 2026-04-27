package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.CompanionScheduleDTO;
import com.delta.common.service.CompanionScheduleService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.CompanionScheduleVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 陪玩师排班管理控制器
 *
 * @author delta
 */
@Tag(name = "陪玩师时间管理", description = "陪玩师时间管理接口")
@RestController
@RequestMapping("/companion-schedules")
public class CompanionScheduleController {

    private static final Logger log = LoggerFactory.getLogger(CompanionScheduleController.class);

    @Autowired
    private CompanionScheduleService companionScheduleService;

    @Operation(summary = "分页查询陪玩师时间")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CompanionScheduleVO>> getPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(name = "companionId", required = false) Long companionId,
            @RequestParam(name = "scheduleDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate,
            @RequestParam(name = "status", required = false) String status) {
        Page<CompanionScheduleVO> page = companionScheduleService.getPage(pageNum, pageSize, companionId, scheduleDate, status);
        return Result.success(page);
    }

    @Operation(summary = "获取陪玩师指定日期的时间")
    @GetMapping("/by-companion-date")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<CompanionScheduleVO>> getByCompanionAndDate(
            @RequestParam(name = "companionId") Long companionId,
            @RequestParam(name = "scheduleDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate) {
        List<CompanionScheduleVO> list = companionScheduleService.getByCompanionAndDate(companionId, scheduleDate);
        return Result.success(list);
    }

    @Operation(summary = "获取指定日期的所有时间")
    @GetMapping("/by-date")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<CompanionScheduleVO>> getByDate(
            @RequestParam(name = "scheduleDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate) {
        List<CompanionScheduleVO> list = companionScheduleService.getByDate(scheduleDate);
        return Result.success(list);
    }

    @Operation(summary = "获取陪玩师时间详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CompanionScheduleVO> getById(@PathVariable("id") Long id) {
        CompanionScheduleVO vo = companionScheduleService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建陪玩师时间")
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> create(@Valid @RequestBody CompanionScheduleDTO dto) {
        companionScheduleService.create(dto);
        return Result.success();
    }

    @Operation(summary = "批量创建陪玩师时间")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> createBatch(
            @RequestParam(name = "companionId") Long companionId,
            @RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestBody(required = false) List<String> timeSlots) {
        companionScheduleService.createBatch(companionId, startDate, endDate, timeSlots);
        return Result.success();
    }

    @Operation(summary = "更新陪玩师时间")
    @PutMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> update(@Valid @RequestBody CompanionScheduleDTO dto) {
        companionScheduleService.update(dto);
        return Result.success();
    }

    @Operation(summary = "更新陪玩师时间状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> updateStatus(
            @RequestParam(name = "id") Long id,
            @RequestParam(name = "status") String status) {
        companionScheduleService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除陪玩师时间")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> delete(@PathVariable("id") Long id) {
        companionScheduleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "删除陪玩师指定日期的所有时间")
    @DeleteMapping("/by-companion-date")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> deleteByCompanionAndDate(
            @RequestParam(name = "companionId") Long companionId,
            @RequestParam(name = "scheduleDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate) {
        companionScheduleService.deleteByCompanionAndDate(companionId, scheduleDate);
        return Result.success();
    }

    @Operation(summary = "导出排班Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "companionId", required = false) Long companionId,
                            @RequestParam(name = "scheduleDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate,
                            @RequestParam(name = "status", required = false) String status) throws IOException {
        Page<CompanionScheduleVO> page = companionScheduleService.getPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, companionId, scheduleDate, status);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("companionName", "陪玩师姓名");
        headers.put("companionNickname", "陪玩师昵称");
        headers.put("scheduleDate", "日期");
        headers.put("timeSlot", "时间段");
        headers.put("startTime", "开始时间");
        headers.put("endTime", "结束时间");
        headers.put("status", "状态");
        headers.put("remark", "备注");
        ExcelUtils.export(response, "排班管理", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("companionName", item.getCompanionName());
            map.put("companionNickname", item.getCompanionNickname());
            map.put("scheduleDate", item.getScheduleDate() != null ? item.getScheduleDate().toString() : "");
            map.put("timeSlot", item.getTimeSlot());
            map.put("startTime", item.getStartTime() != null ? item.getStartTime().toString() : "");
            map.put("endTime", item.getEndTime() != null ? item.getEndTime().toString() : "");
            String statusText = switch (item.getStatus()) {
                case "AVAILABLE" -> "可预约";
                case "BOOKED" -> "已预约";
                case "UNAVAILABLE" -> "不可用";
                default -> item.getStatus();
            };
            map.put("status", statusText);
            map.put("remark", item.getRemark());
            return map;
        });
    }

    @Operation(summary = "导入排班Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                CompanionScheduleDTO dto = new CompanionScheduleDTO();
                String companionIdStr = row.getOrDefault("陪玩师ID", row.getOrDefault("companionId", ""));
                if (!companionIdStr.isEmpty()) dto.setCompanionId(Long.parseLong(companionIdStr));
                String dateStr = row.getOrDefault("日期", row.getOrDefault("scheduleDate", ""));
                if (!dateStr.isEmpty()) dto.setScheduleDate(LocalDate.parse(dateStr));
                dto.setTimeSlot(row.getOrDefault("时间段", row.getOrDefault("timeSlot", "")));
                String startStr = row.getOrDefault("开始时间", row.getOrDefault("startTime", ""));
                if (!startStr.isEmpty()) dto.setStartTime(LocalTime.parse(startStr));
                String endStr = row.getOrDefault("结束时间", row.getOrDefault("endTime", ""));
                if (!endStr.isEmpty()) dto.setEndTime(LocalTime.parse(endStr));
                String statusText = row.getOrDefault("状态", row.getOrDefault("status", "可预约"));
                dto.setStatus(switch (statusText) {
                    case "可预约" -> "AVAILABLE";
                    case "已预约" -> "BOOKED";
                    case "不可用" -> "UNAVAILABLE";
                    default -> statusText;
                });
                dto.setRemark(row.getOrDefault("备注", row.getOrDefault("remark", "")));
                companionScheduleService.create(dto);
                success++;
            } catch (Exception e) {
                fail++;
                log.warn("导入排班失败: {}", e.getMessage());
            }
        }
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
