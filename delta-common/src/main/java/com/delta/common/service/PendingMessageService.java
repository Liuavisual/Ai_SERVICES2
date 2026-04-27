package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.vo.PendingMessageVO;

/**
 * PendingMessageService
 *
 * @author delta
 */
public interface PendingMessageService {

    Page<PendingMessageVO> getPendingMessagePage(Integer pageNum, Integer pageSize, String status, String platform, String keyword);

    Page<PendingMessageVO> getPendingMessagePage(Integer pageNum, Integer pageSize, String status, String platform, String keyword, Long currentUserId, String currentUserRole);

    void handlePendingMessage(PendingMessageHandleDTO handleDTO);

    void handlePendingMessage(PendingMessageHandleDTO handleDTO, Long currentUserId, String currentUserRole);

    void createPendingMessage(Long messageId, Long userId, String keyword, String messageContent);

    void createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform);

    void createPendingMessage(Long messageId, Long userId, String keyword, String messageContent, String platform, String contextSummary);

    Long getPendingCount();

    Long getPendingCount(Long currentUserId, String currentUserRole);
}
