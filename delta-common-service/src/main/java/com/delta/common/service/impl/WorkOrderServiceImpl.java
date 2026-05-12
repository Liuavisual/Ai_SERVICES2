package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.WorkOrderConstants;
import com.delta.common.dto.*;
import com.delta.common.entity.*;
import com.delta.common.enums.ServiceTrackStatusEnum;
import com.delta.common.enums.WorkOrderPriorityEnum;
import com.delta.common.enums.WorkOrderStatusEnum;
import com.delta.common.enums.WorkOrderTypeEnum;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.*;
import com.delta.common.service.WorkOrderService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderRecordMapper workOrderRecordMapper;
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;
    private final ServiceTrackMapper serviceTrackMapper;
    private final UserMapper userMapper;
    private final SysUserMapper sysUserMapper;
    private final CompanionMapper companionMapper;
    private final CsUserCustomerMapper csUserCustomerMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Page<WorkOrderVO> getWorkOrderPage(Integer page, Integer size, String status, String orderType, String priority, String platform, String keyword) {
        return getWorkOrderPage(page, size, status, orderType, priority, platform, keyword, null, null);
    }

    @Override
    public Page<WorkOrderVO> getWorkOrderPage(Integer page, Integer size, String status, String orderType, String priority, String platform, String keyword, Long currentUserId, String currentUserRole) {
        LambdaQueryWrapper<WorkOrder> wrapper = buildQueryWrapper(status, orderType, priority, platform, keyword);
        applyDataScope(wrapper, currentUserId, currentUserRole);
        wrapper.orderByDesc(WorkOrder::getCreatedAt);

        Page<WorkOrder> pageResult = workOrderMapper.selectPage(new Page<>(page, size), wrapper);
        Page<WorkOrderVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(buildVOList(pageResult.getRecords()));
        VoUtils.setRowNumbers(voPage);
        return voPage;
    }

    @Override
    public WorkOrderVO getWorkOrderDetail(Long id) {
        return getWorkOrderDetail(id, null, null);
    }

    @Override
    public WorkOrderVO getWorkOrderDetail(Long id, Long currentUserId, String currentUserRole) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        checkPermission(order, currentUserId, currentUserRole, "VIEW");
        return buildDetailVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrder(WorkOrderCreateDTO dto) {
        WorkOrder order = new WorkOrder();
        order.setOrderNo(generateOrderNo());
        order.setOrderType(dto.getOrderType());
        order.setPriority(dto.getPriority());
        order.setPlatform(dto.getPlatform());
        order.setUserId(dto.getUserId());
        order.setServiceType(dto.getServiceType());
        order.setProblemDetail(dto.getProblemDetail());
        order.setProblemCategory(dto.getProblemCategory());
        order.setTriggerKeyword(dto.getTriggerKeyword());
        order.setContextSummary(dto.getContextSummary());
        order.setRelatedCompanionId(dto.getRelatedCompanionId());
        order.setAssignedCsUserId(dto.getAssignedCsUserId());
        order.setStatus(WorkOrderConstants.STATUS_NEW);
        order.setEscalationLevel(0);
        order.setReminderCount(0);

        enrichCustomerInfo(order, dto.getUserId());
        enrichCsUserInfo(order, dto.getAssignedCsUserId());
        enrichCompanionInfo(order, dto.getRelatedCompanionId());
        setDeadline(order);

        workOrderMapper.insert(order);
        addSystemRecord(order.getId(), "工单创建，来源：" + dto.getPlatform());

        if (WorkOrderConstants.TYPE_BOOKING.equals(dto.getOrderType())
                || WorkOrderConstants.TYPE_SERVICE_TRACK.equals(dto.getOrderType())) {
            createServiceTrack(order.getId(), dto.getUserId(), dto.getContextSummary());
        }

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkOrder(Long id, WorkOrderCreateDTO dto, Long currentUserId, String currentUserRole) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        checkPermission(order, currentUserId, currentUserRole, "EDIT");

        if (dto.getOrderType() != null) order.setOrderType(dto.getOrderType());
        if (dto.getPriority() != null) order.setPriority(dto.getPriority());
        if (dto.getServiceType() != null) order.setServiceType(dto.getServiceType());
        if (dto.getProblemDetail() != null) order.setProblemDetail(dto.getProblemDetail());
        if (dto.getProblemCategory() != null) order.setProblemCategory(dto.getProblemCategory());
        if (dto.getRelatedCompanionId() != null) {
            order.setRelatedCompanionId(dto.getRelatedCompanionId());
            enrichCompanionInfo(order, dto.getRelatedCompanionId());
        }

        workOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptWorkOrder(Long id, Long currentUserId, String currentUserName) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderConstants.STATUS_NEW.equals(order.getStatus())) {
            throw new BusinessException("仅新建状态的工单可以接手");
        }

        String oldStatus = order.getStatus();
        order.setStatus(WorkOrderConstants.STATUS_PROCESSING);
        order.setHandlerId(currentUserId);
        order.setHandlerName(currentUserName);
        workOrderMapper.updateById(order);

        addStatusChangeRecord(order.getId(), currentUserId, currentUserName, "客服接手工单", oldStatus, WorkOrderConstants.STATUS_PROCESSING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitWorkOrder(Long id, WorkOrderSubmitDTO dto, Long currentUserId, String currentUserRole) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        checkPermission(order, currentUserId, currentUserRole, "SUBMIT");
        if (!WorkOrderConstants.STATUS_PROCESSING.equals(order.getStatus())) {
            throw new BusinessException("仅处理中状态的工单可以提交结果");
        }
        validateHandleResult(dto.getHandleResult());

        String oldStatus = order.getStatus();
        order.setStatus(WorkOrderConstants.STATUS_PENDING_CONFIRM);
        order.setHandleResult(dto.getHandleResult());
        workOrderMapper.updateById(order);

        addStatusChangeRecord(order.getId(), currentUserId, order.getHandlerName(), "客服提交处理结果", oldStatus, WorkOrderConstants.STATUS_PENDING_CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmWorkOrder(Long id, WorkOrderConfirmDTO dto) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderConstants.STATUS_PENDING_CONFIRM.equals(order.getStatus())) {
            throw new BusinessException("仅待确认状态的工单可以确认");
        }
        if (dto.getSatisfactionScore() < 1 || dto.getSatisfactionScore() > 5) {
            throw new BusinessException("满意度评分必须在1-5之间");
        }

        order.setStatus(WorkOrderConstants.STATUS_COMPLETED);
        order.setResolvedAt(LocalDateTime.now());
        order.setSatisfactionScore(dto.getSatisfactionScore());
        order.setSatisfactionRemark(dto.getSatisfactionRemark());
        workOrderMapper.updateById(order);

        addSystemRecord(order.getId(), "客户确认完成，满意度评分：" + dto.getSatisfactionScore());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeWorkOrder(Long id, String closeReason, Long currentUserId, String currentUserRole) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        checkPermission(order, currentUserId, currentUserRole, "CLOSE");

        String oldStatus = order.getStatus();
        order.setStatus(WorkOrderConstants.STATUS_CLOSED);
        order.setClosedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        addStatusChangeRecord(order.getId(), currentUserId, null, "工单关闭" + (closeReason != null ? "：" + closeReason : ""), oldStatus, WorkOrderConstants.STATUS_CLOSED);

        terminateServiceTrackIfExists(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWorkOrder(Long id, String cancelReason, Long currentUserId, String currentUserRole) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderConstants.STATUS_NEW.equals(order.getStatus()) && !WorkOrderConstants.STATUS_PROCESSING.equals(order.getStatus())) {
            throw new BusinessException("仅新建或处理中的工单可以取消");
        }
        checkPermission(order, currentUserId, currentUserRole, "CANCEL");

        String oldStatus = order.getStatus();
        order.setStatus(WorkOrderConstants.STATUS_CANCELLED);
        workOrderMapper.updateById(order);

        addStatusChangeRecord(order.getId(), currentUserId, null, "工单取消：" + cancelReason, oldStatus, WorkOrderConstants.STATUS_CANCELLED);

        terminateServiceTrackIfExists(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reopenWorkOrder(Long id, String reopenReason, Long currentUserId) {
        if (!BusinessStatusConstants.ROLE_SYS_ADMIN.equals(getUserRole(currentUserId))) {
            throw new BusinessException("仅系统管理员可以重新打开工单");
        }
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderConstants.STATUS_CLOSED.equals(order.getStatus())) {
            throw new BusinessException("仅已关闭的工单可以重新打开");
        }

        String oldStatus = order.getStatus();
        order.setStatus(WorkOrderConstants.STATUS_PROCESSING);
        order.setResolvedAt(null);
        order.setClosedAt(null);
        workOrderMapper.updateById(order);

        addStatusChangeRecord(order.getId(), currentUserId, null, "工单重新打开：" + reopenReason, oldStatus, WorkOrderConstants.STATUS_PROCESSING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(Long id, WorkOrderRecordDTO dto, Long operatorId, String operatorName, String operatorRole) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(id);
        record.setRecordType(dto.getRecordType());
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setOperatorRole(operatorRole);
        record.setContent(dto.getContent());
        workOrderRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookServiceTrack(Long id, ServiceTrackBookDTO dto) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        ServiceTrack track = getOrCreateServiceTrack(id, order.getUserId());
        if (!WorkOrderConstants.TRACK_STATUS_CONSULTING.equals(track.getTrackStatus())) {
            throw new BusinessException("仅咨询中状态可以确认预约");
        }

        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_BOOKED);
        track.setBookedAt(LocalDateTime.now());
        track.setBookedCompanionId(dto.getBookedCompanionId());
        track.setBookedCompanionName(dto.getBookedCompanionName());
        track.setBookedServiceType(dto.getBookedServiceType());
        track.setBookedTimeSlot(dto.getBookedTimeSlot());
        track.setRelatedOrderId(dto.getRelatedOrderId());
        serviceTrackMapper.updateById(track);

        order.setServiceStatus(WorkOrderConstants.SERVICE_STATUS_PRE_SERVICE);
        if (dto.getRelatedOrderId() != null) {
            order.setRelatedOrderId(dto.getRelatedOrderId());
        }
        workOrderMapper.updateById(order);

        addSystemRecord(id, "确认预约，陪玩师：" + dto.getBookedCompanionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startServiceTrack(Long id, Long companionId, String companionName) {
        ServiceTrack track = getServiceTrackByWorkOrderId(id);
        if (!WorkOrderConstants.TRACK_STATUS_BOOKED.equals(track.getTrackStatus())) {
            throw new BusinessException("仅已预约状态可以开始服务");
        }

        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICING);
        track.setServiceStartedAt(LocalDateTime.now());
        track.setServiceCompanionId(companionId);
        track.setServiceCompanionName(companionName);
        serviceTrackMapper.updateById(track);

        WorkOrder order = workOrderMapper.selectById(id);
        order.setServiceStatus(WorkOrderConstants.SERVICE_STATUS_IN_SERVICE);
        workOrderMapper.updateById(order);

        addSystemRecord(id, "服务开始，陪玩师：" + companionName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endServiceTrack(Long id, ServiceTrackEndDTO dto) {
        ServiceTrack track = getServiceTrackByWorkOrderId(id);
        if (!WorkOrderConstants.TRACK_STATUS_SERVICING.equals(track.getTrackStatus())) {
            throw new BusinessException("仅服务中状态可以结束服务");
        }

        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_SERVICE_DONE);
        track.setServiceEndedAt(LocalDateTime.now());
        track.setServiceCompanionId(dto.getServiceCompanionId());
        track.setServiceCompanionName(dto.getServiceCompanionName());
        track.setServiceDuration(dto.getServiceDuration());
        track.setServiceResult(dto.getServiceResult());
        serviceTrackMapper.updateById(track);

        WorkOrder order = workOrderMapper.selectById(id);
        order.setServiceStatus(WorkOrderConstants.SERVICE_STATUS_POST_SERVICE);
        workOrderMapper.updateById(order);

        addSystemRecord(id, "服务结束，时长：" + dto.getServiceDuration() + "分钟");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmServiceTrack(Long id, Integer customerRating, String customerFeedback) {
        ServiceTrack track = getServiceTrackByWorkOrderId(id);
        if (!WorkOrderConstants.TRACK_STATUS_SERVICE_DONE.equals(track.getTrackStatus())) {
            throw new BusinessException("仅服务完成状态可以客户确认");
        }

        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONFIRMED);
        track.setConfirmedAt(LocalDateTime.now());
        track.setCustomerRating(customerRating);
        track.setCustomerFeedback(customerFeedback);
        serviceTrackMapper.updateById(track);

        addSystemRecord(id, "客户确认服务，评分：" + customerRating);
    }

    @Override
    public Long getPendingCount(Long currentUserId, String currentUserRole) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkOrder::getStatus, WorkOrderConstants.STATUS_NEW, WorkOrderConstants.STATUS_PROCESSING);
        applyDataScope(wrapper, currentUserId, currentUserRole);
        return workOrderMapper.selectCount(wrapper);
    }

    @Override
    public String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = WorkOrderConstants.ORDER_NO_SEQ_KEY_PREFIX + dateStr;
        try {
            Long seq = redisTemplate.opsForValue().increment(key);
            return WorkOrderConstants.ORDER_NO_PREFIX + dateStr + String.format("%04d", seq);
        } catch (Throwable t) {
            log.warn("【工单号生成】Redis不可用，降级使用时间戳+UUID | error={}", t.getMessage());
            long timeComponent = System.currentTimeMillis() % 100000;
            String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            return WorkOrderConstants.ORDER_NO_PREFIX + dateStr + String.format("%05d", timeComponent) + shortUuid.toUpperCase();
        }
    }

    private LambdaQueryWrapper<WorkOrder> buildQueryWrapper(String status, String orderType, String priority, String platform, String keyword) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        if (StringUtils.hasText(orderType)) {
            wrapper.eq(WorkOrder::getOrderType, orderType);
        }
        if (StringUtils.hasText(priority)) {
            wrapper.eq(WorkOrder::getPriority, priority);
        }
        if (StringUtils.hasText(platform)) {
            wrapper.eq(WorkOrder::getPlatform, platform);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(WorkOrder::getOrderNo, keyword)
                    .or().like(WorkOrder::getCustomerName, keyword)
                    .or().like(WorkOrder::getTriggerKeyword, keyword));
        }
        return wrapper;
    }

    private void applyDataScope(LambdaQueryWrapper<WorkOrder> wrapper, Long currentUserId, String currentUserRole) {
        if (currentUserId == null || currentUserRole == null) return;
        if (BusinessStatusConstants.ROLE_SYS_ADMIN.equals(currentUserRole)) return;

        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(currentUserRole)) {
            wrapper.and(w -> w.eq(WorkOrder::getAssignedCsUserId, currentUserId)
                    .or().eq(WorkOrder::getHandlerId, currentUserId)
                    .or().apply("user_id IN (SELECT customer_user_id FROM cs_user_customer WHERE cs_user_id = {0} AND status = 'ACTIVE')", currentUserId));
        }
    }

    private void checkPermission(WorkOrder order, Long currentUserId, String currentUserRole, String action) {
        if (currentUserId == null || currentUserRole == null) return;
        if (BusinessStatusConstants.ROLE_SYS_ADMIN.equals(currentUserRole)) return;

        if (BusinessStatusConstants.ROLE_CS_LEADER.equals(currentUserRole)) return;

        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(currentUserRole)) {
            boolean isAssigned = currentUserId.equals(order.getAssignedCsUserId());
            boolean isHandler = currentUserId.equals(order.getHandlerId());
            boolean isCustomerCs = csUserCustomerMapper.exists(
                    new LambdaQueryWrapper<CsUserCustomer>()
                            .eq(CsUserCustomer::getCsUserId, currentUserId)
                            .eq(CsUserCustomer::getCustomerUserId, order.getUserId())
                            .eq(CsUserCustomer::getStatus, BusinessStatusConstants.ASSIGN_STATUS_ACTIVE));

            if (!isAssigned && !isHandler && !isCustomerCs) {
                throw new BusinessException("无权操作此工单");
            }
            if (Set.of("DELETE", "EXPORT").contains(action)) {
                throw new BusinessException("无权执行此操作");
            }
        }
    }

    private void validateHandleResult(String handleResult) {
        if (handleResult == null || handleResult.isBlank()) {
            throw new BusinessException("处理结果不能为空");
        }
        if (handleResult.length() < WorkOrderConstants.HANDLE_RESULT_MIN_LENGTH) {
            throw new BusinessException("处理结果内容过短，请详细描述处理情况");
        }
    }

    private void enrichCustomerInfo(WorkOrder order, Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            order.setCustomerName(user.getNickname());
        }
    }

    private void enrichCsUserInfo(WorkOrder order, Long csUserId) {
        if (csUserId == null) return;
        SysUser csUser = sysUserMapper.selectById(csUserId);
        if (csUser != null) {
            order.setAssignedCsName(csUser.getRealName());
        }
    }

    private void enrichCompanionInfo(WorkOrder order, Long companionId) {
        if (companionId == null) return;
        Companion companion = companionMapper.selectById(companionId);
        if (companion != null) {
            order.setRelatedCompanionId(companionId);
        }
    }

    private void setDeadline(WorkOrder order) {
        WorkOrderPriorityEnum priority = WorkOrderPriorityEnum.fromCode(order.getPriority());
        order.setDeadline(LocalDateTime.now().plusMinutes(priority.getTimeoutMinutes()));
    }

    private void createServiceTrack(Long workOrderId, Long userId, String consultContent) {
        ServiceTrack track = new ServiceTrack();
        track.setWorkOrderId(workOrderId);
        track.setUserId(userId);
        track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
        track.setConsultStartedAt(LocalDateTime.now());
        track.setConsultContent(consultContent);
        serviceTrackMapper.insert(track);
    }

    private ServiceTrack getOrCreateServiceTrack(Long workOrderId, Long userId) {
        ServiceTrack track = serviceTrackMapper.selectOne(
                new LambdaQueryWrapper<ServiceTrack>().eq(ServiceTrack::getWorkOrderId, workOrderId));
        if (track == null) {
            track = new ServiceTrack();
            track.setWorkOrderId(workOrderId);
            track.setUserId(userId);
            track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_CONSULTING);
            track.setConsultStartedAt(LocalDateTime.now());
            serviceTrackMapper.insert(track);
        }
        return track;
    }

    private ServiceTrack getServiceTrackByWorkOrderId(Long workOrderId) {
        ServiceTrack track = serviceTrackMapper.selectOne(
                new LambdaQueryWrapper<ServiceTrack>().eq(ServiceTrack::getWorkOrderId, workOrderId));
        if (track == null) {
            throw new BusinessException("服务追踪记录不存在");
        }
        return track;
    }

    private void terminateServiceTrackIfExists(Long workOrderId) {
        ServiceTrack track = serviceTrackMapper.selectOne(
                new LambdaQueryWrapper<ServiceTrack>().eq(ServiceTrack::getWorkOrderId, workOrderId));
        if (track != null
                && !WorkOrderConstants.TRACK_STATUS_CONFIRMED.equals(track.getTrackStatus())
                && !WorkOrderConstants.TRACK_STATUS_TERMINATED.equals(track.getTrackStatus())) {
            track.setTrackStatus(WorkOrderConstants.TRACK_STATUS_TERMINATED);
            serviceTrackMapper.updateById(track);
            log.info("【工单联动】工单 {} 关闭/取消，关联ServiceTrack({})状态同步为TERMINATED", workOrderId, track.getId());
        }
    }

    private void addSystemRecord(Long workOrderId, String content) {
        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(workOrderId);
        record.setRecordType(WorkOrderConstants.RECORD_TYPE_SYSTEM_LOG);
        record.setOperatorName("系统");
        record.setContent(content);
        workOrderRecordMapper.insert(record);
    }

    private void addStatusChangeRecord(Long workOrderId, Long operatorId, String operatorName, String content, String oldStatus, String newStatus) {
        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(workOrderId);
        record.setRecordType(WorkOrderConstants.RECORD_TYPE_STATUS_CHANGE);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setContent(content);
        record.setOldStatus(oldStatus);
        record.setNewStatus(newStatus);
        workOrderRecordMapper.insert(record);
    }

    private String getUserRole(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getRole() : null;
    }

    private List<WorkOrderVO> buildVOList(List<WorkOrder> orders) {
        if (orders.isEmpty()) return Collections.emptyList();
        return orders.stream().map(this::buildSimpleVO).collect(Collectors.toList());
    }

    private WorkOrderVO buildSimpleVO(WorkOrder order) {
        WorkOrderVO vo = new WorkOrderVO();
        copyToVO(order, vo);
        return vo;
    }

    private WorkOrderVO buildDetailVO(WorkOrder order) {
        WorkOrderVO vo = new WorkOrderVO();
        copyToVO(order, vo);

        List<WorkOrderRecord> records = workOrderRecordMapper.selectList(
                new LambdaQueryWrapper<WorkOrderRecord>()
                        .eq(WorkOrderRecord::getWorkOrderId, order.getId())
                        .orderByAsc(WorkOrderRecord::getCreatedAt));
        vo.setRecords(records.stream().map(this::toRecordVO).collect(Collectors.toList()));

        List<WorkOrderAttachment> attachments = workOrderAttachmentMapper.selectList(
                new LambdaQueryWrapper<WorkOrderAttachment>()
                        .eq(WorkOrderAttachment::getWorkOrderId, order.getId()));
        vo.setAttachments(attachments.stream().map(this::toAttachmentVO).collect(Collectors.toList()));

        ServiceTrack track = serviceTrackMapper.selectOne(
                new LambdaQueryWrapper<ServiceTrack>()
                        .eq(ServiceTrack::getWorkOrderId, order.getId()));
        if (track != null) {
            vo.setServiceTrack(toServiceTrackVO(track));
        }

        return vo;
    }

    private void copyToVO(WorkOrder order, WorkOrderVO vo) {
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType(order.getOrderType());
        vo.setOrderTypeDesc(Optional.ofNullable(WorkOrderTypeEnum.fromCode(order.getOrderType())).map(WorkOrderTypeEnum::getDesc).orElse(""));
        vo.setPriority(order.getPriority());
        vo.setPriorityDesc(Optional.ofNullable(WorkOrderPriorityEnum.fromCode(order.getPriority())).map(WorkOrderPriorityEnum::getDesc).orElse(""));
        vo.setPlatform(order.getPlatform());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(Optional.ofNullable(WorkOrderStatusEnum.fromCode(order.getStatus())).map(WorkOrderStatusEnum::getDesc).orElse(""));
        vo.setUserId(order.getUserId());
        vo.setCustomerName(order.getCustomerName());
        vo.setCustomerContact(order.getCustomerContact());
        vo.setCustomerLevel(order.getCustomerLevel());
        vo.setServiceType(order.getServiceType());
        vo.setServiceStatus(order.getServiceStatus());
        vo.setProblemDetail(order.getProblemDetail());
        vo.setProblemCategory(order.getProblemCategory());
        vo.setTriggerKeyword(order.getTriggerKeyword());
        vo.setContextSummary(order.getContextSummary());
        vo.setAssignedCsUserId(order.getAssignedCsUserId());
        vo.setAssignedCsName(order.getAssignedCsName());
        vo.setHandlerId(order.getHandlerId());
        vo.setHandlerName(order.getHandlerName());
        vo.setHandleResult(order.getHandleResult());
        vo.setDeadline(order.getDeadline());
        vo.setEscalationLevel(order.getEscalationLevel());
        vo.setReminderCount(order.getReminderCount());
        vo.setRelatedOrderId(order.getRelatedOrderId());
        vo.setRelatedCompanionId(order.getRelatedCompanionId());
        vo.setSatisfactionScore(order.getSatisfactionScore());
        vo.setSatisfactionRemark(order.getSatisfactionRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        vo.setResolvedAt(order.getResolvedAt());
        vo.setClosedAt(order.getClosedAt());
    }

    private WorkOrderRecordVO toRecordVO(WorkOrderRecord record) {
        WorkOrderRecordVO vo = new WorkOrderRecordVO();
        vo.setId(record.getId());
        vo.setRecordType(record.getRecordType());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setOperatorRole(record.getOperatorRole());
        vo.setContent(record.getContent());
        vo.setOldStatus(record.getOldStatus());
        vo.setNewStatus(record.getNewStatus());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    private WorkOrderAttachmentVO toAttachmentVO(WorkOrderAttachment attachment) {
        WorkOrderAttachmentVO vo = new WorkOrderAttachmentVO();
        vo.setId(attachment.getId());
        vo.setWorkOrderId(attachment.getWorkOrderId());
        vo.setRecordId(attachment.getRecordId());
        vo.setFileName(attachment.getFileName());
        vo.setFilePath(attachment.getFilePath());
        vo.setFileType(attachment.getFileType());
        vo.setFileSize(attachment.getFileSize());
        vo.setUploadedBy(attachment.getUploadedBy());
        return vo;
    }

    private ServiceTrackVO toServiceTrackVO(ServiceTrack track) {
        ServiceTrackVO vo = new ServiceTrackVO();
        vo.setId(track.getId());
        vo.setWorkOrderId(track.getWorkOrderId());
        vo.setUserId(track.getUserId());
        vo.setTrackStatus(track.getTrackStatus());
        vo.setTrackStatusDesc(Optional.ofNullable(ServiceTrackStatusEnum.fromCode(track.getTrackStatus())).map(ServiceTrackStatusEnum::getDesc).orElse(""));
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
}
