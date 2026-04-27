package com.delta.common.service;

import com.delta.common.dto.GameConfigDTO;
import com.delta.common.vo.GameConfigVO;

import java.util.List;

public interface GameConfigService {

    List<GameConfigVO> getByClubId(Long clubConfigId);

    GameConfigVO getById(Long id);

    void create(GameConfigDTO dto);

    void update(GameConfigDTO dto);

    void delete(Long id);
}
