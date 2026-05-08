package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.AiPersonalityConstants;
import com.delta.common.dto.PersonalityConfigDTO;
import com.delta.common.entity.AiPersonalityConfig;
import com.delta.common.mapper.AiPersonalityConfigMapper;
import com.delta.common.service.PersonalityConfigService;
import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI人格配置服务实现
 * <p>
 * 实现人格配置的完整生命周期管理，采用如下缓存策略：
 * <ol>
 *   <li>配置列表缓存: Redis Hash, TTL=30分钟, Key=delta:personality:club:{clubId}</li>
 *   <li>活跃配置缓存: Redis String, TTL=10分钟, Key=delta:personality:active:{clubId}:{gameType}</li>
 *   <li>提示词缓存: 复用BaseMessageProcessService中的PERSONALITY_PROMPT_CACHE_KEY</li>
 * </ol>
 * 配置变更时主动清除相关缓存。
 * </p>
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class PersonalityConfigServiceImpl implements PersonalityConfigService {

    private static final Logger log = LoggerFactory.getLogger(PersonalityConfigServiceImpl.class);

    /** 活跃人格配置Redis Key前缀 */
    private static final String ACTIVE_CONFIG_CACHE_PREFIX = "delta:personality:active:";

    /** 活跃配置缓存TTL（秒） */
    private static final long ACTIVE_CONFIG_CACHE_TTL_SECONDS = 600L;

    private final AiPersonalityConfigMapper configMapper;

    private final RedisService redisService;

    @Override
    public List<com.delta.common.vo.PersonalityConfigVO> getConfigsByClub(Long clubConfigId) {
        LambdaQueryWrapper<AiPersonalityConfig> wrapper = new LambdaQueryWrapper<>();
        if (clubConfigId != null) {
            wrapper.eq(AiPersonalityConfig::getClubConfigId, clubConfigId);
        } else {
            wrapper.isNull(AiPersonalityConfig::getClubConfigId);
        }
        wrapper.orderByDesc(AiPersonalityConfig::getEnabled);
        wrapper.orderByDesc(AiPersonalityConfig::getPriority);
        wrapper.orderByAsc(AiPersonalityConfig::getId);

        List<AiPersonalityConfig> configs = configMapper.selectList(wrapper);
        return configs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public com.delta.common.vo.PersonalityConfigVO getConfigById(Long id) {
        AiPersonalityConfig config = configMapper.selectById(id);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.delta.common.vo.PersonalityConfigVO createConfig(PersonalityConfigDTO dto) {
        @SuppressWarnings("null")
        PersonalityConfigDTO dtoNonNull = dto;
        AiPersonalityConfig config = new AiPersonalityConfig();
        BeanUtils.copyProperties(dtoNonNull, config);
        if (config.getIsDefault() == null) {
            config.setIsDefault(false);
        }
        if (config.getEnabled() == null) {
            config.setEnabled(1);
        }
        if (config.getPriority() == null) {
            config.setPriority(0);
        }
        config.setTotalConversations(0L);
        configMapper.insert(config);

        clearPersonalityCache();
        log.info("【人格配置】创建成功 | id={} | style={} | gameType={}", config.getId(), config.getPersonalityStyle(), config.getGameType());
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.delta.common.vo.PersonalityConfigVO updateConfig(Long id, PersonalityConfigDTO dto) {
        AiPersonalityConfig existing = configMapper.selectById(id);
        if (existing == null) {
            throw new com.delta.common.exception.BusinessException("人格配置不存在: id=" + id);
        }

        @SuppressWarnings("null")
        PersonalityConfigDTO updateDto = dto;
        BeanUtils.copyProperties(updateDto, existing, "id", "clubConfigId", "createdAt", "conversionRate", "satisfactionScore", "totalConversations");
        configMapper.updateById(existing);

        clearPersonalityCache();
        log.info("【人格配置】更新成功 | id={} | style={} | gameType={}", id, existing.getPersonalityStyle(), existing.getGameType());
        return convertToVO(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        AiPersonalityConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new com.delta.common.exception.BusinessException("人格配置不存在: id=" + id);
        }
        configMapper.deleteById(id);

        clearPersonalityCache();
        log.info("【人格配置】删除成功 | id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleConfig(Long id, Integer enabled) {
        AiPersonalityConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new com.delta.common.exception.BusinessException("人格配置不存在: id=" + id);
        }
        config.setEnabled(enabled);
        configMapper.updateById(config);

        clearPersonalityCache();
        log.info("【人格配置】切换状态 | id={} | enabled={}", id, enabled);
    }

    @Override
    public AiPersonalityConfig getActivePersonality(Long clubConfigId, String gameType) {
        // 先查缓存
        String cacheKey = buildActiveConfigCacheKey(clubConfigId, gameType);
        try {
            @SuppressWarnings("null")
            Object cachedValue = redisService.get(cacheKey);
            Object cached = cachedValue;
            if (cached != null && cached instanceof Long) {
                AiPersonalityConfig config = configMapper.selectById((Long) cached);
                if (config != null && config.getEnabled() == 1) {
                    log.debug("【人格配置】活跃配置缓存命中 | clubId={} | gameType={} | configId={}", clubConfigId, gameType, config.getId());
                    return config;
                }
            }
        } catch (Exception e) {
            log.debug("【人格配置】读取活跃配置缓存失败 | error={}", e.getMessage());
        }

        AiPersonalityConfig matched = doMatchActivePersonality(clubConfigId, gameType);

        // 缓存匹配结果
        if (matched != null) {
            try {
                @SuppressWarnings("null")
                Long configId = matched.getId();
                redisService.set(cacheKey, configId, ACTIVE_CONFIG_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.debug("【人格配置】缓存活跃配置失败 | error={}", e.getMessage());
            }
        }

        return matched;
    }

    /**
     * 实际执行人格配置匹配逻辑
     * <p>
     * 匹配优先级：游戏特定 > 行业适配 > 俱乐部默认 > 系统全局默认
     * </p>
     *
     * @param clubConfigId 俱乐部配置ID
     * @param gameType     游戏类型
     * @return 匹配到的人格配置
     */
    private AiPersonalityConfig doMatchActivePersonality(Long clubConfigId, String gameType) {
        LambdaQueryWrapper<AiPersonalityConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiPersonalityConfig::getEnabled, 1);
        wrapper.orderByDesc(AiPersonalityConfig::getPriority);
        wrapper.orderByDesc(AiPersonalityConfig::getIsDefault);

        List<AiPersonalityConfig> allConfigs = configMapper.selectList(wrapper);

        // 第一优先级：游戏特定配置（gameType精确匹配）
        if (gameType != null && !gameType.isEmpty()) {
            for (AiPersonalityConfig config : allConfigs) {
                if (gameType.equals(config.getGameType())) {
                    log.debug("【人格配置】游戏特定匹配 | gameType={} | configId={} | style={}", gameType, config.getId(), config.getPersonalityStyle());
                    return config;
                }
            }
        }

        // 第二优先级：俱乐部默认配置
        for (AiPersonalityConfig config : allConfigs) {
            if (config.getIsDefault() != null && config.getIsDefault()
                    && (clubConfigId == null || clubConfigId.equals(config.getClubConfigId()))) {
                log.debug("【人格配置】俱乐部默认匹配 | clubId={} | configId={}", clubConfigId, config.getId());
                return config;
            }
        }

        // 第三优先级：系统全局默认配置
        for (AiPersonalityConfig config : allConfigs) {
            if (config.getIsDefault() != null && config.getIsDefault()
                    && config.getClubConfigId() == null
                    && config.getGameType() == null) {
                log.debug("【人格配置】系统全局默认匹配 | configId={}", config.getId());
                return config;
            }
        }

        // 最终兜底：返回第一个启用的配置（任意一个）
        if (!allConfigs.isEmpty()) {
            log.warn("【人格配置】无精确匹配，使用兜底配置 | clubId={} | gameType={} | configId={}", clubConfigId, gameType, allConfigs.get(0).getId());
            return allConfigs.get(0);
        }

        log.warn("【人格配置】无任何可用配置！将使用硬编码默认值");
        return null;
    }

    @Override
    public String buildPersonalityPrompt(AiPersonalityConfig config) {
        if (config == null) {
            return "";
        }

        String personalityStyle = config.getPersonalityStyle() != null ? config.getPersonalityStyle() : AiPersonalityConstants.DEFAULT_PERSONALITY;
        String clubName = "本";
        String basePrompt = AiPersonalityConstants.getSystemPrompt(personalityStyle, clubName);

        StringBuilder sb = new StringBuilder(basePrompt);

        // 添加行业适配特征
        String industryStyle = config.getIndustryStyle();
        if ("FPS".equals(industryStyle)) {
            sb.append("\n你熟悉FPS射击类游戏的专业术语和战术配合，可以使用军事化风格的简洁表达，强调战术意识和团队协作。");
        } else if ("MOBA".equals(industryStyle)) {
            sb.append("\n你熟悉MOBA竞技类游戏的段位体系和团队术语，可以使用竞技化表达，强调个人能力和团队配乐。");
        }

        // 添加通信风格约束
        sb.append("\n\n【通信风格指南】");
        sb.append("\n- 称呼客户：").append(config.getAddressFormat() != null ? config.getAddressFormat() : "您");
        sb.append("\n- 自称：").append(config.getSelfAddress() != null ? config.getSelfAddress() : "我");
        sb.append("\n- Emoji使用频率：").append(config.getEmojiUsage() != null ? config.getEmojiUsage() : 3).append("级(1-5)");
        sb.append("\n- 网络用语频率：").append(config.getSlangUsage() != null ? config.getSlangUsage() : 2).append("级(1-5)");
        sb.append("\n- 正式程度：").append(config.getFormalityLevel() != null ? config.getFormalityLevel() : 4).append("级(1-5)");
        sb.append("\n- 最大回复长度：").append(config.getMaxReplyLength() != null ? config.getMaxReplyLength() : 500).append("字符");

        // 添加情绪智能参数
        if ("ADVANCED".equals(config.getEmotionIntelligenceLevel()) || "PREMIUM".equals(config.getEmotionIntelligenceLevel())) {
            sb.append("\n\n【情绪智能】");
            sb.append("\n- 请密切关注客户的情绪变化，根据情绪状态调整回复的语气和策略");
            sb.append("\n- 当检测到客户不满时，优先表达理解并安抚情绪，再解决问题");
        }
        if ("PREMIUM".equals(config.getEmotionIntelligenceLevel())) {
            sb.append("\n- 高级模式：可以从客户的用词、标点、回复速度预判情绪走势，主动进行情绪干预");
        }

        // 添加转化引导策略
        String conversionStyle = config.getConversionStyle();
        if (!"NONE".equals(conversionStyle)) {
            sb.append("\n\n【转化引导】");
            if ("DIRECT".equals(conversionStyle)) {
                sb.append("\n- 在价格/服务咨询回答后，直接引导客户预约下单");
                sb.append("\n- 使用明确的话术：\"需要预约的话现在就可以帮您安排哦~\"");
            } else {
                sb.append("\n- 在价格/服务咨询回答后，温和地提醒可以预约");
                sb.append("\n- 使用软性话术：\"如需预约可随时联系人工客服~\"");
            }
        }

        return sb.toString();
    }

    @Override
    public void clearPersonalityCache() {
        try {
            // 清除人格提示词缓存（与BaseMessageProcessService共享）
            redisService.delete("delta:ai:personality_prompt");
            log.debug("【人格配置】清除提示词缓存");
        } catch (Exception e) {
            log.warn("【人格配置】清除缓存失败 | error={}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordConversationEffect(Long configId, boolean isConverted, Double satisfactionScore) {
        if (configId == null) return;
        try {
            AiPersonalityConfig config = configMapper.selectById(configId);
            if (config != null) {
                long total = config.getTotalConversations() != null ? config.getTotalConversations() + 1 : 1;
                config.setTotalConversations(total);

                // 更新转化率（加权移动平均）
                if (config.getConversionRate() != null && config.getConversionRate() > 0) {
                    double oldRate = config.getConversionRate();
                    double newRate = oldRate * 0.9 + (isConverted ? 1.0 : 0.0) * 0.1;
                    config.setConversionRate(newRate);
                } else {
                    config.setConversionRate(isConverted ? 1.0 : 0.0);
                }

                // 更新满意度均分（加权移动平均）
                if (satisfactionScore != null) {
                    if (config.getSatisfactionScore() != null && config.getSatisfactionScore() > 0) {
                        double oldScore = config.getSatisfactionScore();
                        double newScore = oldScore * 0.9 + satisfactionScore * 0.1;
                        config.setSatisfactionScore(newScore);
                    } else {
                        config.setSatisfactionScore(satisfactionScore);
                    }
                }

                configMapper.updateById(config);
            }
        } catch (Exception e) {
            log.warn("【人格配置】记录对话效果失败 | configId={} | error={}", configId, e.getMessage());
        }
    }

    /**
     * 将实体转换为VO
     *
     * @param config 人格配置实体
     * @return VO对象
     */
    private com.delta.common.vo.PersonalityConfigVO convertToVO(AiPersonalityConfig config) {
        if (config == null) return null;
        com.delta.common.vo.PersonalityConfigVO vo = new com.delta.common.vo.PersonalityConfigVO();
        BeanUtils.copyProperties(config, vo);

        // 填充中文描述字段
        vo.setPersonalityStyleText(getStyleText(config.getPersonalityStyle()));
        vo.setIndustryStyleText(getIndustryText(config.getIndustryStyle()));
        vo.setEmotionIntelligenceLevelText(getEmotionLevelText(config.getEmotionIntelligenceLevel()));

        return vo;
    }

    /**
     * 获取风格中文描述
     *
     * @param style 风格代码
     * @return 中文描述
     */
    private String getStyleText(String style) {
        if (style == null) return "未知";
        return switch (style) {
            case "PROFESSIONAL" -> "专业商务型";
            case "CASUAL" -> "轻松休闲型";
            case "ANCIENT" -> "古风雅致型";
            case "SECOND_DIMENSION" -> "二次元萌系型";
            default -> style;
        };
    }

    /**
     * 获取行业适配中文描述
     *
     * @param industry 行业代码
     * @return 中文描述
     */
    private String getIndustryText(String industry) {
        if (industry == null) return "通用";
        return switch (industry) {
            case "GENERAL" -> "通用型";
            case "FPS" -> "FPS射击类";
            case "MOBA" -> "MOBA竞技类";
            default -> industry;
        };
    }

    /**
     * 获取情绪智能等级中文描述
     *
     * @param level 等级代码
     * @return 中文描述
     */
    private String getEmotionLevelText(String level) {
        if (level == null) return "基础";
        return switch (level) {
            case "BASIC" -> "基础（关键词匹配）";
            case "ADVANCED" -> "进阶（上下文感知）";
            case "PREMIUM" -> "高级（情绪预测）";
            default -> level;
        };
    }

    /**
     * 构建活跃配置缓存Key
     *
     * @param clubConfigId 俱乐部配置ID
     * @param gameType     游戏类型
     * @return 缓存Key
     */
    private String buildActiveConfigCacheKey(Long clubConfigId, String gameType) {
        String clubPart = clubConfigId != null ? String.valueOf(clubConfigId) : "system";
        String gamePart = gameType != null ? gameType : "global";
        return ACTIVE_CONFIG_CACHE_PREFIX + clubPart + ":" + gamePart;
    }
}
