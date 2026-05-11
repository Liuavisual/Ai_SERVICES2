package com.delta.admin.controller;

import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.PlatformConfigDTO;
import com.delta.common.service.PlatformConfigService;
import com.delta.common.vo.PlatformConfigVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台配置管理控制器
 * <p>
 * 权限：仅 SYS_ADMIN 可查看和修改
 * </p>
 *
 * @author 刘建国
 */
@RequiredArgsConstructor
@Tag(name = "平台配置管理", description = "平台配置管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/platform-configs")
@PermAuth("platform:manage")
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    /**
     * 获取所有平台配置
     *
     * @return 平台配置列表
     */
    @Operation(summary = "获取所有平台配置")
    @GetMapping
    public Result<List<PlatformConfigVO>> getAllPlatformConfigs() {
        List<PlatformConfigVO> configs = platformConfigService.getAllPlatformConfigs();
        return Result.success(configs);
    }

    /**
     * 获取指定平台配置
     *
     * @param platform 平台标识
     * @return 平台配置信息
     */
    @Operation(summary = "获取指定平台配置")
    @GetMapping("/{platform}")
    public Result<PlatformConfigVO> getPlatformConfigByPlatform(@PathVariable("platform") String platform) {
        PlatformConfigVO config = platformConfigService.getPlatformConfigByPlatform(platform);
        return Result.success(config);
    }

    /**
     * 更新平台配置
     *
     * @param configDTO 平台配置数据传输对象
     * @return 操作结果
     */
    @Operation(summary = "更新平台配置")
    @PutMapping
    public Result<Void> updatePlatformConfig(@Valid @RequestBody PlatformConfigDTO configDTO) {
        platformConfigService.updatePlatformConfig(configDTO);
        return Result.success();
    }
}
