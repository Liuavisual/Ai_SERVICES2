package com.delta.admin.controller;

import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.CsUserCustomerDTO;
import com.delta.common.service.CsUserCustomerService;
import com.delta.common.vo.CsUserCustomerVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客服-客户分配管理控制器
 *
 * @author 刘建国
 */
@Tag(name = "客服-客户分配管理", description = "客服-客户分配管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/cs-user-customer")
@RequiredArgsConstructor
@PermAuth("cs_assignment:view")
public class CsUserCustomerController {

    private final CsUserCustomerService csUserCustomerService;

    @Operation(summary = "分页查询客服-客户分配关系")
    @GetMapping("/page")
    public Result<Page<CsUserCustomerVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "csUserId", required = false) @DecodeId(required = false) Long csUserId,
            @RequestParam(name = "customerUserId", required = false) @DecodeId(required = false) Long customerUserId,
            @RequestParam(name = "status", required = false) String status) {
        Page<CsUserCustomerVO> pageResult = csUserCustomerService.getPage(page, size, csUserId, customerUserId, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取客服-客户分配详情")
    @GetMapping("/{id}")
    public Result<CsUserCustomerVO> getById(@PathVariable("id") @DecodeId Long id) {
        CsUserCustomerVO vo = csUserCustomerService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建客服-客户分配关系")
    @PostMapping
    @PermAuth("cs_assignment:edit")
    public Result<Void> create(@Valid @RequestBody CsUserCustomerDTO dto) {
        csUserCustomerService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新客服-客户分配关系")
    @PutMapping
    @PermAuth("cs_assignment:edit")
    public Result<Void> update(@Valid @RequestBody CsUserCustomerDTO dto) {
        csUserCustomerService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除客服-客户分配关系")
    @DeleteMapping("/{id}")
    @PermAuth("cs_assignment:edit")
    public Result<Void> delete(@PathVariable("id") @DecodeId Long id) {
        csUserCustomerService.delete(id);
        return Result.success();
    }
}
