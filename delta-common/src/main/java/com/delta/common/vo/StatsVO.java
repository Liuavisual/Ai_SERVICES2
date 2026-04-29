package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Overview overview;
    private List<TrendData> trendData;
    private List<CsUserData> csUserData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer totalMessages;
        private Integer totalCustomers;
        private Integer avgResponseTime;
        private Integer pendingCount;
        private Integer aiReplyCount;
        private Integer manualReplyCount;
        private Integer activeCsCount;
        private BigDecimal resolutionRate;
        private BigDecimal customerSatisfaction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String date;
        private Integer messageCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsUserData implements Serializable {
        private static final long serialVersionUID = 1L;
        @ObfuscatedId
        private Long csUserId;
        private String csUserName;
        private Integer messageCount;
        private Integer customerCount;
        private Integer avgResponseTime;
        private BigDecimal resolutionRate;
    }
}
