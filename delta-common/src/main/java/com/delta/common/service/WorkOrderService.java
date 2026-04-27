package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.*;
import com.delta.common.vo.WorkOrderVO;

public interface WorkOrderService {

    Page<WorkOrderVO> getWorkOrderPage(Integer pageNum, Integer pageSize, String status, String orderType, String priority, String platform, String keyword);

    Page<WorkOrderVO> getWorkOrderPage(Integer pageNum, Integer pageSize, String status, String orderType, String priority, String platform, String keyword, Long currentUserId, String currentUserRole);

    WorkOrderVO getWorkOrderDetail(Long id);

    WorkOrderVO getWorkOrderDetail(Long id, Long currentUserId, String currentUserRole);

    Long createWorkOrder(WorkOrderCreateDTO dto);

    void updateWorkOrder(Long id, WorkOrderCreateDTO dto, Long currentUserId, String currentUserRole);

    void acceptWorkOrder(Long id, Long currentUserId, String currentUserName);

    void submitWorkOrder(Long id, WorkOrderSubmitDTO dto, Long currentUserId, String currentUserRole);

    void confirmWorkOrder(Long id, WorkOrderConfirmDTO dto);

    void closeWorkOrder(Long id, String closeReason, Long currentUserId, String currentUserRole);

    void cancelWorkOrder(Long id, String cancelReason, Long currentUserId, String currentUserRole);

    void reopenWorkOrder(Long id, String reopenReason, Long currentUserId);

    void addRecord(Long id, WorkOrderRecordDTO dto, Long operatorId, String operatorName, String operatorRole);

    void bookServiceTrack(Long id, ServiceTrackBookDTO dto);

    void startServiceTrack(Long id, Long companionId, String companionName);

    void endServiceTrack(Long id, ServiceTrackEndDTO dto);

    void confirmServiceTrack(Long id, Integer customerRating, String customerFeedback);

    Long getPendingCount(Long currentUserId, String currentUserRole);

    String generateOrderNo();
}
