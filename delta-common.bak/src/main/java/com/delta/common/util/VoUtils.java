package com.delta.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.delta.common.vo.BaseVO;

import java.util.List;

public final class VoUtils {

    private VoUtils() {
    }

    public static <T extends BaseVO> void setRowNumbers(IPage<T> page) {
        long current = page.getCurrent();
        long size = page.getSize();
        List<T> records = page.getRecords();
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRowNum((int) ((current - 1) * size + i + 1));
        }
    }

    public static <T extends BaseVO> void setRowNumbers(List<T> list, int pageNum, int pageSize) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRowNum((pageNum - 1) * pageSize + i + 1);
        }
    }
}
