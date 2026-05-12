package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.CustomerProfileConstants;
import com.delta.common.dto.CustomerOrderRecordDTO;
import com.delta.common.dto.CustomerProfileUpdateDTO;
import com.delta.common.entity.*;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.*;
import com.delta.common.service.CustomerProfileService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CustomerOrderRecordVO;
import com.delta.common.vo.CustomerProfileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户画像服务实现，基于RFM模型和消费行为分析
 * <p>
 * 画像维度：
 * 1. RFM价值评估 - Recency最近消费/Frequency消费频率/Monetary消费金额
 * 2. 消费行为 - 客单价/消费趋势/复购率/LTV估算
 * 3. 服务偏好 - 游戏/时段/陪玩师等级/订单类型
 * 4. 交互行为 - AI/人工比例/转人工原因/情绪触发
 * 5. 满意度 - 评价/投诉/退款
 * 6. 生命周期 - 客户阶段/活跃度/流失风险
 * 7. 需求分类 - 情感陪伴/技能提升/社交拓展/娱乐
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private static final Logger log = LoggerFactory.getLogger(CustomerProfileServiceImpl.class);

    private final CustomerProfileMapper customerProfileMapper;

    private final CustomerOrderRecordMapper customerOrderRecordMapper;

    private final UserMapper userMapper;

    private final CompanionMapper companionMapper;

    private final SysUserMapper sysUserMapper;

    @Value("${delta.rfm.recency.thresholds:7,14,30,60}")
    private String recencyThresholdsConfig;

    @Value("${delta.rfm.frequency.thresholds:3.0,2.0,1.0,0.5}")
    private String frequencyThresholdsConfig;

    @Value("${delta.rfm.monetary.thresholds:5000,2000,500,100}")
    private String monetaryThresholdsConfig;

    @Override
    public Page<CustomerProfileVO> getProfilePage(Integer page, Integer size, String memberLevel, String riskLevel, String lifecycleStage, String rfmSegment, String keyword) {
        Page<CustomerProfile> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();

        if (memberLevel != null && !memberLevel.trim().isEmpty()) {
            wrapper.eq(CustomerProfile::getMemberLevel, memberLevel);
        }
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            wrapper.eq(CustomerProfile::getRiskLevel, riskLevel);
        }
        if (lifecycleStage != null && !lifecycleStage.trim().isEmpty()) {
            wrapper.eq(CustomerProfile::getLifecycleStage, lifecycleStage);
        }
        if (rfmSegment != null && !rfmSegment.trim().isEmpty()) {
            wrapper.eq(CustomerProfile::getRfmSegment, rfmSegment);
        }

        wrapper.orderByDesc(CustomerProfile::getLastActiveAt);

        Page<CustomerProfile> profilePage = customerProfileMapper.selectPage(pageObj, wrapper);

        List<Long> userIds = profilePage.getRecords().stream()
                .map(CustomerProfile::getUserId)
                .collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> companionIds = profilePage.getRecords().stream()
                .map(CustomerProfile::getFavoriteCompanionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Companion> companionMap = companionIds.isEmpty() ? Map.of() :
                companionMapper.selectByIds(companionIds).stream()
                        .collect(Collectors.toMap(Companion::getId, c -> c));

        List<Long> csUserIds = userMap.values().stream()
                .map(User::getAssignedCsUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, SysUser> csUserMap = csUserIds.isEmpty() ? Map.of() :
                sysUserMapper.selectByIds(csUserIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        Page<CustomerProfileVO> resultPage = new Page<>(profilePage.getCurrent(), profilePage.getSize(), profilePage.getTotal());
        List<CustomerProfileVO> voList = profilePage.getRecords().stream().map(profile -> {
            CustomerProfileVO vo = BeanUtil.copyProperties(profile, CustomerProfileVO.class);
            User user = userMap.get(profile.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
                vo.setPlatform(user.getPlatform());
                if (user.getAssignedCsUserId() != null) {
                    SysUser csUser = csUserMap.get(user.getAssignedCsUserId());
                    if (csUser != null) {
                        vo.setAssignedCsUserName(csUser.getRealName());
                    }
                }
            }
            if (profile.getFavoriteCompanionId() != null) {
                Companion companion = companionMap.get(profile.getFavoriteCompanionId());
                if (companion != null) {
                    vo.setFavoriteCompanionName(companion.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            voList = voList.stream()
                    .filter(vo -> (vo.getNickname() != null && vo.getNickname().contains(kw))
                            || (vo.getTags() != null && vo.getTags().contains(kw))
                            || (vo.getNeedTags() != null && vo.getNeedTags().contains(kw)))
                    .collect(Collectors.toList());
        }

        resultPage.setRecords(voList);
        VoUtils.setRowNumbers(resultPage);
        return resultPage;
    }

    @Override
    public CustomerProfileVO getProfileByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("客户不存在");
        }

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getUserId, userId);
        CustomerProfile profile = customerProfileMapper.selectOne(wrapper);

        if (profile == null) {
            profile = createDefaultProfile(userId);
        }

        CustomerProfileVO vo = BeanUtil.copyProperties(profile, CustomerProfileVO.class);
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPlatform(user.getPlatform());

        if (user.getAssignedCsUserId() != null) {
            SysUser csUser = sysUserMapper.selectById(user.getAssignedCsUserId());
            if (csUser != null) {
                vo.setAssignedCsUserName(csUser.getRealName());
            }
        }

        if (profile.getFavoriteCompanionId() != null) {
            Companion companion = companionMapper.selectById(profile.getFavoriteCompanionId());
            if (companion != null) {
                vo.setFavoriteCompanionName(companion.getNickname());
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(CustomerProfileUpdateDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException("客户ID不能为空");
        }

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getUserId, dto.getUserId());
        CustomerProfile profile = customerProfileMapper.selectOne(wrapper);

        if (profile == null) {
            throw new BusinessException("客户画像不存在");
        }

        if (dto.getTags() != null) {
            profile.setTags(dto.getTags());
        }
        if (dto.getRemark() != null) {
            profile.setRemark(dto.getRemark());
        }
        if (dto.getMemberLevel() != null) {
            profile.setMemberLevel(dto.getMemberLevel());
        }
        if (dto.getRiskLevel() != null) {
            profile.setRiskLevel(dto.getRiskLevel());
        }

        customerProfileMapper.updateById(profile);
        log.info("更新客户画像: userId={}", dto.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrderRecord(CustomerOrderRecordDTO dto) {
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("客户不存在");
        }

        if (dto.getCompanionId() != null) {
            Companion companion = companionMapper.selectById(dto.getCompanionId());
            if (companion == null) {
                throw new BusinessException("陪玩师不存在");
            }
        }

        CustomerOrderRecord record = BeanUtil.copyProperties(dto, CustomerOrderRecord.class);
        if (record.getStatus() == null) {
            record.setStatus(BusinessStatusConstants.ORDER_STATUS_COMPLETED);
        }
        customerOrderRecordMapper.insert(record);

        if (BusinessStatusConstants.ORDER_STATUS_REFUNDED.equals(dto.getStatus())) {
            LambdaQueryWrapper<CustomerProfile> pWrapper = new LambdaQueryWrapper<>();
            pWrapper.eq(CustomerProfile::getUserId, dto.getUserId());
            CustomerProfile p = customerProfileMapper.selectOne(pWrapper);
            if (p != null) {
                p.setRefundCount(p.getRefundCount() + 1);
                customerProfileMapper.updateById(p);
            }
        }

        refreshProfile(dto.getUserId());

        log.info("添加客户消费记录: userId={}, orderType={}, amount={}", dto.getUserId(), dto.getOrderType(), dto.getAmount());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void syncOrderRecord(Order order) {
        if (order == null || order.getUserId() == null) {
            log.warn("订单数据不完整，跳过画像同步: order={}", order);
            return;
        }

        Companion companion = order.getCompanionId() != null ? companionMapper.selectById(order.getCompanionId()) : null;

        CustomerOrderRecord record = new CustomerOrderRecord();
        record.setUserId(order.getUserId());
        record.setCustomerId(order.getUserId());
        record.setOrderId(order.getId());
        record.setCompanionId(order.getCompanionId());
        record.setRecordType("ORDER");
        record.setOrderType(order.getServiceType());
        record.setOrderTime(order.getScheduledStart() != null ? order.getScheduledStart() : order.getCreatedAt());
        record.setGameType(order.getGameType() != null ? order.getGameType() : (companion != null ? companion.getGameType() : null));
        record.setCompanionLevel(companion != null ? companion.getRankLevel() : null);
        record.setStatus(order.getOrderStatus());

        if (order.getDurationMinutes() != null && order.getDurationMinutes() > 0) {
            record.setDurationHours(BigDecimal.valueOf(order.getDurationMinutes())
                    .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP));
        }
        record.setAmount(order.getTotalAmount());

        if (order.getScheduledStart() != null && order.getScheduledEnd() != null) {
            String startStr = order.getScheduledStart().toLocalTime().toString().substring(0, 5);
            String endStr = order.getScheduledEnd().toLocalTime().toString().substring(0, 5);
            record.setTimeSlot(startStr + "-" + endStr);
        }

        record.setRemark(order.getRemark());
        record.setContent("订单编号: " + order.getOrderNo());

        customerOrderRecordMapper.insert(record);

        refreshProfile(order.getUserId());

        log.info("订单同步至客户画像成功: orderNo={}, userId={}, companionId={}, amount={}",
                order.getOrderNo(), order.getUserId(), order.getCompanionId(), order.getTotalAmount());
    }

    @Override
    public Page<CustomerOrderRecordVO> getOrderRecordPage(Integer page, Integer size, Long userId, String orderType, String status) {
        Page<CustomerOrderRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<CustomerOrderRecord> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(CustomerOrderRecord::getUserId, userId);
        }
        if (orderType != null && !orderType.trim().isEmpty()) {
            wrapper.eq(CustomerOrderRecord::getOrderType, orderType);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(CustomerOrderRecord::getStatus, status);
        }

        wrapper.orderByDesc(CustomerOrderRecord::getOrderTime);

        Page<CustomerOrderRecord> recordPageResult = customerOrderRecordMapper.selectPage(recordPage, wrapper);

        List<Long> companionIds = recordPageResult.getRecords().stream()
                .map(CustomerOrderRecord::getCompanionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Companion> companionMap = companionIds.isEmpty() ? Map.of() :
                companionMapper.selectByIds(companionIds).stream()
                        .collect(Collectors.toMap(Companion::getId, c -> c));

        List<Long> userIds = recordPageResult.getRecords().stream()
                .map(CustomerOrderRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        Page<CustomerOrderRecordVO> resultPage = new Page<>(recordPageResult.getCurrent(), recordPageResult.getSize(), recordPageResult.getTotal());
        List<CustomerOrderRecordVO> voList = recordPageResult.getRecords().stream().map(record -> {
            CustomerOrderRecordVO vo = BeanUtil.copyProperties(record, CustomerOrderRecordVO.class);
            User user = userMap.get(record.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }
            if (record.getCompanionId() != null) {
                Companion companion = companionMap.get(record.getCompanionId());
                if (companion != null) {
                    vo.setCompanionName(companion.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        VoUtils.setRowNumbers(resultPage);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshProfile(Long userId) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getUserId, userId);
        CustomerProfile profile = customerProfileMapper.selectOne(wrapper);

        if (profile == null) {
            createDefaultProfile(userId);
            profile = customerProfileMapper.selectOne(wrapper);
        }

        final CustomerProfile p = profile;

        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<CustomerOrderRecord> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(CustomerOrderRecord::getUserId, userId);
        orderWrapper.eq(CustomerOrderRecord::getStatus, BusinessStatusConstants.ORDER_STATUS_COMPLETED);
        List<CustomerOrderRecord> completedOrders = customerOrderRecordMapper.selectList(orderWrapper);

        p.setTotalOrders(completedOrders.size());

        BigDecimal totalSpent = completedOrders.stream()
                .map(CustomerOrderRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        p.setTotalSpent(totalSpent);

        if (!completedOrders.isEmpty()) {
            p.setAvgOrderAmount(totalSpent.divide(BigDecimal.valueOf(completedOrders.size()), 2, RoundingMode.HALF_UP));
        }

        completedOrders.stream()
                .map(CustomerOrderRecord::getAmount)
                .max(BigDecimal::compareTo)
                .ifPresent(p::setMaxOrderAmount);

        completedOrders.stream()
                .map(CustomerOrderRecord::getOrderTime)
                .max(LocalDateTime::compareTo)
                .ifPresent(p::setLastOrderAt);

        List<BigDecimal> durations = completedOrders.stream()
                .map(CustomerOrderRecord::getDurationHours)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!durations.isEmpty()) {
            BigDecimal avgDuration = durations.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(durations.size()), 1, RoundingMode.HALF_UP);
            p.setAvgServiceDuration(avgDuration);
        }

        calculateSpendingTrend(p, userId, now);

        calculateRepurchaseRate(p, userId, now);

        calculateLtv(p, now);

        Map<Long, Long> companionCount = completedOrders.stream()
                .filter(o -> o.getCompanionId() != null)
                .collect(Collectors.groupingBy(CustomerOrderRecord::getCompanionId, Collectors.counting()));

        companionCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> p.setFavoriteCompanionId(entry.getKey()));

        p.setCompanionDiversity(companionCount.size());

        Map<String, Long> gameTypeCount = completedOrders.stream()
                .filter(o -> o.getGameType() != null && !o.getGameType().isEmpty())
                .collect(Collectors.groupingBy(CustomerOrderRecord::getGameType, Collectors.counting()));

        gameTypeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> p.setFavoriteGameType(entry.getKey()));

        Map<String, Long> timeSlotCount = completedOrders.stream()
                .filter(o -> o.getTimeSlot() != null && !o.getTimeSlot().isEmpty())
                .collect(Collectors.groupingBy(CustomerOrderRecord::getTimeSlot, Collectors.counting()));

        timeSlotCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> p.setPreferredTimeSlot(entry.getKey()));

        Map<String, Long> levelCount = completedOrders.stream()
                .filter(o -> o.getCompanionLevel() != null && !o.getCompanionLevel().isEmpty())
                .collect(Collectors.groupingBy(CustomerOrderRecord::getCompanionLevel, Collectors.counting()));

        levelCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> p.setPreferredCompanionLevel(entry.getKey()));

        Map<String, Long> orderTypeCount = completedOrders.stream()
                .filter(o -> o.getOrderType() != null && !o.getOrderType().isEmpty())
                .collect(Collectors.groupingBy(CustomerOrderRecord::getOrderType, Collectors.counting()));

        orderTypeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> p.setPreferredOrderType(entry.getKey()));

        List<CustomerOrderRecord> ratedOrders = completedOrders.stream()
                .filter(o -> o.getRating() != null)
                .collect(Collectors.toList());
        if (!ratedOrders.isEmpty()) {
            double avgRating = ratedOrders.stream()
                    .mapToInt(CustomerOrderRecord::getRating)
                    .average()
                    .orElse(0.0);
            p.setAvgRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
            p.setSatisfactionScore(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        }

        calculateSatisfactionTrend(p, userId);

        calculateRfmScores(p, now);

        calculateLifecycleStage(p, now);

        p.setMemberLevel(calculateMemberLevel(p.getTotalOrders(), p.getTotalSpent()));

        p.setChurnRiskScore(calculateChurnRisk(p));

        p.setRiskLevel(determineRiskLevel(p.getChurnRiskScore()));

        calculateNeedType(p);

        if (p.getTotalMessages() > 0) {
            int total = p.getAiInteractionCount() + p.getManualInteractionCount();
            if (total > 0) {
                p.setAiRatio(BigDecimal.valueOf(p.getAiInteractionCount())
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            }
        }

        p.setActiveDays(calculateActiveDays(p));

        customerProfileMapper.updateById(p);
        log.info("刷新客户画像: userId={}, rfmScore={}, rfmSegment={}, lifecycle={}, totalOrders={}, totalSpent={}",
                userId, p.getRfmTotalScore(), p.getRfmSegment(), p.getLifecycleStage(), p.getTotalOrders(), p.getTotalSpent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initProfileIfNeeded(Long userId) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getUserId, userId);
        CustomerProfile existing = customerProfileMapper.selectOne(wrapper);

        if (existing == null) {
            createDefaultProfile(userId);
            log.info("初始化客户画像: userId={}", userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordInteraction(Long userId, boolean isAi) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getUserId, userId);
        CustomerProfile profile = customerProfileMapper.selectOne(wrapper);

        if (profile == null) {
            profile = createDefaultProfile(userId);
            profile.setTotalMessages(1);
            profile.setLastActiveAt(LocalDateTime.now());
            if (isAi) {
                profile.setAiInteractionCount(1);
            } else {
                profile.setManualInteractionCount(1);
            }
            profile.setFirstContactAt(LocalDateTime.now());
            profile.setAiRatio(isAi ? BigDecimal.ONE : BigDecimal.ZERO);
            customerProfileMapper.updateById(profile);
            return;
        }

        LambdaUpdateWrapper<CustomerProfile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CustomerProfile::getUserId, userId)
                .setSql("total_messages = total_messages + 1")
                .set(CustomerProfile::getLastActiveAt, LocalDateTime.now());

        if (isAi) {
            updateWrapper.setSql("ai_interaction_count = ai_interaction_count + 1");
        } else {
            updateWrapper.setSql("manual_interaction_count = manual_interaction_count + 1");
        }

        if (profile.getFirstContactAt() == null) {
            updateWrapper.set(CustomerProfile::getFirstContactAt, LocalDateTime.now());
        }

        customerProfileMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordHandoffEvent(Long userId, String reason, boolean isEmotion, boolean isOrderIntent) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getUserId, userId);
        CustomerProfile profile = customerProfileMapper.selectOne(wrapper);

        if (profile == null) {
            profile = createDefaultProfile(userId);
            profile.setHumanHandoffCount(1);
            if (isEmotion) profile.setEmotionTriggerCount(1);
            if (isOrderIntent) profile.setOrderIntentCount(1);
            if (reason != null && !reason.isEmpty()) profile.setTopHandoffReason(reason);
            customerProfileMapper.updateById(profile);
            log.info("记录转人工事件: userId={}, reason={}, isEmotion={}, isOrderIntent={}", userId, reason, isEmotion, isOrderIntent);
            return;
        }

        LambdaUpdateWrapper<CustomerProfile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CustomerProfile::getUserId, userId)
                .setSql("human_handoff_count = human_handoff_count + 1");

        if (isEmotion) {
            updateWrapper.setSql("emotion_trigger_count = emotion_trigger_count + 1");
        }

        if (isOrderIntent) {
            updateWrapper.setSql("order_intent_count = order_intent_count + 1");
        }

        if (reason != null && !reason.isEmpty()) {
            updateWrapper.set(CustomerProfile::getTopHandoffReason, reason);
        }

        customerProfileMapper.update(null, updateWrapper);
        log.info("记录转人工事件: userId={}, reason={}, isEmotion={}, isOrderIntent={}", userId, reason, isEmotion, isOrderIntent);
    }

    private CustomerProfile createDefaultProfile(Long userId) {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(userId);
        profile.setRfmRecencyScore(1);
        profile.setRfmFrequencyScore(1);
        profile.setRfmMonetaryScore(1);
        profile.setRfmTotalScore(3);
        profile.setRfmSegment(CustomerProfileConstants.RFM_SEGMENT_NEW);
        profile.setTotalOrders(0);
        profile.setTotalSpent(BigDecimal.ZERO);
        profile.setAvgOrderAmount(BigDecimal.ZERO);
        profile.setSpendingTrend(CustomerProfileConstants.SPENDING_TREND_STABLE);
        profile.setCompanionDiversity(0);
        profile.setTotalMessages(0);
        profile.setAiInteractionCount(0);
        profile.setManualInteractionCount(0);
        profile.setHumanHandoffCount(0);
        profile.setEmotionTriggerCount(0);
        profile.setOrderIntentCount(0);
        profile.setComplaintCount(0);
        profile.setRefundCount(0);
        profile.setLifecycleStage(CustomerProfileConstants.LIFECYCLE_NEW);
        profile.setMemberLevel(CustomerProfileConstants.MEMBER_LEVEL_NORMAL);
        profile.setRiskLevel(CustomerProfileConstants.RISK_LEVEL_LOW);
        profile.setChurnRiskScore(BigDecimal.ZERO);
        profile.setFirstContactAt(LocalDateTime.now());
        profile.setLastActiveAt(LocalDateTime.now());
        profile.setActiveDays(1);
        customerProfileMapper.insert(profile);
        return profile;
    }

    /**
     * RFM评分计算
     * R(Recency): 5=7天内, 4=14天内, 3=30天内, 2=60天内, 1=60天以上
     * F(Frequency): 5=月均3次以上, 4=月均2次, 3=月均1次, 2=2月1次, 1=更低
     * M(Monetary): 5=累计5000+, 4=2000+, 3=500+, 2=100+, 1=100以下
     */
    private void calculateRfmScores(CustomerProfile p, LocalDateTime now) {
        double[] recencyThresholds = parseThresholds(recencyThresholdsConfig);
        double[] frequencyThresholds = parseThresholds(frequencyThresholdsConfig);
        BigDecimal[] monetaryThresholds = parseMonetaryThresholds(monetaryThresholdsConfig);

        int rScore = 1;
        if (p.getLastOrderAt() != null) {
            long daysSinceLastOrder = ChronoUnit.DAYS.between(p.getLastOrderAt(), now);
            if (daysSinceLastOrder <= recencyThresholds[0]) rScore = 5;
            else if (daysSinceLastOrder <= recencyThresholds[1]) rScore = 4;
            else if (daysSinceLastOrder <= recencyThresholds[2]) rScore = 3;
            else if (daysSinceLastOrder <= recencyThresholds[3]) rScore = 2;
        }
        p.setRfmRecencyScore(rScore);

        int fScore = 1;
        if (p.getFirstContactAt() != null && p.getTotalOrders() > 0) {
            long monthsActive = Math.max(1, ChronoUnit.MONTHS.between(p.getFirstContactAt(), now));
            double monthlyAvg = (double) p.getTotalOrders() / monthsActive;
            if (monthlyAvg >= frequencyThresholds[0]) fScore = 5;
            else if (monthlyAvg >= frequencyThresholds[1]) fScore = 4;
            else if (monthlyAvg >= frequencyThresholds[2]) fScore = 3;
            else if (monthlyAvg >= frequencyThresholds[3]) fScore = 2;
        }
        p.setRfmFrequencyScore(fScore);

        int mScore = 1;
        BigDecimal spent = p.getTotalSpent();
        if (spent.compareTo(monetaryThresholds[0]) >= 0) mScore = 5;
        else if (spent.compareTo(monetaryThresholds[1]) >= 0) mScore = 4;
        else if (spent.compareTo(monetaryThresholds[2]) >= 0) mScore = 3;
        else if (spent.compareTo(monetaryThresholds[3]) >= 0) mScore = 2;
        p.setRfmMonetaryScore(mScore);

        int totalScore = rScore + fScore + mScore;
        p.setRfmTotalScore(totalScore);

        p.setRfmSegment(determineRfmSegment(rScore, fScore, mScore));
    }

    /**
     * RFM客户分群
     * CHAMPION: R>=4,F>=4,M>=4 - 重要价值客户
     * LOYAL: F>=4,M>=3 - 忠诚客户
     * POTENTIAL: R>=3,F>=2 - 潜力客户
     * NEW: R>=4,F<=2 - 新客户
     * AT_RISK: R<=2,F>=3 - 流失预警
     * HIBERNATE: R<=2,F<=2,M>=3 - 休眠高价值
     * LOST: R<=1,F<=1 - 流失客户
     */
    private String determineRfmSegment(int r, int f, int m) {
        if (r >= 4 && f >= 4 && m >= 4) return CustomerProfileConstants.RFM_SEGMENT_CHAMPION;
        if (f >= 4 && m >= 3) return CustomerProfileConstants.RFM_SEGMENT_LOYAL;
        if (r >= 3 && f >= 2) return CustomerProfileConstants.RFM_SEGMENT_POTENTIAL;
        if (r >= 4 && f <= 2) return CustomerProfileConstants.RFM_SEGMENT_NEW;
        if (r <= 2 && f >= 3) return CustomerProfileConstants.RFM_SEGMENT_AT_RISK;
        if (r <= 2 && f <= 2 && m >= 3) return CustomerProfileConstants.RFM_SEGMENT_HIBERNATE;
        if (r <= 1 && f <= 1) return CustomerProfileConstants.RFM_SEGMENT_LOST;
        return CustomerProfileConstants.RFM_SEGMENT_POTENTIAL;
    }

    private double[] parseThresholds(String config) {
        String[] parts = config.split(",");
        double[] thresholds = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            thresholds[i] = Double.parseDouble(parts[i].trim());
        }
        return thresholds;
    }

    private BigDecimal[] parseMonetaryThresholds(String config) {
        String[] parts = config.split(",");
        BigDecimal[] thresholds = new BigDecimal[parts.length];
        for (int i = 0; i < parts.length; i++) {
            thresholds[i] = new BigDecimal(parts[i].trim());
        }
        return thresholds;
    }

    /**
     * 消费趋势分析：对比近30天和前30天的消费金额
     */
    private void calculateSpendingTrend(CustomerProfile p, Long userId, LocalDateTime now) {
        LocalDateTime recentStart = now.minusDays(30);
        LocalDateTime previousStart = now.minusDays(60);

        LambdaQueryWrapper<CustomerOrderRecord> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(CustomerOrderRecord::getUserId, userId)
                .eq(CustomerOrderRecord::getStatus, BusinessStatusConstants.ORDER_STATUS_COMPLETED)
                .ge(CustomerOrderRecord::getOrderTime, recentStart);
        BigDecimal recentSpent = customerOrderRecordMapper.selectList(recentWrapper).stream()
                .map(CustomerOrderRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<CustomerOrderRecord> previousWrapper = new LambdaQueryWrapper<>();
        previousWrapper.eq(CustomerOrderRecord::getUserId, userId)
                .eq(CustomerOrderRecord::getStatus, BusinessStatusConstants.ORDER_STATUS_COMPLETED)
                .ge(CustomerOrderRecord::getOrderTime, previousStart)
                .lt(CustomerOrderRecord::getOrderTime, recentStart);
        BigDecimal previousSpent = customerOrderRecordMapper.selectList(previousWrapper).stream()
                .map(CustomerOrderRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (previousSpent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal changeRatio = recentSpent.subtract(previousSpent)
                    .divide(previousSpent, 2, RoundingMode.HALF_UP);
            if (changeRatio.compareTo(CustomerProfileConstants.SPENDING_TREND_THRESHOLD) > 0) {
                p.setSpendingTrend(CustomerProfileConstants.SPENDING_TREND_INCREASING);
            } else if (changeRatio.compareTo(CustomerProfileConstants.SPENDING_TREND_THRESHOLD.negate()) < 0) {
                p.setSpendingTrend(CustomerProfileConstants.SPENDING_TREND_DECREASING);
            } else {
                p.setSpendingTrend(CustomerProfileConstants.SPENDING_TREND_STABLE);
            }
        } else if (recentSpent.compareTo(BigDecimal.ZERO) > 0) {
            p.setSpendingTrend(CustomerProfileConstants.SPENDING_TREND_INCREASING);
        } else {
            p.setSpendingTrend(CustomerProfileConstants.SPENDING_TREND_STABLE);
        }
    }

    /**
     * 复购率计算：近30天内有过2次以上消费的客户占比
     */
    private void calculateRepurchaseRate(CustomerProfile p, Long userId, LocalDateTime now) {
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LambdaQueryWrapper<CustomerOrderRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerOrderRecord::getUserId, userId)
                .eq(CustomerOrderRecord::getStatus, BusinessStatusConstants.ORDER_STATUS_COMPLETED)
                .ge(CustomerOrderRecord::getOrderTime, thirtyDaysAgo);
        long recentOrders = customerOrderRecordMapper.selectCount(wrapper);

        if (recentOrders >= CustomerProfileConstants.REPURCHASE_MIN_ORDERS) {
            p.setRepurchaseRate(CustomerProfileConstants.REPURCHASE_RATE_FULL);
        } else if (recentOrders == 1) {
            p.setRepurchaseRate(CustomerProfileConstants.REPURCHASE_RATE_HALF);
        } else {
            p.setRepurchaseRate(BigDecimal.ZERO);
        }
    }

    /**
     * LTV估算 = 平均客单价 × 月均消费频次 × 预估留存月数(12)
     */
    private void calculateLtv(CustomerProfile p, LocalDateTime now) {
        if (p.getTotalOrders() > 0 && p.getFirstContactAt() != null) {
            long monthsActive = Math.max(1, ChronoUnit.MONTHS.between(p.getFirstContactAt(), now));
            BigDecimal monthlyAvg = BigDecimal.valueOf(p.getTotalOrders())
                    .divide(BigDecimal.valueOf(monthsActive), 2, RoundingMode.HALF_UP);
            BigDecimal ltv = p.getAvgOrderAmount()
                    .multiply(monthlyAvg)
                    .multiply(BigDecimal.valueOf(12));
            p.setEstimatedLtv(ltv.setScale(2, RoundingMode.HALF_UP));
        }
    }

    /**
     * 满意度趋势：对比最近5次评价和之前5次评价
     */
    private void calculateSatisfactionTrend(CustomerProfile p, Long userId) {
        LambdaQueryWrapper<CustomerOrderRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerOrderRecord::getUserId, userId)
                .isNotNull(CustomerOrderRecord::getRating)
                .orderByDesc(CustomerOrderRecord::getOrderTime)
                .last("LIMIT 10");
        List<CustomerOrderRecord> ratedOrders = customerOrderRecordMapper.selectList(wrapper);

        if (ratedOrders.size() >= 4) {
            int half = ratedOrders.size() / 2;
            double recentAvg = ratedOrders.subList(0, half).stream()
                    .mapToInt(CustomerOrderRecord::getRating).average().orElse(0);
            double olderAvg = ratedOrders.subList(half, ratedOrders.size()).stream()
                    .mapToInt(CustomerOrderRecord::getRating).average().orElse(0);

            if (recentAvg > olderAvg + CustomerProfileConstants.SATISFACTION_TREND_THRESHOLD) {
                p.setSatisfactionTrend(CustomerProfileConstants.SATISFACTION_TREND_IMPROVING);
            } else if (recentAvg < olderAvg - CustomerProfileConstants.SATISFACTION_TREND_THRESHOLD) {
                p.setSatisfactionTrend(CustomerProfileConstants.SATISFACTION_TREND_DECLINING);
            } else {
                p.setSatisfactionTrend(CustomerProfileConstants.SATISFACTION_TREND_STABLE);
            }
        }
    }

    /**
     * 客户生命周期阶段判定
     * NEW: 首次接触7天内
     * ACTIVE: 近14天有交互
     * SILENT: 14-30天无交互
     * CHURNED: 30天以上无交互
     * REACTIVATED: 曾经SILENT/CHURNED后又活跃
     */
    private void calculateLifecycleStage(CustomerProfile p, LocalDateTime now) {
        if (p.getFirstContactAt() == null) {
            p.setLifecycleStage(CustomerProfileConstants.LIFECYCLE_NEW);
            return;
        }

        long daysSinceFirst = ChronoUnit.DAYS.between(p.getFirstContactAt(), now);
        long daysSinceActive = p.getLastActiveAt() != null ?
                ChronoUnit.DAYS.between(p.getLastActiveAt(), now) : daysSinceFirst;

        String currentStage = p.getLifecycleStage();
        String newStage;

        if (daysSinceFirst <= CustomerProfileConstants.LIFECYCLE_NEW_MAX_DAYS && p.getTotalOrders() <= CustomerProfileConstants.LIFECYCLE_NEW_MAX_ORDERS) {
            newStage = CustomerProfileConstants.LIFECYCLE_NEW;
        } else if (daysSinceActive <= CustomerProfileConstants.LIFECYCLE_ACTIVE_MAX_INACTIVE_DAYS) {
            if ((CustomerProfileConstants.LIFECYCLE_SILENT.equals(currentStage) || CustomerProfileConstants.LIFECYCLE_CHURNED.equals(currentStage)) && p.getTotalOrders() > 1) {
                newStage = CustomerProfileConstants.LIFECYCLE_REACTIVATED;
            } else {
                newStage = CustomerProfileConstants.LIFECYCLE_ACTIVE;
            }
        } else if (daysSinceActive <= CustomerProfileConstants.LIFECYCLE_SILENT_MAX_INACTIVE_DAYS) {
            newStage = CustomerProfileConstants.LIFECYCLE_SILENT;
        } else {
            newStage = CustomerProfileConstants.LIFECYCLE_CHURNED;
        }

        p.setLifecycleStage(newStage);
    }

    /**
     * 流失风险评分（0-10分）
     * 基于活跃度、消费频率、满意度、AI依赖度综合评估
     */
    private BigDecimal calculateChurnRisk(CustomerProfile p) {
        double score = 0.0;
        LocalDateTime now = LocalDateTime.now();

        if (p.getLastActiveAt() != null) {
            long daysSinceActive = ChronoUnit.DAYS.between(p.getLastActiveAt(), now);
            if (daysSinceActive > 30) score += 3.5;
            else if (daysSinceActive > 14) score += 2.0;
            else if (daysSinceActive > 7) score += 0.8;
        }

        if (p.getTotalOrders() > 0 && p.getLastOrderAt() != null) {
            long daysSinceOrder = ChronoUnit.DAYS.between(p.getLastOrderAt(), now);
            if (daysSinceOrder > 30) score += 2.5;
            else if (daysSinceOrder > 14) score += 1.2;
        }

        if (p.getSatisfactionScore() != null) {
            if (p.getSatisfactionScore().compareTo(BigDecimal.valueOf(3.0)) < 0) score += 2.0;
            else if (p.getSatisfactionScore().compareTo(BigDecimal.valueOf(4.0)) < 0) score += 0.5;
        }

        if (p.getRefundCount() > 0) score += Math.min(p.getRefundCount() * 0.5, 1.5);

        if (p.getComplaintCount() > 0) score += Math.min(p.getComplaintCount() * 0.5, 1.5);

        if (p.getAiRatio() != null && p.getAiRatio().compareTo(BigDecimal.valueOf(0.8)) > 0) {
            score += 0.5;
        }

        if (CustomerProfileConstants.SPENDING_TREND_DECREASING.equals(p.getSpendingTrend())) score += 1.0;

        return BigDecimal.valueOf(Math.min(score, 10.0)).setScale(1, RoundingMode.HALF_UP);
    }

    private String determineRiskLevel(BigDecimal churnRiskScore) {
        if (churnRiskScore.compareTo(CustomerProfileConstants.RISK_THRESHOLD_HIGH) >= 0) return CustomerProfileConstants.RISK_LEVEL_HIGH;
        if (churnRiskScore.compareTo(CustomerProfileConstants.RISK_THRESHOLD_MEDIUM) >= 0) return CustomerProfileConstants.RISK_LEVEL_MEDIUM;
        return CustomerProfileConstants.RISK_LEVEL_LOW;
    }

    /**
     * 会员等级自动计算
     * 基于累计消费和下单次数双维度
     */
    private String calculateMemberLevel(int totalOrders, BigDecimal totalSpent) {
        if (totalOrders >= CustomerProfileConstants.MEMBER_DIAMOND_ORDERS
                && totalSpent.compareTo(CustomerProfileConstants.MEMBER_DIAMOND_SPENT) >= 0) {
            return CustomerProfileConstants.MEMBER_LEVEL_DIAMOND;
        }
        if (totalOrders >= CustomerProfileConstants.MEMBER_PLATINUM_ORDERS
                && totalSpent.compareTo(CustomerProfileConstants.MEMBER_PLATINUM_SPENT) >= 0) {
            return CustomerProfileConstants.MEMBER_LEVEL_PLATINUM;
        }
        if (totalOrders >= CustomerProfileConstants.MEMBER_GOLD_ORDERS
                && totalSpent.compareTo(CustomerProfileConstants.MEMBER_GOLD_SPENT) >= 0) {
            return CustomerProfileConstants.MEMBER_LEVEL_GOLD;
        }
        if (totalOrders >= CustomerProfileConstants.MEMBER_SILVER_ORDERS
                && totalSpent.compareTo(CustomerProfileConstants.MEMBER_SILVER_SPENT) >= 0) {
            return CustomerProfileConstants.MEMBER_LEVEL_SILVER;
        }
        if (totalOrders >= CustomerProfileConstants.MEMBER_BRONZE_ORDERS
                && totalSpent.compareTo(CustomerProfileConstants.MEMBER_BRONZE_SPENT) >= 0) {
            return CustomerProfileConstants.MEMBER_LEVEL_BRONZE;
        }
        return CustomerProfileConstants.MEMBER_LEVEL_NORMAL;
    }

    /**
     * 需求类型分析（基于交互行为特征推断）
     * EMOTIONAL: 情绪触发多、转人工频繁、偏好晚上/通宵
     * SKILL: 下单意图多、指定游戏、偏好高等级陪玩
     * SOCIAL: 陪玩师多样性高、偏好社交类游戏
     * ENTERTAINMENT: 默认分类
     */
    private void calculateNeedType(CustomerProfile p) {
        List<String> needTags = new ArrayList<>();

        boolean isEmotional = p.getEmotionTriggerCount() > CustomerProfileConstants.NEED_EMOTIONAL_TRIGGER_THRESHOLD
                || (p.getHumanHandoffCount() > CustomerProfileConstants.NEED_EMOTIONAL_HANDOFF_THRESHOLD && CustomerProfileConstants.TIME_SLOT_EVENING.equals(p.getPreferredTimeSlot()))
                || CustomerProfileConstants.TIME_SLOT_ALL_NIGHT.equals(p.getPreferredTimeSlot());

        boolean isSkill = p.getOrderIntentCount() > CustomerProfileConstants.NEED_SKILL_INTENT_THRESHOLD
                || CustomerProfileConstants.ORDER_TYPE_SPECIFIC_GAME.equals(p.getPreferredOrderType());

        boolean isSocial = p.getCompanionDiversity() > CustomerProfileConstants.NEED_SOCIAL_DIVERSITY_THRESHOLD;

        if (isEmotional) {
            p.setPrimaryNeedType(CustomerProfileConstants.NEED_TYPE_EMOTIONAL);
            needTags.add(CustomerProfileConstants.NEED_TAG_EMOTIONAL);
        } else if (isSkill) {
            p.setPrimaryNeedType(CustomerProfileConstants.NEED_TYPE_SKILL);
            needTags.add(CustomerProfileConstants.NEED_TAG_SKILL);
        } else if (isSocial) {
            p.setPrimaryNeedType(CustomerProfileConstants.NEED_TYPE_SOCIAL);
            needTags.add(CustomerProfileConstants.NEED_TAG_SOCIAL);
        } else {
            p.setPrimaryNeedType(CustomerProfileConstants.NEED_TYPE_ENTERTAINMENT);
            needTags.add(CustomerProfileConstants.NEED_TAG_ENTERTAINMENT);
        }

        if (isEmotional && isSkill) needTags.add(CustomerProfileConstants.NEED_TAG_SKILL_EMOTIONAL);
        if (isSocial && isEmotional) needTags.add(CustomerProfileConstants.NEED_TAG_SOCIAL_EMOTIONAL);
        if (p.getTotalOrders() >= CustomerProfileConstants.NEED_TAG_HIGH_FREQUENCY_ORDERS) needTags.add(CustomerProfileConstants.NEED_TAG_HIGH_FREQUENCY);
        if (p.getTotalSpent().compareTo(CustomerProfileConstants.NEED_TAG_HIGH_SPENDING) >= 0) needTags.add("高消费力");
        if (CustomerProfileConstants.ORDER_TYPE_NIGHT_PACKAGE.equals(p.getPreferredOrderType())) needTags.add(CustomerProfileConstants.NEED_TAG_NIGHT_PREFERENCE);
        if (p.getAiRatio() != null && p.getAiRatio().compareTo(CustomerProfileConstants.NEED_TAG_MANUAL_DEPENDENCY_AI_RATIO) < 0) needTags.add(CustomerProfileConstants.NEED_TAG_MANUAL_DEPENDENCY);

        p.setNeedTags(String.join(",", needTags));
    }

    private int calculateActiveDays(CustomerProfile p) {
        if (p.getFirstContactAt() == null) return 0;
        long totalDays = ChronoUnit.DAYS.between(p.getFirstContactAt(), LocalDateTime.now());
        if (totalDays <= 0) return 1;
        double activityRatio = Math.min(1.0, (double) p.getTotalMessages() / (totalDays * 2));
        return Math.max(1, (int) (totalDays * activityRatio));
    }
}
