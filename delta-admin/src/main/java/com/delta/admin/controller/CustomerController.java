package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.service.CustomerService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.CustomerVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "客户管理", description = "客户管理接口")
@RestController
@RequestMapping("/customers")
public class CustomerController extends BaseController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "分页查询客户")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CustomerVO>> getCustomerPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "aiEnabled", required = false) Boolean aiEnabled,
            @RequestParam(name = "csUserId", required = false) Long csUserId,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
            csUserId = getCurrentUserId(request);
        }
        Page<CustomerVO> page = customerService.getCustomerPage(pageNum, pageSize, platform, aiEnabled, csUserId, keyword);
        return Result.success(page);
    }

    @Operation(summary = "获取客户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CustomerVO> getCustomerById(@PathVariable("id") Long id,
                                               HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        CustomerVO customerVO = customerService.getCustomerById(id, currentUserId, currentUserRole);
        return Result.success(customerVO);
    }

    @Operation(summary = "切换AI启用状态")
    @PutMapping("/{id}/ai-enabled")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> toggleAiEnabled(
            @PathVariable("id") Long id,
            @Valid @RequestBody ToggleAiEnabledDTO dto) {
        customerService.toggleAiEnabled(id, dto.getAiEnabled());
        return Result.success();
    }

    @Operation(summary = "重新分配客服")
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> assignCustomer(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssignCustomerDTO dto) {
        customerService.assignCustomer(id, dto.getCsUserId(), dto.getAssignType(), dto.getRemark());
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
        private Long csUserId;

        private String assignType;

        private String remark;
    }

    @Operation(summary = "导出客户Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "platform", required = false) String platform,
                            @RequestParam(name = "aiEnabled", required = false) Boolean aiEnabled,
                            @RequestParam(name = "csUserId", required = false) Long csUserId,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            HttpServletRequest request) throws IOException {
        String role = getCurrentUserRole(request);
        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
            csUserId = getCurrentUserId(request);
        }
        Page<CustomerVO> page = customerService.getCustomerPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, platform, aiEnabled, csUserId, keyword);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("platform", "平台");
        headers.put("nickname", "昵称");
        headers.put("aiEnabled", "AI启用");
        headers.put("assignedCsUserName", "分配客服");
        headers.put("messageCount", "消息数");
        headers.put("lastActiveAt", "最后活跃");
        headers.put("createdAt", "创建时间");
        ExcelUtils.export(response, "客户列表", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("platform", item.getPlatform());
            map.put("nickname", item.getNickname());
            map.put("aiEnabled", item.getAiEnabled() != null && item.getAiEnabled() ? "是" : "否");
            map.put("assignedCsUserName", item.getAssignedCsUserName());
            map.put("messageCount", item.getMessageCount());
            map.put("lastActiveAt", item.getLastActiveAt() != null ? item.getLastActiveAt().toString() : "");
            map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
            return map;
        });
    }
}
