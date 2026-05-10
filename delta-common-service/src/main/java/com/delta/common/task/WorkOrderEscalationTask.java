package com.delta.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.WorkOrderConstants;
import com.delta.common.entity.WorkOrder;
import com.delta.common.entity.WorkOrderRecord;
import com.delta.common.enums.WorkOrderPriorityEnum;
import com.delta.common.mapper.WorkOrderMapper;
import com.delta.common.mapper.WorkOrderRecordMapper;
import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 工单升级定时任务，超时自动升级并关闭工单
 * <p>
 * 每60秒巡检一次，采用分页查询避免全表扫描。
 * 建议在 work_orders 表的 (status, deleted, created_at, updated_at) 字段上建立联合索引以优化查询性能。
 * </p>
 *
 * @author 刘建国
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderEscalationTask {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderRecordMapper workOrderRecordMapper;
    private final RedisService redisService;

    /** 每次巡检的最大处理条数 */
    private static final int BATCH_SIZE = 500;

    /** SLA违规Redis Key前缀 */
    private static final String SLA_VIOLATION_KEY_PREFIX = "sla:violation:workorder:";

    /** SLA违规TTL（天） */
    private static final int SLA_VIOLATION_TTL_DAYS = 30;

    /** 日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Scheduled(fixedRate = 60000)
    public void checkWorkOrderTimeout() {
        int pageNum = 1;

        while (true) {
            Page<WorkOrder> page = new Page<>(pageNum, BATCH_SIZE);
            LambdaQueryWrapper<WorkOrder> queryWrapper = new LambdaQueryWrapper<WorkOrder>()
                    .in(WorkOrder::getStatus,
                            WorkOrderConstants.STATUS_NEW,
                            WorkOrderConstants.STATUS_PROCESSING,
                            WorkOrderConstants.STATUS_PENDING_CONFIRM);
            Page<WorkOrder> pageResult = workOrderMapper.selectPage(page, queryWrapper);
            List<WorkOrder> activeOrders = pageResult.getRecords();

            if (activeOrders.isEmpty()) break;

            for (WorkOrder order : activeOrders) {
                try {
                    processTimeout(order);
                } catch (Exception e) {
                    log.warn("工单超时检查异常, orderId={}: {}", order.getId(), e.getMessage());
                }
            }

            if (!pageResult.hasNext()) break;
            pageNum++;
        }
    }

    private void processTimeout(WorkOrder order) {
        if (WorkOrderConstants.STATUS_PENDING_CONFIRM.equals(order.getStatus())) {
            checkConfirmTimeout(order);
            return;
        }
        checkProcessingTimeout(order);
    }

    private void checkConfirmTimeout(WorkOrder order) {
        WorkOrderPriorityEnum priority = WorkOrderPriorityEnum.fromCode(order.getPriority());
        Duration elapsed = Duration.between(order.getUpdatedAt(), LocalDateTime.now());
        if (elapsed.toDays() >= priority.getConfirmDays()) {
            order.setStatus(WorkOrderConstants.STATUS_CLOSED);
            order.setClosedAt(LocalDateTime.now());
            workOrderMapper.updateById(order);
            addSystemRecord(order.getId(), "超时未确认，工单自动关闭");
            log.info("工单超时自动关闭, orderNo={}", order.getOrderNo());
        }
    }

    private void checkProcessingTimeout(WorkOrder order) {
        WorkOrderPriorityEnum priority = WorkOrderPriorityEnum.fromCode(order.getPriority());
        Duration elapsed = Duration.between(order.getCreatedAt(), LocalDateTime.now());
        long minutes = elapsed.toMinutes();

        if (minutes >= priority.getTimeoutMinutes() * 2 && order.getEscalationLevel() < 2) {
            order.setEscalationLevel(2);
            workOrderMapper.updateById(order);
            addSystemRecord(order.getId(), "工单已升级到负责人处理");
            trackSlaViolation(order, "ESCALATED_TO_LEADER", minutes);
            log.warn("【SLA告警】工单严重超时已升级到负责人 | orderNo={} | 已耗时{}分钟 | 阈值={}分钟",
                    order.getOrderNo(), minutes, priority.getTimeoutMinutes());
        } else if (minutes >= priority.getTimeoutMinutes() && order.getEscalationLevel() < 1) {
            order.setEscalationLevel(1);
            order.setReminderCount(order.getReminderCount() + 1);
            workOrderMapper.updateById(order);
            addSystemRecord(order.getId(), "工单处理超时，已发送提醒");
            trackSlaViolation(order, "TIMEOUT_WARNING", minutes);
            log.warn("【SLA告警】工单超时提醒 | orderNo={} | 已耗时{}分钟 | 阈值={}分钟 | 提醒次数={}",
                    order.getOrderNo(), minutes, priority.getTimeoutMinutes(), order.getReminderCount());
        }
    }

    private void addSystemRecord(Long workOrderId, String content) {
        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(workOrderId);
        record.setRecordType(WorkOrderConstants.RECORD_TYPE_SYSTEM_LOG);
        record.setOperatorName("系统");
        record.setContent(content);
        workOrderRecordMapper.insert(record);
    }

    /**
     * 记录SLA违规事件到Redis用于监控大盘统计
     * <p>
     * 使用Hash结构存储单条工单的每次违规，便于后续查询指定工单的历史违规记录。
     * Key格式：sla:violation:workorder:{orderNo}
     * Field格式：{违规类型}_{yyyyMMdd}
     * </p>
     *
     * @param order           工单实体
     * @param violationType   违规类型（TIMEOUT_WARNING / ESCALATED_TO_LEADER）
     * @param elapsedMinutes  已耗时分钟数
     */
    private void trackSlaViolation(WorkOrder order, String violationType, long elapsedMinutes) {
        String violationKey = SLA_VIOLATION_KEY_PREFIX + order.getOrderNo();
        String violationField = violationType + "_" + LocalDate.now().format(DATE_FORMATTER);
        String violationValue = "status=" + order.getStatus()
                + "|minutes=" + elapsedMinutes
                + "|priority=" + order.getPriority()
                + "|assignedCs=" + order.getAssignedCsUserId();

        redisService.hSet(violationKey, violationField, violationValue);
        redisService.expire(violationKey, SLA_VIOLATION_TTL_DAYS, TimeUnit.DAYS);
    }
}
