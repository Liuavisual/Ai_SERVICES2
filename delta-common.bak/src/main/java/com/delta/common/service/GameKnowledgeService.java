package com.delta.common.service;

import com.delta.common.entity.GameKnowledge;

import java.util.List;

/**
 * 游戏知识库服务接口
 * <p>
 * 提供三角洲行动（Delta Force）游戏知识库的搜索、分类查询和AI提示词注入功能。
 * 知识库涵盖游戏规则、角色攻略、操作指南和故障排除四大类内容。
 * 通过Redis缓存热点知识，减少数据库查询压力。
 * </p>
 *
 * @author 刘建国
 */
public interface GameKnowledgeService {

    /**
     * 基于关键词的全文搜索
     * <p>
     * 使用MyBatis Plus的LambdaQueryWrapper + like进行模糊匹配，
     * 优先匹配标题(title)和关键词(keywords)字段，其次匹配内容(content)字段。
     * 返回结果按可靠性(reliability)降序排列。
     * </p>
     *
     * @param query 搜索关键词
     * @return 匹配的知识条目列表，无结果返回空列表
     */
    List<GameKnowledge> searchKnowledge(String query);

    /**
     * 按分类搜索知识库
     * <p>
     * 支持的分类包括：游戏规则(GAME_RULES)、角色设定(CHARACTER_GUIDES)、
     * 操作指南(OPERATION_GUIDES)、故障排除(TROUBLESHOOTING)。
     * </p>
     *
     * @param category 知识分类
     * @return 该分类下的知识条目列表，无结果返回空列表
     */
    List<GameKnowledge> searchByCategory(String category);

    /**
     * 获取游戏规则类知识
     * <p>
     * 根据游戏类型(gameType)筛选游戏规则相关内容，
     * 包括模式说明、胜利条件、计分机制等。
     * </p>
     *
     * @param gameType 游戏类型，如"三角洲行动"
     * @return 游戏规则知识列表，无结果返回空列表
     */
    List<GameKnowledge> getGameRules(String gameType);

    /**
     * 获取角色攻略类知识
     * <p>
     * 根据游戏类型(gameType)筛选角色攻略相关内容，
     * 包括各角色技能、属性、定位、推荐搭配等。
     * </p>
     *
     * @param gameType 游戏类型，如"三角洲行动"
     * @return 角色攻略知识列表，无结果返回空列表
     */
    List<GameKnowledge> getCharacterGuides(String gameType);

    /**
     * 获取故障排除指南
     * <p>
     * 根据关键词搜索故障排除类知识，
     * 包括常见连接问题、崩溃修复、账号安全等解决方案。
     * </p>
     *
     * @param keyword 故障关键词，如"崩溃"、"连接失败"
     * @return 故障排除指南列表，无结果返回空列表
     */
    List<GameKnowledge> getTroubleshooting(String keyword);

    /**
     * 将相关知识注入AI提示词
     * <p>
     * 根据用户消息内容，从知识库中匹配最相关的知识条目，
     * 最多注入3条，总长度控制在500 tokens以内。
     * 优先使用Redis缓存热点知识（delta:game:knowledge:hot:{keyword}），TTL 30分钟。
     * </p>
     *
     * @param userMessage 用户消息内容
     * @return 注入用的知识文本，无匹配返回空字符串
     */
    String injectKnowledgeToPrompt(String userMessage);

    /**
     * 获取所有知识分类列表
     * <p>
     * 去重返回数据库中存在的所有分类名称。
     * </p>
     *
     * @return 分类名称列表，无数据返回空列表
     */
    List<String> getAllCategories();
}
