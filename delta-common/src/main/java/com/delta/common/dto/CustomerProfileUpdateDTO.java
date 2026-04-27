package com.delta.common.dto;

import lombok.Data;

/**
 * 客户画像更新DTO（仅允许修改部分字段）
 *
 * @author delta
 */
@Data
public class CustomerProfileUpdateDTO {

    private Long userId;

    private String tags;

    private String remark;

    private String memberLevel;

    private String riskLevel;
}
