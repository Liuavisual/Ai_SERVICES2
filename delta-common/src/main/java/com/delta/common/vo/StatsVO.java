package com.delta.common.vo;

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

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }

    public List<TrendData> getTrendData() {
        return trendData;
    }

    public void setTrendData(List<TrendData> trendData) {
        this.trendData = trendData;
    }

    public List<CsUserData> getCsUserData() {
        return csUserData;
    }

    public void setCsUserData(List<CsUserData> csUserData) {
        this.csUserData = csUserData;
    }

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

        public Integer getTotalMessages() {
            return totalMessages;
        }

        public void setTotalMessages(Integer totalMessages) {
            this.totalMessages = totalMessages;
        }

        public Integer getTotalCustomers() {
            return totalCustomers;
        }

        public void setTotalCustomers(Integer totalCustomers) {
            this.totalCustomers = totalCustomers;
        }

        public Integer getAvgResponseTime() {
            return avgResponseTime;
        }

        public void setAvgResponseTime(Integer avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
        }

        public Integer getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(Integer pendingCount) {
            this.pendingCount = pendingCount;
        }

        public Integer getAiReplyCount() {
            return aiReplyCount;
        }

        public void setAiReplyCount(Integer aiReplyCount) {
            this.aiReplyCount = aiReplyCount;
        }

        public Integer getManualReplyCount() {
            return manualReplyCount;
        }

        public void setManualReplyCount(Integer manualReplyCount) {
            this.manualReplyCount = manualReplyCount;
        }

        public Integer getActiveCsCount() {
            return activeCsCount;
        }

        public void setActiveCsCount(Integer activeCsCount) {
            this.activeCsCount = activeCsCount;
        }

        public BigDecimal getResolutionRate() {
            return resolutionRate;
        }

        public void setResolutionRate(BigDecimal resolutionRate) {
            this.resolutionRate = resolutionRate;
        }

        public BigDecimal getCustomerSatisfaction() {
            return customerSatisfaction;
        }

        public void setCustomerSatisfaction(BigDecimal customerSatisfaction) {
            this.customerSatisfaction = customerSatisfaction;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String date;
        private Integer messageCount;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getMessageCount() {
            return messageCount;
        }

        public void setMessageCount(Integer messageCount) {
            this.messageCount = messageCount;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CsUserData implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long csUserId;
        private String csUserName;
        private Integer messageCount;
        private Integer customerCount;
        private Integer avgResponseTime;
        private BigDecimal resolutionRate;

        public CsUserData(Long csUserId, String csUserName, Integer messageCount,
                         Integer customerCount, Integer avgResponseTime, BigDecimal resolutionRate) {
            this.csUserId = csUserId;
            this.csUserName = csUserName;
            this.messageCount = messageCount;
            this.customerCount = customerCount;
            this.avgResponseTime = avgResponseTime;
            this.resolutionRate = resolutionRate;
        }

        public Long getCsUserId() {
            return csUserId;
        }

        public void setCsUserId(Long csUserId) {
            this.csUserId = csUserId;
        }

        public String getCsUserName() {
            return csUserName;
        }

        public void setCsUserName(String csUserName) {
            this.csUserName = csUserName;
        }

        public Integer getMessageCount() {
            return messageCount;
        }

        public void setMessageCount(Integer messageCount) {
            this.messageCount = messageCount;
        }

        public Integer getCustomerCount() {
            return customerCount;
        }

        public void setCustomerCount(Integer customerCount) {
            this.customerCount = customerCount;
        }

        public Integer getAvgResponseTime() {
            return avgResponseTime;
        }

        public void setAvgResponseTime(Integer avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
        }

        public BigDecimal getResolutionRate() {
            return resolutionRate;
        }

        public void setResolutionRate(BigDecimal resolutionRate) {
            this.resolutionRate = resolutionRate;
        }
    }
}
