package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

        // 从客户画像获取数据
        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>().eq(CustomerProfile::getUserId, userId)
        );

        LocalDateTime lastActiveAt = null;
        int totalMessages = 0;

        if (profile != null) {
            lastActiveAt = profile.getLastActiveAt();
            totalMessages = profile.getTotalMessages() != null ? profile.getTotalMessages() : 0;
        }

        // 如果画像不存在或无活跃时间，视为新客户
        if (lastActiveAt == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastActive = Duration.between(lastActiveAt, now).toDays();

        // 按优先级判断：已流失 > 流失风险 > 忠实 > 活跃 > 新客户
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
     * <p>
     * 查询客户画像中最后活跃时间在 AT_RISK 和 CHURNED 阈值之间的客户。</p>
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
     * <p>
     * 查询客户画像中最后活跃时间超过 CHURNED 阈值的客户。</p>
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
     * 更新客户生命周期标签
     * <p>
     * 遍历所有有活跃时间的客户画像，根据生命周期阶段自动添加对应标签，
     * 同时更新画像中的 lifecycleStage 字段。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerLifecycleTags() {
        log.info("开始更新客户生命周期标签...");

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(CustomerProfile::getLastActiveAt);
        List<CustomerProfile> profiles = customerProfileMapper.selectList(wrapper);

        int updated = 0;
        for (CustomerProfile profile : profiles) {
            String stage = determineLifecycleStage(profile.getUserId());

            // 更新画像中的生命周期阶段
            profile.setLifecycleStage(stage);

            // 根据阶段确定标签
            String tag = switch (stage) {
                case CustomerLifecycleConstants.STAGE_AT_RISK -> CustomerLifecycleConstants.TAG_AT_RISK;
                case CustomerLifecycleConstants.STAGE_CHURNED -> CustomerLifecycleConstants.TAG_AT_RISK;
                case CustomerLifecycleConstants.STAGE_LOYAL -> CustomerLifecycleConstants.TAG_LOYAL;
                case CustomerLifecycleConstants.STAGE_NEW -> CustomerLifecycleConstants.TAG_NEW;
                default -> null;
            };

            // 如果有标签且当前标签中不包含，则追加
            if (tag != null) {
                String currentTags = profile.getTags();
                if (currentTags == null || !currentTags.contains(tag)) {
                    String newTags = currentTags == null ? tag : currentTags + "," + tag;
                    profile.setTags(newTags);
                }
            }

            customerProfileMapper.updateById(profile);
            updated++;
        }
        log.info("客户生命周期标签更新完成，共更新{}个客户", updated);
    }

    /**
     * 将客户画像列表转换为CustomerVO列表
     *
     * @param profiles 客户画像列表
     * @return 客户VO列表
     */
    private List<CustomerVO> convertToCustomerVOList(List<CustomerProfile> profiles) {
        List<CustomerVO> result = new ArrayList<>();
        for (CustomerProfile profile : profiles) {
            CustomerVO vo = new CustomerVO();
            vo.setId(profile.getUserId());
            vo.setLastActiveAt(profile.getLastActiveAt());
            vo.setMessageCount(profile.getTotalMessages());

            // 从用户表获取基本信息
            User user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setPlatform(user.getPlatform());
            }

            // 设置生命周期阶段
            String stage = determineLifecycleStage(profile.getUserId());
            vo.setLifecycleStage(stage);

            result.add(vo);
        }
        return result;
    }
}
