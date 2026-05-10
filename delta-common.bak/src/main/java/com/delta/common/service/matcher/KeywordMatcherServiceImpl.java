package com.delta.common.service.matcher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.Keyword;
import com.delta.common.mapper.KeywordMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 关键词匹配服务实现，基于Hutool WordTree（Trie树）实现高效多模式匹配
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class KeywordMatcherServiceImpl implements KeywordMatcherService {

    private static final Logger log = LoggerFactory.getLogger(KeywordMatcherServiceImpl.class);

    private final KeywordMapper keywordMapper;

    /** 关键词原始映射（key→Keyword对象），用于获取优先级等属性 */
    private volatile Map<String, Keyword> keywordMap = new ConcurrentHashMap<>();

    /** Hutool WordTree（Trie树），用于O(n)复杂度多模式匹配 */
    private volatile WordTree wordTree = new WordTree();

    @PostConstruct
    public void init() {
        refreshKeywords();
    }

    @Override
    public List<String> matchKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<FoundWord> foundWords = wordTree.matchAllWords(text, -1, true, false);
        if (foundWords.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> matchedKeywords = foundWords.stream()
                .map(FoundWord::getWord)
                .distinct()
                .collect(Collectors.toList());

        matchedKeywords.sort((a, b) -> {
            Keyword ka = keywordMap.get(a);
            Keyword kb = keywordMap.get(b);
            if (ka == null || kb == null) {
                return 0;
            }
            return Integer.compare(kb.getPriority(), ka.getPriority());
        });

        return matchedKeywords;
    }

    @Override
    public String matchFirst(String text, List<String> keywords) {
        if (text == null || text.trim().isEmpty() || keywords == null || keywords.isEmpty()) {
            return null;
        }

        WordTree tempTree = new WordTree();
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isEmpty()) {
                tempTree.addWord(keyword);
            }
        }

        List<FoundWord> foundWords = tempTree.matchAllWords(text, -1, true, false);
        if (foundWords.isEmpty()) {
            return null;
        }

        return foundWords.get(0).getWord();
    }

    @Override
    public void refreshKeywords() {
        log.info("开始刷新关键词库...");

        LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Keyword::getEnabled, true);
        wrapper.orderByDesc(Keyword::getPriority);

        List<Keyword> keywords = keywordMapper.selectList(wrapper);

        Map<String, Keyword> newKeywordMap = new ConcurrentHashMap<>();
        WordTree newWordTree = new WordTree();
        for (Keyword keyword : keywords) {
            newKeywordMap.put(keyword.getKeyword(), keyword);
            newWordTree.addWord(keyword.getKeyword());
        }

        this.keywordMap = newKeywordMap;
        this.wordTree = newWordTree;
        log.info("关键词库刷新完成，共加载 {} 个关键词", keywords.size());
    }
}
