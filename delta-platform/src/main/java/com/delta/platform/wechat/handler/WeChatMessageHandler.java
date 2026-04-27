package com.delta.platform.wechat.handler;

import com.delta.platform.wechat.service.WeChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeChatMessageHandler {

    @Autowired
    private WeChatMessageService weChatMessageService;

    public String handleTextMessage(String fromUser, String content) {
        return weChatMessageService.processTextMessage(fromUser, content);
    }

    public String handleSubscribeEvent(String fromUser) {
        return weChatMessageService.processSubscribeEvent(fromUser);
    }
}
