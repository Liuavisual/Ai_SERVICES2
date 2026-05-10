package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.entity.Keyword;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.KeywordMapper;
import com.delta.common.service.matcher.KeywordMatcherService;
import com.delta.common.vo.KeywordVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class KeywordServiceImplTest {

    @Mock
    private KeywordMapper keywordMapper;

    @Mock
    private KeywordMatcherService keywordMatcherService;

    @InjectMocks
    private KeywordServiceImpl keywordService;

    @Test
    @DisplayName("分页查询关键词 - 无过滤条件返回分页结果")
    void getKeywordPage_noFilter_shouldReturnPagedResults() {
        Page<Keyword> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(keywordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<KeywordVO> result = keywordService.getKeywordPage(1, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(keywordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询关键词 - 带关键词搜索")
    void getKeywordPage_withKeyword_shouldApplyFilter() {
        Page<Keyword> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(keywordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<KeywordVO> result = keywordService.getKeywordPage(1, 10, "退款");

        assertNotNull(result);
        verify(keywordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询关键词 - 不存在抛出异常")
    void getKeywordById_notExist_shouldThrow() {
        when(keywordMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> keywordService.getKeywordById(999L));
    }

    @Test
    @DisplayName("根据ID查询关键词 - 正常返回")
    void getKeywordById_exist_shouldReturnKeyword() {
        Keyword keyword = new Keyword();
        keyword.setId(1L);
        keyword.setKeyword("退款");
        keyword.setPriority(10);
        when(keywordMapper.selectById(1L)).thenReturn(keyword);

        KeywordVO result = keywordService.getKeywordById(1L);

        assertNotNull(result);
        assertEquals("退款", result.getKeyword());
    }

    @Test
    @DisplayName("创建关键词 - 关键词已存在抛出异常")
    void createKeyword_duplicate_shouldThrow() {
        KeywordDTO dto = new KeywordDTO();
        dto.setKeyword("退款");
        when(keywordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> keywordService.createKeyword(dto));
    }

    @Test
    @DisplayName("创建关键词 - 正常创建并刷新缓存")
    void createKeyword_normal_shouldInsertAndRefresh() {
        KeywordDTO dto = new KeywordDTO();
        dto.setKeyword("退款");
        dto.setPriority(10);
        when(keywordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(keywordMapper.insert(any(Keyword.class))).thenReturn(1);

        keywordService.createKeyword(dto);

        verify(keywordMapper).insert(any(Keyword.class));
        verify(keywordMatcherService).refreshKeywords();
    }

    @Test
    @DisplayName("更新关键词 - 不存在抛出异常")
    void updateKeyword_notExist_shouldThrow() {
        KeywordDTO dto = new KeywordDTO();
        dto.setId(999L);
        dto.setKeyword("退款");
        when(keywordMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> keywordService.updateKeyword(dto));
    }

    @Test
    @DisplayName("更新关键词 - 新关键词与其他重复抛出异常")
    void updateKeyword_duplicateNewKeyword_shouldThrow() {
        Keyword existing = new Keyword();
        existing.setId(1L);
        existing.setKeyword("退款");
        when(keywordMapper.selectById(1L)).thenReturn(existing);
        when(keywordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        KeywordDTO dto = new KeywordDTO();
        dto.setId(1L);
        dto.setKeyword("投诉");

        assertThrows(BusinessException.class,
                () -> keywordService.updateKeyword(dto));
    }

    @Test
    @DisplayName("更新关键词 - 正常更新并刷新缓存")
    void updateKeyword_normal_shouldUpdateAndRefresh() {
        Keyword existing = new Keyword();
        existing.setId(1L);
        existing.setKeyword("退款");
        when(keywordMapper.selectById(1L)).thenReturn(existing);
        when(keywordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(keywordMapper.updateById(any(Keyword.class))).thenReturn(1);

        KeywordDTO dto = new KeywordDTO();
        dto.setId(1L);
        dto.setKeyword("退款");
        dto.setPriority(20);

        keywordService.updateKeyword(dto);

        verify(keywordMapper).updateById(any(Keyword.class));
        verify(keywordMatcherService).refreshKeywords();
    }

    @Test
    @DisplayName("删除关键词 - 不存在抛出异常")
    void deleteKeyword_notExist_shouldThrow() {
        when(keywordMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> keywordService.deleteKeyword(999L));
    }

    @Test
    @DisplayName("删除关键词 - 正常删除并刷新缓存")
    void deleteKeyword_normal_shouldDeleteAndRefresh() {
        Keyword keyword = new Keyword();
        keyword.setId(1L);
        when(keywordMapper.selectById(1L)).thenReturn(keyword);
        when(keywordMapper.deleteById(1L)).thenReturn(1);

        keywordService.deleteKeyword(1L);

        verify(keywordMapper).deleteById(1L);
        verify(keywordMatcherService).refreshKeywords();
    }

    @Test
    @DisplayName("刷新关键词Trie树")
    void refreshKeywordTrie_shouldCallRefresh() {
        keywordService.refreshKeywordTrie();

        verify(keywordMatcherService).refreshKeywords();
    }
}
