package com.delta.common.task;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 消息归档定时任务，将超过阈值的旧消息从主表迁移到归档表
 * <p>
 * 每24小时（凌晨3点）执行一次，将创建超过 archive-retention-days 天的消息
 * 从 messages 表迁移到 messages_archive 表，保持主表性能。
 * 采用分批 INSERT...SELECT 策略，单批 2000 条，失败时可回滚。
 * </p>
 *
 * @author 刘建国
 */
@Component
@RequiredArgsConstructor
public class MessageArchiveTask {

    private static final Logger log = LoggerFactory.getLogger(MessageArchiveTask.class);

    /** JDBC模板，用于执行INSERT...SELECT批量迁移 */
    private final JdbcTemplate jdbcTemplate;

    /** 归档保留天数，可通过配置 message.archive.retention-days 覆盖，默认90天 */
    @Value("${message.archive.retention-days:90}")
    private int archiveRetentionDays;

    /** 单批迁移条数 */
    private static final int BATCH_SIZE = 2000;

    /** 单次任务最大迁移批次数（防止一次性迁移过多影响性能） */
    private static final int MAX_BATCHES = 10;

    /** 日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 定时归档旧消息
     * <p>
     * 凌晨3点执行，避开业务高峰期。使用INSERT...SELECT直接完成
     * 整批迁移+删除，避免逐条处理的开销。
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void archiveOldMessages() {
        log.info("【消息归档】开始执行，保留阈值={}天，单批={}条，最大批次={}",
                archiveRetentionDays, BATCH_SIZE, MAX_BATCHES);

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(archiveRetentionDays);
            String thresholdStr = threshold.format(DATE_FORMATTER);

            long totalArchived = 0;
            int batchCount = 0;

            while (batchCount < MAX_BATCHES) {
                long count = countArchivable(thresholdStr);
                if (count == 0) {
                    log.info("【消息归档】无可归档消息，任务完成");
                    break;
                }

                int archived = migrateBatch(thresholdStr);
                if (archived == 0) {
                    log.info("【消息归档】本批无新数据迁移，任务完成");
                    break;
                }

                totalArchived += archived;
                batchCount++;
                log.info("【消息归档】第{}批完成，本批迁移{}条，累计{}条", batchCount, archived, totalArchived);
            }

            if (totalArchived > 0) {
                log.info("【消息归档】本次任务完成，共迁移 {} 条消息到归档表", totalArchived);
            }

        } catch (Exception e) {
            log.error("【消息归档】执行异常，归档操作已回滚", e);
        }
    }

    /**
     * 统计可归档的消息数量
     *
     * @param thresholdStr 时间阈值字符串
     * @return 可归档消息数
     */
    private long countArchivable(String thresholdStr) {
        String countSql = "SELECT COUNT(*) FROM messages WHERE created_at < ?";
        Long count = jdbcTemplate.queryForObject(countSql, Long.class, thresholdStr);
        return count != null ? count : 0;
    }

    /**
     * 执行单批消息迁移 INSERT...SELECT + DELETE
     * <p>
     * 原子操作：先INSERT归档，成功后DELETE原数据。
     * 使用子查询限制迁移范围（基于最早的归档记录ID），避免重复。
     * </p>
     *
     * @param thresholdStr 时间阈值字符串
     * @return 本批实际迁移条数
     */
    private int migrateBatch(String thresholdStr) {
        String insertSql = "INSERT INTO messages_archive ("
                + "id, session_id, user_id, direction, content, content_type, "
                + "is_ai, ai_model, ai_token_count, ai_response_time_ms, "
                + "keyword_triggered, triggered_keyword, cs_user_id, "
                + "emotion_tag, intent_tag, read_status, created_at, updated_at"
                + ") SELECT id, session_id, user_id, direction, content, content_type, "
                + "is_ai, ai_model, ai_token_count, ai_response_time_ms, "
                + "keyword_triggered, triggered_keyword, cs_user_id, "
                + "emotion_tag, intent_tag, read_status, created_at, updated_at "
                + "FROM messages WHERE created_at < ? LIMIT ?";

        int inserted = jdbcTemplate.update(insertSql, thresholdStr, BATCH_SIZE);
        if (inserted == 0) {
            return 0;
        }

        String deleteSql = "DELETE FROM messages WHERE id IN ("
                + "SELECT id FROM messages_archive "
                + "WHERE archived_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR) "
                + "ORDER BY archived_at DESC LIMIT ?)";
        int deleted = jdbcTemplate.update(deleteSql, inserted);

        log.debug("【消息归档】INSERT={}条, DELETE={}条", inserted, deleted);
        return Math.min(inserted, deleted);
    }

    /**
     * 查询归档表数据量（用于监控大盘）
     * <p>
     * 可通过管理端调用此方法获取归档统计。
     * </p>
     *
     * @return 归档消息总数
     */
    public long getArchiveCount() {
        String sql = "SELECT COUNT(*) FROM messages_archive";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }
}