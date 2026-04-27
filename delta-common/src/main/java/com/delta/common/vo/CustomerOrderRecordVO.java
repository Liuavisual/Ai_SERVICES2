package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户消费记录VO
 *
 * @author delta
 */
@Data
public class CustomerOrderRecordVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private Long companionId;

    private String companionName;

    private String orderType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    private BigDecimal durationHours;

    private BigDecimal amount;

    private String gameType;

    private String companionLevel;

    private String timeSlot;

    private Integer rating;

    private String reviewContent;

    private String status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
