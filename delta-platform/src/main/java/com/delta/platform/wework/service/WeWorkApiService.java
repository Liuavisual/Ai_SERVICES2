package com.delta.platform.wework.service;

import java.util.Map;

public interface WeWorkApiService {

    String getAccessToken(String tokenType);

    void sendTextMessage(String externalUserId, String content);

    void sendWelcomeMessage(String externalUserId, String content);

    Map<String, Object> getExternalContact(String externalUserId);

    Map<String, Object> getUserInfo(String userId);
}
