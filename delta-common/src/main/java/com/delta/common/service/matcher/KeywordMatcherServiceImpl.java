package com.delta.common.service.matcher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.Keyword;
import com.delta.common.mapper.KeywordMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 关键词匹配服务实现，基于模糊匹配算法
 *
 * @author delta
 */
@Service
@RequiredArgsConstructor
public class KeywordMatcherServiceImpl implements KeywordMatcherService {

    private static final Logger log = LoggerFactory.getLogger(KeywordMatcherServiceImpl.class);

    private final KeywordMapper keywordMapper;

    private volatile Map<String, Keyword> keywordMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshKeywords();
    }

    @Override
    public List<String> matchKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> matchedKeywords = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (Map.Entry<String, Keyword> entry : keywordMap.entrySet()) {
            String keyword = entry.getKey().toLowerCase();
            if (lowerText.contains(keyword)) {
                matchedKeywords.add(entry.getKey());
            }
        }

        matchedKeywords.sort((a, b) -> {
            Keyword ka = keywordMap.get(a);
            Keyword kb = keywordMap.get(b);
            return Integer.compare(kb.getPriority(), ka.getPriority());
        });

        return matchedKeywords;
    }

    @Override
    public void refreshKeywords() {
        log.info("开始刷新关键词库...");

        LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Keyword::getEnabled, true);
        wrapper.orderByDesc(Keyword::getPriority);

        List<Keyword> keywords = keywordMapper.selectList(wrapper);

        Map<String, Keyword> newKeywordMap = new ConcurrentHashMap<>();
        for (Keyword keyword : keywords) {
            newKeywordMap.put(keyword.getKeyword(), keyword);
        }

        this.keywordMap = newKeywordMap;
        log.info("关键词库刷新完成，共加载 {} 个关键词", keywords.size());
    }
}
