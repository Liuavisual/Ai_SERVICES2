package com.delta.common.service;

import com.delta.common.dto.ClubConfigDTO;
import com.delta.common.entity.ClubConfig;
import com.delta.common.vo.ClubConfigVO;

/**
 * 俱乐部配置服务接口，管理俱乐部信息和定价策略
 *
 * @author delta
 */
public interface ClubConfigService {
    ClubConfig getClubConfig();
    ClubConfigVO getClubConfigVO();
    void updateClubConfig(ClubConfigDTO configDTO);
}
