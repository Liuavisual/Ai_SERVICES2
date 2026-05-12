package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionScheduleMapper;
import com.delta.common.mapper.OrderMapper;
import com.delta.common.vo.CompanionVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CompanionServiceImplTest {

    @Mock
    private CompanionMapper companionMapper;

    @Mock
    private CompanionLevelMapper companionLevelMapper;

    @Mock
    private CompanionScheduleMapper companionScheduleMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CompanionServiceImpl companionService;

    @Test
    @DisplayName("分页查询陪玩师 - 无过滤条件返回分页结果")
    void getPage_noFilter_shouldReturnPagedResults() {
        Page<Companion> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(companionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CompanionVO> result = companionService.getPage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(companionMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询陪玩师 - 带等级名称填充")
    void getPage_withLevel_shouldPopulateLevelName() {
        Companion companion = new Companion();
        companion.setId(1L);
        companion.setNickname("陪玩师A");
        companion.setLevelId(10L);
        companion.setPrice(new BigDecimal("100"));
        companion.setEnabled(1);
        companion.setCreatedAt(LocalDateTime.now());

        Page<Companion> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(companion));
        mockPage.setTotal(1);
        when(companionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        CompanionLevel level = new CompanionLevel();
        level.setId(10L);
        level.setLevelName("金牌");
        level.setBasePrice(new BigDecimal("80"));
        when(companionLevelMapper.selectByIds(anyList())).thenReturn(List.of(level));

        Page<CompanionVO> result = companionService.getPage(1, 10, 10L, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("金牌", result.getRecords().get(0).getLevelName());
    }

    @Test
    @DisplayName("获取所有启用的陪玩师")
    void getAllEnabled_shouldReturnEnabledCompanions() {
        Companion c = new Companion();
        c.setId(1L);
        c.setNickname("陪玩师A");
        c.setEnabled(1);
        when(companionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c));

        List<CompanionVO> result = companionService.getAllEnabled();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("陪玩师A", result.get(0).getNickname());
    }

    @Test
    @DisplayName("根据ID查询陪玩师 - 不存在抛出异常")
    void getById_notExist_shouldThrow() {
        when(companionMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionService.getById(999L));
    }

    @Test
    @DisplayName("根据ID查询陪玩师 - 正常返回带等级信息")
    void getById_exist_shouldReturnWithLevel() {
        Companion companion = new Companion();
        companion.setId(1L);
        companion.setNickname("陪玩师A");
        companion.setLevelId(10L);
        companion.setPrice(new BigDecimal("100"));
        when(companionMapper.selectById(1L)).thenReturn(companion);

        CompanionLevel level = new CompanionLevel();
        level.setId(10L);
        level.setLevelName("金牌");
        level.setBasePrice(new BigDecimal("80"));
        when(companionLevelMapper.selectById(10L)).thenReturn(level);

        CompanionVO result = companionService.getById(1L);

        assertNotNull(result);
        assertEquals("金牌", result.getLevelName());
        assertEquals(new BigDecimal("100"), result.getDisplayPrice());
    }

    @Test
    @DisplayName("创建陪玩师 - 手机号重复抛出异常")
    void create_duplicatePhone_shouldThrow() {
        CompanionDTO dto = new CompanionDTO();
        dto.setPhone("13800138000");
        dto.setNickname("陪玩师A");
        when(companionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> companionService.create(dto));
    }

    @Test
    @DisplayName("创建陪玩师 - 微信号重复抛出异常")
    void create_duplicateWechat_shouldThrow() {
        CompanionDTO dto = new CompanionDTO();
        dto.setWechat("wx_test");
        dto.setNickname("陪玩师A");
        when(companionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> companionService.create(dto));
    }

    @Test
    @DisplayName("创建陪玩师 - 正常创建")
    void create_normal_shouldInsert() {
        CompanionDTO dto = new CompanionDTO();
        dto.setNickname("陪玩师A");
        dto.setPhone("13800138000");
        when(companionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(companionMapper.insert(any(Companion.class))).thenReturn(1);

        companionService.create(dto);

        verify(companionMapper).insert(any(Companion.class));
    }

    @Test
    @DisplayName("更新陪玩师 - 不存在抛出异常")
    void update_notExist_shouldThrow() {
        CompanionDTO dto = new CompanionDTO();
        dto.setId(999L);
        when(companionMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionService.update(dto));
    }

    @Test
    @DisplayName("删除陪玩师 - 不存在抛出异常")
    void delete_notExist_shouldThrow() {
        when(companionMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionService.delete(999L));
    }

    @Test
    @DisplayName("删除陪玩师 - 正常删除")
    void delete_normal_shouldDelete() {
        Companion companion = new Companion();
        companion.setId(1L);
        when(companionMapper.selectById(1L)).thenReturn(companion);
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(companionMapper.deleteById(1L)).thenReturn(1);

        companionService.delete(1L);

        verify(companionMapper).deleteById(1L);
    }
}
