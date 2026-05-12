package com.delta.admin.controller;

import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ServiceItemDTO;
import com.delta.common.dto.ServicePriceRuleDTO;
import com.delta.common.service.ServiceItemService;
import com.delta.common.vo.Result;
import com.delta.common.vo.ServiceItemVO;
import com.delta.common.vo.ServicePriceRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "服务项目管理", description = "服务项目管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/service-items")
@PermAuth("service_item:view")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @Operation(summary = "分页查询服务项目")
    @GetMapping("/page")
    public Result<Page<ServiceItemVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "clubConfigId", required = false) @DecodeId(required = false) Long clubConfigId,
            @RequestParam(name = "gameConfigId", required = false) @DecodeId(required = false) Long gameConfigId) {
        Page<ServiceItemVO> pageResult = serviceItemService.getPage(page, size, clubConfigId, gameConfigId);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取俱乐部的服务项目")
    @GetMapping("/club/{clubConfigId}")
    public Result<List<ServiceItemVO>> getByClubId(@PathVariable("clubConfigId") @DecodeId Long clubConfigId) {
        return Result.success(serviceItemService.getByClubId(clubConfigId));
    }

    @Operation(summary = "获取游戏的服务项目")
    @GetMapping("/game/{gameConfigId}")
    public Result<List<ServiceItemVO>> getByGameId(@PathVariable("gameConfigId") @DecodeId Long gameConfigId) {
        return Result.success(serviceItemService.getByGameId(gameConfigId));
    }

    @Operation(summary = "获取服务项目详情")
    @GetMapping("/{id}")
    public Result<ServiceItemVO> getById(@PathVariable("id") @DecodeId Long id) {
        return Result.success(serviceItemService.getById(id));
    }

    @Operation(summary = "新增服务项目")
    @PostMapping
    @PermAuth("service_item:edit")
    public Result<String> create(@Valid @RequestBody ServiceItemDTO dto) {
        serviceItemService.create(dto);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新服务项目")
    @PutMapping
    @PermAuth("service_item:edit")
    public Result<String> update(@Valid @RequestBody ServiceItemDTO dto) {
        serviceItemService.update(dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除服务项目")
    @DeleteMapping("/{id}")
    @PermAuth("service_item:edit")
    public Result<String> delete(@PathVariable("id") @DecodeId Long id) {
        serviceItemService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取定价规则")
    @GetMapping("/{serviceItemId}/price-rules")
    public Result<List<ServicePriceRuleVO>> getPriceRules(@PathVariable("serviceItemId") @DecodeId Long serviceItemId) {
        return Result.success(serviceItemService.getPriceRules(serviceItemId));
    }

    @Operation(summary = "保存定价规则")
    @PostMapping("/price-rules")
    @PermAuth("service_item:edit")
    public Result<String> savePriceRule(@Valid @RequestBody ServicePriceRuleDTO dto) {
        serviceItemService.savePriceRule(dto);
        return Result.success("保存成功");
    }

    @Operation(summary = "删除定价规则")
    @DeleteMapping("/price-rules/{id}")
    @PermAuth("service_item:edit")
    public Result<String> deletePriceRule(@PathVariable("id") @DecodeId Long id) {
        serviceItemService.deletePriceRule(id);
        return Result.success("删除成功");
    }
}
