package com.delta.common.service.matcher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.Keyword;
import com.delta.common.mapper.KeywordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class KeywordMatcherServiceImplTest {

    @Mock
    private KeywordMapper keywordMapper;

    @InjectMocks
    private KeywordMatcherServiceImpl keywordMatcherService;

    private Keyword keyword1;
    private Keyword keyword2;
    private Keyword keyword3;

    @BeforeEach
    public void setUp() {
        keyword1 = new Keyword();
        keyword1.setId(1L);
        keyword1.setKeyword("陪玩");
        keyword1.setPriority(10);
        keyword1.setEnabled(true);

        keyword2 = new Keyword();
        keyword2.setId(2L);
        keyword2.setKeyword("价格");
        keyword2.setPriority(5);
        keyword2.setEnabled(true);

        keyword3 = new Keyword();
        keyword3.setId(3L);
        keyword3.setKeyword("预约");
        keyword3.setPriority(8);
        keyword3.setEnabled(true);
    }

    @Test
    public void testMatchKeywordsWithNullText() {
        List<String> matchedKeywords = keywordMatcherService.matchKeywords(null);
        assertNotNull(matchedKeywords);
        assertTrue(matchedKeywords.isEmpty());
    }

    @Test
    public void testMatchKeywordsWithEmptyText() {
        List<String> matchedKeywords = keywordMatcherService.matchKeywords("");
        assertNotNull(matchedKeywords);
        assertTrue(matchedKeywords.isEmpty());
    }

    @Test
    public void testMatchKeywordsWithBlankText() {
        List<String> matchedKeywords = keywordMatcherService.matchKeywords("   ");
        assertNotNull(matchedKeywords);
        assertTrue(matchedKeywords.isEmpty());
    }

    @Test
    public void testRefreshKeywords() {
        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1, keyword2, keyword3));

        keywordMatcherService.refreshKeywords();

        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1, keyword2, keyword3));

        keywordMatcherService.refreshKeywords();

        List<String> matchedKeywords = keywordMatcherService.matchKeywords("我想找陪玩，咨询价格，怎么预约");
        assertNotNull(matchedKeywords);
        assertEquals(3, matchedKeywords.size());
    }

    @Test
    public void testMatchKeywordsSingleKeyword() {
        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1));

        keywordMatcherService.refreshKeywords();

        List<String> matchedKeywords = keywordMatcherService.matchKeywords("找个陪玩一起玩游戏");
        assertNotNull(matchedKeywords);
        assertEquals(1, matchedKeywords.size());
        assertEquals("陪玩", matchedKeywords.get(0));
    }

    @Test
    public void testMatchKeywordsMultipleKeywordsSortedByPriority() {
        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1, keyword2, keyword3));

        keywordMatcherService.refreshKeywords();

        List<String> matchedKeywords = keywordMatcherService.matchKeywords("陪玩价格预约");
        assertNotNull(matchedKeywords);
        assertEquals(3, matchedKeywords.size());

        assertEquals("陪玩", matchedKeywords.get(0));
        assertEquals("预约", matchedKeywords.get(1));
        assertEquals("价格", matchedKeywords.get(2));
    }

    @Test
    public void testMatchKeywordsCaseInsensitive() {
        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1));

        keywordMatcherService.refreshKeywords();

        List<String> matchedKeywords = keywordMatcherService.matchKeywords("陪玩");
        assertNotNull(matchedKeywords);
        assertEquals(1, matchedKeywords.size());
    }

    @Test
    public void testMatchKeywordsNoMatch() {
        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1, keyword2, keyword3));

        keywordMatcherService.refreshKeywords();

        List<String> matchedKeywords = keywordMatcherService.matchKeywords("这个文本不包含任何关键词");
        assertNotNull(matchedKeywords);
        assertTrue(matchedKeywords.isEmpty());
    }

    @Test
    public void testMatchKeywordsPartialMatch() {
        when(keywordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(keyword1));

        keywordMatcherService.refreshKeywords();

        List<String> matchedKeywords = keywordMatcherService.matchKeywords("我需要陪玩服务");
        assertNotNull(matchedKeywords);
        assertEquals(1, matchedKeywords.size());
    }
}
