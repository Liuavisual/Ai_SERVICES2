package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.AuditUserDTO;
import com.delta.common.dto.SysUserDTO;
import com.delta.common.vo.SysUserVO;

/**
 * 系统用户服务接口，管理后台账号和权限
 *
 * @author delta
 */
public interface SysUserService {
    
    Page<SysUserVO> getUserPage(Integer pageNum, Integer pageSize, String role, String status);
    
    SysUserVO getUserById(Long id);
    
    void createUser(SysUserDTO userDTO);
    
    void updateUser(SysUserDTO userDTO);
    
    void deleteUser(Long id);
    
    void auditUser(AuditUserDTO auditDTO);
}
