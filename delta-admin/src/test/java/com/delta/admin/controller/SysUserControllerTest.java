package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.AuditUserDTO;
import com.delta.common.dto.SysUserDTO;
import com.delta.common.service.SysUserService;
import com.delta.common.util.IdObfuscateUtils;
import com.delta.common.vo.Result;
import com.delta.common.vo.SysUserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserControllerTest {

    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private SysUserController sysUserController;

    @Test
    @DisplayName("分页查询用户列表 - 成功返回分页数据")
    void getUserPage_shouldReturnPagedData() {
        SysUserVO vo = new SysUserVO();
        vo.setId(1L);
        vo.setUsername("admin");
        vo.setRealName("管理员");
        vo.setRole("SYS_ADMIN");
        vo.setStatus("ACTIVE");

        Page<SysUserVO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(vo));

        when(sysUserService.getUserPage(anyInt(), anyInt(), any(), any())).thenReturn(page);

        Result<Page<SysUserVO>> result = sysUserController.getUserPage(1, 10, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
        assertEquals("admin", result.getData().getRecords().get(0).getUsername());
    }

    @Test
    @DisplayName("分页查询用户列表 - 带角色和状态过滤")
    void getUserPage_withRoleAndStatusFilter_shouldReturnFilteredData() {
        Page<SysUserVO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());

        when(sysUserService.getUserPage(anyInt(), anyInt(), eq("CS_STAFF"), eq("ACTIVE"))).thenReturn(page);

        Result<Page<SysUserVO>> result = sysUserController.getUserPage(1, 10, "CS_STAFF", "ACTIVE");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    @DisplayName("获取用户详情 - 成功返回用户信息")
    void getUserById_shouldReturnUserInfo() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);
        SysUserVO vo = new SysUserVO();
        vo.setId(1L);
        vo.setUsername("admin");

        when(sysUserService.getUserById(1L)).thenReturn(vo);

        Result<SysUserVO> result = sysUserController.getUserById(obfuscatedId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("admin", result.getData().getUsername());
    }

    @Test
    @DisplayName("创建用户 - 成功创建")
    void createUser_withValidData_shouldReturnSuccess() {
        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setRealName("新用户");
        dto.setRole("CS_STAFF");
        dto.setStatus("ACTIVE");

        doNothing().when(sysUserService).createUser(any(SysUserDTO.class));

        Result<Void> result = sysUserController.createUser(dto);

        assertEquals(200, result.getCode());
        verify(sysUserService).createUser(dto);
    }

    @Test
    @DisplayName("更新用户 - 成功更新")
    void updateUser_withValidData_shouldReturnSuccess() {
        SysUserDTO dto = new SysUserDTO();
        dto.setId(1L);
        dto.setUsername("admin");
        dto.setRealName("管理员");
        dto.setRole("SYS_ADMIN");
        dto.setStatus("ACTIVE");

        doNothing().when(sysUserService).updateUser(any(SysUserDTO.class));

        Result<Void> result = sysUserController.updateUser(dto);

        assertEquals(200, result.getCode());
        verify(sysUserService).updateUser(dto);
    }

    @Test
    @DisplayName("删除用户 - 成功删除")
    void deleteUser_withValidId_shouldReturnSuccess() {
        String obfuscatedId = IdObfuscateUtils.encode(1L);

        doNothing().when(sysUserService).deleteUser(anyLong());

        Result<Void> result = sysUserController.deleteUser(obfuscatedId);

        assertEquals(200, result.getCode());
        verify(sysUserService).deleteUser(1L);
    }

    @Test
    @DisplayName("审核用户 - 成功审核")
    void auditUser_withValidData_shouldReturnSuccess() {
        AuditUserDTO auditDTO = new AuditUserDTO();

        doNothing().when(sysUserService).auditUser(any(AuditUserDTO.class));

        Result<Void> result = sysUserController.auditUser(auditDTO);

        assertEquals(200, result.getCode());
        verify(sysUserService).auditUser(auditDTO);
    }
}
