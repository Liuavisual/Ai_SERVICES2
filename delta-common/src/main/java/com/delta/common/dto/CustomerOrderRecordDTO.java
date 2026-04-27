package com.delta.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户消费记录DTO
 *
 * @author delta
 */
@Data
public class CustomerOrderRecordDTO {

    @NotNull(message = "客户ID不能为空")
    private Long userId;

    private Long companionId;

    @NotNull(message = "订单类型不能为空")
    private String orderType;

    @NotNull(message = "下单时间不能为空")
    private LocalDateTime orderTime;

    private BigDecimal durationHours;

    @NotNull(message = "消费金额不能为空")
    private BigDecimal amount;

    private String gameType;

    private String companionLevel;

    private String timeSlot;

    private Integer rating;

    private String reviewContent;

    private String status;

    private String remark;
}
