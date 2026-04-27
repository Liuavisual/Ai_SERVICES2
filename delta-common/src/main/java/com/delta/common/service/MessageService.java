package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.MessageVO;

/**
 * 消息服务接口，管理客户对话消息的存储和查询
 *
 * @author delta
 */
public interface MessageService {

    Page<MessageVO> getMessagePage(Integer pageNum, Integer pageSize, Long userId, String platform, String direction, Boolean isAi, Boolean keywordTriggered, String keyword);
}
