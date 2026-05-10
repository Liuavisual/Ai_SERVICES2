package com.delta.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.KeywordVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VoUtilsTest {

    @Test
    void setRowNumbers_shouldSetCorrectRowNumbers_forFirstPage() {
        Page<KeywordVO> page = new Page<>(1, 10, 30);
        List<KeywordVO> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            records.add(new KeywordVO());
        }
        page.setRecords(records);
        VoUtils.setRowNumbers(page);

        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, page.getRecords().get(i).getRowNum());
        }
    }

    @Test
    void setRowNumbers_shouldSetCorrectRowNumbers_forSecondPage() {
        Page<KeywordVO> page = new Page<>(2, 10, 30);
        List<KeywordVO> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            records.add(new KeywordVO());
        }
        page.setRecords(records);
        VoUtils.setRowNumbers(page);

        for (int i = 0; i < 10; i++) {
            assertEquals(10 + i + 1, page.getRecords().get(i).getRowNum());
        }
    }

    @Test
    void setRowNumbers_shouldSetCorrectRowNumbers_forThirdPage() {
        Page<KeywordVO> page = new Page<>(3, 10, 25);
        List<KeywordVO> records = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            records.add(new KeywordVO());
        }
        page.setRecords(records);
        VoUtils.setRowNumbers(page);

        for (int i = 0; i < 5; i++) {
            assertEquals(20 + i + 1, page.getRecords().get(i).getRowNum());
        }
    }

    @Test
    void setRowNumbers_shouldHandleEmptyPage() {
        Page<KeywordVO> page = new Page<>(1, 10, 0);
        page.setRecords(new ArrayList<>());
        VoUtils.setRowNumbers(page);
        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void setRowNumbers_shouldHandleSingleRecord() {
        Page<KeywordVO> page = new Page<>(1, 10, 1);
        List<KeywordVO> records = new ArrayList<>();
        records.add(new KeywordVO());
        page.setRecords(records);
        VoUtils.setRowNumbers(page);
        assertEquals(1, page.getRecords().get(0).getRowNum());
    }

    @Test
    void setRowNumbersList_shouldSetCorrectRowNumbers() {
        List<KeywordVO> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(new KeywordVO());
        }
        VoUtils.setRowNumbers(list, 2, 10);

        for (int i = 0; i < 5; i++) {
            assertEquals(10 + i + 1, list.get(i).getRowNum());
        }
    }
}
