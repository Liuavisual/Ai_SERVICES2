package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单查询数据传输对象")
public class OrderQueryDTO {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "陪玩师ID", example = "2001")
    private Long companionId;

    @Schema(description = "订单状态", example = "COMPLETED", allowableValues = {"PENDING", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private String orderStatus;

    @Schema(description = "支付状态", example = "PAID", allowableValues = {"UNPAID", "PAID", "REFUNDED"})
    private String paymentStatus;

    @Schema(description = "来源", example = "微信", allowableValues = {"微信", "企微", "APP", "网页"})
    private String source;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCompanionId() { return companionId; }
    public void setCompanionId(Long companionId) { this.companionId = companionId; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
