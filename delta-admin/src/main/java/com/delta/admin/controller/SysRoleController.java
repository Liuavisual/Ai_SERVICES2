package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.entity.SysRole;
import com.delta.common.service.SysRoleService;
import com.delta.common.vo.Result;
import com.delta.common.vo.SysRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统角色管理控制器
 * <p>
 * 提供角色的完整生命周期管理接口：
 * <ol>
 *   <li>分页查询角色列表</li>
 *   <li>获取所有启用角色</li>
 *   <li>获取角色详情（含权限列表）</li>
 *   <li>创建/更新/删除角色</li>
 *   <li>复制角色（含权限克隆）</li>
 *   <li>角色权限分配与移除</li>
 *   <li>用户角色批量分配与查询</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 */
@Tag(name = "角色管理", description = "管理系统角色及角色权限分配")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/sys-roles")
@RequiredArgsConstructor
@PermAuth("permission:manage")
public class SysRoleController extends BaseController {

    /** 角色管理服务 */
    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色列表
     *
     * @param page    页码（默认1）
     * @param size    每页数量（默认10）
     * @param keyword 搜索关键词（匹配角色编码或角色名称）
     * @return 角色分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询角色", description = "按关键词模糊匹配角色编码或名称，返回分页结果")
    public Result<Page<SysRoleVO>> getRolePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(sysRoleService.getRolePage(page, size, keyword));
    }

    /**
     * 获取所有启用角色
     *
     * @return 所有启用状态的角色列表（含权限）
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有角色", description = "返回所有启用状态的角色及其关联权限列表")
    public Result<List<SysRoleVO>> listAllRoles() {
        return Result.success(sysRoleService.listAllRoles());
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情（含权限列表）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色完整信息，包含权限列表")
    public Result<SysRoleVO> getRoleDetail(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        return Result.success(sysRoleService.getRoleDetail(id));
    }

    /**
     * 创建角色
     *
     * @param role 角色实体（至少包含roleCode和roleName）
     * @return 创建成功后的角色实体
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的自定义角色，角色编码不可重复")
    public Result<SysRole> createRole(@Valid @RequestBody SysRole role) {
        return Result.success(sysRoleService.createRole(role));
    }

    /**
     * 更新角色
     *
     * @param role 角色实体（必须包含id）
     * @return 更新后的角色实体
     */
    @PutMapping
    @Operation(summary = "更新角色", description = "根据角色ID更新角色信息，系统内置角色不可修改编码")
    public Result<SysRole> updateRole(@Valid @RequestBody SysRole role) {
        return Result.success(sysRoleService.updateRole(role.getId(), role));
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除指定角色及其关联的权限和用户绑定，系统内置角色不可删除")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 复制角色
     *
     * @param id   源角色ID
     * @param body 包含新角色编码和名称的请求体 (newRoleCode, newRoleName)
     * @return 复制后的新角色实体
     */
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制角色", description = "克隆指定角色的基本信息及其所有权限到新角色")
    public Result<SysRole> copyRole(
            @Parameter(description = "源角色ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newRoleCode = body.get("newRoleCode");
        String newRoleName = body.get("newRoleName");
        return Result.success(sysRoleService.copyRole(id, newRoleCode, newRoleName));
    }

    /**
     * 分配角色权限（全量覆盖）
     *
     * @param id   角色ID
     * @param body 权限ID列表 (permIds: [1, 2, 3])
     * @return 操作结果
     */
    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配角色权限", description = "为指定角色设置权限列表（全量覆盖，先清除再设置）")
    public Result<Void> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> permIds = body.get("permIds");
        sysRoleService.assignPermissions(id, permIds);
        return Result.success();
    }

    /**
     * 移除角色单个权限
     *
     * @param id     角色ID
     * @param permId 权限ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/permissions/{permId}")
    @Operation(summary = "移除角色单个权限", description = "从指定角色中移除某个具体权限")
    public Result<Void> removePermission(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Parameter(description = "权限ID") @PathVariable Long permId) {
        sysRoleService.removePermission(id, permId);
        return Result.success();
    }

    /**
     * 批量分配用户角色
     *
     * @param userId 用户ID
     * @param body   角色ID列表 (roleIds: [1, 2, 3])
     * @return 操作结果
     */
    @PutMapping("/assign/{userId}")
    @Operation(summary = "批量分配用户角色", description = "为指定用户设置自定义角色列表（全量覆盖，先清除再设置）")
    public Result<Void> batchAssignRolesToUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        sysRoleService.batchAssignRolesToUser(userId, roleIds);
        return Result.success();
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 该用户绑定的角色列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户角色列表", description = "获取指定用户绑定的所有自定义角色")
    public Result<List<SysRole>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(sysRoleService.getUserRoles(userId));
    }
}