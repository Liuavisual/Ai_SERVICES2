package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.entity.Message;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.CustomerService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CustomerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户服务实现，处理客户信息CRUD和AI开关管理
 *
 * @author delta
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private CsUserCustomerMapper csUserCustomerMapper;

    @Override
    public Page<CustomerVO> getCustomerPage(Integer pageNum, Integer pageSize, String platform, Boolean aiEnabled, Long csUserId, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (platform != null && !platform.isEmpty()) {
            wrapper.eq(User::getPlatform, platform);
        }

        if (aiEnabled != null) {
            wrapper.eq(User::getAiEnabled, aiEnabled);
        }

        if (csUserId != null) {
            wrapper.eq(User::getAssignedCsUserId, csUserId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickname, keyword);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<Long> csUserIds = userPage.getRecords().stream()
                .map(User::getAssignedCsUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Integer> messageCountMap = userIds.isEmpty() ? Map.of() :
                messageMapper.selectList(new LambdaQueryWrapper<Message>()
                                .in(Message::getUserId, userIds))
                        .stream()
                        .collect(Collectors.groupingBy(Message::getUserId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        Map<Long, SysUser> csUserMap = csUserIds.isEmpty() ? Map.of() :
                sysUserMapper.selectBatchIds(csUserIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        Page<CustomerVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<CustomerVO> voList = userPage.getRecords().stream().map(user -> {
            CustomerVO vo = BeanUtil.copyProperties(user, CustomerVO.class);
            vo.setAssignedCsUserId(user.getAssignedCsUserId());
            vo.setMessageCount(messageCountMap.getOrDefault(user.getId(), 0));
            if (user.getAssignedCsUserId() != null) {
                SysUser csUser = csUserMap.get(user.getAssignedCsUserId());
                if (csUser != null) {
                    vo.setAssignedCsUserName(csUser.getRealName());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        VoUtils.setRowNumbers(resultPage);
        return resultPage;
    }

    @Override
    public CustomerVO getCustomerById(Long id) {
        return getCustomerById(id, null, null);
    }

    @Override
    public CustomerVO getCustomerById(Long id, Long currentUserId, String currentUserRole) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }

        if (currentUserId != null && BusinessStatusConstants.ROLE_CS_STAFF.equals(currentUserRole)) {
            if (!isCustomerAssignedToCsStaff(id, currentUserId)) {
                throw new BusinessException("您无权查看此客户信息，只能查看分配给您的客户");
            }
        }

        CustomerVO vo = BeanUtil.copyProperties(user, CustomerVO.class);

        LambdaQueryWrapper<Message> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(Message::getUserId, id);
        msgWrapper.orderByDesc(Message::getCreatedAt);
        Page<Message> msgPage = messageMapper.selectPage(new Page<>(1, 1), msgWrapper);
        Message lastMessage = msgPage.getRecords().isEmpty() ? null : msgPage.getRecords().get(0);
        if (lastMessage != null) {
            vo.setLastActiveAt(lastMessage.getCreatedAt());
        }

        int messageCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getUserId, id)).intValue();
        vo.setMessageCount(messageCount);

        if (user.getAssignedCsUserId() != null) {
            SysUser csUser = sysUserMapper.selectById(user.getAssignedCsUserId());
            if (csUser != null) {
                vo.setAssignedCsUserName(csUser.getRealName());
            }
        }

        return vo;
    }

    @Override
    public boolean isCustomerAssignedToCsStaff(Long customerId, Long csUserId) {
        if (customerId == null || csUserId == null) {
            return false;
        }

        User user = userMapper.selectById(customerId);
        if (user != null && user.getAssignedCsUserId() != null && user.getAssignedCsUserId().equals(csUserId)) {
            return true;
        }

        LambdaQueryWrapper<CsUserCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsUserCustomer::getCsUserId, csUserId);
        wrapper.eq(CsUserCustomer::getCustomerUserId, customerId);
        wrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        wrapper.eq(CsUserCustomer::getDeleted, BusinessStatusConstants.NOT_DELETED);
        return csUserCustomerMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleAiEnabled(Long id, Boolean aiEnabled) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("客户不存在");
        }
        user.setAiEnabled(aiEnabled);
        userMapper.updateById(user);
        log.info("更新客户AI状态: customerId={}, aiEnabled={}", id, aiEnabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignCustomer(Long id, Long csUserId, String assignType, String remark) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("客户不存在");
        }
        if (csUserId != null) {
            SysUser csUser = sysUserMapper.selectById(csUserId);
            if (csUser == null) {
                throw new BusinessException("分配的客服不存在");
            }
        }

        Long previousCsUserId = user.getAssignedCsUserId();

        user.setAssignedCsUserId(csUserId);
        userMapper.updateById(user);
        log.info("分配客户: customerId={}, csUserId={}, assignType={}", id, csUserId, assignType);

        if (previousCsUserId != null && !previousCsUserId.equals(csUserId)) {
            LambdaQueryWrapper<CsUserCustomer> deactivateWrapper = new LambdaQueryWrapper<>();
            deactivateWrapper.eq(CsUserCustomer::getCsUserId, previousCsUserId);
            deactivateWrapper.eq(CsUserCustomer::getCustomerUserId, id);
            deactivateWrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
            List<CsUserCustomer> previousAssignments = csUserCustomerMapper.selectList(deactivateWrapper);
            for (CsUserCustomer prev : previousAssignments) {
                prev.setStatus(BusinessStatusConstants.ASSIGN_STATUS_INACTIVE);
                csUserCustomerMapper.updateById(prev);
            }
        }

        if (csUserId != null) {
            LambdaQueryWrapper<CsUserCustomer> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(CsUserCustomer::getCsUserId, csUserId);
            existWrapper.eq(CsUserCustomer::getCustomerUserId, id);
            CsUserCustomer existing = csUserCustomerMapper.selectOne(existWrapper);

            if (existing != null) {
                if (!BusinessStatusConstants.ASSIGN_STATUS_ACTIVE.equals(existing.getStatus())) {
                    existing.setStatus(BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
                    existing.setAssignedAt(LocalDateTime.now());
                    csUserCustomerMapper.updateById(existing);
                }
            } else {
                CsUserCustomer newAssignment = new CsUserCustomer();
                newAssignment.setCsUserId(csUserId);
                newAssignment.setCustomerUserId(id);
                newAssignment.setAssignType(assignType != null ? assignType : BusinessStatusConstants.ASSIGN_TYPE_MANUAL);
                newAssignment.setStatus(BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
                newAssignment.setAssignedAt(LocalDateTime.now());
                csUserCustomerMapper.insert(newAssignment);
            }
        }
    }
}
