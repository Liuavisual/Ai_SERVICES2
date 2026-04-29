package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.WorkOrderConstants;
import com.delta.common.dto.ServiceTrackBookDTO;
import com.delta.common.dto.ServiceTrackEndDTO;
import com.delta.common.entity.ServiceTrack;
import com.delta.common.entity.User;
import com.delta.common.enums.ServiceTrackStatusEnum;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ServiceTrackMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.ServiceTrackService;
import com.delta.common.vo.ServiceTrackVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTrackServiceImpl implements ServiceTrackService {

    private static final Logger log = LoggerFactory.getLogger(ServiceTrackServiceImpl.class);

    private final ServiceTrackMapper serviceTrackMapper;

    private final UserMapper userMapper;

    @Override
    public ServiceTrackVO getById(Long id) {
        if (id == null) return null;
        ServiceTrack track = serviceTrackMapper.selectById(id);
        return convertToVO(track);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceTrackVO createConsult(Long userId, Long workOrderId, String consultContent) {
        validateUserExists(userId);

        ServiceTrack track = new ServiceTrack();
        track.setWorkOrderId(workOrderId);
        track.setUserId(userId);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
        track.setConsultStartedAt(LocalDateTime.now());
        track.setConsultContent(consultContent);
        serviceTrackMapper.insert(track);

        log.info("创建服务追踪(咨询): trackId={}, userId={}, workOrderId={}", track.getId(), userId, workOrderId);
        return convertToVO(track);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookService(Long trackId, Long userId, ServiceTrackBookDTO bookDTO) {
        ServiceTrack track = getAndValidateTransition(trackId, WorkOrderConstants.TRACK_STATUS_CONSULTING,
                WorkOrderConstants.TRACK_STATUS_BOOKED);

        track.setBookedAt(LocalDateTime.now());
        track.setBookedCompanionId(bookDTO.getBookedCompanionId());
        track.setBookedCompanionName(bookDTO.getBookedCompanionName());
        track.setBookedServiceType(bookDTO.getBookedServiceType());
        track.setBookedTimeSlot(bookDTO.getBookedTimeSlot());
        track.setRelatedOrderId(bookDTO.getRelatedOrderId());
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_BOOKED);
        serviceTrackMapper.updateById(track);

        log.info("服务追踪-预约: trackId={}, companionId={}", trackId, bookDTO.getBookedCompanionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(Long trackId, Long serviceCompanionId, String serviceCompanionName) {
        ServiceTrack track = getAndValidateTransition(trackId, WorkOrderConstants.TRACK_STATUS_BOOKED,
                WorkOrderConstants.TRACK_STATUS_SERVICING);

        track.setServiceStartedAt(LocalDateTime.now());
        track.setServiceCompanionId(serviceCompanionId);
        track.setServiceCompanionName(serviceCompanionName);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICING);
        serviceTrackMapper.updateById(track);

        log.info("服务追踪-开始服务: trackId={}, companionId={}", trackId, serviceCompanionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endService(Long trackId, ServiceTrackEndDTO endDTO) {
        ServiceTrack track = getAndValidateTransition(trackId, WorkOrderConstants.TRACK_STATUS_SERVICING,
                WorkOrderConstants.TRACK_STATUS_SERVICE_DONE);

        track.setServiceEndedAt(LocalDateTime.now());
        track.setServiceCompanionId(endDTO.getServiceCompanionId() != null ? endDTO.getServiceCompanionId() : track.getServiceCompanionId());
        track.setServiceCompanionName(endDTO.getServiceCompanionName() != null ? endDTO.getServiceCompanionName() : track.getServiceCompanionName());
        track.setServiceDuration(endDTO.getServiceDuration());
        track.setServiceResult(endDTO.getServiceResult());
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICE_DONE);
        serviceTrackMapper.updateById(track);

        log.info("服务追踪-结束服务: trackId={}, duration={}min", trackId, endDTO.getServiceDuration());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(Long trackId, Integer customerRating, String customerFeedback) {
        ServiceTrack track = getAndValidateTransition(trackId, WorkOrderConstants.TRACK_STATUS_SERVICE_DONE,
                WorkOrderConstants.TRACK_STATUS_CONFIRMED);

        if (customerRating != null && (customerRating < 1 || customerRating > 5)) {
            throw new BusinessException("评分必须在1-5之间");
        }

        track.setConfirmedAt(LocalDateTime.now());
        track.setCustomerRating(customerRating);
        track.setCustomerFeedback(customerFeedback);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONFIRMED);
        serviceTrackMapper.updateById(track);

        log.info("服务追踪-评价确认: trackId={}, rating={}/5", trackId, customerRating);
    }

    @Override
    public List<ServiceTrackVO> listByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        LambdaQueryWrapper<ServiceTrack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTrack::getUserId, userId);
        wrapper.orderByDesc(ServiceTrack::getCreatedAt);
        List<ServiceTrack> tracks = serviceTrackMapper.selectList(wrapper);
        return convertList(tracks);
    }

    @Override
    public List<ServiceTrackVO> listByOrderId(Long orderId) {
        if (orderId == null) return new ArrayList<>();
        LambdaQueryWrapper<ServiceTrack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTrack::getRelatedOrderId, orderId);
        wrapper.orderByDesc(ServiceTrack::getCreatedAt);
        List<ServiceTrack> tracks = serviceTrackMapper.selectList(wrapper);
        return convertList(tracks);
    }

    private void validateUserExists(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在: " + userId);
        }
    }

    private ServiceTrack getAndValidateTransition(Long trackId, String expectedStatus, String targetStatus) {
        if (trackId == null) {
            throw new BusinessException("追踪ID不能为空");
        }
        ServiceTrack track = serviceTrackMapper.selectById(trackId);
        if (track == null) {
            throw new BusinessException("服务追踪记录不存在: " + trackId);
        }
        if (!expectedStatus.equals(track.getTrackStatus())) {
            throw new BusinessException("当前状态为" + (track.getTrackStatus()) + "，无法执行此操作。期望状态: " + expectedStatus);
        }
        return track;
    }

    private ServiceTrackVO convertToVO(ServiceTrack track) {
        if (track == null) return null;
        ServiceTrackVO vo = new ServiceTrackVO();
        vo.setId(track.getId());
        vo.setWorkOrderId(track.getWorkOrderId());
        vo.setUserId(track.getUserId());
        vo.setTrackStatus(track.getTrackStatus());
        vo.setTrackStatusDesc(ServiceTrackStatusEnum.fromCode(track.getTrackStatus()).getDesc());
        vo.setConsultStartedAt(track.getConsultStartedAt());
        vo.setConsultContent(track.getConsultContent());
        vo.setBookedAt(track.getBookedAt());
        vo.setBookedCompanionId(track.getBookedCompanionId());
        vo.setBookedCompanionName(track.getBookedCompanionName());
        vo.setBookedServiceType(track.getBookedServiceType());
        vo.setBookedTimeSlot(track.getBookedTimeSlot());
        vo.setRelatedOrderId(track.getRelatedOrderId());
        vo.setServiceStartedAt(track.getServiceStartedAt());
        vo.setServiceCompanionId(track.getServiceCompanionId());
        vo.setServiceCompanionName(track.getServiceCompanionName());
        vo.setServiceDuration(track.getServiceDuration());
        vo.setServiceEndedAt(track.getServiceEndedAt());
        vo.setServiceResult(track.getServiceResult());
        vo.setConfirmedAt(track.getConfirmedAt());
        vo.setCustomerRating(track.getCustomerRating());
        vo.setCustomerFeedback(track.getCustomerFeedback());
        return vo;
    }

    private List<ServiceTrackVO> convertList(List<ServiceTrack> tracks) {
        List<ServiceTrackVO> result = new ArrayList<>(tracks.size());
        for (ServiceTrack track : tracks) {
            result.add(convertToVO(track));
        }
        return result;
    }
}
