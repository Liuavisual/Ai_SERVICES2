package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单附件实体
 * <p>
 * 对应数据库表 work_order_attachments，存储工单关联的附件信息，
 * 包括文件名、路径、类型、大小、上传人等。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order_attachments")
@Table(name = "work_order_attachments", indexes = {
        @Index(name = "idx_work_order_attachments_work_order_id", columnList = "work_order_id")
})
public class WorkOrderAttachment extends BaseEntity {

    /** 关联工单ID */
    private Long workOrderId;

    /** 关联记录ID */
    private Long recordId;

    /** 文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件类型 */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传人ID */
    private Long uploadedBy;
}
