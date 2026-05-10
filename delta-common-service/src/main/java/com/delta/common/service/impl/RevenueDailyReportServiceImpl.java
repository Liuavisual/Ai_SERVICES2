package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.RevenueDailyReport;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.mapper.RevenueDailyReportMapper;
import com.delta.common.service.RevenueDailyReportService;
import com.delta.common.vo.RevenueDailyReportVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * 营收日报服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class RevenueDailyReportServiceImpl implements RevenueDailyReportService {

    private static final Logger log = LoggerFactory.getLogger(RevenueDailyReportServiceImpl.class);

    private final RevenueDailyReportMapper revenueDailyReportMapper;
    private final ClubConfigMapper clubConfigMapper;

    @Override
    public Page<RevenueDailyReportVO> getPage(Integer page, Integer size, Long clubConfigId, LocalDate startDate, LocalDate endDate, String gameType) {
        Page<RevenueDailyReport> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<RevenueDailyReport> wrapper = new LambdaQueryWrapper<>();

        if (clubConfigId != null) {
            wrapper.eq(RevenueDailyReport::getClubConfigId, clubConfigId);
        }
        if (startDate != null) {
            wrapper.ge(RevenueDailyReport::getReportDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(RevenueDailyReport::getReportDate, endDate);
        }
        if (gameType != null && !gameType.trim().isEmpty()) {
            wrapper.eq(RevenueDailyReport::getGameType, gameType);
        }
        wrapper.orderByDesc(RevenueDailyReport::getReportDate);

        Page<RevenueDailyReport> result = revenueDailyReportMapper.selectPage(pageObj, wrapper);
        Page<RevenueDailyReportVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(report -> {
            RevenueDailyReportVO vo = BeanUtil.copyProperties(report, RevenueDailyReportVO.class);
            ClubConfig config = clubConfigMapper.selectById(report.getClubConfigId());
            if (config != null) {
                vo.setClubName(config.getClubName());
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public RevenueDailyReportVO getById(Long id) {
        RevenueDailyReport report = revenueDailyReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("营收日报不存在");
        }
        RevenueDailyReportVO vo = BeanUtil.copyProperties(report, RevenueDailyReportVO.class);
        ClubConfig config = clubConfigMapper.selectById(report.getClubConfigId());
        if (config != null) {
            vo.setClubName(config.getClubName());
        }
        return vo;
    }

    @Override
    public void generateDailyReport(Long clubConfigId, LocalDate reportDate) {
        // 检查是否已存在该日报表
        LambdaQueryWrapper<RevenueDailyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RevenueDailyReport::getClubConfigId, clubConfigId);
        wrapper.eq(RevenueDailyReport::getReportDate, reportDate);
        RevenueDailyReport existing = revenueDailyReportMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("营收日报已存在，跳过生成: clubConfigId={}, reportDate={}", clubConfigId, reportDate);
            return;
        }

        // 生成日报（实际应从订单、会话等数据汇总计算，此处使用默认值）
        RevenueDailyReport report = new RevenueDailyReport();
        report.setClubConfigId(clubConfigId);
        report.setReportDate(reportDate);
        report.setTotalOrders(0);
        report.setCompletedOrders(0);
        report.setRefundOrders(0);
        report.setTotalRevenue(BigDecimal.ZERO);
        report.setPlatformIncome(BigDecimal.ZERO);
        report.setAiConversations(0);
        report.setAiHandleRate(BigDecimal.ZERO);
        report.setAvgSatisfaction(BigDecimal.ZERO);
        report.setNewCustomers(0);
        report.setRepeatCustomers(0);
        report.setActiveCompanions(0);
        revenueDailyReportMapper.insert(report);
        log.info("生成营收日报成功: clubConfigId={}, reportDate={}", clubConfigId, reportDate);
    }
}
