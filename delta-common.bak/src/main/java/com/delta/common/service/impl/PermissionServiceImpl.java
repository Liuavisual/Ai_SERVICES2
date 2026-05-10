package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.SysPermission;
import com.delta.common.entity.SysRolePermission;
import com.delta.common.mapper.SysPermissionMapper;
import com.delta.common.mapper.SysRolePermissionMapper;
import com.delta.common.service.PermissionService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.SysPermissionVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统权限管理服务实现
 * <p>
 * 权限缓存策略：
 * <ul>
 *   <li>用户权限缓存: Redis Set, Key=delta:perm:user:{userId}, TTL=30分钟</li>
 *   <li>角色权限缓存: Redis Set, Key=delta:perm:role:{roleId}, TTL=30分钟</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    /** 用户权限缓存Key前缀 */
    private static final String USER_PERM_CACHE_PREFIX = "delta:perm:user:";
    /** 角色权限缓存Key前缀 */
    private static final String ROLE_PERM_CACHE_PREFIX = "delta:perm:role:";
    /** 缓存TTL（分钟） */
    private static final long CACHE_TTL_MINUTES = 30;

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final RedisService redisService;

    @Override
    public List<SysPermissionVO> listAll() {
        List<SysPermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSortOrder));
        return permissions.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<SysPermissionVO> listByGroup(String permGroup) {
        List<SysPermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getPermGroup, permGroup)
                        .orderByAsc(SysPermission::getSortOrder));
        return permissions.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("null")
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) return Collections.emptyList();
        String cacheKey = USER_PERM_CACHE_PREFIX + userId;
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                String cachedStr = cached.toString();
                if (!cachedStr.isEmpty()) {
                    return Arrays.asList(cachedStr.split(","));
                }
            }
        } catch (Exception e) {
            log.debug("读取用户权限缓存失败: userId={}", userId);
        }
        List<String> perms = new ArrayList<>(permissionMapper.findPermCodesByUserId(userId));
        try {
            String joinedPerms = String.join(",", perms);
            redisService.set(cacheKey, joinedPerms, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.debug("缓存用户权限失败: userId={}", userId);
        }
        return perms;
    }

    @Override
    public boolean hasPermission(Long userId, String permCode) {
        if (userId == null || permCode == null) return false;
        try {
            List<String> perms = getUserPermissions(userId);
            return perms.contains(permCode);
        } catch (Exception e) {
            log.warn("权限检查异常: userId={}, permCode={}, error={}", userId, permCode, e.getMessage());
            return false;
        }
    }

    @Override
    public List<SysPermissionVO> getRolePermissions(Long roleId) {
        String cacheKey = ROLE_PERM_CACHE_PREFIX + roleId;
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                String cachedStr = cached.toString();
                if (!cachedStr.isEmpty()) {
                    List<Long> ids = Arrays.stream(cachedStr.split(","))
                            .map(Long::parseLong).collect(Collectors.toList());
                    List<SysPermission> perms = permissionMapper.selectList(
                            new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, ids));
                    return perms.stream().map(this::toVO).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.debug("读取角色权限缓存失败: roleId={}", roleId);
        }
        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        ).stream().map(SysRolePermission::getPermId).collect(Collectors.toList());
        List<SysPermission> perms = permIds.isEmpty() ? Collections.emptyList()
                : permissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permIds));
        return perms.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permIds != null && !permIds.isEmpty()) {
            for (Long permId : permIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermId(permId);
                rolePermissionMapper.insert(rp);
            }
        }
        clearRoleCache(roleId);
        log.info("角色权限分配完成: roleId={}, permCount={}", roleId, permIds != null ? permIds.size() : 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultPermissions() {
        long count = permissionMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("系统权限已初始化，跳过: count={}", count);
            return;
        }
        List<SysPermission> defaults = buildDefaultPermissions();
        for (SysPermission p : defaults) {
            permissionMapper.insert(p);
        }
        log.info("系统默认权限初始化完成: count={}", defaults.size());
    }

    /**
     * 构建系统默认权限列表
     * 覆盖所有26个页面的 CRUD 操作
     */
    private List<SysPermission> buildDefaultPermissions() {
        List<SysPermission> list = new ArrayList<>();
        int sort = 1;

        // 工作台
        list.add(buildPerm("dashboard:view", "查看工作台", "dashboard", "view", "查看运营数据仪表盘", sort++));

        // 客户管理
        list.add(buildPerm("customer:view", "查看客户", "customer", "view", "查看客户名录和画像", sort++));
        list.add(buildPerm("customer:assign", "分配客户", "customer", "manage", "将客户分配给客服", sort++));
        list.add(buildPerm("customer:export", "导出客户", "customer", "export", "导出客户数据", sort++));
        list.add(buildPerm("customer:satisfaction", "管理满意度", "customer", "manage", "查看和回复客户满意度评价", sort++));
        list.add(buildPerm("customer:lifecycle", "管理生命周期", "customer", "manage", "管理客户生命周期阶段", sort++));

        // 陪玩管理
        list.add(buildPerm("companion:view", "查看陪玩师", "companion", "view", "查看陪玩师列表和详情", sort++));
        list.add(buildPerm("companion:edit", "编辑陪玩师", "companion", "edit", "新增/编辑/启用陪玩师", sort++));
        list.add(buildPerm("companion:delete", "删除陪玩师", "companion", "delete", "删除陪玩师", sort++));
        list.add(buildPerm("companion:level", "管理等级", "companion", "manage", "管理陪玩师等级体系", sort++));
        list.add(buildPerm("companion:schedule", "管理排班", "companion", "manage", "管理陪玩师排班调度", sort++));

        // 业务运营
        list.add(buildPerm("order:view", "查看订单", "order", "view", "查看订单列表和详情", sort++));
        list.add(buildPerm("order:edit", "编辑订单", "order", "edit", "修改订单状态", sort++));
        list.add(buildPerm("order:refund", "订单退款", "order", "manage", "发起订单退款", sort++));
        list.add(buildPerm("order:workorder", "管理工单", "order", "manage", "查看和管理客服工单", sort++));
        list.add(buildPerm("service:view", "查看服务", "service", "view", "查看服务项目和服务追踪", sort++));
        list.add(buildPerm("service:edit", "编辑服务", "service", "edit", "新增/编辑服务项目", sort++));
        list.add(buildPerm("service:package", "管理套餐", "service", "manage", "管理活动优惠套餐", sort++));

        // 消息处理
        list.add(buildPerm("message:view", "查看消息", "message", "view", "查看消息记录", sort++));
        list.add(buildPerm("message:pending", "处理待办", "message", "manage", "认领和处理待办消息", sort++));

        // 系统配置
        list.add(buildPerm("config:club", "俱乐部配置", "config", "manage", "管理俱乐部品牌配置", sort++));
        list.add(buildPerm("config:game", "游戏配置", "config", "manage", "管理支持的陪玩游戏配置", sort++));
        list.add(buildPerm("config:platform", "平台配置", "config", "manage", "管理外部平台接入参数", sort++));
        list.add(buildPerm("config:ai", "AI配置", "config", "manage", "管理AI引擎参数配置", sort++));
        list.add(buildPerm("config:faq", "知识库管理", "config", "manage", "管理FAQ知识库", sort++));
        list.add(buildPerm("config:keyword", "关键词管理", "config", "manage", "管理关键词触发规则", sort++));
        list.add(buildPerm("config:reply", "回复话术", "config", "manage", "管理预设回复话术模板", sort++));
        list.add(buildPerm("config:sysuser", "人员管理", "config", "manage", "管理系统用户和角色", sort++));

        // 系统管理（最高权限）
        list.add(buildPerm("system:admin", "系统管理", "system", "manage", "最高权限，管理所有系统配置", sort++));
        list.add(buildPerm("system:permission", "权限管理", "system", "manage", "管理角色和权限分配", sort++));

        // 开发工具
        list.add(buildPerm("tool:chattest", "对话试炼", "tool", "manage", "使用AI对话试炼沙箱", sort++));

        return list;
    }

    private SysPermission buildPerm(String code, String name, String group, String action, String desc, int sort) {
        SysPermission p = new SysPermission();
        p.setPermCode(code);
        p.setPermName(name);
        p.setPermGroup(group);
        p.setActionType(action);
        p.setDescription(desc);
        p.setSortOrder(sort);
        p.setStatus(1);
        return p;
    }

    private SysPermissionVO toVO(SysPermission p) {
        SysPermissionVO vo = new SysPermissionVO();
        vo.setId(p.getId());
        vo.setPermCode(p.getPermCode());
        vo.setPermName(p.getPermName());
        vo.setPermGroup(p.getPermGroup());
        vo.setActionType(p.getActionType());
        vo.setDescription(p.getDescription());
        vo.setSortOrder(p.getSortOrder());
        vo.setStatus(p.getStatus());
        return vo;
    }

    private void clearRoleCache(Long roleId) {
        try {
            redisService.delete(ROLE_PERM_CACHE_PREFIX + roleId);
        } catch (Exception e) {
            log.debug("清除角色权限缓存失败: roleId={}", roleId);
        }
    }
}
