package com.delta.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI人格配置数据传输对象
 * <p>
 * 用于前端管理界面创建和更新人格配置时的数据传输。
 * 包含所有可配置的人格维度参数。
 * </p>
 *
 * @author 刘建国
 */
@Data
public class PersonalityConfigDTO {

    /** 所属俱乐部配置ID，null表示系统默认配置 */
    private Long clubConfigId;

    /** 基础风格：PROFESSIONAL / CASUAL / ANCIENT / SECOND_DIMENSION */
    @NotBlank(message = "基础风格不能为空")
    private String personalityStyle;

    /** 行业适配：GENERAL / FPS / MOBA */
    @NotBlank(message = "行业适配不能为空")
    private String industryStyle;

    /** 游戏标识（如delta_force），null=全局配置 */
    @Size(max = 64, message = "游戏标识最长64字符")
    private String gameType;

    /** 是否为默认配置 */
    private Boolean isDefault;

    /** 情绪智能等级：BASIC / ADVANCED / PREMIUM */
    private String emotionIntelligenceLevel;

    /** 情绪敏感度(1-10) */
    @Min(value = 1, message = "敏感度最低为1")
    @Max(value = 10, message = "敏感度最高为10")
    private Integer sentimentSensitivity;

    /** 是否主动安抚 */
    private Boolean proactiveComfort;

    /** 称呼客户方式 */
    @Size(max = 32, message = "称呼方式最长32字符")
    private String addressFormat;

    /** AI自称方式 */
    @Size(max = 32, message = "自称方式最长32字符")
    private String selfAddress;

    /** Emoji使用频率(1-5) */
    @Min(value = 1, message = "emoji频率最低为1")
    @Max(value = 5, message = "emoji频率最高为5")
    private Integer emojiUsage;

    /** 网络用语频率(1-5) */
    @Min(value = 1, message = "网络用语频率最低为1")
    @Max(value = 5, message = "网络用语频率最高为5")
    private Integer slangUsage;

    /** 正式程度(1-5) */
    @Min(value = 1, message = "正式程度最低为1")
    @Max(value = 5, message = "正式程度最高为5")
    private Integer formalityLevel;

    /** 欢迎语风格 */
    @Size(max = 128, message = "欢迎语最长128字符")
    private String greetingStyle;

    /** 最大回复长度 */
    @Min(value = 100, message = "回复长度最少100字符")
    @Max(value = 2000, message = "回复长度最多2000字符")
    private Integer maxReplyLength;

    /** 是否使用游戏术语 */
    private Boolean useGameTerminology;

    /** 转化引导风格：DIRECT / SOFT / NONE */
    private String conversionStyle;

    /** 优先级 */
    private Integer priority;

    /** 备注说明 */
    @Size(max = 255, message = "备注最长255字符")
    private String remark;
}
