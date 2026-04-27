package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private Long userId;
    private String direction;
    private String content;
    private Boolean isAi;
    private Boolean keywordTriggered;
    private String userNickname;
    private String userPlatform;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsAi() {
        return isAi;
    }

    public void setIsAi(Boolean isAi) {
        this.isAi = isAi;
    }

    public Boolean getKeywordTriggered() {
        return keywordTriggered;
    }

    public void setKeywordTriggered(Boolean keywordTriggered) {
        this.keywordTriggered = keywordTriggered;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserPlatform() {
        return userPlatform;
    }

    public void setUserPlatform(String userPlatform) {
        this.userPlatform = userPlatform;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
