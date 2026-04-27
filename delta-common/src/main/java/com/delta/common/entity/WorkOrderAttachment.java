package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order_attachments")
public class WorkOrderAttachment extends BaseEntity {

    private Long workOrderId;
    private Long recordId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Long uploadedBy;
}
