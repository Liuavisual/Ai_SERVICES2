package com.delta.common.service;

import com.delta.common.vo.ServiceTrackVO;

import java.util.List;

public interface ServiceTrackService {

    ServiceTrackVO getById(Long id);

    ServiceTrackVO createConsult(Long userId, Long workOrderId, String consultContent);

    void bookService(Long trackId, Long userId, Object bookDTO);

    void startService(Long trackId, Long serviceCompanionId, String serviceCompanionName);

    void endService(Long trackId, Object endDTO);

    void submitRating(Long trackId, Integer customerRating, String customerFeedback);

    List<ServiceTrackVO> listByUserId(Long userId);

    List<ServiceTrackVO> listByOrderId(Long orderId);
}
