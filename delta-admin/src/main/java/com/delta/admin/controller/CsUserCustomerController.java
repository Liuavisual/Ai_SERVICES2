package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CsUserCustomerDTO;
import com.delta.common.service.CsUserCustomerService;
import com.delta.common.vo.CsUserCustomerVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 客服-客户分配管理控制器
 *
 * @author delta
 */
@Tag(name = "客服-客户分配管理", description = "客服-客户分配管理接口")
@RestController
@RequestMapping("/v1/cs-user-customer")
@RequiredArgsConstructor
public class CsUserCustomerController extends BaseController {

    private final CsUserCustomerService csUserCustomerService;

    @Operation(summary = "分页查询客服-客户分配关系")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CsUserCustomerVO>> getPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "csUserId", required = false) String csUserId,
            @RequestParam(name = "customerUserId", required = false) String customerUserId,
            @RequestParam(name = "status", required = false) String status) {
        Long decodedCsUserId = csUserId != null ? decodeId(csUserId) : null;
        Long decodedCustomerUserId = customerUserId != null ? decodeId(customerUserId) : null;
        Page<CsUserCustomerVO> pageResult = csUserCustomerService.getPage(page, size, decodedCsUserId, decodedCustomerUserId, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取客服-客户分配详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CsUserCustomerVO> getById(@PathVariable("id") String id) {
        CsUserCustomerVO vo = csUserCustomerService.getById(decodeId(id));
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
    public Result<Void> delete(@PathVariable("id") String id) {
        csUserCustomerService.delete(decodeId(id));
        return Result.success();
    }
}
