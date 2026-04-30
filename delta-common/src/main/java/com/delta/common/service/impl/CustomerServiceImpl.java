package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
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
import com.delta.common.util.ExcelUtils;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CustomerVO;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户服务实现，处理客户信息CRUD和AI开关管理
 *
 * @author delta
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final UserMapper userMapper;

    private final MessageMapper messageMapper;

    private final SysUserMapper sysUserMapper;

    private final CsUserCustomerMapper csUserCustomerMapper;

    @Override
    public Page<CustomerVO> getCustomerPage(Integer page, Integer size, String platform, Boolean aiEnabled, Long csUserId, String keyword) {
        Page<User> pageObj = new Page<>(page, size);
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

        Page<User> userPageResult = userMapper.selectPage(pageObj, wrapper);

        List<Long> userIds = userPageResult.getRecords().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<Long> csUserIds = pageObj.getRecords().stream()
                .map(User::getAssignedCsUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 使用SQL聚合查询替代selectList全量加载，仅统计消息数量
        Map<Long, Integer> messageCountMap;
        if (userIds.isEmpty()) {
            messageCountMap = Map.of();
        } else {
            QueryWrapper<Message> msgWrapper = new QueryWrapper<>();
            msgWrapper.select("user_id", "COUNT(*) as count");
            msgWrapper.in("user_id", userIds);
            msgWrapper.groupBy("user_id");
            List<Map<String, Object>> msgCountResults = messageMapper.selectMaps(msgWrapper);
            messageCountMap = new HashMap<>();
            for (Map<String, Object> row : msgCountResults) {
                Long userId = ((Number) row.get("user_id")).longValue();
                Integer count = ((Number) row.get("count")).intValue();
                messageCountMap.put(userId, count);
            }
        }

        Map<Long, SysUser> csUserMap = csUserIds.isEmpty() ? Map.of() :
                sysUserMapper.selectByIds(csUserIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        Page<CustomerVO> resultPage = new Page<>(userPageResult.getCurrent(), userPageResult.getSize(), userPageResult.getTotal());
        List<CustomerVO> voList = userPageResult.getRecords().stream().map(user -> {
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

    /**
     * 导出客户Excel
     *
     * @param response  HTTP响应
     * @param platform  平台
     * @param aiEnabled AI启用状态
     * @param csUserId  客服用户ID
     * @param keyword   关键词
     */
    @Override
    public void exportCustomers(HttpServletResponse response, String platform, Boolean aiEnabled, Long csUserId, String keyword) {
        try {
            Page<CustomerVO> pageResult = getCustomerPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, platform, aiEnabled, csUserId, keyword);
            LinkedHashMap<String, String> headers = new LinkedHashMap<>();
            headers.put("id", "ID");
            headers.put("platform", "平台");
            headers.put("nickname", "昵称");
            headers.put("aiEnabled", "AI启用");
            headers.put("assignedCsUserName", "分配客服");
            headers.put("messageCount", "消息数");
            headers.put("lastActiveAt", "最后活跃");
            headers.put("createdAt", "创建时间");
            ExcelUtils.export(response, "客户列表", headers, pageResult.getRecords(), item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", item.getId());
                map.put("platform", item.getPlatform());
                map.put("nickname", item.getNickname());
                map.put("aiEnabled", item.getAiEnabled() != null && item.getAiEnabled() ? "是" : "否");
                map.put("assignedCsUserName", item.getAssignedCsUserName());
                map.put("messageCount", item.getMessageCount());
                map.put("lastActiveAt", item.getLastActiveAt() != null ? item.getLastActiveAt().toString() : "");
                map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
                return map;
            });
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }
}
