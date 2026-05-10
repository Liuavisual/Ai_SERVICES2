package com.delta.common.service.impl;

import com.delta.common.constant.WorkOrderConstants;
import com.delta.common.dto.ServiceTrackBookDTO;
import com.delta.common.dto.ServiceTrackEndDTO;
import com.delta.common.entity.ServiceTrack;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ServiceTrackMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.vo.ServiceTrackVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTrackServiceImplTest {

    @Mock
    private ServiceTrackMapper serviceTrackMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ServiceTrackServiceImpl serviceTrackService;

    @Test
    @DisplayName("根据ID查询 - ID为null返回null")
    void getById_nullId_shouldReturnNull() {
        ServiceTrackVO result = serviceTrackService.getById(null);

        assertNull(result);
    }

    @Test
    @DisplayName("根据ID查询 - 记录不存在返回null")
    void getById_notExist_shouldReturnNull() {
        when(serviceTrackMapper.selectById(999L)).thenReturn(null);

        ServiceTrackVO result = serviceTrackService.getById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("根据ID查询 - 正常返回ServiceTrackVO")
    void getById_exist_shouldReturnVO() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setUserId(100L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);

        ServiceTrackVO result = serviceTrackService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(WorkOrderConstants.TRACK_STATUS_CONSULTING, result.getTrackStatus());
    }

    @Test
    @DisplayName("创建咨询 - 用户不存在抛出异常")
    void createConsult_userNotExist_shouldThrow() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> serviceTrackService.createConsult(999L, 1L, "咨询内容"));
    }

    @Test
    @DisplayName("创建咨询 - 正常创建")
    void createConsult_normal_shouldInsert() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(serviceTrackMapper.insert(any(ServiceTrack.class))).thenReturn(1);

        ServiceTrackVO result = serviceTrackService.createConsult(1L, 100L, "我想咨询");

        assertNotNull(result);
        assertEquals("STARTED", result.getTrackStatus());
        verify(serviceTrackMapper).insert(any(ServiceTrack.class));
    }

    @Test
    @DisplayName("预约服务 - 任意状态均可预约（无状态校验）")
    void bookService_wrongStatus_shouldNotThrow() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICING);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);
        when(serviceTrackMapper.updateById(any(ServiceTrack.class))).thenReturn(1);

        ServiceTrackBookDTO dto = new ServiceTrackBookDTO();
        assertDoesNotThrow(() -> serviceTrackService.bookService(1L, 1L, dto));
    }

    @Test
    @DisplayName("预约服务 - 正常预约")
    void bookService_normal_shouldUpdateStatus() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);
        when(serviceTrackMapper.updateById(any(ServiceTrack.class))).thenReturn(1);

        ServiceTrackBookDTO dto = new ServiceTrackBookDTO();
        dto.setBookedCompanionId(10L);
        dto.setBookedCompanionName("陪玩师A");

        serviceTrackService.bookService(1L, 1L, dto);

        ArgumentCaptor<ServiceTrack> captor = ArgumentCaptor.forClass(ServiceTrack.class);
        verify(serviceTrackMapper).updateById(captor.capture());
        assertEquals(WorkOrderConstants.ORDER_STATUS_IN_PROGRESS, captor.getValue().getTrackStatus());
    }

    @Test
    @DisplayName("开始服务 - 任意状态均可开始（无状态校验）")
    void startService_wrongStatus_shouldNotThrow() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);
        when(serviceTrackMapper.updateById(any(ServiceTrack.class))).thenReturn(1);

        assertDoesNotThrow(() -> serviceTrackService.startService(1L, 10L, "陪玩师A"));
    }

    @Test
    @DisplayName("开始服务 - 正常开始")
    void startService_normal_shouldUpdateStatus() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.ORDER_STATUS_IN_PROGRESS);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);
        when(serviceTrackMapper.updateById(any(ServiceTrack.class))).thenReturn(1);

        serviceTrackService.startService(1L, 10L, "陪玩师A");

        ArgumentCaptor<ServiceTrack> captor = ArgumentCaptor.forClass(ServiceTrack.class);
        verify(serviceTrackMapper).updateById(captor.capture());
        assertEquals(WorkOrderConstants.ORDER_STATUS_IN_PROGRESS, captor.getValue().getTrackStatus());
    }

    @Test
    @DisplayName("结束服务 - 正常结束")
    void endService_normal_shouldUpdateStatus() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICING);
        track.setServiceCompanionId(10L);
        track.setServiceCompanionName("陪玩师A");
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);
        when(serviceTrackMapper.updateById(any(ServiceTrack.class))).thenReturn(1);

        ServiceTrackEndDTO dto = new ServiceTrackEndDTO();
        dto.setServiceDuration(60);
        dto.setServiceResult("完成");

        serviceTrackService.endService(1L, dto);

        ArgumentCaptor<ServiceTrack> captor = ArgumentCaptor.forClass(ServiceTrack.class);
        verify(serviceTrackMapper).updateById(captor.capture());
        assertEquals(WorkOrderConstants.STATUS_COMPLETED, captor.getValue().getTrackStatus());
    }

    @Test
    @DisplayName("提交评价 - 评分超出范围抛出异常")
    void submitRating_invalidRating_shouldThrow() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICE_DONE);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);

        assertThrows(BusinessException.class,
                () -> serviceTrackService.submitRating(1L, 6, "很好"));
    }

    @Test
    @DisplayName("提交评价 - 正常提交")
    void submitRating_normal_shouldUpdateStatus() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICE_DONE);
        when(serviceTrackMapper.selectById(1L)).thenReturn(track);
        when(serviceTrackMapper.updateById(any(ServiceTrack.class))).thenReturn(1);

        serviceTrackService.submitRating(1L, 5, "非常满意");

        ArgumentCaptor<ServiceTrack> captor = ArgumentCaptor.forClass(ServiceTrack.class);
        verify(serviceTrackMapper).updateById(captor.capture());
        assertEquals(WorkOrderConstants.STATUS_COMPLETED, captor.getValue().getTrackStatus());
    }

    @Test
    @DisplayName("按用户ID查询 - userId为null返回空列表")
    void listByUserId_nullUserId_shouldReturnEmptyList() {
        List<ServiceTrackVO> result = serviceTrackService.listByUserId(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按用户ID查询 - 正常返回列表")
    void listByUserId_normal_shouldReturnList() {
        ServiceTrack track = new ServiceTrack();
        track.setId(1L);
        track.setUserId(100L);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
        when(serviceTrackMapper.selectList(any())).thenReturn(List.of(track));

        List<ServiceTrackVO> result = serviceTrackService.listByUserId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
