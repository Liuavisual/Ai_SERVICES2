package com.delta.admin.controller;

import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.PersonalityConfigDTO;
import com.delta.common.service.PersonalityConfigService;
import com.delta.common.vo.PersonalityConfigVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI人格配置管理控制器
 * <p>
 * 提供人格配置的完整CRUD管理接口，支持：
 * <ol>
 *   <li>全局默认人格配置 - 适用于所有游戏场景</li>
 *   <li>游戏特定人格配置 - 为三角洲行动、英雄联盟等游戏定制独立人格</li>
 *   <li>俱乐部专属配置 - 每个俱乐部可自定义品牌人格</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 */
@Tag(name = "AI人格配置管理", description = "管理AI客服人格配置的CRUD操作")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/personality")
@RequiredArgsConstructor
public class PersonalityConfigController extends BaseController {

    private final PersonalityConfigService personalityConfigService;

    /**
     * 查询俱乐部的人格式配置列表
     * <p>
     * 如果clubId为空，则查询系统默认配置列表。
     * 返回结果按启用状态降序、优先级降序、ID升序排列。
     * </p>
     *
     * @param clubId 俱乐部配置ID（可选，null=系统默认）
     * @return 人格配置列表
     */
    @Operation(summary = "查询人格配置列表", description = "获取俱乐部或系统的所有AI人格配置")
    @GetMapping("/configs")
    public Result<List<PersonalityConfigVO>> listConfigs(
            @Parameter(description = "俱乐部配置ID，null=系统默认")
            @RequestParam(required = false) Long clubId
    ) {
        List<PersonalityConfigVO> configs = personalityConfigService.getConfigsByClub(clubId);
        return Result.success(configs);
    }

    /**
     * 查询单个人格配置详情
     *
     * @param id 配置ID
     * @return 人格配置详情VO
     */
    @Operation(summary = "查询人格配置详情", description = "根据ID获取单个人格配置的完整信息")
    @GetMapping("/configs/{id}")
    public Result<PersonalityConfigVO> getConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long id
    ) {
        PersonalityConfigVO config = personalityConfigService.getConfigById(id);
        if (config == null) {
            return Result.error(404, "人格配置不存在");
        }
        return Result.success(config);
    }

    /**
     * 创建新的人格配置
     * <p>
     * 创建成功后自动清除Redis缓存，下次消息处理自动加载新配置。
     * </p>
     *
     * @param dto 人格配置参数
     * @return 创建后的人格配置VO
     */
    @Operation(summary = "创建人格配置", description = "创建新的AI人格配置，支持全局/游戏特定/俱乐部专属三种层级")
    @PostMapping("/configs")
    public Result<PersonalityConfigVO> createConfig(
            @Parameter(description = "人格配置参数", required = true)
            @Valid @RequestBody PersonalityConfigDTO dto
    ) {
        PersonalityConfigVO config = personalityConfigService.createConfig(dto);
        return Result.success(config);
    }

    /**
     * 更新已有的人格配置
     * <p>
     * 更新成功后自动清除Redis缓存。
     * 注意：统计类字段（转化率、满意度、对话数）不会被DTO覆盖，它们由系统自动维护。
     * </p>
     *
     * @param id  配置ID
     * @param dto 更新的参数
     * @return 更新后的人格配置VO
     */
    @Operation(summary = "更新人格配置", description = "更新已有AI人格配置的参数，统计字段不受影响")
    @PutMapping("/configs/{id}")
    public Result<PersonalityConfigVO> updateConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新的参数", required = true)
            @Valid @RequestBody PersonalityConfigDTO dto
    ) {
        PersonalityConfigVO config = personalityConfigService.updateConfig(id, dto);
        return Result.success(config);
    }

    /**
     * 删除人格配置（逻辑删除）
     *
     * @param id 配置ID
     * @return 操作结果
     */
    @Operation(summary = "删除人格配置", description = "逻辑删除指定的人格配置")
    @DeleteMapping("/configs/{id}")
    public Result<Void> deleteConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long id
    ) {
        personalityConfigService.deleteConfig(id);
        return Result.success();
    }

    /**
     * 启用/禁用指定的人格配置
     * <p>
     * 禁用后该配置不会参与运行时匹配。
     * 建议使用此接口而非删除来进行A/B测试。
     * </p>
     *
     * @param id      配置ID
     * @param enabled 启用状态：1-启用，0-禁用
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用人格配置", description = "切换人格配置的启用状态，可用于A/B测试")
    @PutMapping("/configs/{id}/toggle")
    public Result<Void> toggleConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "启用状态：1-启用，0-禁用", required = true)
            @RequestParam Integer enabled
    ) {
        personalityConfigService.toggleConfig(id, enabled);
        return Result.success();
    }
}
