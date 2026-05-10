package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.MessageConstants;
import com.delta.common.entity.Message;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.entity.CsUserCustomer;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.mapper.CsUserCustomerMapper;
import com.delta.common.service.StatsService;
import com.delta.common.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final MessageMapper messageMapper;

    private final UserMapper userMapper;

    private final PendingMessageMapper pendingMessageMapper;

    private final SysUserMapper sysUserMapper;

    private final CsUserCustomerMapper csUserCustomerMapper;

    @Override
    public StatsVO getPersonalStats(Long csUserId, String period, String date) {
        StatsVO stats = new StatsVO();

        LocalDateTime startTime = getStartTime(period, date);
        LocalDateTime endTime = getEndTime(period, date);

        List<Long> customerIds = getCustomerIdsByCsUserId(csUserId);
        Long totalMessages = countMessagesByCustomers(customerIds, startTime, endTime);
        int totalCustomers = customerIds.size();
        Long pendingCount = countPendingByCsUserId(csUserId, customerIds);
        Long aiReplyCount = countAiRepliesByCustomers(customerIds, startTime, endTime);
        Long manualReplyCount = totalMessages - aiReplyCount;

        StatsVO.Overview overview = new StatsVO.Overview();
        overview.setTotalMessages(totalMessages.intValue());
        overview.setTotalCustomers(totalCustomers);
        overview.setAvgResponseTime(0);
        overview.setPendingCount(pendingCount.intValue());
        overview.setAiReplyCount(aiReplyCount.intValue());
        overview.setManualReplyCount(Math.max(0, manualReplyCount.intValue()));
        stats.setOverview(overview);

        stats.setTrendData(generateTrendData(customerIds, period));

        return stats;
    }

    @Override
    public StatsVO getTeamStats(String period, String date) {
        StatsVO stats = new StatsVO();

        LocalDateTime startTime = getStartTime(period, date);
        LocalDateTime endTime = getEndTime(period, date);

        List<SysUser> csStaffList = getActiveCsStaff();
        List<Long> csUserIds = csStaffList.stream().map(SysUser::getId).collect(Collectors.toList());

        Map<Long, List<Long>> csCustomerMap = batchGetCustomerIdsByCsUserIds(csUserIds);

        long totalMessages = 0;
        int totalCustomers = 0;
        long totalAiReplies = 0;

        List<StatsVO.CsUserData> csUserData = new ArrayList<>();
        for (SysUser csUser : csStaffList) {
            List<Long> customerIds = csCustomerMap.getOrDefault(csUser.getId(), List.of());
            Long msgCount = countMessagesByCustomers(customerIds, startTime, endTime);
            Long aiCount = countAiRepliesByCustomers(customerIds, startTime, endTime);

            totalMessages += msgCount;
            totalCustomers += customerIds.size();
            totalAiReplies += aiCount;

            csUserData.add(new StatsVO.CsUserData(
                    csUser.getId(),
                    csUser.getRealName(),
                    msgCount.intValue(),
                    customerIds.size(),
                    0,
                    BigDecimal.ZERO
            ));
        }

        Long pendingCount = countAllPending();

        StatsVO.Overview overview = new StatsVO.Overview();
        overview.setTotalMessages((int) totalMessages);
        overview.setTotalCustomers(totalCustomers);
        overview.setAvgResponseTime(0);
        overview.setPendingCount(pendingCount.intValue());
        overview.setActiveCsCount(csStaffList.size());
        overview.setAiReplyCount((int) totalAiReplies);
        overview.setManualReplyCount((int) (totalMessages - totalAiReplies));
        stats.setOverview(overview);

        stats.setTrendData(generateTrendData(null, period));
        stats.setCsUserData(csUserData);

        return stats;
    }

    @Override
    public StatsVO getGlobalStats(String period, String date) {
        StatsVO stats = new StatsVO();

        LocalDateTime startTime = getStartTime(period, date);
        LocalDateTime endTime = getEndTime(period, date);

        Long totalMessages = countAllMessages(startTime, endTime);
        Long totalCustomers = countAllCustomers(startTime, endTime);
        Long aiReplyCount = countAllAiReplies(startTime, endTime);
        Long manualReplyCount = totalMessages - aiReplyCount;
        Long pendingCount = countAllPending();
        List<SysUser> csStaffList = getActiveCsStaff();
        List<Long> csUserIds = csStaffList.stream().map(SysUser::getId).collect(Collectors.toList());
        Map<Long, List<Long>> csCustomerMap = batchGetCustomerIdsByCsUserIds(csUserIds);

        StatsVO.Overview overview = new StatsVO.Overview();
        overview.setTotalMessages(totalMessages.intValue());
        overview.setTotalCustomers(totalCustomers.intValue());
        overview.setAvgResponseTime(0);
        overview.setPendingCount(pendingCount.intValue());
        overview.setAiReplyCount(aiReplyCount.intValue());
        overview.setManualReplyCount(Math.max(0, manualReplyCount.intValue()));
        overview.setActiveCsCount(csStaffList.size());
        stats.setOverview(overview);

        stats.setTrendData(generateTrendData(null, period));

        List<StatsVO.CsUserData> csUserData = new ArrayList<>();
        for (SysUser csUser : csStaffList) {
            List<Long> customerIds = csCustomerMap.getOrDefault(csUser.getId(), List.of());
            Long msgCount = countMessagesByCustomers(customerIds, startTime, endTime);
            csUserData.add(new StatsVO.CsUserData(
                    csUser.getId(),
                    csUser.getRealName(),
                    msgCount.intValue(),
                    customerIds.size(),
                    0,
                    BigDecimal.ZERO
            ));
        }
        stats.setCsUserData(csUserData);

        return stats;
    }

    private LocalDateTime getStartTime(String period, String date) {
        if (date != null && !date.isEmpty()) {
            try {
                LocalDate d = LocalDate.parse(date);
                return d.atStartOfDay();
            } catch (Exception e) {
                log.warn("日期解析失败，使用默认值: date={}", date);
            }
        }
        LocalDate today = LocalDate.now();
        switch (period != null ? period : "DAILY") {
            case "WEEKLY":
                return today.minusDays(6).atStartOfDay();
            case "MONTHLY":
                return today.minusDays(29).atStartOfDay();
            case "QUARTERLY":
                return today.minusMonths(3).atStartOfDay();
            case "YEARLY":
                return today.minusYears(1).atStartOfDay();
            default:
                return today.atStartOfDay();
        }
    }

    private LocalDateTime getEndTime(String period, String date) {
        if (date != null && !date.isEmpty()) {
            try {
                LocalDate d = LocalDate.parse(date);
                return d.atTime(LocalTime.MAX);
            } catch (Exception e) {
                log.warn("日期解析失败，使用默认值: date={}", date);
            }
        }
        return LocalDateTime.now();
    }

    private List<Long> getCustomerIdsByCsUserId(Long csUserId) {
        LambdaQueryWrapper<CsUserCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsUserCustomer::getCsUserId, csUserId);
        wrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        List<CsUserCustomer> assignments = csUserCustomerMapper.selectList(wrapper);
        return assignments.stream().map(CsUserCustomer::getCustomerUserId).collect(Collectors.toList());
    }

    private Map<Long, List<Long>> batchGetCustomerIdsByCsUserIds(List<Long> csUserIds) {
        if (csUserIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<CsUserCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CsUserCustomer::getCsUserId, csUserIds);
        wrapper.eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        List<CsUserCustomer> assignments = csUserCustomerMapper.selectList(wrapper);
        return assignments.stream().collect(Collectors.groupingBy(
                CsUserCustomer::getCsUserId,
                Collectors.mapping(CsUserCustomer::getCustomerUserId, Collectors.toList())
        ));
    }

    private Long countMessagesByCustomers(List<Long> customerIds, LocalDateTime start, LocalDateTime end) {
        if (customerIds.isEmpty()) {
            return 0L;
        }
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Message::getUserId, customerIds);
        wrapper.ge(Message::getCreatedAt, start);
        wrapper.le(Message::getCreatedAt, end);
        return messageMapper.selectCount(wrapper);
    }

    private Long countAiRepliesByCustomers(List<Long> customerIds, LocalDateTime start, LocalDateTime end) {
        if (customerIds.isEmpty()) {
            return 0L;
        }
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Message::getUserId, customerIds);
        wrapper.eq(Message::getDirection, MessageConstants.DIRECTION_OUT);
        wrapper.eq(Message::getAi, true);
        wrapper.ge(Message::getCreatedAt, start);
        wrapper.le(Message::getCreatedAt, end);
        return messageMapper.selectCount(wrapper);
    }

    private Long countPendingByCsUserId(Long csUserId, List<Long> customerIds) {
        if (customerIds.isEmpty()) {
            return 0L;
        }
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingMessage::getStatus, BusinessStatusConstants.PENDING_STATUS_PENDING);
        wrapper.in(PendingMessage::getUserId, customerIds);
        return pendingMessageMapper.selectCount(wrapper);
    }

    private Long countAllMessages(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Message::getCreatedAt, start);
        wrapper.le(Message::getCreatedAt, end);
        return messageMapper.selectCount(wrapper);
    }

    private Long countAllCustomers(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreatedAt, start);
        wrapper.le(User::getCreatedAt, end);
        return userMapper.selectCount(wrapper);
    }

    private Long countAllAiReplies(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getDirection, MessageConstants.DIRECTION_OUT);
        wrapper.eq(Message::getAi, true);
        wrapper.ge(Message::getCreatedAt, start);
        wrapper.le(Message::getCreatedAt, end);
        return messageMapper.selectCount(wrapper);
    }

    private Long countAllPending() {
        LambdaQueryWrapper<PendingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingMessage::getStatus, BusinessStatusConstants.PENDING_STATUS_PENDING);
        return pendingMessageMapper.selectCount(wrapper);
    }

    private List<SysUser> getActiveCsStaff() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getRole, BusinessStatusConstants.ROLE_CS_STAFF, BusinessStatusConstants.ROLE_CS_LEADER);
        wrapper.eq(SysUser::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE);
        return sysUserMapper.selectList(wrapper);
    }

    /**
     * 按日期分组统计消息数量（单次SQL查询替代循环查询）
     *
     * @param customerIds 客户ID列表，为null或空时统计全部
     * @param start       起始时间
     * @param end         结束时间
     * @param aiOnly      是否仅统计AI回复
     * @return 日期到消息数的映射
     */
    private Map<LocalDate, Long> getMessageCountByDateRange(List<Long> customerIds, LocalDateTime start, LocalDateTime end, boolean aiOnly) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(created_at) as date", "COUNT(*) as count");
        if (customerIds != null && !customerIds.isEmpty()) {
            wrapper.in("user_id", customerIds);
        }
        if (aiOnly) {
            wrapper.eq("direction", MessageConstants.DIRECTION_OUT);
            wrapper.eq("is_ai", true);
        }
        wrapper.ge("created_at", start);
        wrapper.lt("created_at", end);
        wrapper.groupBy("DATE(created_at)");
        List<Map<String, Object>> results = messageMapper.selectMaps(wrapper);
        Map<LocalDate, Long> map = new HashMap<>(32);
        for (Map<String, Object> row : results) {
            LocalDate date = LocalDate.parse(row.get("date").toString());
            Long count = ((Number) row.get("count")).longValue();
            map.put(date, count);
        }
        return map;
    }

    private List<StatsVO.TrendData> generateTrendData(List<Long> customerIds, String period) {
        List<StatsVO.TrendData> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        int days = 7;
        if ("WEEKLY".equals(period)) {
            days = 7;
        } else if ("MONTHLY".equals(period)) {
            days = 30;
        } else if ("QUARTERLY".equals(period)) {
            days = 90;
        } else if ("YEARLY".equals(period)) {
            days = 365;
        }

        LocalDate startDate = today.minusDays(days - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        // 单次SQL查询获取所有日期的消息统计，替代循环查询
        Map<LocalDate, Long> msgCountMap = getMessageCountByDateRange(customerIds, startDateTime, endDateTime, false);

        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = d.format(formatter);
            long count = msgCountMap.getOrDefault(d, 0L);
            trendData.add(new StatsVO.TrendData(label, (int) count));
        }

        return trendData;
    }
}
