package com.delta.common.service;

import com.delta.common.vo.SysPermissionVO;

import java.util.List;

/**
 * 系统权限管理服务接口
 *
 * @author 刘建国
 */
public interface PermissionService {

    /**
     * 获取所有权限列表
     *
     * @return 权限列表
     */
    List<SysPermissionVO> listAll();

    /**
     * 按分组获取权限列表
     *
     * @param permGroup 权限分组
     * @return 权限列表
     */
    List<SysPermissionVO> listByGroup(String permGroup);

    /**
     * 获取用户的所有有效权限编码
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId   用户ID
     * @param permCode 权限编码
     * @return 是否拥有
     */
    boolean hasPermission(Long userId, String permCode);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermissionVO> getRolePermissions(Long roleId);

    /**
     * 分配权限给角色
     *
     * @param roleId   角色ID
     * @param permIds  权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permIds);

    /**
     * 初始化系统默认权限（首次部署时调用）
     */
    void initDefaultPermissions();
}
