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
     * 覆盖所有模块的权限项，与控制器中的 @PermAuth 注解权限码完全对齐
     */
    private List<SysPermission> buildDefaultPermissions() {
        List<SysPermission> list = new ArrayList<>();
        int sort = 1;

        // ===== 权限管理模块 =====
        list.add(buildPerm("permission:manage", "权限管理", "permission", "manage", "管理角色和权限分配", sort++));

        // ===== 工作台 =====
        list.add(buildPerm("dashboard:view", "查看工作台", "dashboard", "view", "查看运营数据仪表盘", sort++));
        list.add(buildPerm("stats:view", "查看统计", "stats", "view", "查看统计数据", sort++));

        // ===== 客户管理 =====
        list.add(buildPerm("customer:view", "查看客户", "customer", "view", "查看客户名录", sort++));
        list.add(buildPerm("customer:edit", "编辑客户", "customer", "edit", "编辑客户信息", sort++));
        list.add(buildPerm("customer:assign", "分配客户", "customer", "manage", "将客户分配给客服", sort++));
        list.add(buildPerm("customer:export", "导出客户", "customer", "export", "导出客户数据", sort++));
        list.add(buildPerm("customer_profile:view", "查看客户画像", "customer", "view", "查看客户画像和消费记录", sort++));
        list.add(buildPerm("customer_profile:edit", "编辑客户画像", "customer", "edit", "编辑客户画像和消费记录", sort++));
        list.add(buildPerm("customer_satisfaction:view", "查看满意度", "customer", "view", "查看客户满意度评价", sort++));
        list.add(buildPerm("customer_satisfaction:edit", "管理满意度", "customer", "edit", "回复和管理满意度评价", sort++));
        list.add(buildPerm("customer_lifecycle:view", "查看生命周期", "customer", "view", "查看客户生命周期阶段", sort++));
        list.add(buildPerm("customer_lifecycle:edit", "管理生命周期", "customer", "edit", "管理客户生命周期阶段", sort++));

        // ===== 陪玩师管理 =====
        list.add(buildPerm("companion:view", "查看陪玩师", "companion", "view", "查看陪玩师列表和详情", sort++));
        list.add(buildPerm("companion:edit", "编辑陪玩师", "companion", "edit", "新增/编辑陪玩师", sort++));
        list.add(buildPerm("companion:export", "导出陪玩师", "companion", "export", "导出陪玩师Excel", sort++));
        list.add(buildPerm("companion:import", "导入陪玩师", "companion", "import", "导入陪玩师Excel", sort++));
        list.add(buildPerm("companion:rating", "评分看板", "companion", "view", "查看陪玩师评分数据", sort++));
        list.add(buildPerm("companion_level:view", "查看等级", "companion", "view", "查看陪玩师等级", sort++));
        list.add(buildPerm("companion_level:edit", "编辑等级", "companion", "edit", "编辑陪玩师等级", sort++));
        list.add(buildPerm("companion_level:export", "导出等级", "companion", "export", "导出等级Excel", sort++));
        list.add(buildPerm("companion_level:import", "导入等级", "companion", "import", "导入等级Excel", sort++));
        list.add(buildPerm("companion_settlement:view", "查看结算", "companion", "view", "查看陪玩师结算", sort++));
        list.add(buildPerm("companion_settlement:edit", "编辑结算", "companion", "edit", "编辑陪玩师结算", sort++));
        list.add(buildPerm("companion_training:view", "查看培训", "companion", "view", "查看陪玩师培训", sort++));
        list.add(buildPerm("companion_training:edit", "编辑培训", "companion", "edit", "编辑陪玩师培训", sort++));

        // ===== 排班管理 =====
        list.add(buildPerm("schedule:view", "查看排班", "schedule", "view", "查看陪玩师排班", sort++));
        list.add(buildPerm("schedule:edit", "编辑排班", "schedule", "edit", "编辑陪玩师排班", sort++));
        list.add(buildPerm("schedule:export", "导出排班", "schedule", "export", "导出排班Excel", sort++));
        list.add(buildPerm("schedule:import", "导入排班", "schedule", "import", "导入排班Excel", sort++));

        // ===== 订单管理 =====
        list.add(buildPerm("order:view", "查看订单", "order", "view", "查看订单列表和详情", sort++));
        list.add(buildPerm("order:edit", "编辑订单", "order", "edit", "修改订单状态", sort++));
        list.add(buildPerm("order:review", "订单评价", "order", "edit", "提交订单评价", sort++));
        list.add(buildPerm("order_status_history:view", "订单历史", "order", "view", "查看订单状态变更历史", sort++));

        // ===== 工单管理 =====
        list.add(buildPerm("work_order:view", "查看工单", "order", "view", "查看客服工单", sort++));
        list.add(buildPerm("work_order:edit", "编辑工单", "order", "edit", "编辑客服工单", sort++));

        // ===== 服务管理 =====
        list.add(buildPerm("service_item:view", "查看服务项目", "service", "view", "查看服务项目", sort++));
        list.add(buildPerm("service_item:edit", "编辑服务项目", "service", "edit", "编辑服务项目", sort++));
        list.add(buildPerm("service_track:view", "服务追踪", "service", "view", "查看服务追踪记录", sort++));
        list.add(buildPerm("pricing_plan:view", "查看定价", "service", "view", "查看定价方案", sort++));
        list.add(buildPerm("pricing_plan:edit", "编辑定价", "service", "edit", "编辑定价方案", sort++));

        // ===== 活动套餐 =====
        list.add(buildPerm("activity_package:view", "查看套餐", "activity", "view", "查看活动套餐", sort++));
        list.add(buildPerm("activity_package:edit", "编辑套餐", "activity", "edit", "编辑活动套餐", sort++));
        list.add(buildPerm("activity_package:export", "导出套餐", "activity", "export", "导出套餐Excel", sort++));
        list.add(buildPerm("activity_package:import", "导入套餐", "activity", "import", "导入套餐Excel", sort++));

        // ===== 订阅管理 =====
        list.add(buildPerm("subscription:view", "查看订阅", "subscription", "view", "查看俱乐部订阅", sort++));
        list.add(buildPerm("subscription:edit", "编辑订阅", "subscription", "edit", "管理俱乐部订阅", sort++));

        // ===== 营销活动 =====
        list.add(buildPerm("campaign:view", "查看活动", "campaign", "view", "查看营销活动", sort++));
        list.add(buildPerm("campaign:edit", "编辑活动", "campaign", "edit", "管理营销活动", sort++));

        // ===== 消息管理 =====
        list.add(buildPerm("message:view", "查看消息", "message", "view", "查看消息记录", sort++));
        list.add(buildPerm("pending_message:view", "查看待办", "message", "view", "查看待办消息", sort++));
        list.add(buildPerm("pending_message:edit", "处理待办", "message", "edit", "认领和处理待办消息", sort++));
        list.add(buildPerm("pending_message:export", "导出待办", "message", "export", "导出待办消息", sort++));
        list.add(buildPerm("chat:view", "对话测试", "message", "view", "使用AI对话测试沙箱", sort++));

        // ===== 裂变推荐 =====
        list.add(buildPerm("referral:view", "查看推荐", "referral", "view", "查看裂变推荐记录", sort++));
        list.add(buildPerm("referral:edit", "编辑推荐", "referral", "edit", "管理裂变推荐", sort++));

        // ===== 客服分配 =====
        list.add(buildPerm("cs_assignment:view", "查看分配", "assignment", "view", "查看客服客户分配", sort++));
        list.add(buildPerm("cs_assignment:edit", "编辑分配", "assignment", "edit", "管理客服客户分配", sort++));

        // ===== 系统配置 =====
        list.add(buildPerm("platform:manage", "平台配置", "config", "manage", "管理外部平台接入参数", sort++));
        list.add(buildPerm("club_config:view", "查看俱乐部", "config", "view", "查看俱乐部品牌配置", sort++));
        list.add(buildPerm("club_config:edit", "编辑俱乐部", "config", "edit", "编辑俱乐部品牌配置", sort++));
        list.add(buildPerm("game_config:view", "查看游戏", "config", "view", "查看游戏配置", sort++));
        list.add(buildPerm("game_config:edit", "编辑游戏", "config", "edit", "编辑游戏配置", sort++));
        list.add(buildPerm("game_config:export", "导出游戏", "config", "export", "导出游戏配置", sort++));
        list.add(buildPerm("ai_config:view", "查看AI配置", "config", "view", "查看AI引擎参数", sort++));
        list.add(buildPerm("ai_config:edit", "编辑AI配置", "config", "edit", "编辑AI引擎参数", sort++));
        list.add(buildPerm("faq_item:view", "查看FAQ", "config", "view", "查看FAQ知识库", sort++));
        list.add(buildPerm("faq_item:edit", "编辑FAQ", "config", "edit", "编辑FAQ知识库", sort++));
        list.add(buildPerm("faq_item:import", "导入FAQ", "config", "import", "导入FAQ知识库", sort++));
        list.add(buildPerm("keyword:view", "查看关键词", "config", "view", "查看关键词规则", sort++));
        list.add(buildPerm("keyword:edit", "编辑关键词", "config", "edit", "编辑关键词规则", sort++));
        list.add(buildPerm("keyword:import", "导入关键词", "config", "import", "导入关键词Excel", sort++));
        list.add(buildPerm("reply:view", "查看话术", "config", "view", "查看回复话术模板", sort++));
        list.add(buildPerm("reply:edit", "编辑话术", "config", "edit", "编辑回复话术模板", sort++));
        list.add(buildPerm("reply:import", "导入话术", "config", "import", "导入话术Excel", sort++));
        list.add(buildPerm("sys_user:view", "查看用户", "config", "view", "查看系统用户", sort++));
        list.add(buildPerm("sys_user:edit", "编辑用户", "config", "edit", "管理系统用户", sort++));
        list.add(buildPerm("sys_user:audit", "审核用户", "config", "edit", "审核用户注册", sort++));
        list.add(buildPerm("sys_user:export", "导出用户", "config", "export", "导出用户Excel", sort++));

        // ===== 质量检查 =====
        list.add(buildPerm("quality_check:view", "查看质检", "quality", "view", "查看质检记录", sort++));
        list.add(buildPerm("quality_check:edit", "编辑质检", "quality", "edit", "编辑质检记录", sort++));

        // ===== 财报 =====
        list.add(buildPerm("revenue_report:view", "查看财报", "revenue", "view", "查看营收日报", sort++));
        list.add(buildPerm("revenue_report:edit", "编辑财报", "revenue", "edit", "编辑营收数据", sort++));

        // ===== 缓存管理 =====
        list.add(buildPerm("cache:view", "查看缓存", "cache", "view", "查看缓存状态", sort++));
        list.add(buildPerm("cache:edit", "管理缓存", "cache", "edit", "清除和管理缓存", sort++));

        // ===== 系统管理 =====
        list.add(buildPerm("system:admin", "系统管理", "system", "manage", "最高权限，管理所有系统配置", sort++));

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
