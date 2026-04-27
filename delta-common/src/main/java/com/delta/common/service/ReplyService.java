package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ReplyDTO;
import com.delta.common.vo.ReplyVO;

/**
 * 自动回复服务接口，管理关键词和欢迎语回复规则
 *
 * @author delta
 */
public interface ReplyService {

    Page<ReplyVO> getReplyPage(Integer pageNum, Integer pageSize, String triggerType);

    ReplyVO getReplyById(Long id);

    void createReply(ReplyDTO replyDTO);

    void updateReply(ReplyDTO replyDTO);

    void deleteReply(Long id);

    String getWelcomeReply();

    String getDefaultReply();

    String getKeywordReply(String keyword);
}
