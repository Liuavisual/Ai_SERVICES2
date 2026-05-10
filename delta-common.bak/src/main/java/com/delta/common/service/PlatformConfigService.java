package com.delta.common.service;

import com.delta.common.dto.PlatformConfigDTO;
import com.delta.common.vo.PlatformConfigVO;

import java.util.List;

/**
 * 平台配置服务接口，管理各平台接入参数
 *
 * @author 刘建国
 */
public interface PlatformConfigService {

    List<PlatformConfigVO> getAllPlatformConfigs();

    PlatformConfigVO getPlatformConfigByPlatform(String platform);

    void updatePlatformConfig(PlatformConfigDTO configDTO);

    boolean isPlatformEnabled(String platform);
}
