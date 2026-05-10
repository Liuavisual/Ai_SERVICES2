package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统计数据视图对象")
public class StatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "概览数据")
    private Overview overview;

    @Schema(description = "趋势数据列表")
    private List<TrendData> trendData;

    @Schema(description = "客服数据列表")
    private List<CsUserData> csUserData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "概览数据")
    public static class Overview implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "总消息数", example = "12580")
        private Integer totalMessages;

        @Schema(description = "总客户数", example = "356")
        private Integer totalCustomers;

        @Schema(description = "平均响应时间(秒)", example = "30")
        private Integer avgResponseTime;

        @Schema(description = "待处理数量", example = "15")
        private Integer pendingCount;

        @Schema(description = "AI回复数量", example = "8900")
        private Integer aiReplyCount;

        @Schema(description = "人工回复数量", example = "3680")
        private Integer manualReplyCount;

        @Schema(description = "在线客服数", example = "8")
        private Integer activeCsCount;

        @Schema(description = "解决率", example = "0.95")
        private BigDecimal resolutionRate;

        @Schema(description = "客户满意度", example = "4.5")
        private BigDecimal customerSatisfaction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "趋势数据")
    public static class TrendData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "日期", example = "2026-01-01")
        private String date;

        @Schema(description = "消息数量", example = "580")
        private Integer messageCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "客服数据")
    public static class CsUserData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "客服ID", example = "d_xxxxx")
        @ObfuscatedId
        private Long csUserId;

        @Schema(description = "客服名称", example = "客服小李")
        private String csUserName;

        @Schema(description = "消息数量", example = "1200")
        private Integer messageCount;

        @Schema(description = "服务客户数", example = "56")
        private Integer customerCount;

        @Schema(description = "平均响应时间(秒)", example = "25")
        private Integer avgResponseTime;

        @Schema(description = "解决率", example = "0.92")
        private BigDecimal resolutionRate;
    }
}
