package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天测试发送数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "聊天测试发送数据传输对象")
public class ChatTestSendDTO {

    @Schema(description = "客户昵称", example = "测试用户")
    @NotBlank(message = "客户昵称不能为空")
    /** 客户昵称 */    private String customerNickname;

    @Schema(description = "来源平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    @NotBlank(message = "平台不能为空")
    /** 来源平台 */    private String platform;

    @Schema(description = "分配的客服ID", example = "3001")
    /** 分配的客服ID */    private Long csUserId;

    @Schema(description = "消息内容", example = "你好，我想咨询陪玩服务")
    @NotBlank(message = "消息内容不能为空")
    /** 消息内容 */    private String content;

    public String getCustomerNickname() {
        return customerNickname;
    }

    public void setCustomerNickname(String customerNickname) {
        this.customerNickname = customerNickname;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Long getCsUserId() {
        return csUserId;
    }

    public void setCsUserId(Long csUserId) {
        this.csUserId = csUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
