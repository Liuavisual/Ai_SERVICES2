package com.delta.common.service.matcher;

import java.util.List;

/**
 * 关键词匹配服务接口，实现消息与关键词的模糊匹配
 *
 * @author 刘建国
 */
public interface KeywordMatcherService {

    List<String> matchKeywords(String text);

    void refreshKeywords();
}
