package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.GameConfigDTO;
import com.delta.common.vo.GameConfigVO;

import java.util.List;

public interface GameConfigService {

    List<GameConfigVO> getByClubId(Long clubConfigId);

    GameConfigVO getById(Long id);

    void create(GameConfigDTO dto);

    void update(GameConfigDTO dto);

    void delete(Long id);

    Page<GameConfigVO> getPage(Integer page, Integer size, Long clubConfigId);
    List<GameConfigVO> getAllEnabled();
}
