package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "刷新令牌数据传输对象")
public class RefreshTokenDTO {

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.xxx")
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
