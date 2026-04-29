package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.dto.ReplyDTO;
import com.delta.common.vo.ReplyVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 自动回复服务接口，管理关键词和欢迎语回复规则
 *
 * @author delta
 */
public interface ReplyService {

    Page<ReplyVO> getReplyPage(Integer page, Integer size, String triggerType);

    ReplyVO getReplyById(Long id);

    void createReply(ReplyDTO replyDTO);

    void updateReply(ReplyDTO replyDTO);

    void deleteReply(Long id);

    String getWelcomeReply();

    String getDefaultReply();

    String getKeywordReply(String keyword);

    /**
     * 导出回复话术Excel
     *
     * @param response    HTTP响应
     * @param triggerType 触发类型
     */
    void exportReplies(HttpServletResponse response, String triggerType);

    /**
     * 导入回复话术Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    ImportResultDTO importReplies(MultipartFile file);
}
