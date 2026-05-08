package com.delta.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.WorkOrderConstants;
import com.delta.common.entity.WorkOrder;
import com.delta.common.entity.WorkOrderRecord;
import com.delta.common.enums.WorkOrderPriorityEnum;
import com.delta.common.mapper.WorkOrderMapper;
import com.delta.common.mapper.WorkOrderRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    /** 每次巡检的最大处理条数 */
    private static final int BATCH_SIZE = 500;

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
            log.warn("工单升级到负责人, orderNo={}", order.getOrderNo());
        } else if (minutes >= priority.getTimeoutMinutes() && order.getEscalationLevel() < 1) {
            order.setEscalationLevel(1);
            order.setReminderCount(order.getReminderCount() + 1);
            workOrderMapper.updateById(order);
            addSystemRecord(order.getId(), "工单处理超时，已发送提醒");
            log.warn("工单超时提醒, orderNo={}", order.getOrderNo());
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
}
