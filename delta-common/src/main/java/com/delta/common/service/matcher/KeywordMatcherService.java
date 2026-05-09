package com.delta.common.service.matcher;

import java.util.List;

/**
 * 关键词匹配服务接口，实现消息与关键词的Trie树高效匹配
 * <p>
 * 基于Hutool WordTree实现O(n)复杂度的多模式匹配，
 * 相比传统 contains() 遍历的O(n×m)有显著性能提升。
 * </p>
 *
 * @author 刘建国
 */
public interface KeywordMatcherService {

    /**
     * 从文本中匹配所有数据库关键词（O(n)复杂度）
     *
     * @param text 待匹配文本
     * @return 匹配到的关键词列表（按优先级降序），未匹配返回空列表
     */
    List<String> matchKeywords(String text);

    /**
     * 从自定义关键词列表中匹配首个命中项（O(n)复杂度 Trie树匹配）
     * <p>
     * 相比传统 contains() 遍历，关键词数量越多性能优势越明显。
     * 适用于意图检测、情绪识别等固定关键词列表的场景。
     * </p>
     *
     * @param text     待匹配文本
     * @param keywords 自定义关键词列表
     * @return 匹配到的第一个关键词，未匹配返回null
     */
    String matchFirst(String text, List<String> keywords);

    /**
     * 刷新关键词库，从数据库重新加载所有已启用关键词
     */
    void refreshKeywords();
}
