package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.dto.AiConfigUpdateDTO;
import com.delta.common.entity.AiConfig;
import com.delta.common.mapper.AiConfigMapper;
import com.delta.common.service.AiConfigService;
import com.delta.common.vo.AiConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI配置服务实现，管理AI模型参数
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class AiConfigServiceImpl implements AiConfigService {

    private final AiConfigMapper aiConfigMapper;

    @Override
    public List<AiConfigVO> getAllConfigs() {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AiConfig::getId);
        List<AiConfig> list = aiConfigMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigs(AiConfigUpdateDTO updateDTO) {
        for (AiConfigUpdateDTO.ConfigUpdateItem item : updateDTO.getUpdates()) {
            LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiConfig::getConfigKey, item.getConfigKey());
            AiConfig config = aiConfigMapper.selectOne(wrapper);
            if (config != null) {
                config.setConfigValue(item.getConfigValue());
                aiConfigMapper.updateById(config);
            }
        }
    }

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getConfigKey, configKey);
        AiConfig config = aiConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    private AiConfigVO convertToVO(AiConfig config) {
        if (config == null) {
            return null;
        }
        AiConfigVO vo = new AiConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
