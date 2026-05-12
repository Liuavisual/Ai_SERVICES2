package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.FaqItemDTO;
import com.delta.common.entity.FaqItem;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.FaqItemMapper;
import com.delta.common.vo.FaqItemVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class FaqItemServiceImplTest {

    @Mock
    private FaqItemMapper faqItemMapper;

    @InjectMocks
    private FaqItemServiceImpl faqItemService;

    @Test
    @DisplayName("分页查询FAQ - 无过滤条件返回分页结果")
    void getFaqItems_noFilter_shouldReturnPagedResults() {
        Page<FaqItem> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(faqItemMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<FaqItemVO> result = faqItemService.getFaqItems(1, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询FAQ - 按分类过滤")
    void getFaqItems_withCategory_shouldApplyFilter() {
        Page<FaqItem> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(faqItemMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<FaqItemVO> result = faqItemService.getFaqItems(1, 10, "游戏");

        assertNotNull(result);
        verify(faqItemMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取启用的FAQ列表")
    void getEnabledFaqItems_shouldReturnEnabledOnly() {
        FaqItem item = new FaqItem();
        item.setId(1L);
        item.setQuestion("如何退款？");
        item.setEnabled(BusinessStatusConstants.ENABLED_INT);
        when(faqItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));

        List<FaqItem> result = faqItemService.getEnabledFaqItems();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("如何退款？", result.get(0).getQuestion());
    }

    @Test
    @DisplayName("添加FAQ - dto为null抛出异常")
    void addFaqItem_nullDto_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> faqItemService.addFaqItem(null));
    }

    @Test
    @DisplayName("添加FAQ - 正常添加")
    void addFaqItem_normal_shouldInsert() {
        FaqItemDTO dto = new FaqItemDTO();
        dto.setQuestion("如何退款？");
        dto.setAnswer("请联系客服处理退款");
        dto.setCategory("售后");
        dto.setEnabled(1);
        when(faqItemMapper.insert(any(FaqItem.class))).thenReturn(1);

        faqItemService.addFaqItem(dto);

        verify(faqItemMapper).insert(any(FaqItem.class));
    }

    @Test
    @DisplayName("更新FAQ - dto为null抛出异常")
    void updateFaqItem_nullDto_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> faqItemService.updateFaqItem(null));
    }

    @Test
    @DisplayName("更新FAQ - id为null抛出异常")
    void updateFaqItem_nullId_shouldThrow() {
        FaqItemDTO dto = new FaqItemDTO();

        assertThrows(BusinessException.class,
                () -> faqItemService.updateFaqItem(dto));
    }

    @Test
    @DisplayName("更新FAQ - 不存在抛出异常")
    void updateFaqItem_notExist_shouldThrow() {
        FaqItemDTO dto = new FaqItemDTO();
        dto.setId(999L);
        when(faqItemMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> faqItemService.updateFaqItem(dto));
    }

    @Test
    @DisplayName("更新FAQ - 正常更新")
    void updateFaqItem_normal_shouldUpdate() {
        FaqItem existing = new FaqItem();
        existing.setId(1L);
        when(faqItemMapper.selectById(1L)).thenReturn(existing);
        when(faqItemMapper.updateById(any(FaqItem.class))).thenReturn(1);

        FaqItemDTO dto = new FaqItemDTO();
        dto.setId(1L);
        dto.setQuestion("如何退款？");
        dto.setAnswer("更新后的回答");

        faqItemService.updateFaqItem(dto);

        verify(faqItemMapper).updateById(any(FaqItem.class));
    }

    @Test
    @DisplayName("删除FAQ - 不存在抛出异常")
    void deleteFaqItem_notExist_shouldThrow() {
        when(faqItemMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> faqItemService.deleteFaqItem(999L));
    }

    @Test
    @DisplayName("删除FAQ - 正常删除")
    void deleteFaqItem_normal_shouldDelete() {
        FaqItem existing = new FaqItem();
        existing.setId(1L);
        when(faqItemMapper.selectById(1L)).thenReturn(existing);
        when(faqItemMapper.deleteById(1L)).thenReturn(1);

        faqItemService.deleteFaqItem(1L);

        verify(faqItemMapper).deleteById(1L);
    }
}
