package com.delta.platform.wework.adapter;

import com.delta.common.constant.PlatformConstants;
import com.delta.platform.wework.service.WeWorkApiService;
import com.delta.platform.wework.service.WeWorkMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wework", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WeWorkAdapter {

    private final WeWorkApiService weWorkApiService;
    private final WeWorkMessageService weWorkMessageService;

    public String getPlatform() {
        return PlatformConstants.WEWORK;
    }

    public String sendMessage(String externalUserId, String content) {
        log.info("企业微信发送消息: externalUserId={}", externalUserId);
        try {
            weWorkApiService.sendTextMessage(externalUserId, content);
            return "success";
        } catch (Exception e) {
            log.error("企业微信发送消息失败: externalUserId={}", externalUserId, e);
            return "fail";
        }
    }

    public String processTextMessage(String externalUserId, String content) {
        log.info("企业微信处理文本消息: externalUserId={}", externalUserId);
        return weWorkMessageService.handleTextMessage(externalUserId, content);
    }

    public void processAddContact(String externalUserId, String userId) {
        log.info("企业微信处理添加客户: externalUserId={}, userId={}", externalUserId, userId);
        weWorkMessageService.handleAddExternalContact(externalUserId, userId);
    }
}
