package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI人格配置实体
 * <p>
 * 对应数据库表 ai_personality_config，存储AI客服的人格配置参数。
 * 支持全局默认配置、游戏特定覆盖配置、俱乐部专属配置三级层次。
 * 运行时按优先级匹配：游戏特定 > 行业适配 > 俱乐部默认 > 系统全局。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_personality_config")
@Table(name = "ai_personality_config", indexes = {
        @Index(name = "idx_pc_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_pc_game_type", columnList = "game_type"),
        @Index(name = "idx_pc_personality_style", columnList = "personality_style"),
        @Index(name = "idx_pc_enabled_priority", columnList = "enabled,priority")
})
public class AiPersonalityConfig extends BaseEntity {

    /** 所属俱乐部配置ID，null表示系统默认配置 */
    @TableField("club_config_id")
    private Long clubConfigId;

    /** 基础风格：PROFESSIONAL-专业型, CASUAL-休闲型, ANCIENT-古风型, SECOND_DIMENSION-二次元 */
    @TableField("personality_style")
    private String personalityStyle;

    /** 行业适配：GENERAL-通用型, FPS-射击类, MOBA-竞技类 */
    @TableField("industry_style")
    private String industryStyle;

    /** 游戏标识（如delta_force、league_of_legends），null表示全局配置，非null表示游戏特定覆盖 */
    @TableField("game_type")
    private String gameType;

    /** 是否为默认配置：1-是，0-否 */
    @TableField("is_default")
    private Boolean isDefault;

    /** 情绪智能等级：BASIC-基础(关键词匹配), ADVANCED-进阶(上下文感知), PREMIUM-高级(情绪预测) */
    @TableField("emotion_intelligence_level")
    private String emotionIntelligenceLevel;

    /** 情绪敏感度(1-10)，值越高越容易触发情绪响应 */
    @TableField("sentiment_sensitivity")
    private Integer sentimentSensitivity;

    /** 是否主动安抚客户：1-是，0-否 */
    @TableField("proactive_comfort")
    private Boolean proactiveComfort;

    /** 称呼客户方式：如"您"、"老板"、"阁下"、"主人" */
    @TableField("address_format")
    private String addressFormat;

    /** AI自称方式：如"我"、"咱"、"小的"、"人家" */
    @TableField("self_address")
    private String selfAddress;

    /** Emoji使用频率(1-5)，1=几乎不用，5=频繁使用 */
    @TableField("emoji_usage")
    private Integer emojiUsage;

    /** 网络用语使用频率(1-5)，1=规范用语，5=大量网络用语 */
    @TableField("slang_usage")
    private Integer slangUsage;

    /** 正式程度(1-5)，1=非常随意，5=非常正式 */
    @TableField("formality_level")
    private Integer formalityLevel;

    /** 欢迎语风格模板 */
    @TableField("greeting_style")
    private String greetingStyle;

    /** 最大回复长度限制（字符数） */
    @TableField("max_reply_length")
    private Integer maxReplyLength;

    /** 是否使用游戏专业术语：1-是，0-否 */
    @TableField("use_game_terminology")
    private Boolean useGameTerminology;

    /** 转化引导风格：DIRECT-直接引导, SOFT-软性引导, NONE-不引导 */
    @TableField("conversion_style")
    private String conversionStyle;

    /** 该人格配置的转化率（用于A/B测试对比） */
    @TableField("conversion_rate")
    private Double conversionRate;

    /** 该人格配置的满意度均分（用于效果评估） */
    @TableField("satisfaction_score")
    private Double satisfactionScore;

    /** 该人格配置下的总对话数 */
    @TableField("total_conversations")
    private Long totalConversations;

    /** 是否启用：1-启用，0-禁用 */
    @TableField("enabled")
    private Integer enabled;

    /** 匹配优先级（数值越大越优先匹配） */
    @TableField("priority")
    private Integer priority;

    /** 配置备注说明 */
    @TableField("remark")
    private String remark;
}
