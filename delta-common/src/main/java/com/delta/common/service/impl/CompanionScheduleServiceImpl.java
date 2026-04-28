package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CompanionScheduleDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionSchedule;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionScheduleMapper;
import com.delta.common.service.CompanionScheduleService;
import com.delta.common.vo.CompanionScheduleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompanionScheduleServiceImpl implements CompanionScheduleService {

    private static final Logger log = LoggerFactory.getLogger(CompanionScheduleServiceImpl.class);

    @Autowired
    private CompanionScheduleMapper companionScheduleMapper;

    @Autowired
    private CompanionMapper companionMapper;

    private static final int MAX_BATCH_DAYS = 31;

    @Override
    public Page<CompanionScheduleVO> getPage(Integer pageNum, Integer pageSize, Long companionId, LocalDate scheduleDate, String status) {
        Page<CompanionSchedule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CompanionSchedule> wrapper = new LambdaQueryWrapper<>();

        if (companionId != null) {
            wrapper.eq(CompanionSchedule::getCompanionId, companionId);
        }

        if (scheduleDate != null) {
            wrapper.eq(CompanionSchedule::getScheduleDate, scheduleDate);
        }

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(CompanionSchedule::getStatus, status);
        }

        wrapper.orderByAsc(CompanionSchedule::getScheduleDate)
               .orderByAsc(CompanionSchedule::getStartTime);

        Page<CompanionSchedule> schedulePage = companionScheduleMapper.selectPage(page, wrapper);

        List<Long> companionIds = schedulePage.getRecords().stream()
                .map(CompanionSchedule::getCompanionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Companion> companionMap = companionIds.isEmpty() ? Map.of() :
                companionMapper.selectBatchIds(companionIds).stream()
                        .collect(Collectors.toMap(Companion::getId, c -> c));

        Page<CompanionScheduleVO> resultPage = new Page<>(schedulePage.getCurrent(), schedulePage.getSize(), schedulePage.getTotal());
        List<CompanionScheduleVO> voList = schedulePage.getRecords().stream().map(s -> {
            CompanionScheduleVO vo = BeanUtil.copyProperties(s, CompanionScheduleVO.class);
            Companion companion = companionMap.get(s.getCompanionId());
            if (companion != null) {
                vo.setCompanionName(companion.getRealName());
                vo.setCompanionNickname(companion.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public List<CompanionScheduleVO> getByCompanionAndDate(Long companionId, LocalDate scheduleDate) {
        LambdaQueryWrapper<CompanionSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionSchedule::getCompanionId, companionId);
        wrapper.eq(CompanionSchedule::getScheduleDate, scheduleDate);
        wrapper.orderByAsc(CompanionSchedule::getStartTime);

        List<CompanionSchedule> schedules = companionScheduleMapper.selectList(wrapper);
        return schedules.stream()
                .map(s -> BeanUtil.copyProperties(s, CompanionScheduleVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanionScheduleVO> getByDate(LocalDate scheduleDate) {
        LambdaQueryWrapper<CompanionSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionSchedule::getScheduleDate, scheduleDate);
        wrapper.orderByAsc(CompanionSchedule::getStartTime);

        List<CompanionSchedule> schedules = companionScheduleMapper.selectList(wrapper);

        List<Long> companionIds = schedules.stream()
                .map(CompanionSchedule::getCompanionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Companion> companionMap = companionIds.isEmpty() ? Map.of() :
                companionMapper.selectBatchIds(companionIds).stream()
                        .collect(Collectors.toMap(Companion::getId, c -> c));

        return schedules.stream().map(s -> {
            CompanionScheduleVO vo = BeanUtil.copyProperties(s, CompanionScheduleVO.class);
            Companion companion = companionMap.get(s.getCompanionId());
            if (companion != null) {
                vo.setCompanionName(companion.getRealName());
                vo.setCompanionNickname(companion.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public CompanionScheduleVO getById(Long id) {
        CompanionSchedule schedule = companionScheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("陪玩师时间不存在");
        }

        CompanionScheduleVO vo = BeanUtil.copyProperties(schedule, CompanionScheduleVO.class);
        Companion companion = companionMapper.selectById(schedule.getCompanionId());
        if (companion != null) {
            vo.setCompanionName(companion.getRealName());
            vo.setCompanionNickname(companion.getNickname());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CompanionScheduleDTO dto) {
        if (dto.getCompanionId() == null) {
            throw new BusinessException("陪玩师ID不能为空");
        }
        Companion companion = companionMapper.selectById(dto.getCompanionId());
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }
        if (dto.getScheduleDate() == null) {
            throw new BusinessException("排班日期不能为空");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BusinessException("开始时间和结束时间不能为空");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException("开始时间必须早于结束时间");
        }

        checkTimeConflict(dto.getCompanionId(), dto.getScheduleDate(), dto.getStartTime(), dto.getEndTime(), null);

        CompanionSchedule schedule = BeanUtil.copyProperties(dto, CompanionSchedule.class);
        if (schedule.getStatus() == null) {
            schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
        }
        companionScheduleMapper.insert(schedule);
        log.info("创建陪玩师时间成功: companionId={}, date={}, time={}", schedule.getCompanionId(), schedule.getScheduleDate(), schedule.getTimeSlot());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(Long companionId, LocalDate startDate, LocalDate endDate, List<String> timeSlots) {
        if (companionId == null) {
            throw new BusinessException("陪玩师ID不能为空");
        }
        Companion companion = companionMapper.selectById(companionId);
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException("开始日期不能早于今天");
        }
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysBetween > MAX_BATCH_DAYS) {
            throw new BusinessException("批量创建日期范围不能超过" + MAX_BATCH_DAYS + "天");
        }
        if (timeSlots == null || timeSlots.isEmpty()) {
            throw new BusinessException("批量创建时必须指定时间段列表，或使用时间范围接口");
        }

        List<CompanionSchedule> existingSchedules = getExistingSchedules(companionId, startDate, endDate);
        Set<String> existingKeys = existingSchedules.stream()
                .map(s -> s.getScheduleDate() + "|" + s.getStartTime() + "|" + s.getEndTime())
                .collect(Collectors.toSet());

        List<CompanionSchedule> toInsert = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            for (String timeSlot : timeSlots) {
                try {
                    String[] times = timeSlot.split("-");
                    if (times.length == 2) {
                        LocalTime startTime = LocalTime.parse(times[0] + ":00");
                        LocalTime endTime = LocalTime.parse(times[1] + ":00");
                        String key = currentDate + "|" + startTime + "|" + endTime;

                        if (existingKeys.contains(key)) {
                            log.debug("跳过已存在的排班: companionId={}, date={}, slot={}", companionId, currentDate, timeSlot);
                            continue;
                        }

                        CompanionSchedule schedule = new CompanionSchedule();
                        schedule.setCompanionId(companionId);
                        schedule.setScheduleDate(currentDate);
                        schedule.setTimeSlot(timeSlot);
                        schedule.setStartTime(startTime);
                        schedule.setEndTime(endTime);
                        schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
                        toInsert.add(schedule);
                    }
                } catch (Exception e) {
                    log.warn("解析时间段失败: {}", timeSlot, e);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        for (CompanionSchedule schedule : toInsert) {
            companionScheduleMapper.insert(schedule);
        }

        log.info("批量创建陪玩师时间成功: companionId={}, total={}, inserted={}, skipped={}",
                companionId, toInsert.size() + (existingKeys.size()), toInsert.size(), existingKeys.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CompanionScheduleDTO dto) {
        CompanionSchedule schedule = companionScheduleMapper.selectById(dto.getId());
        if (schedule == null) {
            throw new BusinessException("陪玩师时间不存在");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                throw new BusinessException("开始时间必须早于结束时间");
            }
            Long cid = dto.getCompanionId() != null ? dto.getCompanionId() : schedule.getCompanionId();
            LocalDate date = dto.getScheduleDate() != null ? dto.getScheduleDate() : schedule.getScheduleDate();
            checkTimeConflict(cid, date, dto.getStartTime(), dto.getEndTime(), dto.getId());
        }

        BeanUtil.copyProperties(dto, schedule, "id", "createdAt");
        companionScheduleMapper.updateById(schedule);
        log.info("更新陪玩师时间成功: id={}", schedule.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        CompanionSchedule schedule = companionScheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("陪玩师时间不存在");
        }

        if (BusinessStatusConstants.SCHEDULE_STATUS_BOOKED.equals(schedule.getStatus()) && BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE.equals(status)) {
            log.info("将已预约状态改回可预约: id={}", id);
        }

        schedule.setStatus(status);
        companionScheduleMapper.updateById(schedule);
        log.info("更新陪玩师时间状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CompanionSchedule schedule = companionScheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("陪玩师时间不存在");
        }

        if (BusinessStatusConstants.SCHEDULE_STATUS_BOOKED.equals(schedule.getStatus())) {
            throw new BusinessException("已预约的排班不能删除，请先取消预约或联系客户");
        }

        companionScheduleMapper.deleteById(id);
        log.info("删除陪玩师时间成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCompanionAndDate(Long companionId, LocalDate scheduleDate) {
        LambdaQueryWrapper<CompanionSchedule> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(CompanionSchedule::getCompanionId, companionId);
        checkWrapper.eq(CompanionSchedule::getScheduleDate, scheduleDate);
        checkWrapper.eq(CompanionSchedule::getStatus, BusinessStatusConstants.SCHEDULE_STATUS_BOOKED);

        Long bookedCount = companionScheduleMapper.selectCount(checkWrapper);
        if (bookedCount > 0) {
            throw new BusinessException("该日期存在" + bookedCount + "条已预约排班，请先处理后再清空");
        }

        LambdaQueryWrapper<CompanionSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionSchedule::getCompanionId, companionId);
        wrapper.eq(CompanionSchedule::getScheduleDate, scheduleDate);

        int count = companionScheduleMapper.delete(wrapper);
        log.info("删除陪玩师指定日期时间成功: companionId={}, date={}, count={}", companionId, scheduleDate, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTimeRange(Long companionId, LocalDate scheduleDate, LocalTime rangeStart, LocalTime rangeEnd) {
        if (companionId == null) {
            throw new BusinessException("陪玩师ID不能为空");
        }
        Companion companion = companionMapper.selectById(companionId);
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }
        if (scheduleDate == null) {
            throw new BusinessException("排班日期不能为空");
        }
        if (rangeStart == null || rangeEnd == null) {
            throw new BusinessException("开始时间和结束时间不能为空");
        }
        if (!rangeStart.isBefore(rangeEnd)) {
            throw new BusinessException("开始时间必须早于结束时间");
        }

        checkTimeConflict(companionId, scheduleDate, rangeStart, rangeEnd, null);

        CompanionSchedule schedule = new CompanionSchedule();
        schedule.setCompanionId(companionId);
        schedule.setScheduleDate(scheduleDate);
        String timeSlotStr = formatTimeSlot(rangeStart, rangeEnd);
        schedule.setTimeSlot(timeSlotStr);
        schedule.setStartTime(rangeStart);
        schedule.setEndTime(rangeEnd);
        schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
        companionScheduleMapper.insert(schedule);
        log.info("创建自由时间段成功: companionId={}, date={}, time={}", companionId, scheduleDate, timeSlotStr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTimeRangeBatch(Long companionId, LocalDate startDate, LocalDate endDate, LocalTime dailyStart, LocalTime dailyEnd) {
        if (companionId == null) {
            throw new BusinessException("陪玩师ID不能为空");
        }
        Companion companion = companionMapper.selectById(companionId);
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (dailyStart == null || dailyEnd == null) {
            throw new BusinessException("每日开始和结束时间不能为空");
        }
        if (!dailyStart.isBefore(dailyEnd)) {
            throw new BusinessException("每日开始时间必须早于结束时间");
        }
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysBetween > MAX_BATCH_DAYS) {
            throw new BusinessException("批量创建日期范围不能超过" + MAX_BATCH_DAYS + "天");
        }

        List<CompanionSchedule> existingSchedules = getExistingSchedules(companionId, startDate, endDate);
        Set<String> existingKeys = existingSchedules.stream()
                .map(s -> s.getScheduleDate() + "|" + s.getStartTime() + "|" + s.getEndTime())
                .collect(Collectors.toSet());

        List<CompanionSchedule> toInsert = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String key = currentDate + "|" + dailyStart + "|" + dailyEnd;
            if (!existingKeys.contains(key)) {
                checkTimeConflict(companionId, currentDate, dailyStart, dailyEnd, null);

                CompanionSchedule schedule = new CompanionSchedule();
                schedule.setCompanionId(companionId);
                schedule.setScheduleDate(currentDate);
                schedule.setTimeSlot(formatTimeSlot(dailyStart, dailyEnd));
                schedule.setStartTime(dailyStart);
                schedule.setEndTime(dailyEnd);
                schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
                toInsert.add(schedule);
            } else {
                log.debug("跳过已存在的排班: companionId={}, date={}", companionId, currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        for (CompanionSchedule schedule : toInsert) {
            companionScheduleMapper.insert(schedule);
        }

        log.info("批量创建自由时间段成功: companionId={}, days={}, inserted={}, skipped={}",
            companionId, daysBetween, toInsert.size(), existingKeys.size());
    }

    private String formatTimeSlot(LocalTime start, LocalTime end) {
        return start.toString() + "-" + end.toString();
    }

    private void checkTimeConflict(Long companionId, LocalDate scheduleDate, LocalTime startTime, LocalTime endTime, Long excludeId) {
        LambdaQueryWrapper<CompanionSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionSchedule::getCompanionId, companionId);
        wrapper.eq(CompanionSchedule::getScheduleDate, scheduleDate);
        wrapper.lt(CompanionSchedule::getStartTime, endTime);
        wrapper.gt(CompanionSchedule::getEndTime, startTime);
        if (excludeId != null) {
            wrapper.ne(CompanionSchedule::getId, excludeId);
        }

        Long conflictCount = companionScheduleMapper.selectCount(wrapper);
        if (conflictCount > 0) {
            throw new BusinessException("该时间段与已有排班冲突，请检查后重试");
        }
    }

    private List<CompanionSchedule> getExistingSchedules(Long companionId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CompanionSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionSchedule::getCompanionId, companionId);
        wrapper.ge(CompanionSchedule::getScheduleDate, startDate);
        wrapper.le(CompanionSchedule::getScheduleDate, endDate);
        return companionScheduleMapper.selectList(wrapper);
    }
}
