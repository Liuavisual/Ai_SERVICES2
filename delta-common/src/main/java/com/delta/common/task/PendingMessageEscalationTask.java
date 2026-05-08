package com.delta.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.PendingMessage;
import com.delta.common.enums.PendingMessageStatusEnum;
import com.delta.common.mapper.PendingMessageMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 待处理消息升级定时任务，超时自动升级并提醒客服负责人
 * <p>
 * 每60秒巡检一次，采用分页查询避免全表扫描。
 * 建议在 pending_messages 表的 (status, deleted, deadline) 字段上建立联合索引以优化查询性能。
 * </p>
 *
 * @author delta
 */
@Component
@RequiredArgsConstructor
public class PendingMessageEscalationTask {

    private static final Logger log = LoggerFactory.getLogger(PendingMessageEscalationTask.class);

    private final PendingMessageMapper pendingMessageMapper;

    private static final int WARNING_SECONDS = 300;

    private static final int ESCALATE_SECONDS = 600;

    /** 每次巡检的最大处理条数 */
    private static final int BATCH_SIZE = 500;

    @Scheduled(fixedRate = 60000)
    public void checkAndEscalate() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int pageNum = 1;
            int escalatedCount = 0;
            int warnedCount = 0;

            while (true) {
                Page<PendingMessage> page = new Page<>(pageNum, BATCH_SIZE);
                LambdaQueryWrapper<PendingMessage> activeWrapper = new LambdaQueryWrapper<>();
                activeWrapper.in(PendingMessage::getStatus,
                        PendingMessageStatusEnum.PENDING.getCode(),
                        PendingMessageStatusEnum.PROCESSING.getCode());
                Page<PendingMessage> pageResult = pendingMessageMapper.selectPage(page, activeWrapper);
                List<PendingMessage> activeMessages = pageResult.getRecords();

                if (activeMessages.isEmpty()) break;

                for (PendingMessage pm : activeMessages) {
                    if (pm.getDeadline() == null) continue;

                    long overSeconds = Duration.between(pm.getDeadline(), now).getSeconds();

                    if (overSeconds > ESCALATE_SECONDS
                            && (pm.getEscalationLevel() == null || pm.getEscalationLevel() < 2)) {
                        escalateToLeader(pm);
                        escalatedCount++;
                    } else if (overSeconds > WARNING_SECONDS && overSeconds <= ESCALATE_SECONDS
                            && (pm.getEscalationLevel() == null || pm.getEscalationLevel() < 1)) {
                        sendWarning(pm);
                        warnedCount++;
                    }
                }

                if (!pageResult.hasNext()) break;
                pageNum++;
            }

            if (escalatedCount > 0 || warnedCount > 0) {
                log.info("【待处理消息巡检】警告{}条，升级上报{}条", warnedCount, escalatedCount);
            }

        } catch (Exception e) {
            log.error("待处理消息升级检查异常", e);
        }
    }

    private void sendWarning(PendingMessage pm) {
        LambdaUpdateWrapper<PendingMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PendingMessage::getId, pm.getId());
        updateWrapper.set(PendingMessage::getEscalationLevel, 1);
        updateWrapper.set(PendingMessage::getReminderCount,
                (pm.getReminderCount() != null ? pm.getReminderCount() : 0) + 1);
        pendingMessageMapper.update(null, updateWrapper);
        log.warn("【超时警告】待处理消息#{} 已超时，请尽快处理 | 客户:{} | 原因:{} | 原始消息:{}",
                pm.getId(), pm.getUserId(), pm.getPendingReason(), pm.getKeyword());
    }

    private void escalateToLeader(PendingMessage pm) {
        LambdaUpdateWrapper<PendingMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PendingMessage::getId, pm.getId());
        updateWrapper.set(PendingMessage::getEscalationLevel, 2);
        updateWrapper.set(PendingMessage::getReminderCount,
                (pm.getReminderCount() != null ? pm.getReminderCount() : 0) + 1);
        pendingMessageMapper.update(null, updateWrapper);
        log.error("【升级上报】待处理消息#{} 已严重超时，已上报客服负责人 | 客户:{} | 平台:{} | 原因:{} | 关键词:{}",
                pm.getId(), pm.getUserId(), pm.getPlatform(), pm.getPendingReason(), pm.getKeyword());
    }
}
