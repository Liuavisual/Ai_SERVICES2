package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.vo.CompanionLevelVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanionLevelServiceImplTest {

    @Mock
    private CompanionLevelMapper companionLevelMapper;

    @InjectMocks
    private CompanionLevelServiceImpl companionLevelService;

    @Test
    @DisplayName("分页查询等级 - 无过滤条件返回分页结果")
    void getPage_noFilter_shouldReturnPagedResults() {
        Page<CompanionLevel> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(companionLevelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CompanionLevelVO> result = companionLevelService.getPage(1, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询等级 - 按等级名称搜索")
    void getPage_withLevelName_shouldApplyFilter() {
        Page<CompanionLevel> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(companionLevelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CompanionLevelVO> result = companionLevelService.getPage(1, 10, "金牌");

        assertNotNull(result);
        verify(companionLevelMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取所有启用等级")
    void getAllEnabled_shouldReturnEnabledLevels() {
        CompanionLevel level = new CompanionLevel();
        level.setId(1L);
        level.setLevelName("金牌");
        level.setEnabled(BusinessStatusConstants.ENABLED_INT);
        when(companionLevelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(level));

        List<CompanionLevelVO> result = companionLevelService.getAllEnabled();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("金牌", result.get(0).getLevelName());
    }

    @Test
    @DisplayName("根据ID查询等级 - 不存在抛出异常")
    void getById_notExist_shouldThrow() {
        when(companionLevelMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionLevelService.getById(999L));
    }

    @Test
    @DisplayName("根据ID查询等级 - 正常返回")
    void getById_exist_shouldReturnLevel() {
        CompanionLevel level = new CompanionLevel();
        level.setId(1L);
        level.setLevelName("金牌");
        level.setLevelCode("GOLD");
        level.setBasePrice(new BigDecimal("100"));
        when(companionLevelMapper.selectById(1L)).thenReturn(level);

        CompanionLevelVO result = companionLevelService.getById(1L);

        assertNotNull(result);
        assertEquals("金牌", result.getLevelName());
        assertEquals("GOLD", result.getLevelCode());
    }

    @Test
    @DisplayName("创建等级 - 等级编码已存在抛出异常")
    void create_duplicateCode_shouldThrow() {
        CompanionLevelDTO dto = new CompanionLevelDTO();
        dto.setLevelCode("GOLD");
        dto.setLevelName("金牌");
        when(companionLevelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> companionLevelService.create(dto));
    }

    @Test
    @DisplayName("创建等级 - 正常创建")
    void create_normal_shouldInsert() {
        CompanionLevelDTO dto = new CompanionLevelDTO();
        dto.setLevelCode("GOLD");
        dto.setLevelName("金牌");
        dto.setBasePrice(new BigDecimal("100"));
        when(companionLevelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(companionLevelMapper.insert(any(CompanionLevel.class))).thenReturn(1);

        companionLevelService.create(dto);

        verify(companionLevelMapper).insert(any(CompanionLevel.class));
    }

    @Test
    @DisplayName("更新等级 - 不存在抛出异常")
    void update_notExist_shouldThrow() {
        CompanionLevelDTO dto = new CompanionLevelDTO();
        dto.setId(999L);
        dto.setLevelCode("GOLD");
        when(companionLevelMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionLevelService.update(dto));
    }

    @Test
    @DisplayName("更新等级 - 新编码与其他重复抛出异常")
    void update_duplicateNewCode_shouldThrow() {
        CompanionLevel existing = new CompanionLevel();
        existing.setId(1L);
        existing.setLevelCode("SILVER");
        when(companionLevelMapper.selectById(1L)).thenReturn(existing);
        when(companionLevelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CompanionLevelDTO dto = new CompanionLevelDTO();
        dto.setId(1L);
        dto.setLevelCode("GOLD");

        assertThrows(BusinessException.class,
                () -> companionLevelService.update(dto));
    }

    @Test
    @DisplayName("更新等级 - 正常更新")
    void update_normal_shouldUpdate() {
        CompanionLevel existing = new CompanionLevel();
        existing.setId(1L);
        existing.setLevelCode("GOLD");
        when(companionLevelMapper.selectById(1L)).thenReturn(existing);
        when(companionLevelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(companionLevelMapper.updateById(any(CompanionLevel.class))).thenReturn(1);

        CompanionLevelDTO dto = new CompanionLevelDTO();
        dto.setId(1L);
        dto.setLevelCode("GOLD");
        dto.setLevelName("金牌升级版");

        companionLevelService.update(dto);

        verify(companionLevelMapper).updateById(any(CompanionLevel.class));
    }

    @Test
    @DisplayName("删除等级 - 不存在抛出异常")
    void delete_notExist_shouldThrow() {
        when(companionLevelMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> companionLevelService.delete(999L));
    }

    @Test
    @DisplayName("删除等级 - 正常删除")
    void delete_normal_shouldDelete() {
        CompanionLevel level = new CompanionLevel();
        level.setId(1L);
        when(companionLevelMapper.selectById(1L)).thenReturn(level);
        when(companionLevelMapper.deleteById(1L)).thenReturn(1);

        companionLevelService.delete(1L);

        verify(companionLevelMapper).deleteById(1L);
    }
}
