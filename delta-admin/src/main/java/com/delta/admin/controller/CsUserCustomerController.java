package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CsUserCustomerDTO;
import com.delta.common.service.CsUserCustomerService;
import com.delta.common.vo.CsUserCustomerVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 客服-客户分配管理控制器
 *
 * @author delta
 */
@Tag(name = "客服-客户分配管理", description = "客服-客户分配管理接口")
@RestController
@RequestMapping("/cs-user-customer")
public class CsUserCustomerController {

    @Autowired
    private CsUserCustomerService csUserCustomerService;

    @Operation(summary = "分页查询客服-客户分配关系")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CsUserCustomerVO>> getPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "csUserId", required = false) Long csUserId,
            @RequestParam(name = "customerUserId", required = false) Long customerUserId,
            @RequestParam(name = "status", required = false) String status) {
        Page<CsUserCustomerVO> page = csUserCustomerService.getPage(pageNum, pageSize, csUserId, customerUserId, status);
        return Result.success(page);
    }

    @Operation(summary = "获取客服-客户分配详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CsUserCustomerVO> getById(@PathVariable("id") Long id) {
        CsUserCustomerVO vo = csUserCustomerService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建客服-客户分配关系")
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> create(@Valid @RequestBody CsUserCustomerDTO dto) {
        csUserCustomerService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新客服-客户分配关系")
    @PutMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> update(@Valid @RequestBody CsUserCustomerDTO dto) {
        csUserCustomerService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除客服-客户分配关系")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> delete(@PathVariable("id") Long id) {
        csUserCustomerService.delete(id);
        return Result.success();
    }
}
