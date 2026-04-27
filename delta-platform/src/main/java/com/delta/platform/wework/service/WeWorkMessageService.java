package com.delta.platform.wework.service;

import com.delta.common.dto.WeWorkCallbackDTO;

public interface WeWorkMessageService {

    void handleCallback(WeWorkCallbackDTO callbackDTO);

    String handleTextMessage(String externalUserId, String content);

    void handleAddExternalContact(String externalUserId, String userId);

    void handleDelExternalContact(String externalUserId);
}
