package com.delta.platform.wechat.handler;

import com.delta.platform.wechat.service.WeChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeChatMessageHandler {

    private final WeChatMessageService weChatMessageService;

    public String handleTextMessage(String fromUser, String content) {
        return weChatMessageService.processTextMessage(fromUser, content);
    }

    public String handleSubscribeEvent(String fromUser) {
        return weChatMessageService.processSubscribeEvent(fromUser);
    }
}
