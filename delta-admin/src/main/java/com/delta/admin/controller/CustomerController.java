package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.service.CustomerService;
import com.delta.common.vo.CustomerVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "客户管理", description = "客户管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/customers")
@RequiredArgsConstructor
public class CustomerController extends BaseController {

    private final CustomerService customerService;

    @Operation(summary = "分页查询客户")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CustomerVO>> getCustomerPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "aiEnabled", required = false) Boolean aiEnabled,
            @RequestParam(name = "csUserId", required = false) String csUserId,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        Long decodedCsUserId = csUserId != null ? decodeId(csUserId) : null;
        String role = getCurrentUserRole(request);
        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
            decodedCsUserId = getCurrentUserId(request);
        }
        Page<CustomerVO> pageResult = customerService.getCustomerPage(page, size, platform, aiEnabled, decodedCsUserId, keyword);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取客户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CustomerVO> getCustomerById(@PathVariable("id") String id,
                                               HttpServletRequest request) {
        Long decodedId = decodeId(id);
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        CustomerVO customerVO = customerService.getCustomerById(decodedId, currentUserId, currentUserRole);
        return Result.success(customerVO);
    }

    @Operation(summary = "切换AI启用状态")
    @PutMapping("/{id}/ai-enabled")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> toggleAiEnabled(
            @PathVariable("id") String id,
            @Valid @RequestBody ToggleAiEnabledDTO dto) {
        customerService.toggleAiEnabled(decodeId(id), dto.getAiEnabled());
        return Result.success();
    }

    @Operation(summary = "重新分配客服")
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    @AuditLog(module = "客户管理", action = "分配客户")
    public Result<Void> assignCustomer(
            @PathVariable("id") String id,
            @Valid @RequestBody AssignCustomerDTO dto) {
        customerService.assignCustomer(decodeId(id), dto.getDecodedCsUserId(), dto.getAssignType(), dto.getRemark());
        return Result.success();
    }

    @Data
    public static class ToggleAiEnabledDTO {
        @NotNull(message = "AI状态不能为空")
        private Boolean aiEnabled;
    }

    @Data
    public static class AssignCustomerDTO {
        @NotNull(message = "客服ID不能为空")
        private String csUserId;

        private String assignType;

        private String remark;

        public Long getDecodedCsUserId() {
            return com.delta.common.util.IdObfuscateUtils.decodeRequired(csUserId);
        }
    }

    @Operation(summary = "导出客户Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "platform", required = false) String platform,
                            @RequestParam(name = "aiEnabled", required = false) Boolean aiEnabled,
                            @RequestParam(name = "csUserId", required = false) String csUserId,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            HttpServletRequest request) {
        Long decodedCsUserId = csUserId != null ? decodeId(csUserId) : null;
        String role = getCurrentUserRole(request);
        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
            decodedCsUserId = getCurrentUserId(request);
        }
        customerService.exportCustomers(response, platform, aiEnabled, decodedCsUserId, keyword);
    }
}
