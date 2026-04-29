package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户画像更新DTO（仅允许修改部分字段）
 *
 * @author delta
 */
@Data
@Schema(description = "客户画像更新数据传输对象")
public class CustomerProfileUpdateDTO {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "标签", example = "高价值用户,VIP")
    private String tags;

    @Schema(description = "备注", example = "重点客户，需定期回访")
    private String remark;

    @Schema(description = "会员等级", example = "GOLD", allowableValues = {"NORMAL", "SILVER", "GOLD", "DIAMOND"})
    private String memberLevel;

    @Schema(description = "风险等级", example = "LOW", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String riskLevel;
}
