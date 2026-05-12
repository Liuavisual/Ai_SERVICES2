package com.delta.admin.controller;

import com.delta.common.service.StatsService;

import com.delta.common.vo.Result;
import com.delta.common.vo.StatsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    @Test
    @DisplayName("获取个人统计数据 - 成功返回概览和趋势数据")
    void getPersonalStats_shouldReturnStatsData() {
        StatsVO.Overview overview = new StatsVO.Overview();
        overview.setTotalMessages(1000);
        overview.setTotalCustomers(50);
        overview.setAvgResponseTime(30);
        overview.setPendingCount(5);
        overview.setAiReplyCount(800);
        overview.setManualReplyCount(200);
        overview.setActiveCsCount(3);
        overview.setResolutionRate(new BigDecimal("0.95"));
        overview.setCustomerSatisfaction(new BigDecimal("4.5"));

        StatsVO.TrendData trend = new StatsVO.TrendData();
        trend.setDate("2026-01-01");
        trend.setMessageCount(100);

        StatsVO statsVO = new StatsVO();
        statsVO.setOverview(overview);
        statsVO.setTrendData(List.of(trend));

        when(statsService.getPersonalStats(any(), anyString(), any())).thenReturn(statsVO);

        Result<StatsVO> result = statsController.getPersonalStats(null, "DAILY", null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1000, result.getData().getOverview().getTotalMessages());
        assertEquals(50, result.getData().getOverview().getTotalCustomers());
        assertNotNull(result.getData().getTrendData());
        assertEquals(1, result.getData().getTrendData().size());
        assertEquals("2026-01-01", result.getData().getTrendData().get(0).getDate());
    }

    @Test
    @DisplayName("获取个人统计数据 - 带指定客服ID")
    void getPersonalStats_withCsUserId_shouldReturnStatsData() {
        StatsVO statsVO = new StatsVO();
        statsVO.setOverview(new StatsVO.Overview());

        when(statsService.getPersonalStats(eq(1L), eq("WEEKLY"), any())).thenReturn(statsVO);

        Result<StatsVO> result = statsController.getPersonalStats(1L, "WEEKLY", null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getOverview());
    }

    @Test
    @DisplayName("获取团队统计数据 - 成功返回概览和趋势数据")
    void getTeamStats_shouldReturnStatsData() {
        StatsVO.Overview overview = new StatsVO.Overview();
        overview.setTotalMessages(5000);
        overview.setTotalCustomers(200);
        overview.setAvgResponseTime(25);

        StatsVO statsVO = new StatsVO();
        statsVO.setOverview(overview);

        when(statsService.getTeamStats(anyString(), any())).thenReturn(statsVO);

        Result<StatsVO> result = statsController.getTeamStats("DAILY", null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(5000, result.getData().getOverview().getTotalMessages());
    }

    @Test
    @DisplayName("获取全局统计数据 - 成功返回概览和趋势数据")
    void getGlobalStats_shouldReturnStatsData() {
        StatsVO.Overview overview = new StatsVO.Overview();
        overview.setTotalMessages(10000);
        overview.setTotalCustomers(500);
        overview.setAvgResponseTime(20);

        StatsVO statsVO = new StatsVO();
        statsVO.setOverview(overview);

        when(statsService.getGlobalStats(anyString(), any())).thenReturn(statsVO);

        Result<StatsVO> result = statsController.getGlobalStats("DAILY", null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(10000, result.getData().getOverview().getTotalMessages());
    }

    @Test
    @DisplayName("获取全局统计数据 - 带日期参数")
    void getGlobalStats_withDate_shouldReturnStatsData() {
        StatsVO statsVO = new StatsVO();
        statsVO.setOverview(new StatsVO.Overview());

        when(statsService.getGlobalStats(eq("DAILY"), eq("2026-01-01"))).thenReturn(statsVO);

        Result<StatsVO> result = statsController.getGlobalStats("DAILY", "2026-01-01");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }
}
