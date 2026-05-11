package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.RevenueDailyReportService;
import com.delta.common.vo.RevenueDailyReportVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "营收报表管理", description = "数据分析/BI报表接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/reports")
@RequiredArgsConstructor
@PermAuth("revenue_report:view")
public class RevenueDailyReportController extends BaseController {

    private final RevenueDailyReportService revenueDailyReportService;

    @Operation(summary = "分页查询营收日报")
    @GetMapping("/page")
    public Result<Page<RevenueDailyReportVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "clubConfigId", required = false) String clubConfigId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(name = "gameType", required = false) String gameType) {
        Long decodedClubConfigId = clubConfigId != null ? decodeId(clubConfigId) : null;
        return Result.success(revenueDailyReportService.getPage(page, size, decodedClubConfigId, startDate, endDate, gameType));
    }

    @Operation(summary = "获取营收日报详情")
    @GetMapping("/{id}")
    public Result<RevenueDailyReportVO> getById(@PathVariable("id") String id) {
        return Result.success(revenueDailyReportService.getById(decodeId(id)));
    }

    @Operation(summary = "生成营收日报")
    @PostMapping("/generate")
    @PermAuth("revenue_report:edit")
    @AuditLog(module = "营收报表", action = "生成日报")
    public Result<Void> generateReport(@RequestBody Map<String, String> params) {
        Long clubConfigId = decodeId(params.get("clubConfigId"));
        LocalDate reportDate = LocalDate.parse(params.get("reportDate"));
        revenueDailyReportService.generateDailyReport(clubConfigId, reportDate);
        return Result.success();
    }
}
