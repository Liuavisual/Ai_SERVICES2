package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.vo.PendingMessageVO;

public interface PendingMessageService {

    Page<PendingMessageVO> getPendingMessagePage(Integer page, Integer size, String status, String platform, String keyword);

    Page<PendingMessageVO> getPendingMessagePage(Integer page, Integer size, String status, String platform, String keyword, Long currentUserId, String currentUserRole);

    PendingMessageVO getPendingMessageById(Long id);

    void handlePendingMessage(PendingMessageHandleDTO handleDTO);

    void handlePendingMessage(PendingMessageHandleDTO handleDTO, Long currentUserId, String currentUserRole);

    boolean createPendingMessage(Long messageId, Long userId, String keyword, String messageContent);

    boolean createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform);

    boolean createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform, String contextSummary);

    Long getPendingCount();

    Long getPendingCount(Long currentUserId, String currentUserRole);
}
