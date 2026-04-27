package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.vo.CompanionVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 陪玩师服务接口，管理陪玩师信息和状态
 *
 * @author delta
 */
public interface CompanionService {

    Page<CompanionVO> getPage(Integer pageNum, Integer pageSize, Long levelId, String nickname, Integer enabled);

    List<CompanionVO> getAllEnabled();

    List<CompanionVO> getAvailableByDateAndLevel(LocalDate date, Long levelId);

    CompanionVO getById(Long id);

    void create(CompanionDTO dto);

    void update(CompanionDTO dto);

    void delete(Long id);
}
