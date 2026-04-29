package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CsUserCustomerDTO;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.CsUserCustomerService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CsUserCustomerVO;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客服-客户分配服务实现，管理绑定关系
 *
 * @author delta
 */
@Service
@RequiredArgsConstructor
public class CsUserCustomerServiceImpl implements CsUserCustomerService {
    
    private final CsUserCustomerMapper csUserCustomerMapper;
    
    private final SysUserMapper sysUserMapper;
    
    private final UserMapper userMapper;
    
    @Override
    public Page<CsUserCustomerVO> getPage(Integer page, Integer size, Long csUserId, Long customerUserId, String status) {
        Page<CsUserCustomer> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<CsUserCustomer> wrapper = new LambdaQueryWrapper<>();
        
        if (csUserId != null) {
            wrapper.eq(CsUserCustomer::getCsUserId, csUserId);
        }
        if (customerUserId != null) {
            wrapper.eq(CsUserCustomer::getCustomerUserId, customerUserId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CsUserCustomer::getStatus, status);
        }
        
        wrapper.orderByDesc(CsUserCustomer::getCreatedAt);
        
        Page<CsUserCustomer> resultPage = csUserCustomerMapper.selectPage(pageObj, wrapper);

        Page<CsUserCustomerVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());

        if (resultPage.getRecords().isEmpty()) {
            return voPage;
        }
        
        Map<Long, String> csUserNameMap = sysUserMapper.selectByIds(
            resultPage.getRecords().stream().map(CsUserCustomer::getCsUserId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
        
        Map<Long, String> customerUserNameMap = userMapper.selectByIds(
            resultPage.getRecords().stream().map(CsUserCustomer::getCustomerUserId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(User::getId, User::getNickname));
        
        Map<Long, String> assignedByNameMap = sysUserMapper.selectByIds(
            resultPage.getRecords().stream().map(CsUserCustomer::getAssignedBy).filter(id -> id != null).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
        
        voPage.setRecords(resultPage.getRecords().stream().map(item -> {
            if (item == null) {
                return null;
            }
            CsUserCustomerVO vo = new CsUserCustomerVO();
            BeanUtils.copyProperties(item, vo);
            
            vo.setCsUserName(csUserNameMap.get(item.getCsUserId()));
            vo.setCustomerUserName(customerUserNameMap.get(item.getCustomerUserId()));
            vo.setAssignedByName(assignedByNameMap.get(item.getAssignedBy()));
            
            if (BusinessStatusConstants.ASSIGN_TYPE_MANUAL.equals(item.getAssignType())) {
                vo.setAssignTypeDesc("手动分配");
            } else if (BusinessStatusConstants.ASSIGN_TYPE_SYSTEM.equals(item.getAssignType())) {
                vo.setAssignTypeDesc("系统分配");
            }
            
            if (BusinessStatusConstants.ASSIGN_STATUS_ACTIVE.equals(item.getStatus())) {
                vo.setStatusDesc("有效");
            } else if (BusinessStatusConstants.ASSIGN_STATUS_INACTIVE.equals(item.getStatus())) {
                vo.setStatusDesc("无效");
            }
            
            return vo;
        }).toList());
        VoUtils.setRowNumbers(voPage);
        
        return voPage;
    }
    
    @Override
    public CsUserCustomerVO getById(Long id) {
        CsUserCustomer entity = csUserCustomerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("分配关系不存在");
        }
        
        CsUserCustomerVO vo = new CsUserCustomerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CsUserCustomerDTO dto) {
        if (dto == null) {
            throw new BusinessException("分配关系参数不能为空");
        }
        LambdaQueryWrapper<CsUserCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsUserCustomer::getCsUserId, dto.getCsUserId())
               .eq(CsUserCustomer::getCustomerUserId, dto.getCustomerUserId());
        
        CsUserCustomer exist = csUserCustomerMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException("该客户已分配给该客服");
        }
        
        CsUserCustomer entity = new CsUserCustomer();
        BeanUtils.copyProperties(dto, entity);
        
        if (!StringUtils.hasText(entity.getAssignType())) {
            entity.setAssignType(BusinessStatusConstants.ASSIGN_TYPE_MANUAL);
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        }
        entity.setAssignedAt(LocalDateTime.now());
        
        csUserCustomerMapper.insert(entity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CsUserCustomerDTO dto) {
        if (dto == null) {
            throw new BusinessException("分配关系参数不能为空");
        }
        CsUserCustomer entity = csUserCustomerMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("分配关系不存在");
        }
        
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "assignedAt");
        csUserCustomerMapper.updateById(entity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        csUserCustomerMapper.deleteById(id);
    }
}
