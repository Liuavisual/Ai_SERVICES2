package com.delta.admin.controller;

import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.ClubConfigDTO;
import com.delta.common.service.CacheService;
import com.delta.common.service.ClubConfigService;
import com.delta.common.vo.ClubConfigVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 俱乐部配置管理控制器
 *
 * @author 刘建国
 */
@RequiredArgsConstructor
@Tag(name = "俱乐部配置管理", description = "俱乐部配置管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/club-config")
public class ClubConfigController {

    private static final Logger log = LoggerFactory.getLogger(ClubConfigController.class);

    private final ClubConfigService clubConfigService;

    private final CacheService cacheService;

    @Operation(summary = "获取俱乐部配置")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<ClubConfigVO> getClubConfig() {
        return Result.success(clubConfigService.getClubConfigVO());
    }

    @Operation(summary = "更新俱乐部配置")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> updateClubConfig(@Valid @RequestBody ClubConfigDTO configDTO) {
        clubConfigService.updateClubConfig(configDTO);
        log.info("更新俱乐部配置，刷新俱乐部配置缓存");
        cacheService.reloadClubConfig();
        return Result.success("更新成功");
    }
}
