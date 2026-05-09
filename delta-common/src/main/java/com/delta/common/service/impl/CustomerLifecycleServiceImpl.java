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
import org.springframework.beans.factory.annotation.Value;
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
 * 基于客户画像(CustomerProfile)中的活跃时间、消息数量、RFM得分和消费数据，
 * 综合判断客户生命周期阶段，并提供流失风险预警和标签更新功能。
 * RFM数据来源：CustomerProfile中的32字段消费行为数据（总订单、总消费、RFM分群等）。
 * </p>
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

    /** AT_RISK流失风险阈值（天），可通过配置 customer.lifecycle.at-risk-days 覆盖，默认7天 */
    @Value("${customer.lifecycle.at-risk-days:7}")
    private int atRiskDaysThreshold;

    /** CHURNED已流失阈值（天），可通过配置 customer.lifecycle.churned-days 覆盖，默认30天 */
    @Value("${customer.lifecycle.churned-days:30}")
    private int churnedDaysThreshold;

    /**
     * 判断客户生命周期阶段
     * <p>
     * 综合RFM消费数据和活跃度指标进行判定：</p>
     * <ul>
     *   <li>CHURNED：超30天未活跃</li>
     *   <li>AT_RISK：超7天未活跃但曾有消费记录 → 高价值流失预警</li>
     *   <li>LOYAL：RFM高价值(rfmTotalScore>=10) 或 (消息>50且订单>=3)</li>
     *   <li>ACTIVE：有消费记录(totalOrders>=1) 或 消息>5</li>
     *   <li>NEW：无活跃记录或首次接触</li>
     * </ul>
     *
     * @param userId 客户用户ID
     * @return 生命周期阶段标识
     */
    @Override
    public String determineLifecycleStage(Long userId) {
        if (userId == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>().eq(CustomerProfile::getUserId, userId)
        );

        if (profile == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        return determineStageFromProfile(profile);
    }

    /**
     * 根据画像数据直接计算生命周期阶段（无需额外DB查询）
     * <p>
     * 综合RFM消费数据（totalOrders/totalSpent/rfmTotalScore）和活跃度（lastActiveAt/totalMessages）
     * 进行多维度生命周期判定，避免仅依赖单一指标导致的误判。
     * </p>
     *
     * @param profile 客户画像
     * @return 生命周期阶段标识
     */
    private String determineStageFromProfile(CustomerProfile profile) {
        LocalDateTime lastActiveAt = profile.getLastActiveAt();
        int totalMessages = profile.getTotalMessages() != null ? profile.getTotalMessages() : 0;
        int totalOrders = profile.getTotalOrders() != null ? profile.getTotalOrders() : 0;
        int rfmTotalScore = profile.getRfmTotalScore() != null ? profile.getRfmTotalScore() : 0;

        if (lastActiveAt == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastActive = Duration.between(lastActiveAt, now).toDays();

        if (daysSinceLastActive > churnedDaysThreshold) {
            return CustomerLifecycleConstants.STAGE_CHURNED;
        }
        if (daysSinceLastActive > atRiskDaysThreshold) {
            return CustomerLifecycleConstants.STAGE_AT_RISK;
        }

        boolean isRfmHighValue = rfmTotalScore >= CustomerLifecycleConstants.RFM_HIGH_VALUE_THRESHOLD;
        boolean isMessagingLoyal = totalMessages > CustomerLifecycleConstants.LOYAL_MIN_MESSAGES
                && totalOrders >= CustomerLifecycleConstants.LOYAL_MIN_ORDERS;

        if (isRfmHighValue || isMessagingLoyal) {
            return CustomerLifecycleConstants.STAGE_LOYAL;
        }

        boolean hasOrders = totalOrders >= CustomerLifecycleConstants.ACTIVE_MIN_ORDERS;
        boolean hasMessages = totalMessages > CustomerLifecycleConstants.ACTIVE_MIN_MESSAGES;

        if (hasOrders || hasMessages) {
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
        LocalDateTime churnedThreshold = LocalDateTime.now().minusDays(churnedDaysThreshold);
        LocalDateTime atRiskThreshold = LocalDateTime.now().minusDays(atRiskDaysThreshold);

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
        LocalDateTime churnedThreshold = LocalDateTime.now().minusDays(churnedDaysThreshold);

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(CustomerProfile::getLastActiveAt, churnedThreshold);
        wrapper.orderByAsc(CustomerProfile::getLastActiveAt);

        List<CustomerProfile> profiles = customerProfileMapper.selectList(wrapper);
        return convertToCustomerVOList(profiles);
    }

    /**
     * 更新客户生命周期标签（批量SQL更新替代N+1循环）
     * <p>
     * 使用LambdaUpdateWrapper按条件批量更新，整合RFM消费维度判定。
     * LOYAL判定：RFM综合分>=10 或 (消息>50且订单>=3) 且 最近活跃。
     * ACTIVE判定：有消费(订单>=1) 或 有消息(>5) 且 最近活跃。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerLifecycleTags() {
        log.info("开始更新客户生命周期标签（含RFM综合判定）...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime churnedThreshold = now.minusDays(churnedDaysThreshold);
        LocalDateTime atRiskThreshold = now.minusDays(atRiskDaysThreshold);

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
                .and(w -> w.ge(CustomerProfile::getRfmTotalScore, CustomerLifecycleConstants.RFM_HIGH_VALUE_THRESHOLD)
                        .or(i -> i.gt(CustomerProfile::getTotalMessages, CustomerLifecycleConstants.LOYAL_MIN_MESSAGES)
                                .ge(CustomerProfile::getTotalOrders, CustomerLifecycleConstants.LOYAL_MIN_ORDERS)))
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_LOYAL);
        updated += customerProfileMapper.update(null, loyalWrapper);

        LambdaUpdateWrapper<CustomerProfile> activeWrapper = new LambdaUpdateWrapper<>();
        activeWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_ACTIVE)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .ge(CustomerProfile::getLastActiveAt, atRiskThreshold)
                .and(w -> w.ge(CustomerProfile::getTotalOrders, CustomerLifecycleConstants.ACTIVE_MIN_ORDERS)
                        .or(i -> i.gt(CustomerProfile::getTotalMessages, CustomerLifecycleConstants.ACTIVE_MIN_MESSAGES)))
                .not(w -> w.ge(CustomerProfile::getRfmTotalScore, CustomerLifecycleConstants.RFM_HIGH_VALUE_THRESHOLD)
                        .or(i -> i.gt(CustomerProfile::getTotalMessages, CustomerLifecycleConstants.LOYAL_MIN_MESSAGES)
                                .ge(CustomerProfile::getTotalOrders, CustomerLifecycleConstants.LOYAL_MIN_ORDERS)))
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_ACTIVE);
        updated += customerProfileMapper.update(null, activeWrapper);

        LambdaUpdateWrapper<CustomerProfile> newWrapper = new LambdaUpdateWrapper<>();
        newWrapper.set(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_NEW)
                .isNotNull(CustomerProfile::getLastActiveAt)
                .ge(CustomerProfile::getLastActiveAt, atRiskThreshold)
                .le(CustomerProfile::getTotalMessages, CustomerLifecycleConstants.ACTIVE_MIN_MESSAGES)
                .lt(CustomerProfile::getTotalOrders, CustomerLifecycleConstants.ACTIVE_MIN_ORDERS)
                .ne(CustomerProfile::getLifecycleStage, CustomerLifecycleConstants.STAGE_NEW);
        updated += customerProfileMapper.update(null, newWrapper);

        log.info("客户生命周期标签更新完成（含RFM），共更新{}个客户", updated);
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
