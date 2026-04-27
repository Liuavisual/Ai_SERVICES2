package com.delta.admin.controller;

import com.delta.common.dto.ServiceItemDTO;
import com.delta.common.dto.ServicePriceRuleDTO;
import com.delta.common.service.ServiceItemService;
import com.delta.common.vo.Result;
import com.delta.common.vo.ServiceItemVO;
import com.delta.common.vo.ServicePriceRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务项目管理", description = "服务项目管理接口")
@RestController
@RequestMapping("/service-items")
public class ServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    @Operation(summary = "获取俱乐部的服务项目")
    @GetMapping("/club/{clubConfigId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<ServiceItemVO>> getByClubId(@PathVariable("clubConfigId") Long clubConfigId) {
        return Result.success(serviceItemService.getByClubId(clubConfigId));
    }

    @Operation(summary = "获取游戏的服务项目")
    @GetMapping("/game/{gameConfigId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<ServiceItemVO>> getByGameId(@PathVariable("gameConfigId") Long gameConfigId) {
        return Result.success(serviceItemService.getByGameId(gameConfigId));
    }

    @Operation(summary = "获取服务项目详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<ServiceItemVO> getById(@PathVariable("id") Long id) {
        return Result.success(serviceItemService.getById(id));
    }

    @Operation(summary = "新增服务项目")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> create(@Valid @RequestBody ServiceItemDTO dto) {
        serviceItemService.create(dto);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新服务项目")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> update(@Valid @RequestBody ServiceItemDTO dto) {
        serviceItemService.update(dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除服务项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> delete(@PathVariable("id") Long id) {
        serviceItemService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取定价规则")
    @GetMapping("/{serviceItemId}/price-rules")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<List<ServicePriceRuleVO>> getPriceRules(@PathVariable("serviceItemId") Long serviceItemId) {
        return Result.success(serviceItemService.getPriceRules(serviceItemId));
    }

    @Operation(summary = "保存定价规则")
    @PostMapping("/price-rules")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> savePriceRule(@Valid @RequestBody ServicePriceRuleDTO dto) {
        serviceItemService.savePriceRule(dto);
        return Result.success("保存成功");
    }

    @Operation(summary = "删除定价规则")
    @DeleteMapping("/price-rules/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<String> deletePriceRule(@PathVariable("id") Long id) {
        serviceItemService.deletePriceRule(id);
        return Result.success("删除成功");
    }
}
