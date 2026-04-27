package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.beans.factory.annotation.Autowired;
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
public class StatsServiceImpl implements StatsService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PendingMessageMapper pendingMessageMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private CsUserCustomerMapper csUserCustomerMapper;

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
        wrapper.eq(CsUserCustomer::getDeleted, BusinessStatusConstants.NOT_DELETED);
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
        wrapper.eq(CsUserCustomer::getDeleted, BusinessStatusConstants.NOT_DELETED);
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
        wrapper.eq(Message::getIsAi, true);
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
        wrapper.eq(Message::getIsAi, true);
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
        wrapper.eq(SysUser::getDeleted, BusinessStatusConstants.NOT_DELETED);
        return sysUserMapper.selectList(wrapper);
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
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = today.atTime(LocalTime.MAX);

        Map<String, Long> dailyCounts = new HashMap<>();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (customerIds != null && !customerIds.isEmpty()) {
            wrapper.in(Message::getUserId, customerIds);
        }
        wrapper.ge(Message::getCreatedAt, rangeStart);
        wrapper.le(Message::getCreatedAt, rangeEnd);
        wrapper.select(Message::getCreatedAt);
        List<Message> messages = messageMapper.selectList(wrapper);
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Message msg : messages) {
            if (msg.getCreatedAt() != null) {
                String dayKey = msg.getCreatedAt().toLocalDate().format(dayFormatter);
                dailyCounts.merge(dayKey, 1L, Long::sum);
            }
        }

        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = d.format(formatter);
            String dayKey = d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long count = dailyCounts.getOrDefault(dayKey, 0L);
            trendData.add(new StatsVO.TrendData(label, (int) count));
        }

        return trendData;
    }
}
