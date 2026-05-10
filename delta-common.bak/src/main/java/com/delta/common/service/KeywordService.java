package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.vo.KeywordVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 关键词服务接口，管理关键词的增删改查
 *
 * @author 刘建国
 */
public interface KeywordService {

    Page<KeywordVO> getKeywordPage(Integer page, Integer size, String keyword);

    KeywordVO getKeywordById(Long id);

    void createKeyword(KeywordDTO keywordDTO);

    void updateKeyword(KeywordDTO keywordDTO);

    void deleteKeyword(Long id);

    void refreshKeywordTrie();

    /**
     * 导出关键词Excel
     *
     * @param response HTTP响应
     * @param keyword  关键词搜索条件
     */
    void exportKeywords(HttpServletResponse response, String keyword);

    /**
     * 导入关键词Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    ImportResultDTO importKeywords(MultipartFile file);
}
