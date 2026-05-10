package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CustomerSatisfactionDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CustomerSatisfaction;
import com.delta.common.entity.ServiceTrack;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CustomerSatisfactionMapper;
import com.delta.common.mapper.ServiceTrackMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.util.IdObfuscateUtils;
import com.delta.common.vo.CustomerSatisfactionVO;
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
class CustomerSatisfactionServiceImplTest {

    @Mock
    private CustomerSatisfactionMapper satisfactionMapper;

    @Mock
    private ServiceTrackMapper serviceTrackMapper;

    @Mock
    private CompanionMapper companionMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CustomerSatisfactionServiceImpl customerSatisfactionService;

    @Test
    @DisplayName("提交满意度评价 - 服务追踪不存在抛出异常")
    void submitSatisfaction_trackNotExist_shouldThrow() {
        CustomerSatisfactionDTO dto = new CustomerSatisfactionDTO();
        dto.setServiceTrackId(IdObfuscateUtils.encode(999L));
        dto.setRating(5);
        when(serviceTrackMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> customerSatisfactionService.submitSatisfaction(1L, dto));
    }

    @Test
    @DisplayName("提交满意度评价 - 已提交过评价抛出异常")
    void submitSatisfaction_alreadySubmitted_shouldThrow() {
        Long trackId = 1L;
        ServiceTrack track = new ServiceTrack();
        track.setId(trackId);
        track.setBookedCompanionId(10L);
        when(serviceTrackMapper.selectById(trackId)).thenReturn(track);
        when(satisfactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CustomerSatisfactionDTO dto = new CustomerSatisfactionDTO();
        dto.setServiceTrackId(IdObfuscateUtils.encode(trackId));
        dto.setRating(5);

        assertThrows(BusinessException.class,
                () -> customerSatisfactionService.submitSatisfaction(1L, dto));
    }

    @Test
    @DisplayName("提交满意度评价 - 正常提交")
    void submitSatisfaction_normal_shouldInsert() {
        Long trackId = 1L;
        ServiceTrack track = new ServiceTrack();
        track.setId(trackId);
        track.setBookedCompanionId(10L);
        when(serviceTrackMapper.selectById(trackId)).thenReturn(track);
        when(satisfactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(satisfactionMapper.insert(any(CustomerSatisfaction.class))).thenReturn(1);

        CustomerSatisfactionDTO dto = new CustomerSatisfactionDTO();
        dto.setServiceTrackId(IdObfuscateUtils.encode(trackId));
        dto.setRating(5);
        dto.setFeedback("非常满意");
        dto.setServiceType("游戏陪玩");

        CustomerSatisfactionVO result = customerSatisfactionService.submitSatisfaction(1L, dto);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("非常满意", result.getFeedback());
        verify(satisfactionMapper).insert(any(CustomerSatisfaction.class));
    }

    @Test
    @DisplayName("提交满意度评价 - 默认匿名设置为0")
    void submitSatisfaction_defaultAnonymous_shouldSetZero() {
        Long trackId = 1L;
        ServiceTrack track = new ServiceTrack();
        track.setId(trackId);
        track.setBookedCompanionId(10L);
        when(serviceTrackMapper.selectById(trackId)).thenReturn(track);
        when(satisfactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(satisfactionMapper.insert(any(CustomerSatisfaction.class))).thenReturn(1);

        CustomerSatisfactionDTO dto = new CustomerSatisfactionDTO();
        dto.setServiceTrackId(IdObfuscateUtils.encode(trackId));
        dto.setRating(4);
        dto.setIsAnonymous(null);

        CustomerSatisfactionVO result = customerSatisfactionService.submitSatisfaction(1L, dto);

        assertNotNull(result);
        assertEquals(0, result.getIsAnonymous());
    }

    @Test
    @DisplayName("分页查询满意度评价 - 无过滤条件返回分页结果")
    void getSatisfactions_noFilter_shouldReturnPagedResults() {
        Page<CustomerSatisfaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(satisfactionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CustomerSatisfactionVO> result = customerSatisfactionService.getSatisfactions(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询满意度评价 - 填充陪玩师和客户信息")
    void getSatisfactions_withData_shouldPopulateNames() {
        CustomerSatisfaction satisfaction = new CustomerSatisfaction();
        satisfaction.setId(1L);
        satisfaction.setUserId(100L);
        satisfaction.setCompanionId(200L);
        satisfaction.setRating(5);
        satisfaction.setIsAnonymous(0);

        Page<CustomerSatisfaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(satisfaction));
        mockPage.setTotal(1);
        when(satisfactionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Companion companion = new Companion();
        companion.setId(200L);
        companion.setNickname("陪玩师A");
        when(companionMapper.selectByIds(anyList())).thenReturn(List.of(companion));

        User user = new User();
        user.setId(100L);
        user.setNickname("客户A");
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(user));

        Page<CustomerSatisfactionVO> result = customerSatisfactionService.getSatisfactions(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("陪玩师A", result.getRecords().get(0).getCompanionName());
        assertEquals("客户A", result.getRecords().get(0).getUserNickname());
    }

    @Test
    @DisplayName("分页查询满意度评价 - 匿名评价显示匿名用户")
    void getSatisfactions_anonymous_shouldShowAnonymousName() {
        CustomerSatisfaction satisfaction = new CustomerSatisfaction();
        satisfaction.setId(1L);
        satisfaction.setUserId(100L);
        satisfaction.setCompanionId(200L);
        satisfaction.setRating(5);
        satisfaction.setIsAnonymous(1);

        Page<CustomerSatisfaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(satisfaction));
        mockPage.setTotal(1);
        when(satisfactionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Companion companion = new Companion();
        companion.setId(200L);
        companion.setNickname("陪玩师A");
        when(companionMapper.selectByIds(anyList())).thenReturn(List.of(companion));

        User user = new User();
        user.setId(100L);
        user.setNickname("客户A");
        when(userMapper.selectByIds(anyList())).thenReturn(List.of(user));

        Page<CustomerSatisfactionVO> result = customerSatisfactionService.getSatisfactions(1, 10, null, null, null);

        assertEquals("匿名用户", result.getRecords().get(0).getUserNickname());
    }

    @Test
    @DisplayName("获取评价数量 - 正常返回数量")
    void getRatingCount_shouldReturnCount() {
        when(satisfactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        Long count = customerSatisfactionService.getRatingCount(1L);

        assertEquals(10L, count);
    }
}
