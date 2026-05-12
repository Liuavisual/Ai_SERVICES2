package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.*;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.*;
import com.delta.common.service.SysRoleService;
import com.delta.common.vo.SysPermissionVO;
import com.delta.common.vo.SysRoleVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统角色管理服务实现
 * <p>
 * 提供角色全生命周期管理 + 权限分配 + 用户角色绑定。
 * 系统内置角色（is_system=1）不可删除，但可修改权限。
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private static final Logger log = LoggerFactory.getLogger(SysRoleServiceImpl.class);

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionServiceImpl permissionService;

    @Override
    public Page<SysRoleVO> getRolePage(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword));
        }
        wrapper.orderByAsc(SysRole::getSortOrder).orderByAsc(SysRole::getId);

        Page<SysRole> rolePage = roleMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, List<SysPermissionVO>> permMap = new HashMap<>();
        for (SysRole role : rolePage.getRecords()) {
            permMap.put(role.getId(), permissionService.getRolePermissions(role.getId()));
        }

        Page<SysRoleVO> voPage = new Page<>(page, size, rolePage.getTotal());
        voPage.setRecords(rolePage.getRecords().stream().map(r -> {
            SysRoleVO vo = toVO(r, permMap.getOrDefault(r.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<SysRoleVO> listAllRoles() {
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSortOrder));
        return roles.stream().map(r -> {
            List<SysPermissionVO> perms = permissionService.getRolePermissions(r.getId());
            return toVO(r, perms);
        }).collect(Collectors.toList());
    }

    @Override
    public SysRoleVO getRoleDetail(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) throw new BusinessException("角色不存在");
        return toVO(role, permissionService.getRolePermissions(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole createRole(SysRole role) {
        if (!StringUtils.hasText(role.getRoleCode())) {
            throw new BusinessException("角色编码不能为空");
        }
        if (!StringUtils.hasText(role.getRoleName())) {
            throw new BusinessException("角色名称不能为空");
        }
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortOrder() == null) role.setSortOrder(99);
        if (role.getIsSystem() == null) role.setIsSystem(0);

        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在: " + role.getRoleCode());
        }

        roleMapper.insert(role);
        log.info("角色创建成功: code={}, name={}", role.getRoleCode(), role.getRoleName());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(Long roleId, SysRole updateData) {
        SysRole existing = roleMapper.selectById(roleId);
        if (existing == null) throw new BusinessException("角色不存在");

        if (StringUtils.hasText(updateData.getRoleName())) {
            existing.setRoleName(updateData.getRoleName());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getSortOrder() != null) {
            existing.setSortOrder(updateData.getSortOrder());
        }
        roleMapper.updateById(existing);
        log.info("角色更新成功: id={}, name={}", roleId, existing.getRoleName());
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) throw new BusinessException("角色不存在");
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BusinessException("系统内置角色不可删除");
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        userRoleMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
        log.info("角色删除成功: id={}, code={}", roleId, role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole copyRole(Long sourceRoleId, String newRoleCode, String newRoleName) {
        SysRole source = roleMapper.selectById(sourceRoleId);
        if (source == null) throw new BusinessException("源角色不存在");

        SysRole newRole = new SysRole();
        newRole.setRoleCode(newRoleCode);
        newRole.setRoleName(newRoleName);
        newRole.setDescription(source.getDescription() + " (复制)");
        newRole.setIsSystem(0);
        newRole.setStatus(source.getStatus());
        newRole.setSortOrder((source.getSortOrder() != null ? source.getSortOrder() : 99) + 1);

        createRole(newRole);

        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, sourceRoleId)
        ).stream().map(SysRolePermission::getPermId).collect(Collectors.toList());

        if (!permIds.isEmpty()) {
            permissionService.assignPermissions(newRole.getId(), permIds);
        }

        log.info("角色复制成功: source={}, target={}({})", source.getRoleCode(), newRoleCode, newRole.getId());
        return newRole;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permIds) {
        permissionService.assignPermissions(roleId, permIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermission(Long roleId, Long permId) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId)
                        .eq(SysRolePermission::getPermId, permId));
        log.info("移除角色权限: roleId={}, permId={}", roleId, permId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignRolesToUser(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        log.info("用户角色分配完成: userId={}, roleCount={}", userId, roleIds != null ? roleIds.size() : 0);
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        return roleMapper.findByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultRolesAndPermissions() {
        permissionService.initDefaultPermissions();

        long roleCount = roleMapper.selectCount(new LambdaQueryWrapper<>());
        if (roleCount > 0) {
            log.info("系统角色已初始化，跳过: count={}", roleCount);
            return;
        }

        Map<String, SysRole> roles = createDefaultRoles();
        Map<String, List<String>> rolePermMap = buildRolePermissionMapping();

        for (Map.Entry<String, SysRole> entry : roles.entrySet()) {
            SysRole role = entry.getValue();
            List<String> permCodes = rolePermMap.getOrDefault(entry.getKey(), List.of());
            if (!permCodes.isEmpty()) {
                List<Long> permIds = permissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .in(SysPermission::getPermCode, permCodes)
                ).stream().map(SysPermission::getId).collect(Collectors.toList());

                if (!permIds.isEmpty()) {
                    permIds.forEach(permId -> {
                        SysRolePermission rp = new SysRolePermission();
                        rp.setRoleId(role.getId());
                        rp.setPermId(permId);
                        rolePermissionMapper.insert(rp);
                    });
                }
            }
            log.info("角色初始化: code={}, name={}, permCount={}", role.getRoleCode(), role.getRoleName(),
                    permCodes.size());
        }

        log.info("系统默认角色及权限初始化完成: roleCount={}", roles.size());
    }

    private Map<String, SysRole> createDefaultRoles() {
        Map<String, SysRole> roles = new LinkedHashMap<>();

        SysRole admin = buildRole("SYS_ADMIN", "超级管理员", "系统最高权限管理者", 1, 1);
        roleMapper.insert(admin);
        roles.put("SYS_ADMIN", admin);

        SysRole csLeader = buildRole("CS_LEADER", "客服主管", "客服团队管理者", 1, 2);
        roleMapper.insert(csLeader);
        roles.put("CS_LEADER", csLeader);

        SysRole csStaff = buildRole("CS_STAFF", "客服人员", "一线客服工作人员", 1, 3);
        roleMapper.insert(csStaff);
        roles.put("CS_STAFF", csStaff);

        SysRole companion = buildRole("COMPANION", "陪玩师", "陪玩师角色", 1, 4);
        roleMapper.insert(companion);
        roles.put("COMPANION", companion);

        return roles;
    }

    private SysRole buildRole(String code, String name, String desc, int isSystem, int sort) {
        SysRole role = new SysRole();
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setDescription(desc);
        role.setIsSystem(isSystem);
        role.setStatus(1);
        role.setSortOrder(sort);
        return role;
    }

    private Map<String, List<String>> buildRolePermissionMapping() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("SYS_ADMIN", List.of("*", "system:admin", "permission:manage",
                // 工作台
                "dashboard:view", "stats:view",
                // 客户管理
                "customer:view", "customer:edit", "customer:assign", "customer:export",
                "customer_profile:view", "customer_profile:edit",
                "customer_satisfaction:view", "customer_satisfaction:edit",
                "customer_lifecycle:view", "customer_lifecycle:edit",
                // 陪玩师管理
                "companion:view", "companion:edit", "companion:export", "companion:import",
                "companion:rating",
                "companion_level:view", "companion_level:edit", "companion_level:export", "companion_level:import",
                "companion_settlement:view", "companion_settlement:edit",
                "companion_training:view", "companion_training:edit",
                // 排班管理
                "schedule:view", "schedule:edit", "schedule:export", "schedule:import",
                // 订单管理
                "order:view", "order:edit", "order:review",
                "order_status_history:view",
                "work_order:view", "work_order:edit",
                // 服务管理
                "service_item:view", "service_item:edit", "service_track:view",
                "pricing_plan:view", "pricing_plan:edit",
                // 活动套餐
                "activity_package:view", "activity_package:edit",
                "activity_package:export", "activity_package:import",
                // 订阅管理
                "subscription:view", "subscription:edit",
                // 营销活动
                "campaign:view", "campaign:edit",
                // 消息管理
                "message:view", "pending_message:view", "pending_message:edit",
                "pending_message:export", "chat:view",
                // 裂变推荐
                "referral:view", "referral:edit",
                // 客服分配
                "cs_assignment:view", "cs_assignment:edit",
                // 系统配置
                "platform:manage", "club_config:view", "club_config:edit",
                "game_config:view", "game_config:edit", "game_config:export",
                "ai_config:view", "ai_config:edit",
                "faq_item:view", "faq_item:edit", "faq_item:import",
                "keyword:view", "keyword:edit", "keyword:import",
                "reply:view", "reply:edit", "reply:import",
                "sys_user:view", "sys_user:edit", "sys_user:audit", "sys_user:export",
                // 质量检查
                "quality_check:view", "quality_check:edit",
                // 财报
                "revenue_report:view", "revenue_report:edit",
                // 缓存
                "cache:view", "cache:edit"));

        map.put("CS_LEADER", List.of(
                "dashboard:view", "stats:view",
                "customer:view", "customer:edit", "customer:assign", "customer:export",
                "customer_profile:view", "customer_profile:edit",
                "customer_satisfaction:view", "customer_satisfaction:edit",
                "customer_lifecycle:view", "customer_lifecycle:edit",
                "companion:view", "companion:edit", "companion:export",
                "companion_level:view", "companion_level:edit", "companion_level:export",
                "companion_settlement:view",
                "companion_training:view", "companion_training:edit",
                "schedule:view", "schedule:edit", "schedule:export",
                "order:view", "order:edit", "order:review", "order_status_history:view",
                "work_order:view", "work_order:edit",
                "service_item:view", "service_item:edit", "service_track:view",
                "pricing_plan:view",
                "activity_package:view", "activity_package:edit", "activity_package:export",
                "subscription:view",
                "campaign:view", "campaign:edit",
                "message:view", "pending_message:view", "pending_message:edit",
                "chat:view",
                "referral:view", "referral:edit",
                "cs_assignment:view", "cs_assignment:edit",
                "club_config:view", "game_config:view", "game_config:export",
                "faq_item:view", "faq_item:edit",
                "keyword:view", "keyword:edit", "keyword:import",
                "reply:view", "reply:edit", "reply:import",
                "sys_user:view", "sys_user:audit", "sys_user:export",
                "quality_check:view", "quality_check:edit",
                "revenue_report:view"));

        map.put("CS_STAFF", List.of(
                "dashboard:view", "stats:view",
                "customer:view",
                "customer_profile:view",
                "customer_satisfaction:view",
                "customer_lifecycle:view",
                "companion:view",
                "companion_level:view",
                "schedule:view",
                "order:view", "order_status_history:view",
                "work_order:view", "work_order:edit",
                "service_item:view", "service_track:view",
                "pricing_plan:view",
                "activity_package:view",
                "message:view", "pending_message:view",
                "chat:view",
                "referral:view",
                "faq_item:view",
                "keyword:view",
                "reply:view"));

        map.put("COMPANION", List.of(
                "dashboard:view",
                "companion:view",
                "schedule:view",
                "order:view"));

        return map;
    }

    private SysRoleVO toVO(SysRole role, List<SysPermissionVO> permissions) {
        SysRoleVO vo = new SysRoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setIsSystem(role.getIsSystem());
        vo.setStatus(role.getStatus());
        vo.setSortOrder(role.getSortOrder());
        vo.setPermissions(permissions);
        vo.setPermissionCount(permissions.size());
        return vo;
    }
}