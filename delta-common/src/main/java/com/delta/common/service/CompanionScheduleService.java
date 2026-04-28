package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionScheduleDTO;
import com.delta.common.vo.CompanionScheduleVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 陪玩师排班服务接口，管理排班和预约状态
 *
 * @author delta
 */
public interface CompanionScheduleService {

    Page<CompanionScheduleVO> getPage(Integer pageNum, Integer pageSize, Long companionId, LocalDate scheduleDate, String status);

    List<CompanionScheduleVO> getByCompanionAndDate(Long companionId, LocalDate scheduleDate);

    List<CompanionScheduleVO> getByDate(LocalDate scheduleDate);

    CompanionScheduleVO getById(Long id);

    void create(CompanionScheduleDTO dto);

    void createBatch(Long companionId, LocalDate startDate, LocalDate endDate, List<String> timeSlots);

    void createTimeRange(Long companionId, LocalDate scheduleDate, LocalTime rangeStart, LocalTime rangeEnd);

    void createTimeRangeBatch(Long companionId, LocalDate startDate, LocalDate endDate, LocalTime dailyStart, LocalTime dailyEnd);

    void update(CompanionScheduleDTO dto);

    void updateStatus(Long id, String status);

    void delete(Long id);

    void deleteByCompanionAndDate(Long companionId, LocalDate scheduleDate);
}
