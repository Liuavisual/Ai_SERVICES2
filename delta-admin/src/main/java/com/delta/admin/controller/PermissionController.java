package com.delta.admin.controller;

import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.entity.SysRole;
import com.delta.common.entity.SysUserRole;
import com.delta.common.mapper.SysRoleMapper;
import com.delta.common.mapper.SysUserRoleMapper;
import com.delta.common.service.PermissionService;
import com.delta.common.vo.Result;
import com.delta.common.vo.SysPermissionVO;
import com.delta.common.vo.SysRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理控制器
 * <p>
 * 提供权限管理的完整接口：
 * <ol>
 *   <li>权限列表查询 - 查看系统中所有可分配的权限</li>
 *   <li>角色管理 - 创建/编辑/删除自定义角色</li>
 *   <li>权限分配 - 为角色分配权限</li>
 *   <li>用户角色分配 - 为用户分配角色</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 */
@Tag(name = "权限管理", description = "管理角色和权限的分配")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/permission")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYS_ADMIN')")
public class PermissionController extends BaseController {

    private final PermissionService permissionService;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    // ==================== 权限查询 ====================

    @GetMapping("/list")
    @Operation(summary = "获取所有权限列表", description = "返回系统中所有可分配的权限，按分组排列")
    public Result<List<SysPermissionVO>> listPermissions() {
        return Result.success(permissionService.listAll());
    }

    @GetMapping("/list-by-group")
    @Operation(summary = "按分组获取权限列表", description = "获取指定分组的权限列表")
    public Result<List<SysPermissionVO>> listPermissionsByGroup(
            @Parameter(description = "权限分组") @RequestParam String group) {
        return Result.success(permissionService.listByGroup(group));
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    @Operation(summary = "获取所有角色", description = "返回系统中所有自定义角色及其关联权限")
    public Result<List<SysRoleVO>> listRoles() {
        List<SysRole> roles = roleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                        .orderByAsc(SysRole::getSortOrder));
        List<SysRoleVO> vos = roles.stream().map(r -> {
            SysRoleVO vo = new SysRoleVO();
            vo.setId(r.getId());
            vo.setRoleCode(r.getRoleCode());
            vo.setRoleName(r.getRoleName());
            vo.setDescription(r.getDescription());
            vo.setIsSystem(r.getIsSystem());
            vo.setStatus(r.getStatus());
            vo.setSortOrder(r.getSortOrder());
            vo.setPermissions(permissionService.getRolePermissions(r.getId()));
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    @PostMapping("/roles")
    @Operation(summary = "创建角色", description = "创建新的自定义角色")
    public Result<SysRole> createRole(@RequestBody SysRole role) {
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortOrder() == null) role.setSortOrder(99);
        if (role.getIsSystem() == null) role.setIsSystem(0);
        roleMapper.insert(role);
        return Result.success(role);
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) return Result.error(404, "角色不存在");
        existing.setRoleName(role.getRoleName());
        existing.setDescription(role.getDescription());
        existing.setStatus(role.getStatus());
        existing.setSortOrder(role.getSortOrder());
        roleMapper.updateById(existing);
        return Result.success(existing);
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "删除角色", description = "删除自定义角色（系统内置角色不可删除）")
    public Result<Void> deleteRole(@PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) return Result.error(404, "角色不存在");
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            return Result.error(400, "系统内置角色不可删除");
        }
        roleMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 权限分配 ====================

    @PutMapping("/roles/{roleId}/permissions")
    @Operation(summary = "分配角色权限", description = "为指定角色设置权限列表（全量覆盖）")
    public Result<Void> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> permIds = body.get("permIds");
        permissionService.assignPermissions(roleId, permIds);
        return Result.success();
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限")
    public Result<List<SysPermissionVO>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(permissionService.getRolePermissions(roleId));
    }

    // ==================== 用户角色分配 ====================

    @PutMapping("/users/{userId}/roles")
    @Operation(summary = "分配用户角色", description = "为指定用户设置自定义角色（全量覆盖）")
    public Result<Void> assignUserRoles(
            @PathVariable Long userId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }

    @GetMapping("/users/{userId}/roles")
    @Operation(summary = "获取用户角色", description = "获取指定用户的自定义角色列表")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        return Result.success(roleMapper.findByUserId(userId));
    }

    // ==================== 权限初始化 ====================

    @PostMapping("/init")
    @Operation(summary = "初始化默认权限", description = "首次部署时初始化系统默认权限定义（已存在则跳过）")
    public Result<Void> initPermissions() {
        permissionService.initDefaultPermissions();
        return Result.success();
    }
}
