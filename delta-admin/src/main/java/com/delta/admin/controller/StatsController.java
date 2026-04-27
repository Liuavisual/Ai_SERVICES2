package com.delta.admin.controller;

import com.delta.common.service.StatsService;
import com.delta.common.vo.Result;
import com.delta.common.vo.StatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 统计数据控制器
 * 提供个人、团队及全局的统计信息查询接口
 *
 * @author delta
 */
@RestController
@RequestMapping("/stats")
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 获取个人统计数据
     *
     * @param csUserId 客服用户ID，可选
     * @param period   统计周期，默认为 DAILY（日）
     * @param date     统计日期，可选
     * @return 个人统计结果
     */
    @GetMapping("/personal")
    public Result<StatsVO> getPersonalStats(
            @RequestParam(name = "csUserId", required = false) Long csUserId,
            @RequestParam(name = "period", defaultValue = "DAILY") String period,
            @RequestParam(name = "date", required = false) String date) {
        StatsVO stats = statsService.getPersonalStats(csUserId, period, date);
        return Result.success(stats);
    }

    /**
     * 获取团队统计数据
     *
     * @param period 统计周期，默认为 DAILY（日）
     * @param date   统计日期，可选
     * @return 团队统计结果
     */
    @GetMapping("/team")
    public Result<StatsVO> getTeamStats(
            @RequestParam(name = "period", defaultValue = "DAILY") String period,
            @RequestParam(name = "date", required = false) String date) {
        StatsVO stats = statsService.getTeamStats(period, date);
        return Result.success(stats);
    }

    /**
     * 获取全局统计数据
     *
     * @param period 统计周期，默认为 DAILY（日）
     * @param date   统计日期，可选
     * @return 全局统计结果
     */
    @GetMapping("/global")
    public Result<StatsVO> getGlobalStats(
            @RequestParam(name = "period", defaultValue = "DAILY") String period,
            @RequestParam(name = "date", required = false) String date) {
        StatsVO stats = statsService.getGlobalStats(period, date);
        return Result.success(stats);
    }
}
