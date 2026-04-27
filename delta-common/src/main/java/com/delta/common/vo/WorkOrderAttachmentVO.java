package com.delta.common.vo;

import lombok.Data;

@Data
public class WorkOrderAttachmentVO {

    private Long id;
    private Long workOrderId;
    private Long recordId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Long uploadedBy;
}
