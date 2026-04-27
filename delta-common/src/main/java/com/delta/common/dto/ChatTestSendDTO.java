package com.delta.common.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 聊天测试发送数据传输对象
 *
 * @author delta
 */
@Data
public class ChatTestSendDTO {

    @NotBlank(message = "客户昵称不能为空")
    /** 客户昵称 */    private String customerNickname;

    @NotBlank(message = "平台不能为空")
    /** 来源平台 */    private String platform;

    /** 分配的客服ID */    private Long csUserId;

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
