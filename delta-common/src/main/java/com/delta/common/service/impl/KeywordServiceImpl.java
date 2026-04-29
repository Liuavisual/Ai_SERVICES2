package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.entity.Keyword;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.KeywordMapper;
import com.delta.common.service.KeywordService;
import com.delta.common.service.matcher.KeywordMatcherService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.KeywordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关键词服务实现，管理关键词CRUD
 *
 * @author delta
 */
@Service
public class KeywordServiceImpl implements KeywordService {

    private static final Logger log = LoggerFactory.getLogger(KeywordServiceImpl.class);

    @Autowired
    private KeywordMapper keywordMapper;

    @Autowired
    private KeywordMatcherService keywordMatcherService;

    @Override
    public Page<KeywordVO> getKeywordPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<Keyword> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Keyword::getKeyword, keyword);
        }

        wrapper.orderByDesc(Keyword::getPriority)
               .orderByDesc(Keyword::getCreatedAt);

        Page<Keyword> keywordPage = keywordMapper.selectPage(page, wrapper);

        Page<KeywordVO> resultPage = new Page<>(keywordPage.getCurrent(), keywordPage.getSize(), keywordPage.getTotal());
        resultPage.setRecords(BeanUtil.copyToList(keywordPage.getRecords(), KeywordVO.class));
        VoUtils.setRowNumbers(resultPage);

        return resultPage;
    }

    @Override
    public KeywordVO getKeywordById(Long id) {
        Keyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            throw new BusinessException("关键词不存在");
        }
        return BeanUtil.copyProperties(keyword, KeywordVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createKeyword(KeywordDTO keywordDTO) {
        LambdaQueryWrapper<Keyword> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Keyword::getKeyword, keywordDTO.getKeyword());
        if (keywordMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException("关键词已存在");
        }

        Keyword keyword = BeanUtil.copyProperties(keywordDTO, Keyword.class);
        keywordMapper.insert(keyword);
        keywordMatcherService.refreshKeywords();
        log.info("创建关键词成功: {}", keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKeyword(KeywordDTO keywordDTO) {
        Keyword keyword = keywordMapper.selectById(keywordDTO.getId());
        if (keyword == null) {
            throw new BusinessException("关键词不存在");
        }

        LambdaQueryWrapper<Keyword> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Keyword::getKeyword, keywordDTO.getKeyword())
                  .ne(Keyword::getId, keywordDTO.getId());
        if (keywordMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException("关键词已存在");
        }

        BeanUtil.copyProperties(keywordDTO, keyword, "id", "createdAt");
        keywordMapper.updateById(keyword);
        keywordMatcherService.refreshKeywords();
        log.info("更新关键词成功: {}", keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKeyword(Long id) {
        Keyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            throw new BusinessException("关键词不存在");
        }

        keywordMapper.deleteById(id);
        keywordMatcherService.refreshKeywords();
        log.info("删除关键词成功: id={}", id);
    }

    @Override
    public void refreshKeywordTrie() {
        keywordMatcherService.refreshKeywords();
    }
}
