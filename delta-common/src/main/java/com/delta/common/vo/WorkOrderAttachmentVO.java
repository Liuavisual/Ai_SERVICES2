package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkOrderAttachmentVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long workOrderId;
    @JsonIgnore
    private Long recordId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    @ObfuscatedId
    private Long uploadedBy;
}
