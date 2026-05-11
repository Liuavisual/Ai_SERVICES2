package com.delta.admin.controller;

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
public class ServiceItemController extends BaseController {

    private final ServiceItemService serviceItemService;

    @Operation(summary = "分页查询服务项目")
    @GetMapping("/page")
    public Result<Page<ServiceItemVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "clubConfigId", required = false) String clubConfigId,
            @RequestParam(name = "gameConfigId", required = false) String gameConfigId) {
        Long decodedClubConfigId = clubConfigId != null ? decodeId(clubConfigId) : null;
        Long decodedGameConfigId = gameConfigId != null ? decodeId(gameConfigId) : null;
        Page<ServiceItemVO> pageResult = serviceItemService.getPage(page, size, decodedClubConfigId, decodedGameConfigId);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取俱乐部的服务项目")
    @GetMapping("/club/{clubConfigId}")
    public Result<List<ServiceItemVO>> getByClubId(@PathVariable("clubConfigId") String clubConfigId) {
        return Result.success(serviceItemService.getByClubId(decodeId(clubConfigId)));
    }

    @Operation(summary = "获取游戏的服务项目")
    @GetMapping("/game/{gameConfigId}")
    public Result<List<ServiceItemVO>> getByGameId(@PathVariable("gameConfigId") String gameConfigId) {
        return Result.success(serviceItemService.getByGameId(decodeId(gameConfigId)));
    }

    @Operation(summary = "获取服务项目详情")
    @GetMapping("/{id}")
    public Result<ServiceItemVO> getById(@PathVariable("id") String id) {
        return Result.success(serviceItemService.getById(decodeId(id)));
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
    public Result<String> delete(@PathVariable("id") String id) {
        serviceItemService.delete(decodeId(id));
        return Result.success("删除成功");
    }

    @Operation(summary = "获取定价规则")
    @GetMapping("/{serviceItemId}/price-rules")
    public Result<List<ServicePriceRuleVO>> getPriceRules(@PathVariable("serviceItemId") String serviceItemId) {
        return Result.success(serviceItemService.getPriceRules(decodeId(serviceItemId)));
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
    public Result<String> deletePriceRule(@PathVariable("id") String id) {
        serviceItemService.deletePriceRule(decodeId(id));
        return Result.success("删除成功");
    }
}
