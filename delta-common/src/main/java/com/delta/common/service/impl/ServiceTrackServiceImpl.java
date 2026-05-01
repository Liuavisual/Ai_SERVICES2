package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.entity.ServiceTrack;
import com.delta.common.entity.User;
import com.delta.common.mapper.ServiceTrackMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.ServiceTrackService;
import com.delta.common.vo.ServiceTrackVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceTrackServiceImpl implements ServiceTrackService {

    private static final Logger log = LoggerFactory.getLogger(ServiceTrackServiceImpl.class);

    private final ServiceTrackMapper serviceTrackMapper;

    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

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
        track.setUserId(userId);
        track.setTrackType("CONSULT");
        track.setRelatedId(workOrderId);
        track.setTrackStatus("STARTED");
        track.setStartedAt(LocalDateTime.now());
        track.setCurrentStep("CONSULTING");

        Map<String, Object> data = new HashMap<>();
        data.put("consultContent", consultContent);
        data.put("workOrderId", workOrderId);
        track.setTrackData(toJson(data));

        serviceTrackMapper.insert(track);
        log.info("创建服务追踪(咨询): trackId={}, userId={}", track.getId(), userId);
        return convertToVO(track);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookService(Long trackId, Long userId, Object bookDTO) {
        ServiceTrack track = getAndValidateTrack(trackId);

        Map<String, Object> data = parseJson(track.getTrackData());
        if (data == null) data = new HashMap<>();
        data.put("bookedAt", LocalDateTime.now().toString());
        if (bookDTO != null) {
            Map<String, Object> bookMap = objectMapper.convertValue(bookDTO, new TypeReference<Map<String, Object>>() {});
            data.putAll(bookMap);
        }

        track.setCurrentStep("BOOKED");
        track.setTrackStatus("IN_PROGRESS");
        track.setTrackData(toJson(data));
        serviceTrackMapper.updateById(track);
        log.info("服务追踪-预约: trackId={}", trackId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(Long trackId, Long serviceCompanionId, String serviceCompanionName) {
        ServiceTrack track = getAndValidateTrack(trackId);

        Map<String, Object> data = parseJson(track.getTrackData());
        if (data == null) data = new HashMap<>();
        data.put("serviceStartedAt", LocalDateTime.now().toString());
        data.put("serviceCompanionId", serviceCompanionId);
        data.put("serviceCompanionName", serviceCompanionName);

        track.setCurrentStep("SERVICING");
        track.setTrackStatus("IN_PROGRESS");
        track.setTrackData(toJson(data));
        serviceTrackMapper.updateById(track);
        log.info("服务追踪-开始服务: trackId={}, companionId={}", trackId, serviceCompanionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endService(Long trackId, Object endDTO) {
        ServiceTrack track = getAndValidateTrack(trackId);

        Map<String, Object> data = parseJson(track.getTrackData());
        if (data == null) data = new HashMap<>();
        data.put("serviceEndedAt", LocalDateTime.now().toString());
        if (endDTO != null) {
            Map<String, Object> endMap = objectMapper.convertValue(endDTO, new TypeReference<Map<String, Object>>() {});
            data.putAll(endMap);
        }

        track.setCurrentStep("SERVICE_DONE");
        track.setTrackStatus("COMPLETED");
        track.setCompletedAt(LocalDateTime.now());
        if (data.containsKey("serviceDuration")) {
            track.setDurationSeconds(Integer.parseInt(data.get("serviceDuration").toString()) * 60);
        }
        track.setResult("SUCCESS");
        track.setTrackData(toJson(data));
        serviceTrackMapper.updateById(track);
        log.info("服务追踪-结束服务: trackId={}", trackId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(Long trackId, Integer customerRating, String customerFeedback) {
        ServiceTrack track = getAndValidateTrack(trackId);

        if (customerRating != null && (customerRating < 1 || customerRating > 5)) {
            throw new RuntimeException("评分必须在1-5之间");
        }

        Map<String, Object> data = parseJson(track.getTrackData());
        if (data == null) data = new HashMap<>();
        data.put("confirmedAt", LocalDateTime.now().toString());
        data.put("customerRating", customerRating);
        data.put("customerFeedback", customerFeedback);

        track.setCurrentStep("CONFIRMED");
        track.setTrackStatus("COMPLETED");
        track.setTrackData(toJson(data));
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
        wrapper.eq(ServiceTrack::getRelatedId, orderId);
        wrapper.eq(ServiceTrack::getTrackType, "CONSULT");
        wrapper.orderByDesc(ServiceTrack::getCreatedAt);
        List<ServiceTrack> tracks = serviceTrackMapper.selectList(wrapper);
        return convertList(tracks);
    }

    private void validateUserExists(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }
    }

    private ServiceTrack getAndValidateTrack(Long trackId) {
        if (trackId == null) {
            throw new RuntimeException("追踪ID不能为空");
        }
        ServiceTrack track = serviceTrackMapper.selectById(trackId);
        if (track == null) {
            throw new RuntimeException("服务追踪记录不存在: " + trackId);
        }
        return track;
    }

    private ServiceTrackVO convertToVO(ServiceTrack track) {
        if (track == null) return null;
        ServiceTrackVO vo = new ServiceTrackVO();
        vo.setId(track.getId());
        vo.setUserId(track.getUserId());
        vo.setTrackStatus(track.getTrackStatus());
        return vo;
    }

    private List<ServiceTrackVO> convertList(List<ServiceTrack> tracks) {
        List<ServiceTrackVO> result = new ArrayList<>(tracks.size());
        for (ServiceTrack track : tracks) {
            result.add(convertToVO(track));
        }
        return result;
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return "{}";
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            return new HashMap<>();
        }
    }
}
