package com.delta.common.service;

import com.delta.common.dto.PersonalityConfigDTO;
import com.delta.common.entity.AiPersonalityConfig;
import com.delta.common.vo.PersonalityConfigVO;

import java.util.List;

/**
 * AI人格配置服务接口
 * <p>
 * 提供人格配置的完整生命周期管理，包括：
 * <ol>
 *   <li>CRUD操作 - 创建、查询、更新、删除人格配置</li>
 *   <li>运行时匹配 - 根据用户、游戏类型动态选择最佳人格</li>
 *   <li>提示词生成 - 将人格配置转换为AI系统提示词</li>
 *   <li>缓存管理 - 配置变更时的缓存清除</li>
 *   <li>效果追踪 - 记录和统计各人格配置的转化率和满意度</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 */
public interface PersonalityConfigService {

    /**
     * 获取俱乐部所有启用的AI人格配置列表
     *
     * @param clubConfigId 俱乐部配置ID，null表示查询系统默认配置
     * @return 人格配置列表
     */
    List<PersonalityConfigVO> getConfigsByClub(Long clubConfigId);

    /**
     * 根据ID获取单个人格配置
     *
     * @param id 配置ID
     * @return 人格配置VO，不存在返回null
     */
    PersonalityConfigVO getConfigById(Long id);

    /**
     * 创建新的人格配置
     * <p>
     * 创建成功后自动清除相关Redis缓存，确保下次消息处理加载新配置。
     * </p>
     *
     * @param dto 人格配置DTO
     * @return 创建后的VO
     */
    PersonalityConfigVO createConfig(PersonalityConfigDTO dto);

    /**
     * 更新已有的人格配置
     * <p>
     * 更新成功后自动清除相关Redis缓存。
     * </p>
     *
     * @param id  配置ID
     * @param dto 更新数据DTO
     * @return 更新后的VO
     */
    PersonalityConfigVO updateConfig(Long id, PersonalityConfigDTO dto);

    /**
     * 删除人格配置（逻辑删除）
     *
     * @param id 配置ID
     */
    void deleteConfig(Long id);

    /**
     * 启用/禁用人格配置
     *
     * @param id      配置ID
     * @param enabled 是否启用：1-启用，0-禁用
     */
    void toggleConfig(Long id, Integer enabled);

    /**
     * 获取当前活跃的人格配置（运行时匹配）
     * <p>
     * 匹配优先级（从高到低）：
     * <ol>
     *   <li>游戏特定配置（gameType匹配）</li>
     *   <li>行业适配配置（industryStyle匹配）</li>
     *   <li>俱乐部默认配置（isDefault=true）</li>
     *   <li>系统全局默认配置</li>
     * </ol>
     * </p>
     *
     * @param clubConfigId 俱乐部配置ID
     * @param gameType     游戏类型标识（如delta_force）
     * @return 匹配到的人格配置实体，找不到返回系统默认配置
     */
    AiPersonalityConfig getActivePersonality(Long clubConfigId, String gameType);

    /**
     * 构建AI人格系统提示词
     * <p>
     * 将人格配置对象转换为AI可理解的System Prompt文本。
     * 整合基础风格描述、行业适配特征、游戏特定语言、通信风格参数等。
     * </p>
     *
     * @param config 人格配置对象
     * @return 完整的系统提示词文本
     */
    String buildPersonalityPrompt(AiPersonalityConfig config);

    /**
     * 清除人格提示词相关缓存
     * <p>
     * 在配置变更后调用，确保下次消息处理加载最新配置。
     * </p>
     */
    void clearPersonalityCache();

    /**
     * 记录人格配置的对话效果
     * <p>
     * 每次AI对话完成后调用，累计对话数和更新转化率/满意度统计。
     * </p>
     *
     * @param configId      人格配置ID
     * @param isConverted   是否转化为订单
     * @param satisfactionScore 满意度评分（可选，null表示暂无）
     */
    void recordConversationEffect(Long configId, boolean isConverted, Double satisfactionScore);
}
