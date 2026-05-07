package com.delta.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI人格配置视图对象
 * <p>
 * 用于API响应中返回人格配置的完整信息。
 * 相比实体类，VO包含了更适合前端展示的格式化数据。
 * </p>
 *
 * @author 刘建国
 */
@Data
public class PersonalityConfigVO {

    /** 配置主键ID */
    private Long id;

    /** 所属俱乐部配置ID */
    private Long clubConfigId;

    /** 俱乐部名称（冗余展示用） */
    private String clubName;

    /** 基础风格 */
    private String personalityStyle;

    /** 基础风格中文描述 */
    private String personalityStyleText;

    /** 行业适配 */
    private String industryStyle;

    /** 行业适配中文描述 */
    private String industryStyleText;

    /** 游戏标识 */
    private String gameType;

    /** 游戏名称（冗余展示用） */
    private String gameTypeText;

    /** 是否为默认配置 */
    private Boolean isDefault;

    /** 情绪智能等级 */
    private String emotionIntelligenceLevel;

    /** 情绪智能等级中文描述 */
    private String emotionIntelligenceLevelText;

    /** 情绪敏感度(1-10) */
    private Integer sentimentSensitivity;

    /** 是否主动安抚 */
    private Boolean proactiveComfort;

    /** 称呼客户方式 */
    private String addressFormat;

    /** AI自称方式 */
    private String selfAddress;

    /** Emoji使用频率(1-5) */
    private Integer emojiUsage;

    /** 网络用语频率(1-5) */
    private Integer slangUsage;

    /** 正式程度(1-5) */
    private Integer formalityLevel;

    /** 欢迎语风格 */
    private String greetingStyle;

    /** 最大回复长度 */
    private Integer maxReplyLength;

    /** 是否使用游戏术语 */
    private Boolean useGameTerminology;

    /** 转化引导风格 */
    private String conversionStyle;

    /** 转化率 */
    private Double conversionRate;

    /** 满意度均分 */
    private Double satisfactionScore;

    /** 总对话数 */
    private Long totalConversations;

    /** 是否启用 */
    private Integer enabled;

    /** 优先级 */
    private Integer priority;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
