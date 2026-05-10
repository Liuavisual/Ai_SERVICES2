package com.delta.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "基础视图对象")
public class BaseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "行号", example = "1")
    private Integer rowNum;
}
