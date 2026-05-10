package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.RevenueDailyReportVO;

import java.time.LocalDate;

/**
 * 营收日报服务接口
 *
 * @author 刘建国
 */
public interface RevenueDailyReportService {

    Page<RevenueDailyReportVO> getPage(Integer page, Integer size, Long clubConfigId, LocalDate startDate, LocalDate endDate, String gameType);

    RevenueDailyReportVO getById(Long id);

    void generateDailyReport(Long clubConfigId, LocalDate reportDate);
}
