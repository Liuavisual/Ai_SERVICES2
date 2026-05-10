package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 回复话术使用统计实体
 * <p>
 * 对应数据库表 reply_usage_stat，统计各预设回复话术的使用频率和转化效果（P2改进）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("reply_usage_stat")
@Table(name = "reply_usage_stat", indexes = {
        @Index(name = "idx_rus_reply_id", columnList = "reply_id"),
        @Index(name = "idx_rus_date", columnList = "stat_date")
})
public class ReplyUsageStat {

    /** 主键 */
    @TableField("id")
    private Long id;

    /** 回复话术ID */
    @TableField("reply_id")
    private Long replyId;

    /** 统计日期 */
    @TableField("stat_date")
    private java.time.LocalDate statDate;

    /** 使用次数 */
    @TableField("use_count")
    private Long useCount;

    /** 转化次数（使用该话术后用户下单） */
    @TableField("conversion_count")
    private Long conversionCount;
}
