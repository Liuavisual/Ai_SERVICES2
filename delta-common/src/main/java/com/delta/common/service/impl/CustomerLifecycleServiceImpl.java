package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.delta.common.constant.CustomerLifecycleConstants;
import com.delta.common.entity.CustomerProfile;
import com.delta.common.entity.User;
import com.delta.common.mapper.CustomerProfileMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.CustomerLifecycleService;
import com.delta.common.vo.CustomerVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户生命周期服务实现
 * <p>
 * 基于客户画像(CustomerProfile)中的活跃时间和消息数量，
 * 判断客户生命周期阶段，并提供流失风险预警和标签更新功能。</p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CustomerLifecycleServiceImpl implements CustomerLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(CustomerLifecycleServiceImpl.class);

    /** 用户Mapper */
    private final UserMapper userMapper;

    /** 客户画像Mapper */
    private final CustomerProfileMapper customerProfileMapper;

    /**
     * 判断客户生命周期阶段
     * <p>
     * 优先从客户画像获取最后活跃时间和消息数量，
     * 如果画像不存在则从消息表查询。</p>
     *
     * @param userId 客户用户ID
     * @return 生命周期阶段标识（NEW/ACTIVE/LOYAL/AT_RISK/CHURNED）
     */
    @Override
    public String determineLifecycleStage(Long userId) {
        if (userId == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>().eq(CustomerProfile::getUserId, userId)
        );

        LocalDateTime lastActiveAt = null;
        int totalMessages = 0;

        if (profile != null) {
            lastActiveAt = profile.getLastActiveAt();
            totalMessages = profile.getTotalMessages() != null ? profile.getTotalMessages() : 0;
        }

        if (lastActiveAt == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastActive = Duration.between(lastActiveAt, now).toDays();

        if (daysSinceLastActive > CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD) {
            return CustomerLifecycleConstants.STAGE_CHURNED;
        }
        if (daysSinceLastActive > CustomerLifecycleConstants.AT_RISK_DAYS_THRESHOLD) {
            return CustomerLifecycleConstants.STAGE_AT_RISK;
        }
        if (totalMessages > 50) {
            return CustomerLifecycleConstants.STAGE_LOYAL;
        }
        if (totalMessages > 5) {
            return CustomerLifecycleConstants.STAGE_ACTIVE;
        }
        return CustomerLifecycleConstants.STAGE_NEW;
    }

    /**
     * 根据画像数据直接计算生命周期阶段（无需额外DB查询）
     *
     * @param profile 客户画像
     * @return 生命周期阶段标识
     */
    private String determineStageFromProfile(CustomerProfile profile) {
        LocalDateTime lastActiveAt = profile.getLastActiveAt();
        int totalMessages = profile.getTotalMessages() != null ? profile.getTotalMessages() : 0;

        if (lastActiveAt == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastActive = Duration.between(lastActiveAt, now).toDays();

        if (daysSinceLastActive > CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD) {
            return CustomerLifecycleConstants.STAGE_CHURNED;
        }
        if (daysSinceLastActive > CustomerLifecycleConstants.AT_RISK_DAYS_THRESHOLD) {
            return CustomerLifecycleConstants.STAGE_AT_RISK;
        }
        if (totalMessages > 50) {
            return CustomerLifecycleConstants.STAGE_LOYAL;
        }
        if (totalMessages > 5) {
            return CustomerLifecycleConstants.STAGE_ACTIVE;
        }
        return CustomerLifecycleConstants.STAGE_NEW;
    }

    /**
     * 获取流失风险客户列表
     *
     * @return 流失风险客户VO列表
     */
    @Override
    public List<CustomerVO> getAtRiskCustomers() {
        LocalDateTime churnedThreshold = LocalDateTime.now().minusDays(CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD);
        LocalDateTime atRiskThreshold = LocalDateTime.now().minusDays(CustomerLifecycleConstants.AT_RISK_DAYS_THRESHOLD);

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(CustomerProfile::getLastActiveAt, atRiskThreshold);
        wrapper.gt(CustomerProfile::getLastActiveAt, churnedThreshold);
        wrapper.orderByAsc(CustomerProfile::getLastActiveAt);

        List<CustomerProfile> profiles = customerProfileMapper.selectList(wrapper);
        return convertToCustomerVOList(profiles);
    }

    /**
     * 获取已流失客户列表
     *
     * @return 已流失客户VO列表
     */
    @Override
    public List<CustomerVO> getChurnedCustomers() {
        LocalDateTime churnedThreshold = LocalDateTime.now().minusDays(CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD);

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(CustomerProfile::getLastActiveAt, churnedThreshold);
        wrapper.orderByAsc(CustomerProfile::getLastActiveAt);

        List<CustomerProfile> profiles = customerProfileMapper.selectList(wrapper);
        return convertToCustomerVOList(profiles);
    }

    /**
     * 更新客户生命周期标签（批量SQL更新替代N+1循环）
     * <p>
     * 使用LambdaUpdateWrapper按条件批量更新，避免逐条select+update。
     * 每个阶段一条UPDATE语句，共5条SQL替代原先N*2条。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerLifecycleTags() {
        log.info("开始更新客户生命周期标签...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime churnedThreshold = now.minusDays(CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD);
        LocalDateTime atRiskThreshold = now.minusDays(CustomerLifecycleConstants.AT_RISK_DAYS_THRESHOLD);

        int updated = 0;

        LambdaUpdateWrapper<CustomerProfile> churnedWrapper = new LambdaUpdateWrapper<>();
        churnedWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_CHURNED)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .lt(CustomerProfile::getLastActiveAt, churnedThreshold)
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_CHURNED);
        updated += customerProfileMapper.update(null, churnedWrapper);

        LambdaUpdateWrapper<CustomerProfile> atRiskWrapper = new LambdaUpdateWrapper<>();
        atRiskWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_AT_RISK)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .ge(CustomerProfile::getLastActiveAt, churnedThreshold)
                .lt(CustomerProfile::getLastActiveAt, atRiskThreshold)
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_AT_RISK);
        updated += customerProfileMapper.update(null, atRiskWrapper);

        LambdaUpdateWrapper<CustomerProfile> loyalWrapper = new LambdaUpdateWrapper<>();
        loyalWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_LOYAL)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .ge(CustomerProfile::getLastActiveAt, atRiskThreshold)
                .gt(CustomerProfile::getTotalMessages, 50)
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_LOYAL);
        updated += customerProfileMapper.update(null, loyalWrapper);

        LambdaUpdateWrapper<CustomerProfile> activeWrapper = new LambdaUpdateWrapper<>();
        activeWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_ACTIVE)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .ge(CustomerProfile::getLastActiveAt, atRiskThreshold)
                .le(CustomerProfile::getTotalMessages, 50)
                .gt(CustomerProfile::getTotalMessages, 5)
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_ACTIVE);
        updated += customerProfileMapper.update(null, activeWrapper);

        LambdaUpdateWrapper<CustomerProfile> newWrapper = new LambdaUpdateWrapper<>();
        newWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_NEW)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .ge(CustomerProfile::getLastActiveAt, atRiskThreshold)
                .le(CustomerProfile::getTotalMessages, 5)
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_NEW);
        updated += customerProfileMapper.update(null, newWrapper);

        log.info("客户生命周期标签更新完成，共更新{}个客户", updated);
    }

    /**
     * 将客户画像列表转换为CustomerVO列表（批量查询替代N+1）
     *
     * @param profiles 客户画像列表
     * @return 客户VO列表
     */
    private List<CustomerVO> convertToCustomerVOList(List<CustomerProfile> profiles) {
        if (profiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = profiles.stream()
                .map(CustomerProfile::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CustomerVO> result = new ArrayList<>();
        for (CustomerProfile profile : profiles) {
            CustomerVO vo = new CustomerVO();
            vo.setId(profile.getUserId());
            vo.setLastActiveAt(profile.getLastActiveAt());
            vo.setMessageCount(profile.getTotalMessages());

            User user = userMap.get(profile.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setPlatform(user.getPlatform());
            }

            String stage = determineStageFromProfile(profile);
            vo.setLifecycleStage(stage);

            result.add(vo);
        }
        return result;
    }
}
