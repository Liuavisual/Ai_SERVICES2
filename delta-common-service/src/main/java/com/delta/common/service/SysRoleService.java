package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.SysRole;
import com.delta.common.vo.SysRoleVO;

import java.util.List;

/**
 * 系统角色管理服务接口
 * <p>
 * 提供完整的角色生命周期管理：
 * <ul>
 *   <li>角色CRUD（创建/查询/更新/删除）</li>
 *   <li>角色复制（克隆已有角色及其权限）</li>
 *   <li>批量分配/移除权限</li>
 *   <li>权限继承与覆盖机制</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
public interface SysRoleService {

    Page<SysRoleVO> getRolePage(Integer page, Integer size, String keyword);

    List<SysRoleVO> listAllRoles();

    SysRoleVO getRoleDetail(Long roleId);

    SysRole createRole(SysRole role);

    SysRole updateRole(Long roleId, SysRole role);

    void deleteRole(Long roleId);

    SysRole copyRole(Long sourceRoleId, String newRoleCode, String newRoleName);

    void assignPermissions(Long roleId, List<Long> permIds);

    void removePermission(Long roleId, Long permId);

    void batchAssignRolesToUser(Long userId, List<Long> roleIds);

    List<SysRole> getUserRoles(Long userId);

    void initDefaultRolesAndPermissions();
}