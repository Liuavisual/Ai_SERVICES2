package com.delta.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.CustomerLifecycleConstants;
import com.delta.common.entity.CustomerProfile;
import com.delta.common.entity.CustomerWarningRule;
import com.delta.common.mapper.CustomerProfileMapper;
import com.delta.common.mapper.CustomerWarningRuleMapper;
import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 客户唤醒定时任务，自动扫描AT_RISK客户并执行挽回策略
 * <p>
 * 每30分钟巡检一次，根据 customer_warning_rule 表中配置的规则，
 * 对处于 AT_RISK 流失风险阶段的客户执行自动唤醒动作。
 * 使用Redis记录每日唤醒状态，同一天内不重复唤醒同一客户。
 * </p>
 * <p>
 * 支持的动作类型：
 * <ul>
 *   <li>NOTIFY_CS - 通知客服主动联系客户</li>
 *   <li>SEND_COUPON - 自动发放优惠券激励复购</li>
 *   <li>MARK_VIP - 标记为VIP关怀名单</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Component
@RequiredArgsConstructor
public class CustomerWakeupTask {

    private static final Logger log = LoggerFactory.getLogger(CustomerWakeupTask.class);

    /** 客户画像Mapper */
    private final CustomerProfileMapper customerProfileMapper;

    /** 客户预警规则Mapper */
    private final CustomerWarningRuleMapper customerWarningRuleMapper;

    /** Redis缓存服务，用于唤醒冷却跟踪 */
    private final RedisService redisService;

    /** 日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 定时扫描并唤醒处于 AT_RISK 状态的客户
     * <p>
     * 使用 Redis 分布式锁防止多实例并发执行。
     * 执行流程：
     * 1. 获取分布式锁，失败则跳过本次执行
     * 2. 加载所有已启用的 AT_RISK 预警规则
     * 3. 从缓存中按规则类型获取触达客户列表
     * 4. 根据规则动作类型执行唤醒操作
     * 5. 记录唤醒日志供审计追溯
     * </p>
     */
    @Scheduled(fixedRate = 1800000)
    public void execute() {
        String lockKey = "task:lock:customer_wakeup";
        Boolean locked = redisService.setIfAbsent(lockKey, "1", 300, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return;
        }
        try {
            scanAndWakeupAtRiskCustomers();
        } catch (Throwable t) {
            log.error("【客户唤醒】执行异常", t);
        } finally {
            redisService.delete(lockKey);
        }
    }

    /**
     * 扫描并唤醒风险客户
     */
    private void scanAndWakeupAtRiskCustomers() {
        log.info("【客户唤醒】开始扫描AT_RISK客户...");

        try {
            List<CustomerWarningRule> rules = loadEnabledAtRiskRules();
            if (rules.isEmpty()) {
                log.debug("【客户唤醒】无启用的AT_RISK预警规则，跳过");
                return;
            }

            int totalWoken = 0;
            String todayKey = LocalDate.now().format(DATE_FORMATTER);

            for (CustomerWarningRule rule : rules) {
                int ruleWokenCount = processRule(rule, todayKey);
                totalWoken += ruleWokenCount;
            }

            log.info("【客户唤醒】扫描完成，本次共唤醒 {} 个客户", totalWoken);

        } catch (Exception e) {
            log.error("【客户唤醒】扫描异常", e);
        }
    }

    /**
     * 加载所有启用的AT_RISK预警规则，按优先级降序排列
     *
     * @return 启用的AT_RISK规则列表
     */
    private List<CustomerWarningRule> loadEnabledAtRiskRules() {
        LambdaQueryWrapper<CustomerWarningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerWarningRule::getMonitorStage, CustomerLifecycleConstants.STAGE_AT_RISK);
        wrapper.eq(CustomerWarningRule::getEnabled, 1);
        wrapper.orderByDesc(CustomerWarningRule::getPriority);
        List<CustomerWarningRule> rules = customerWarningRuleMapper.selectList(wrapper);
        log.debug("【客户唤醒】加载AT_RISK预警规则 {} 条", rules.size());
        return rules;
    }

    /**
     * 处理单条唤醒规则
     *
     * @param rule     预警规则
     * @param todayKey 今日日期Key（yyyMMdd）
     * @return 本规则唤醒的客户数量
     */
    private int processRule(CustomerWarningRule rule, String todayKey) {
        int wokenCount = 0;
        int pageNum = 1;

        while (true) {
            Page<CustomerProfile> page = new Page<>(pageNum, CustomerLifecycleConstants.WAKEUP_BATCH_SIZE);
            LambdaQueryWrapper<CustomerProfile> queryWrapper = buildAtRiskQuery();
            Page<CustomerProfile> pageResult = customerProfileMapper.selectPage(page, queryWrapper);
            List<CustomerProfile> atRiskProfiles = pageResult.getRecords();

            if (atRiskProfiles.isEmpty()) {
                break;
            }

            for (CustomerProfile profile : atRiskProfiles) {
                if (tryWakeupCustomer(profile, rule, todayKey)) {
                    wokenCount++;
                }
            }

            if (!pageResult.hasNext()) {
                break;
            }
            pageNum++;
        }

        if (wokenCount > 0) {
            log.info("【客户唤醒】规则「{}」唤醒 {} 个客户 | 动作={} | 优先级={}",
                    rule.getRuleName(), wokenCount, rule.getActionType(), rule.getPriority());
        }

        return wokenCount;
    }

    /**
     * 构建AT_RISK客户查询条件
     *
     * @return LambdaQueryWrapper查询条件
     */
    private LambdaQueryWrapper<CustomerProfile> buildAtRiskQuery() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime atRiskThreshold = now.minusDays(CustomerLifecycleConstants.AT_RISK_DAYS_THRESHOLD);
        LocalDateTime churnedThreshold = now.minusDays(CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD);

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(CustomerProfile::getLastActiveAt);
        wrapper.lt(CustomerProfile::getLastActiveAt, atRiskThreshold);
        wrapper.gt(CustomerProfile::getLastActiveAt, churnedThreshold);
        wrapper.orderByAsc(CustomerProfile::getLastActiveAt);
        return wrapper;
    }

    /**
     * 尝试唤醒单个客户（含冷却检查）
     *
     * @param profile  客户画像
     * @param rule     预警规则
     * @param todayKey 今日日期Key
     * @return true-成功唤醒，false-跳过（冷却中）
     */
    private boolean tryWakeupCustomer(CustomerProfile profile, CustomerWarningRule rule, String todayKey) {
        Long userId = profile.getUserId();
        if (userId == null) {
            return false;
        }

        String cooldownKey = CustomerLifecycleConstants.WAKEUP_KEY_PREFIX + userId + ":" + todayKey;
        Boolean isFirstWakeup = redisService.setIfAbsent(cooldownKey, "1",
                CustomerLifecycleConstants.WAKEUP_COOLDOWN_DAYS, TimeUnit.DAYS);

        if (isFirstWakeup != null && !isFirstWakeup) {
            log.debug("【客户唤醒】客户 {} 今日已唤醒，跳过", userId);
            return false;
        }

        executeWakeupAction(userId, profile, rule);
        return true;
    }

    /**
     * 执行唤醒动作
     * <p>
     * 根据规则配置的动作类型执行对应的唤醒操作。
     * 当前版本以日志记录为主，后续可扩展对接通知系统、优惠券系统等外部服务。
     * </p>
     *
     * @param userId  客户用户ID
     * @param profile 客户画像
     * @param rule    预警规则
     */
    private void executeWakeupAction(Long userId, CustomerProfile profile, CustomerWarningRule rule) {
        String actionType = rule.getActionType();
        String customerInfo = buildCustomerInfo(userId, profile);

        switch (actionType) {
            case CustomerLifecycleConstants.ACTION_NOTIFY_CS:
                log.warn("【客户唤醒-NOTIFY_CS】流失风险客户需客服关注 | userId={} | {} | 规则={} | 最后活跃={} | 消息数={}",
                        userId, customerInfo, rule.getRuleName(),
                        profile.getLastActiveAt(), profile.getTotalMessages());
                break;

            case CustomerLifecycleConstants.ACTION_SEND_COUPON:
                log.warn("【客户唤醒-SEND_COUPON】自动发放优惠券 | userId={} | {} | 规则={} | 优惠券参数={} | 最后活跃={}",
                        userId, customerInfo, rule.getRuleName(),
                        rule.getActionParams(), profile.getLastActiveAt());
                break;

            case CustomerLifecycleConstants.ACTION_MARK_VIP:
                log.warn("【客户唤醒-MARK_VIP】标记VIP关怀名单 | userId={} | {} | 规则={} | VIP参数={} | 总消息数={}",
                        userId, customerInfo, rule.getRuleName(),
                        rule.getActionParams(), profile.getTotalMessages());
                break;

            default:
                log.warn("【客户唤醒-UNKNOWN】未知唤醒动作类型 | userId={} | action={} | 规则={}",
                        userId, actionType, rule.getRuleName());
                break;
        }
    }

    /**
     * 构建客户简要信息用于日志输出
     *
     * @param userId  客户用户ID
     * @param profile 客户画像
     * @return 客户信息摘要文本
     */
    private String buildCustomerInfo(Long userId, CustomerProfile profile) {
        Map<String, Object> info = new HashMap<>(8);
        info.put("总消息数", profile.getTotalMessages());
        info.put("总订单数", profile.getTotalOrders());

        String lifecycleStage = determineLifecycleQuick(profile);
        info.put("生命周期", lifecycleStage);

        if (profile.getRfmSegment() != null) {
            info.put("RFM分群", profile.getRfmSegment());
        }
        if (profile.getRiskLevel() != null) {
            info.put("风险等级", profile.getRiskLevel());
        }

        return info.toString();
    }

    /**
     * 快速判定生命周期阶段（用于日志，不修改数据）
     *
     * @param profile 客户画像
     * @return 生命周期阶段标识
     */
    private String determineLifecycleQuick(CustomerProfile profile) {
        LocalDateTime lastActiveAt = profile.getLastActiveAt();
        if (lastActiveAt == null) {
            return CustomerLifecycleConstants.STAGE_NEW;
        }

        long daysSinceLastActive = java.time.Duration.between(lastActiveAt, LocalDateTime.now()).toDays();

        if (daysSinceLastActive > CustomerLifecycleConstants.CHURNED_DAYS_THRESHOLD) {
            return CustomerLifecycleConstants.STAGE_CHURNED;
        }
        if (daysSinceLastActive > CustomerLifecycleConstants.AT_RISK_DAYS_THRESHOLD) {
            return CustomerLifecycleConstants.STAGE_AT_RISK;
        }

        int totalMessages = profile.getTotalMessages() != null ? profile.getTotalMessages() : 0;
        if (totalMessages > 50) {
            return CustomerLifecycleConstants.STAGE_LOYAL;
        }
        if (totalMessages > 5) {
            return CustomerLifecycleConstants.STAGE_ACTIVE;
        }
        return CustomerLifecycleConstants.STAGE_NEW;
    }
}