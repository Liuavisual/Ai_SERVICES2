package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.service.CompanionService;
import com.delta.common.vo.CompanionVO;
import com.delta.common.vo.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanionControllerTest {

    @Mock
    private CompanionService companionService;

    @InjectMocks
    private CompanionController companionController;

    @Test
    @DisplayName("分页查询陪玩师 - 成功返回分页数据")
    void getPage_shouldReturnPagedData() {
        CompanionVO vo = new CompanionVO();
        vo.setId(1L);
        vo.setNickname("小明同学");

        Page<CompanionVO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(vo));

        when(companionService.getPage(anyInt(), anyInt(), any(), any(), any())).thenReturn(page);

        Result<Page<CompanionVO>> result = companionController.getPage(1, 10, null, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
        assertEquals("小明同学", result.getData().getRecords().get(0).getNickname());
    }

    @Test
    @DisplayName("分页查询陪玩师 - 带等级ID过滤")
    void getPage_withLevelIdFilter_shouldReturnFilteredData() {
        Page<CompanionVO> page = new Page<>(1, 10, 0);
        page.setRecords(List.of());

        when(companionService.getPage(anyInt(), anyInt(), eq(1L), any(), any())).thenReturn(page);

        Result<Page<CompanionVO>> result = companionController.getPage(1, 10, 1L, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    @DisplayName("获取所有启用的陪玩师 - 成功返回列表")
    void getAllEnabled_shouldReturnEnabledList() {
        CompanionVO vo = new CompanionVO();
        vo.setId(1L);
        vo.setNickname("小明同学");

        when(companionService.getAllEnabled()).thenReturn(List.of(vo));

        Result<List<CompanionVO>> result = companionController.getAllEnabled();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("小明同学", result.getData().get(0).getNickname());
    }

    @Test
    @DisplayName("获取指定日期和等级的可用陪玩师 - 成功返回列表")
    void getAvailable_shouldReturnAvailableList() {
        LocalDate date = LocalDate.of(2026, 1, 1);

        CompanionVO vo = new CompanionVO();
        vo.setId(1L);
        vo.setNickname("小明同学");

        when(companionService.getAvailableByDateAndLevel(eq(date), eq(1L))).thenReturn(List.of(vo));

        Result<List<CompanionVO>> result = companionController.getAvailable(date, 1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("获取陪玩师详情 - 成功返回详情")
    void getById_shouldReturnCompanionInfo() {
        CompanionVO vo = new CompanionVO();
        vo.setId(1L);
        vo.setNickname("小明同学");

        when(companionService.getById(1L)).thenReturn(vo);

        Result<CompanionVO> result = companionController.getById(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("小明同学", result.getData().getNickname());
    }

    @Test
    @DisplayName("创建陪玩师 - 成功创建")
    void create_withValidData_shouldReturnSuccess() {
        CompanionDTO dto = new CompanionDTO();
        dto.setRealName("王小明");
        dto.setNickname("小明同学");
        dto.setPhone("13800138000");
        dto.setLevelId(1L);
        dto.setPrice(new BigDecimal("88.00"));
        dto.setEnabled(true);

        doNothing().when(companionService).create(any(CompanionDTO.class));

        Result<Void> result = companionController.create(dto);

        assertEquals(200, result.getCode());
        verify(companionService).create(dto);
    }

    @Test
    @DisplayName("更新陪玩师 - 成功更新")
    void update_withValidData_shouldReturnSuccess() {
        CompanionDTO dto = new CompanionDTO();
        dto.setId(1L);
        dto.setRealName("王小明");
        dto.setNickname("小明同学");
        dto.setEnabled(true);

        doNothing().when(companionService).update(any(CompanionDTO.class));

        Result<Void> result = companionController.update(dto);

        assertEquals(200, result.getCode());
        verify(companionService).update(dto);
    }

    @Test
    @DisplayName("删除陪玩师 - 成功删除")
    void delete_withValidId_shouldReturnSuccess() {
        doNothing().when(companionService).delete(anyLong());

        Result<Void> result = companionController.delete(1L);

        assertEquals(200, result.getCode());
        verify(companionService).delete(1L);
    }

    @Test
    @DisplayName("导入陪玩师Excel - 成功导入")
    void importExcel_shouldReturnImportResult() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});

        ImportResultDTO importResult = new ImportResultDTO(10, 2);

        when(companionService.importCompanions(any())).thenReturn(importResult);

        Result<Map<String, Object>> result = companionController.importExcel(file);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(10, result.getData().get("success"));
        assertEquals(2, result.getData().get("fail"));
        assertEquals(12, result.getData().get("total"));
    }
}
