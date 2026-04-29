package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CompanionScheduleDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionSchedule;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionScheduleMapper;
import com.delta.common.vo.CompanionScheduleVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanionScheduleServiceImplTest {

    @Mock
    private CompanionScheduleMapper companionScheduleMapper;

    @Mock
    private CompanionMapper companionMapper;

    @InjectMocks
    private CompanionScheduleServiceImpl companionScheduleService;

    @Test
    @DisplayName("分页查询排班 - 无过滤条件返回分页结果")
    void getPage_noFilter_shouldReturnPagedResults() {
        Page<CompanionSchedule> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(companionScheduleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CompanionScheduleVO> result = companionScheduleService.getPage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询排班 - 填充陪玩师名称")
    void getPage_withData_shouldPopulateCompanionName() {
        CompanionSchedule schedule = new CompanionSchedule();
        schedule.setId(1L);
        schedule.setCompanionId(10L);
        schedule.setScheduleDate(LocalDate.now());
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(12, 0));

        Page<CompanionSchedule> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(schedule));
        mockPage.setTotal(1);
        when(companionScheduleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Companion companion = new Companion();
        companion.setId(10L);
        companion.setRealName("张三");
        companion.setNickname("小三");
        when(companionMapper.selectByIds(anyList())).thenReturn(List.of(companion));

        Page<CompanionScheduleVO> result = companionScheduleService.getPage(1, 10, 10L, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("张三", result.getRecords().get(0).getCompanionName());
        assertEquals("小三", result.getRecords().get(0).getCompanionNickname());
    }

    @Test
    @DisplayName("根据ID查询排班 - 不存在抛出异常")
    void getById_notExist_shouldThrow() {
        when(companionScheduleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.getById(999L));
    }

    @Test
    @DisplayName("根据ID查询排班 - 正常返回带陪玩师信息")
    void getById_exist_shouldReturnWithCompanion() {
        CompanionSchedule schedule = new CompanionSchedule();
        schedule.setId(1L);
        schedule.setCompanionId(10L);
        schedule.setScheduleDate(LocalDate.now());
        when(companionScheduleMapper.selectById(1L)).thenReturn(schedule);

        Companion companion = new Companion();
        companion.setId(10L);
        companion.setRealName("张三");
        companion.setNickname("小三");
        when(companionMapper.selectById(10L)).thenReturn(companion);

        CompanionScheduleVO result = companionScheduleService.getById(1L);

        assertNotNull(result);
        assertEquals("张三", result.getCompanionName());
    }

    @Test
    @DisplayName("创建排班 - 陪玩师ID为空抛出异常")
    void create_nullCompanionId_shouldThrow() {
        CompanionScheduleDTO dto = new CompanionScheduleDTO();

        assertThrows(BusinessException.class,
                () -> companionScheduleService.create(dto));
    }

    @Test
    @DisplayName("创建排班 - 陪玩师不存在抛出异常")
    void create_companionNotExist_shouldThrow() {
        CompanionScheduleDTO dto = new CompanionScheduleDTO();
        dto.setCompanionId(999L);
        when(companionMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.create(dto));
    }

    @Test
    @DisplayName("创建排班 - 排班日期为空抛出异常")
    void create_nullScheduleDate_shouldThrow() {
        CompanionScheduleDTO dto = new CompanionScheduleDTO();
        dto.setCompanionId(1L);
        Companion companion = new Companion();
        companion.setId(1L);
        when(companionMapper.selectById(1L)).thenReturn(companion);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.create(dto));
    }

    @Test
    @DisplayName("创建排班 - 开始时间不早于结束时间抛出异常")
    void create_invalidTimeRange_shouldThrow() {
        CompanionScheduleDTO dto = new CompanionScheduleDTO();
        dto.setCompanionId(1L);
        dto.setScheduleDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalTime.of(14, 0));
        dto.setEndTime(LocalTime.of(10, 0));

        Companion companion = new Companion();
        companion.setId(1L);
        when(companionMapper.selectById(1L)).thenReturn(companion);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.create(dto));
    }

    @Test
    @DisplayName("创建排班 - 时间冲突抛出异常")
    void create_timeConflict_shouldThrow() {
        CompanionScheduleDTO dto = new CompanionScheduleDTO();
        dto.setCompanionId(1L);
        dto.setScheduleDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(12, 0));

        Companion companion = new Companion();
        companion.setId(1L);
        when(companionMapper.selectById(1L)).thenReturn(companion);
        when(companionScheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.create(dto));
    }

    @Test
    @DisplayName("创建排班 - 正常创建")
    void create_normal_shouldInsert() {
        CompanionScheduleDTO dto = new CompanionScheduleDTO();
        dto.setCompanionId(1L);
        dto.setScheduleDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(12, 0));

        Companion companion = new Companion();
        companion.setId(1L);
        when(companionMapper.selectById(1L)).thenReturn(companion);
        when(companionScheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(companionScheduleMapper.insert(any(CompanionSchedule.class))).thenReturn(1);

        companionScheduleService.create(dto);

        verify(companionScheduleMapper).insert(any(CompanionSchedule.class));
    }

    @Test
    @DisplayName("更新排班状态 - 正常更新")
    void updateStatus_normal_shouldUpdate() {
        CompanionSchedule schedule = new CompanionSchedule();
        schedule.setId(1L);
        schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
        when(companionScheduleMapper.selectById(1L)).thenReturn(schedule);
        when(companionScheduleMapper.updateById(any(CompanionSchedule.class))).thenReturn(1);

        companionScheduleService.updateStatus(1L, BusinessStatusConstants.SCHEDULE_STATUS_BOOKED);

        verify(companionScheduleMapper).updateById(any(CompanionSchedule.class));
    }

    @Test
    @DisplayName("删除排班 - 已预约状态抛出异常")
    void delete_bookedStatus_shouldThrow() {
        CompanionSchedule schedule = new CompanionSchedule();
        schedule.setId(1L);
        schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_BOOKED);
        when(companionScheduleMapper.selectById(1L)).thenReturn(schedule);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.delete(1L));
    }

    @Test
    @DisplayName("删除排班 - 正常删除")
    void delete_normal_shouldDelete() {
        CompanionSchedule schedule = new CompanionSchedule();
        schedule.setId(1L);
        schedule.setStatus(BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
        when(companionScheduleMapper.selectById(1L)).thenReturn(schedule);
        when(companionScheduleMapper.deleteById(1L)).thenReturn(1);

        companionScheduleService.delete(1L);

        verify(companionScheduleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("按日期删除排班 - 存在已预约排班抛出异常")
    void deleteByCompanionAndDate_hasBooked_shouldThrow() {
        when(companionScheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> companionScheduleService.deleteByCompanionAndDate(1L, LocalDate.now()));
    }

    @Test
    @DisplayName("按日期删除排班 - 正常删除")
    void deleteByCompanionAndDate_normal_shouldDelete() {
        when(companionScheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(companionScheduleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);

        companionScheduleService.deleteByCompanionAndDate(1L, LocalDate.now());

        verify(companionScheduleMapper).delete(any(LambdaQueryWrapper.class));
    }
}
