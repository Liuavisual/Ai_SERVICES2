package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.vo.CompanionLevelVO;

import java.util.List;

/**
 * 陪玩师等级服务接口，管理等级体系和定价
 *
 * @author delta
 */
public interface CompanionLevelService {

    Page<CompanionLevelVO> getPage(Integer pageNum, Integer pageSize, String levelName);

    List<CompanionLevelVO> getAllEnabled();

    CompanionLevelVO getById(Long id);

    void create(CompanionLevelDTO dto);

    void update(CompanionLevelDTO dto);

    void delete(Long id);
}
