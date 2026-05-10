package com.delta.common.service;

import com.delta.common.dto.ChatTestSendDTO;
import com.delta.common.vo.ChatTestReplyVO;

/**
 * 聊天测试服务接口，处理测试环境下的客户对话
 *
 * @author 刘建国
 */
public interface ChatTestService {

    ChatTestReplyVO sendMessage(ChatTestSendDTO sendDTO);
}
