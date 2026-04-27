package com.delta.platform.wechat.service;

/**
 * 微信消息服务接口，处理微信公众号消息收发
 *
 * @author delta
 */
public interface WeChatMessageService {

    String processTextMessage(String fromUser, String content);

    String processSubscribeEvent(String fromUser);
}
