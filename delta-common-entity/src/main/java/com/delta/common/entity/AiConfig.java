package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI配置实体
 * <p>
 * 对应数据库表 ai_config，存储AI客服系统的键值对配置项，
 * 如模型名称、温度参数、系统提示词等。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_config")
public class AiConfig extends BaseEntity {

    /** 配置键名，如 model_name、temperature、system_prompt */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置类型，用于分组管理 */
    private String configType;

    /** 配置描述说明 */
    private String description;
}
