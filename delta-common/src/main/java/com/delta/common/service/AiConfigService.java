package com.delta.common.service;

import com.delta.common.dto.AiConfigUpdateDTO;
import com.delta.common.vo.AiConfigVO;

import java.util.List;

/**
 * AI配置服务接口，管理AI模型参数和提示词
 *
 * @author delta
 */
public interface AiConfigService {

    List<AiConfigVO> getAllConfigs();

    void updateConfigs(AiConfigUpdateDTO updateDTO);

    String getConfigValue(String configKey);
}
