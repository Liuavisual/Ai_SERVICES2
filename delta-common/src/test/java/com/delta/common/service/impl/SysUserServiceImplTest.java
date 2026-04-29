package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.AuditUserDTO;
import com.delta.common.dto.SysUserDTO;
import com.delta.common.entity.SysUser;
import com.delta.common.enums.RoleEnum;
import com.delta.common.enums.UserStatusEnum;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.vo.SysUserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @Test
    @DisplayName("分页查询用户 - 无过滤条件返回分页结果")
    void getUserPage_noFilter_shouldReturnPagedResults() {
        Page<SysUser> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(sysUserMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<SysUserVO> result = sysUserService.getUserPage(1, 10, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(sysUserMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询用户 - 带角色和状态过滤")
    void getUserPage_withFilters_shouldApplyFilters() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRole(RoleEnum.SYS_ADMIN.getCode());
        user.setStatus(UserStatusEnum.ACTIVE.getCode());

        Page<SysUser> mockPage = new Page<>(1, 10);
        mockPage.setRecords(java.util.List.of(user));
        mockPage.setTotal(1);
        when(sysUserMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<SysUserVO> result = sysUserService.getUserPage(1, 10, "SYS_ADMIN", "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("系统管理员", result.getRecords().get(0).getRoleDesc());
        assertEquals("正常", result.getRecords().get(0).getStatusDesc());
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户不存在抛出异常")
    void getUserById_notExist_shouldThrow() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> sysUserService.getUserById(999L));
    }

    @Test
    @DisplayName("根据ID查询用户 - 正常返回用户信息")
    void getUserById_exist_shouldReturnUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRole(RoleEnum.SYS_ADMIN.getCode());
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        when(sysUserMapper.selectById(1L)).thenReturn(user);

        SysUserVO result = sysUserService.getUserById(1L);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("系统管理员", result.getRoleDesc());
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在抛出异常")
    void createUser_usernameExists_shouldThrow() {
        SysUser existUser = new SysUser();
        existUser.setId(1L);
        existUser.setUsername("admin");
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existUser);

        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        assertThrows(BusinessException.class,
                () -> sysUserService.createUser(dto));
    }

    @Test
    @DisplayName("创建用户 - 正常创建并加密密码")
    void createUser_normal_shouldEncryptPassword() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encoded_password");
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setRole(RoleEnum.CS_STAFF.getCode());

        sysUserService.createUser(dto);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        assertEquals("encoded_password", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("创建用户 - 无密码不加密")
    void createUser_noPassword_shouldNotEncrypt() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("newuser");
        dto.setRole(RoleEnum.CS_STAFF.getCode());

        sysUserService.createUser(dto);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        assertNull(captor.getValue().getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("更新用户 - 用户不存在抛出异常")
    void updateUser_notExist_shouldThrow() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        SysUserDTO dto = new SysUserDTO();
        dto.setId(999L);

        assertThrows(BusinessException.class,
                () -> sysUserService.updateUser(dto));
    }

    @Test
    @DisplayName("更新用户 - 正常更新")
    void updateUser_normal_shouldUpdate() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUserDTO dto = new SysUserDTO();
        dto.setId(1L);
        dto.setRealName("新名字");

        sysUserService.updateUser(dto);

        verify(sysUserMapper).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("删除用户 - 正常删除")
    void deleteUser_normal_shouldDelete() {
        when(sysUserMapper.deleteById(1L)).thenReturn(1);

        sysUserService.deleteUser(1L);

        verify(sysUserMapper).deleteById(1L);
    }

    @Test
    @DisplayName("审核用户 - 用户不存在抛出异常")
    void auditUser_notExist_shouldThrow() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        AuditUserDTO dto = new AuditUserDTO();
        dto.setUserId(999L);
        dto.setStatus(UserStatusEnum.ACTIVE.getCode());

        assertThrows(BusinessException.class,
                () -> sysUserService.auditUser(dto));
    }

    @Test
    @DisplayName("审核用户 - 非待审核状态抛出异常")
    void auditUser_notPending_shouldThrow() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        when(sysUserMapper.selectById(1L)).thenReturn(user);

        AuditUserDTO dto = new AuditUserDTO();
        dto.setUserId(1L);
        dto.setStatus(UserStatusEnum.ACTIVE.getCode());

        assertThrows(BusinessException.class,
                () -> sysUserService.auditUser(dto));
    }

    @Test
    @DisplayName("审核用户 - 正常审核通过")
    void auditUser_normal_shouldUpdateStatus() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setStatus(UserStatusEnum.PENDING.getCode());
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        AuditUserDTO dto = new AuditUserDTO();
        dto.setUserId(1L);
        dto.setStatus(UserStatusEnum.ACTIVE.getCode());

        sysUserService.auditUser(dto);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        assertEquals(UserStatusEnum.ACTIVE.getCode(), captor.getValue().getStatus());
    }
}
