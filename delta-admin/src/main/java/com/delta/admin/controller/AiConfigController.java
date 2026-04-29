package com.delta.admin.controller;

import com.delta.common.dto.AiConfigUpdateDTO;
import com.delta.common.service.AiConfigService;
import com.delta.common.service.CacheService;
import com.delta.common.vo.AiConfigVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI配置管理控制器
 *
 * @author delta
 */
@Tag(name = "AI配置管理", description = "AI配置管理接口")
@RestController
@RequestMapping("/v1/ai-config")
public class AiConfigController {

    private static final Logger log = LoggerFactory.getLogger(AiConfigController.class);

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private CacheService cacheService;

    @Operation(summary = "获取所有AI配置")
    @GetMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<List<AiConfigVO>> getAllConfigs() {
        List<AiConfigVO> configs = aiConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "更新AI配置")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> updateConfigs(@Valid @RequestBody AiConfigUpdateDTO updateDTO) {
        aiConfigService.updateConfigs(updateDTO);
        log.info("更新AI配置，刷新AI配置缓存");
        cacheService.reloadAiConfig();
        return Result.success("更新成功");
    }
}
