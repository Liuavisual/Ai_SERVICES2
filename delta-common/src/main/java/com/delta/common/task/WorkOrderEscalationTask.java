package com.delta.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderEscalationTask {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderRecordMapper workOrderRecordMapper;

    @Scheduled(fixedRate = 60000)
    public void checkWorkOrderTimeout() {
        List<WorkOrder> activeOrders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>()
                        .in(WorkOrder::getStatus,
                                WorkOrderConstants.STATUS_NEW,
                                WorkOrderConstants.STATUS_PROCESSING,
                                WorkOrderConstants.STATUS_PENDING_CONFIRM));

        for (WorkOrder order : activeOrders) {
            try {
                processTimeout(order);
            } catch (Exception e) {
                log.warn("工单超时检查异常, orderId={}: {}", order.getId(), e.getMessage());
            }
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
