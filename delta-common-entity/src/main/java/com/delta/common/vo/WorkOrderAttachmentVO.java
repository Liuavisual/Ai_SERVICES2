package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单附件视图对象")
public class WorkOrderAttachmentVO extends BaseVO {

    @Schema(description = "附件ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "工单ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long workOrderId;

    @JsonIgnore
    private Long recordId;

    @Schema(description = "文件名", example = "screenshot.png")
    private String fileName;

    @Schema(description = "文件路径", example = "/uploads/2026/01/screenshot.png")
    private String filePath;

    @Schema(description = "文件类型", example = "IMAGE", allowableValues = {"IMAGE", "VIDEO", "AUDIO", "DOCUMENT", "OTHER"})
    private String fileType;

    @Schema(description = "文件大小(字节)", example = "1024000")
    private Long fileSize;

    @Schema(description = "上传人ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long uploadedBy;
}
