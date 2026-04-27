package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.vo.KeywordVO;

/**
 * 关键词服务接口，管理关键词的增删改查
 *
 * @author delta
 */
public interface KeywordService {

    Page<KeywordVO> getKeywordPage(Integer pageNum, Integer pageSize, String keyword);

    KeywordVO getKeywordById(Long id);

    void createKeyword(KeywordDTO keywordDTO);

    void updateKeyword(KeywordDTO keywordDTO);

    void deleteKeyword(Long id);

    void refreshKeywordTrie();
}
