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
import com.delta.common.service.SysUserService;
import com.delta.common.vo.SysUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 系统用户服务实现，管理后台账号和权限
 *
 * @author delta
 */
@Service
public class SysUserServiceImpl implements SysUserService {
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Page<SysUserVO> getUserPage(Integer pageNum, Integer pageSize, String role, String status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysUser::getStatus, status);
        }
        
        wrapper.orderByDesc(SysUser::getCreatedAt);
        
        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);
        
        Page<SysUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::convertToVO).toList());
        
        return voPage;
    }
    
    @Override
    public SysUserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUserDTO userDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, userDTO.getUsername());
        SysUser existUser = sysUserMapper.selectOne(wrapper);
        
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus(UserStatusEnum.ACTIVE.getCode());
        }
        
        sysUserMapper.insert(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserDTO userDTO) {
        SysUser user = sysUserMapper.selectById(userDTO.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        BeanUtils.copyProperties(userDTO, user, "id", "username", "password", "createdAt", "createdBy");
        
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        sysUserMapper.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        sysUserMapper.deleteById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditUser(AuditUserDTO auditDTO) {
        SysUser user = sysUserMapper.selectById(auditDTO.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!UserStatusEnum.PENDING.getCode().equals(user.getStatus())) {
            throw new BusinessException("只有待审核状态的用户可以审核");
        }
        
        user.setStatus(auditDTO.getStatus());
        sysUserMapper.updateById(user);
    }
    
    private SysUserVO convertToVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        
        RoleEnum roleEnum = RoleEnum.fromCode(user.getRole());
        if (roleEnum != null) {
            vo.setRoleDesc(roleEnum.getDesc());
        }
        
        UserStatusEnum statusEnum = UserStatusEnum.fromCode(user.getStatus());
        if (statusEnum != null) {
            vo.setStatusDesc(statusEnum.getDesc());
        }
        
        return vo;
    }
}
