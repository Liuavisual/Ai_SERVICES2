package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.AuditUserDTO;
import com.delta.common.dto.SysUserDTO;
import com.delta.common.service.SysUserService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.Result;
import com.delta.common.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统用户管理控制器
 * <p>
 * 权限控制策略：
 * <ul>
 *   <li>查询用户列表/详情：CS_LEADER 及以上</li>
 *   <li>创建/更新/删除用户：SYS_ADMIN 专属</li>
 *   <li>审核用户：CS_LEADER 及以上</li>
 * </ul>
 * </p>
 *
 * @author delta
 */
@Tag(name = "系统用户管理", description = "系统用户管理接口")
@RestController
@RequestMapping("/v1/sys-users")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询系统用户")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Page<SysUserVO>> getUserPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "status", required = false) String status) {
        Page<SysUserVO> pageResult = sysUserService.getUserPage(page, size, role, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取系统用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<SysUserVO> getUserById(@PathVariable("id") String id) {
        SysUserVO vo = sysUserService.getUserById(decodeId(id));
        return Result.success(vo);
    }

    @Operation(summary = "创建系统用户")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "用户管理", action = "创建用户")
    public Result<Void> createUser(@Valid @RequestBody SysUserDTO userDTO) {
        sysUserService.createUser(userDTO);
        return Result.success();
    }

    @Operation(summary = "更新系统用户")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "用户管理", action = "更新用户")
    public Result<Void> updateUser(@Valid @RequestBody SysUserDTO userDTO) {
        sysUserService.updateUser(userDTO);
        return Result.success();
    }

    @Operation(summary = "删除系统用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @AuditLog(module = "用户管理", action = "删除用户")
    public Result<Void> deleteUser(@PathVariable("id") String id) {
        sysUserService.deleteUser(decodeId(id));
        return Result.success();
    }

    @Operation(summary = "审核用户")
    @PostMapping("/audit")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> auditUser(@Valid @RequestBody AuditUserDTO auditDTO) {
        sysUserService.auditUser(auditDTO);
        return Result.success();
    }

    @Operation(summary = "导出系统用户Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "role", required = false) String role,
                            @RequestParam(name = "status", required = false) String status) throws IOException {
        Page<SysUserVO> page = sysUserService.getUserPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, role, status);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("username", "用户名");
        headers.put("realName", "真实姓名");
        headers.put("roleDesc", "角色");
        headers.put("statusDesc", "状态");
        headers.put("createdAt", "创建时间");
        ExcelUtils.export(response, "系统用户", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("username", item.getUsername());
            map.put("realName", item.getRealName());
            map.put("roleDesc", item.getRoleDesc());
            map.put("statusDesc", item.getStatusDesc());
            map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
            return map;
        });
    }
}
