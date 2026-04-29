package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CustomerProfileUpdateDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CustomerProfile;
import com.delta.common.entity.SysUser;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CustomerProfileMapper;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.vo.CustomerProfileVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceImplTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), CustomerProfile.class);
    }

    @Mock
    private CustomerProfileMapper customerProfileMapper;

    @Mock
    private com.delta.common.mapper.CustomerOrderRecordMapper customerOrderRecordMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanionMapper companionMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private CustomerProfileServiceImpl customerProfileService;

    @Test
    @DisplayName("分页查询客户画像 - 无过滤条件返回分页结果")
    void getProfilePage_noFilter_shouldReturnPagedResults() {
        Page<CustomerProfile> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(customerProfileMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<CustomerProfileVO> result = customerProfileService.getProfilePage(1, 10, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页查询客户画像 - 带关键词过滤")
    void getProfilePage_withKeyword_shouldFilterByNickname() {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(1L);
        profile.setUserId(100L);
        when(customerProfileMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<>(1, 10));

        Page<CustomerProfileVO> result = customerProfileService.getProfilePage(1, 10, null, null, null, null, "测试");

        assertNotNull(result);
    }

    @Test
    @DisplayName("根据用户ID查询画像 - 用户不存在抛出异常")
    void getProfileByUserId_userNotExist_shouldThrow() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> customerProfileService.getProfileByUserId(999L));
    }

    @Test
    @DisplayName("根据用户ID查询画像 - 画像不存在自动创建默认画像")
    void getProfileByUserId_noProfile_shouldCreateDefault() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(customerProfileMapper.insert(any(CustomerProfile.class))).thenReturn(1);

        CustomerProfileVO result = customerProfileService.getProfileByUserId(1L);

        assertNotNull(result);
        assertEquals("测试用户", result.getNickname());
        verify(customerProfileMapper).insert(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("根据用户ID查询画像 - 正常返回带客服和陪玩师信息")
    void getProfileByUserId_exist_shouldReturnWithDetails() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPlatform("wechat");
        user.setAssignedCsUserId(100L);
        when(userMapper.selectById(1L)).thenReturn(user);

        CustomerProfile profile = new CustomerProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setFavoriteCompanionId(200L);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);

        SysUser csUser = new SysUser();
        csUser.setId(100L);
        csUser.setRealName("客服张三");
        when(sysUserMapper.selectById(100L)).thenReturn(csUser);

        Companion companion = new Companion();
        companion.setId(200L);
        companion.setNickname("陪玩师A");
        when(companionMapper.selectById(200L)).thenReturn(companion);

        CustomerProfileVO result = customerProfileService.getProfileByUserId(1L);

        assertNotNull(result);
        assertEquals("客服张三", result.getAssignedCsUserName());
        assertEquals("陪玩师A", result.getFavoriteCompanionName());
    }

    @Test
    @DisplayName("更新客户画像 - userId为空抛出异常")
    void updateProfile_nullUserId_shouldThrow() {
        CustomerProfileUpdateDTO dto = new CustomerProfileUpdateDTO();

        assertThrows(BusinessException.class,
                () -> customerProfileService.updateProfile(dto));
    }

    @Test
    @DisplayName("更新客户画像 - 画像不存在抛出异常")
    void updateProfile_profileNotExist_shouldThrow() {
        CustomerProfileUpdateDTO dto = new CustomerProfileUpdateDTO();
        dto.setUserId(1L);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> customerProfileService.updateProfile(dto));
    }

    @Test
    @DisplayName("更新客户画像 - 正常更新标签和备注")
    void updateProfile_normal_shouldUpdateFields() {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(profile);
        when(customerProfileMapper.updateById(any(CustomerProfile.class))).thenReturn(1);

        CustomerProfileUpdateDTO dto = new CustomerProfileUpdateDTO();
        dto.setUserId(1L);
        dto.setTags("VIP,高消费");
        dto.setRemark("重要客户");

        customerProfileService.updateProfile(dto);

        verify(customerProfileMapper).updateById(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("初始化画像 - 已存在不创建")
    void initProfileIfNeeded_alreadyExists_shouldNotCreate() {
        CustomerProfile existing = new CustomerProfile();
        existing.setId(1L);
        existing.setUserId(1L);
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        customerProfileService.initProfileIfNeeded(1L);

        verify(customerProfileMapper, never()).insert(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("初始化画像 - 不存在则创建")
    void initProfileIfNeeded_notExists_shouldCreate() {
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(customerProfileMapper.insert(any(CustomerProfile.class))).thenReturn(1);

        customerProfileService.initProfileIfNeeded(1L);

        verify(customerProfileMapper).insert(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("记录交互 - 画像不存在自动创建后更新")
    void recordInteraction_noProfile_shouldCreateAndRecord() {
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(customerProfileMapper.insert(any(CustomerProfile.class))).thenReturn(1);
        when(customerProfileMapper.updateById(any(CustomerProfile.class))).thenReturn(1);

        customerProfileService.recordInteraction(1L, true);

        verify(customerProfileMapper).insert(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("记录转人工事件 - 画像不存在自动创建后更新")
    void recordHandoffEvent_noProfile_shouldCreateAndRecord() {
        when(customerProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(customerProfileMapper.insert(any(CustomerProfile.class))).thenReturn(1);
        when(customerProfileMapper.updateById(any(CustomerProfile.class))).thenReturn(1);

        customerProfileService.recordHandoffEvent(1L, "情绪触发", true, false);

        verify(customerProfileMapper).insert(any(CustomerProfile.class));
        verify(customerProfileMapper).updateById(any(CustomerProfile.class));
    }
}
