package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.ClubSubscription;
import com.delta.common.entity.PricingPlan;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.mapper.ClubSubscriptionMapper;
import com.delta.common.mapper.PricingPlanMapper;
import com.delta.common.service.ClubSubscriptionService;
import com.delta.common.vo.ClubSubscriptionVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 俱乐部订阅服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class ClubSubscriptionServiceImpl implements ClubSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(ClubSubscriptionServiceImpl.class);

    private final ClubSubscriptionMapper clubSubscriptionMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final ClubConfigMapper clubConfigMapper;

    @Override
    public Page<ClubSubscriptionVO> getPage(Integer page, Integer size, Long clubConfigId, String status) {
        Page<ClubSubscription> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ClubSubscription> wrapper = new LambdaQueryWrapper<>();

        if (clubConfigId != null) {
            wrapper.eq(ClubSubscription::getClubConfigId, clubConfigId);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ClubSubscription::getStatus, status);
        }
        wrapper.orderByDesc(ClubSubscription::getCreatedAt);

        Page<ClubSubscription> result = clubSubscriptionMapper.selectPage(pageObj, wrapper);
        Page<ClubSubscriptionVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(sub -> {
            ClubSubscriptionVO vo = BeanUtil.copyProperties(sub, ClubSubscriptionVO.class);
            PricingPlan plan = pricingPlanMapper.selectById(sub.getPlanId());
            if (plan != null) {
                vo.setPlanName(plan.getPlanName());
            }
            ClubConfig config = clubConfigMapper.selectById(sub.getClubConfigId());
            if (config != null) {
                vo.setClubName(config.getClubName());
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public ClubSubscriptionVO getById(Long id) {
        ClubSubscription sub = clubSubscriptionMapper.selectById(id);
        if (sub == null) {
            throw new BusinessException("订阅记录不存在");
        }
        return buildVO(sub);
    }

    @Override
    public ClubSubscriptionVO getByClubConfigId(Long clubConfigId) {
        LambdaQueryWrapper<ClubSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubSubscription::getClubConfigId, clubConfigId);
        wrapper.orderByDesc(ClubSubscription::getCreatedAt);
        wrapper.last("LIMIT 1");
        ClubSubscription sub = clubSubscriptionMapper.selectOne(wrapper);
        return sub != null ? buildVO(sub) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subscribe(Long clubConfigId, Long planId) {
        PricingPlan plan = pricingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("定价方案不存在");
        }

        // 先取消已有订阅
        LambdaQueryWrapper<ClubSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubSubscription::getClubConfigId, clubConfigId);
        wrapper.eq(ClubSubscription::getStatus, "ACTIVE");
        ClubSubscription existing = clubSubscriptionMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setStatus("CANCELLED");
            clubSubscriptionMapper.updateById(existing);
        }

        ClubSubscription sub = new ClubSubscription();
        sub.setClubConfigId(clubConfigId);
        sub.setPlanId(planId);
        sub.setStatus("ACTIVE");
        sub.setStartAt(LocalDateTime.now());
        sub.setExpireAt(LocalDateTime.now().plusMonths(1));
        sub.setPaidAmount(plan.getMonthlyPrice());
        sub.setPaidAt(LocalDateTime.now());
        clubSubscriptionMapper.insert(sub);
        log.info("俱乐部订阅成功: clubConfigId={}, planId={}", clubConfigId, planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(Long id) {
        ClubSubscription sub = clubSubscriptionMapper.selectById(id);
        if (sub == null) {
            throw new BusinessException("订阅记录不存在");
        }
        sub.setStatus("CANCELLED");
        clubSubscriptionMapper.updateById(sub);
        log.info("取消订阅成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renewSubscription(Long id, Integer months) {
        ClubSubscription sub = clubSubscriptionMapper.selectById(id);
        if (sub == null) {
            throw new BusinessException("订阅记录不存在");
        }
        sub.setExpireAt(sub.getExpireAt().plusMonths(months));
        sub.setStatus("ACTIVE");
        clubSubscriptionMapper.updateById(sub);
        log.info("续费订阅成功: id={}, months={}", id, months);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void trial(Long clubConfigId) {
        // 查找基础版方案用于试用
        LambdaQueryWrapper<PricingPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(PricingPlan::getPlanCode, "BASIC");
        PricingPlan plan = pricingPlanMapper.selectOne(planWrapper);
        if (plan == null) {
            throw new BusinessException("基础版方案不存在，无法开通试用");
        }

        ClubSubscription sub = new ClubSubscription();
        sub.setClubConfigId(clubConfigId);
        sub.setPlanId(plan.getId());
        sub.setStatus("TRIAL");
        sub.setStartAt(LocalDateTime.now());
        sub.setExpireAt(LocalDateTime.now().plusDays(15));
        sub.setTrialEndAt(LocalDateTime.now().plusDays(15));
        sub.setPaidAmount(java.math.BigDecimal.ZERO);
        clubSubscriptionMapper.insert(sub);
        log.info("开通试用成功: clubConfigId={}", clubConfigId);
    }

    private ClubSubscriptionVO buildVO(ClubSubscription sub) {
        ClubSubscriptionVO vo = BeanUtil.copyProperties(sub, ClubSubscriptionVO.class);
        PricingPlan plan = pricingPlanMapper.selectById(sub.getPlanId());
        if (plan != null) {
            vo.setPlanName(plan.getPlanName());
        }
        ClubConfig config = clubConfigMapper.selectById(sub.getClubConfigId());
        if (config != null) {
            vo.setClubName(config.getClubName());
        }
        return vo;
    }
}
