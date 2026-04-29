package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.CustomerLifecycleConstants;
import com.delta.common.entity.CustomerProfile;
import com.delta.common.entity.User;
import com.delta.common.mapper.CustomerProfileMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.vo.CustomerVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerLifecycleServiceImplTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), CustomerProfile.class);
    }

    @Mock
    private UserMapper userMapper;

    @Mock
    private CustomerProfileMapper customerProfileMapper;

    @InjectMocks
    private CustomerLifecycleServiceImpl customerLifecycleService;

    @Test
    @DisplayName("判断生命周期阶段 - userId为null返回NEW")
    void determineLifecycleStage_nullUserId_shouldReturnNew() {
        String stage = customerLifecycleService.determineLifecycleStage(null);

        assertEquals(CustomerLifecycleConstants.STAGE_NEW, stage);
    }

    @Test
    @DisplayName("判断生命周期阶段 - 无画像返回NEW")
    void determineLifecycleStage_noProfile_shouldReturnNew() {
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        String stage = customerLifecycleService.determineLifecycleStage(1L);

        assertEquals(CustomerLifecycleConstants.STAGE_NEW, stage);
    }

    @Test
    @DisplayName("判断生命周期阶段 - 画像无活跃时间返回NEW")
    void determineLifecycleStage_noActiveTime_shouldReturnNew() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(null);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        String stage = customerLifecycleService.determineLifecycleStage(1L);

        assertEquals(CustomerLifecycleConstants.STAGE_NEW, stage);
    }

    @Test
    @DisplayName("判断生命周期阶段 - 超过流失阈值返回CHURNED")
    void determineLifecycleStage_churned_shouldReturnChurned() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(60));
        profile.setTotalMessages(100);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        String stage = customerLifecycleService.determineLifecycleStage(1L);

        assertEquals(CustomerLifecycleConstants.STAGE_CHURNED, stage);
    }

    @Test
    @DisplayName("判断生命周期阶段 - 超过风险阈值返回AT_RISK")
    void determineLifecycleStage_atRisk_shouldReturnAtRisk() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(15));
        profile.setTotalMessages(100);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        String stage = customerLifecycleService.determineLifecycleStage(1L);

        assertEquals(CustomerLifecycleConstants.STAGE_AT_RISK, stage);
    }

    @Test
    @DisplayName("判断生命周期阶段 - 消息数大于50返回LOYAL")
    void determineLifecycleStage_loyal_shouldReturnLoyal() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(2));
        profile.setTotalMessages(60);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        String stage = customerLifecycleService.determineLifecycleStage(1L);

        assertEquals(CustomerLifecycleConstants.STAGE_LOYAL, stage);
    }

    @Test
    @DisplayName("判断生命周期阶段 - 消息数大于5返回ACTIVE")
    void determineLifecycleStage_active_shouldReturnActive() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(2));
        profile.setTotalMessages(10);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        String stage = customerLifecycleService.determineLifecycleStage(1L);

        assertEquals(CustomerLifecycleConstants.STAGE_ACTIVE, stage);
    }

    @Test
    @DisplayName("获取流失风险客户列表 - 正常返回")
    void getAtRiskCustomers_shouldReturnAtRiskList() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(15));
        profile.setTotalMessages(10);
        when(customerProfileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(profile));
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);

        List<CustomerVO> result = customerLifecycleService.getAtRiskCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CustomerLifecycleConstants.STAGE_AT_RISK, result.get(0).getLifecycleStage());
    }

    @Test
    @DisplayName("获取已流失客户列表 - 正常返回")
    void getChurnedCustomers_shouldReturnChurnedList() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(60));
        profile.setTotalMessages(10);
        when(customerProfileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(profile));
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        User user = new User();
        user.setId(1L);
        user.setNickname("流失用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);

        List<CustomerVO> result = customerLifecycleService.getChurnedCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CustomerLifecycleConstants.STAGE_CHURNED, result.get(0).getLifecycleStage());
    }

    @Test
    @DisplayName("更新客户生命周期标签 - 正常更新")
    void updateCustomerLifecycleTags_shouldUpdateTags() {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(15));
        profile.setTotalMessages(100);
        profile.setTags(null);
        when(customerProfileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(profile));
        when(customerProfileMapper.updateById(any(CustomerProfile.class))).thenReturn(1);

        customerLifecycleService.updateCustomerLifecycleTags();

        verify(customerProfileMapper).updateById(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("更新客户生命周期标签 - 标签已存在不重复追加")
    void updateCustomerLifecycleTags_tagExists_shouldNotDuplicate() {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setLastActiveAt(LocalDateTime.now().minusDays(15));
        profile.setTotalMessages(100);
        profile.setTags("流失风险");
        when(customerProfileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(profile));
        when(customerProfileMapper.updateById(any(CustomerProfile.class))).thenReturn(1);

        customerLifecycleService.updateCustomerLifecycleTags();

        verify(customerProfileMapper).updateById(any(CustomerProfile.class));
    }
}
