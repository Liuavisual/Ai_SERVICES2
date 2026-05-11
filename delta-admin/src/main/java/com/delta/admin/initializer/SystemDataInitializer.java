package com.delta.admin.initializer;

import com.delta.common.service.PermissionService;
import com.delta.common.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动数据初始化器
 * <p>
 * 在应用启动时自动初始化：
 * <ol>
 *   <li>系统默认权限定义（sys_permission表）</li>
 *   <li>系统默认角色（SYS_ADMIN/CS_LEADER/CS_STAFF/COMPANION）</li>
 *   <li>角色-权限关联关系</li>
 * </ol>
 * 所有初始化操作均为幂等，已存在的数据不会重复创建。
 * </p>
 *
 * @author 刘建国
 */
@Component
@RequiredArgsConstructor
public class SystemDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemDataInitializer.class);

    private final SysRoleService sysRoleService;
    private final PermissionService permissionService;

    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化系统权限数据...");
            permissionService.initDefaultPermissions();
            sysRoleService.initDefaultRolesAndPermissions();
            log.info("系统权限数据初始化完成");
        } catch (Exception e) {
            log.error("系统权限数据初始化失败: {}", e.getMessage(), e);
        }
    }
}