package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.WorkOrderConstants;
import com.delta.common.entity.WorkOrder;
import com.delta.common.entity.WorkOrderRecord;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.*;
import com.delta.common.vo.WorkOrderVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkOrderServiceImpl 单元测试
 * 验证工单服务的核心业务逻辑
 *
 * @author 刘建国
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class WorkOrderServiceImplTest {

    /** 工单Mapper */
    @Mock
    private WorkOrderMapper workOrderMapper;

    /** 工单操作记录Mapper */
    @Mock
    private WorkOrderRecordMapper workOrderRecordMapper;

    /** 工单附件Mapper */
    @Mock
    private WorkOrderAttachmentMapper workOrderAttachmentMapper;

    /** 服务追踪Mapper */
    @Mock
    private ServiceTrackMapper serviceTrackMapper;

    /** 客户Mapper */
    @Mock
    private UserMapper userMapper;

    /** 系统用户Mapper */
    @Mock
    private SysUserMapper sysUserMapper;

    /** 陪玩师Mapper */
    @Mock
    private CompanionMapper companionMapper;

    /** 客服-客户分配Mapper */
    @Mock
    private CsUserCustomerMapper csUserCustomerMapper;

    /** Redis模板 */
    @Mock
    private StringRedisTemplate redisTemplate;

    /** Redis值操作 */
    @Mock
    private ValueOperations<String, String> valueOperations;

    /** 被测服务实例 */
    @InjectMocks
    private WorkOrderServiceImpl workOrderService;

    /**
     * 测试分页查询工单 - 无过滤条件
     * 验证返回分页结果
     */
    @Test
    void getWorkOrderPage_shouldReturnPagedResults() {
        // 准备模拟数据
        Page<WorkOrder> mockPage = new Page<>(1, 10);
        mockPage.setRecords(java.util.Collections.emptyList());
        mockPage.setTotal(0);
        when(workOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // 执行测试
        Page<WorkOrderVO> result = workOrderService.getWorkOrderPage(1, 10, null, null, null, null, null);

        // 验证结果
        assertNotNull(result, "分页结果不应为null");
        assertEquals(0, result.getTotal(), "总记录数应为0");
        verify(workOrderMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    /**
     * 测试分页查询工单 - 带状态过滤条件
     * 验证过滤条件被正确应用
     */
    @Test
    void getWorkOrderPage_withStatusFilter_shouldApplyFilter() {
        // 准备模拟数据
        Page<WorkOrder> mockPage = new Page<>(1, 10);
        mockPage.setRecords(java.util.Collections.emptyList());
        mockPage.setTotal(0);
        when(workOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // 执行测试
        Page<WorkOrderVO> result = workOrderService.getWorkOrderPage(1, 10, "NEW", null, null, null, null);

        // 验证结果
        assertNotNull(result, "分页结果不应为null");
        verify(workOrderMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    /**
     * 测试接手工单 - 正常流程
     * 验证工单状态从NEW变为PROCESSING
     */
    @Test
    void acceptWorkOrder_shouldUpdateStatus() {
        // 准备模拟数据
        WorkOrder wo = new WorkOrder();
        wo.setId(1L);
        wo.setStatus(WorkOrderConstants.STATUS_NEW);
        when(workOrderMapper.selectById(1L)).thenReturn(wo);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(workOrderRecordMapper.insert(any(WorkOrderRecord.class))).thenReturn(1);

        // 执行测试
        workOrderService.acceptWorkOrder(1L, 100L, "测试客服");

        // 使用ArgumentCaptor验证状态更新
        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderMapper).updateById(captor.capture());
        WorkOrder updated = captor.getValue();
        assertEquals(WorkOrderConstants.STATUS_PROCESSING, updated.getStatus(), "状态应为PROCESSING");
        assertEquals(100L, updated.getHandlerId(), "处理人ID应为100");
        assertEquals("测试客服", updated.getHandlerName(), "处理人姓名应匹配");
    }

    /**
     * 测试接手工单 - 工单状态非NEW时应抛出异常
     * 验证仅新建状态的工单可以接手
     */
    @Test
    void acceptWorkOrder_wrongStatus_shouldThrow() {
        // 准备模拟数据 - 工单状态为CLOSED
        WorkOrder wo = new WorkOrder();
        wo.setId(1L);
        wo.setStatus(WorkOrderConstants.STATUS_CLOSED);
        when(workOrderMapper.selectById(1L)).thenReturn(wo);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> workOrderService.acceptWorkOrder(1L, 100L, "测试客服"),
                "非NEW状态工单接手应抛出BusinessException");
    }

    /**
     * 测试接手工单 - 工单不存在时应抛出异常
     */
    @Test
    void acceptWorkOrder_notExist_shouldThrow() {
        // 准备模拟数据 - 工单不存在
        when(workOrderMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> workOrderService.acceptWorkOrder(999L, 100L, "测试客服"),
                "不存在的工单接手应抛出BusinessException");
    }

    /**
     * 测试关闭工单 - 正常流程
     * 验证工单状态变为CLOSED
     */
    @Test
    void closeWorkOrder_shouldUpdateStatus() {
        // 准备模拟数据
        WorkOrder wo = new WorkOrder();
        wo.setId(1L);
        wo.setStatus(WorkOrderConstants.STATUS_PROCESSING);
        when(workOrderMapper.selectById(1L)).thenReturn(wo);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(workOrderRecordMapper.insert(any(WorkOrderRecord.class))).thenReturn(1);

        // 执行测试
        workOrderService.closeWorkOrder(1L, "问题已解决", 100L, "SYS_ADMIN");

        // 使用ArgumentCaptor验证状态更新
        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderMapper).updateById(captor.capture());
        assertEquals(WorkOrderConstants.STATUS_CLOSED, captor.getValue().getStatus(), "状态应为CLOSED");
    }

    /**
     * 测试取消工单 - 正常流程（NEW状态）
     * 验证工单状态变为CANCELLED
     */
    @Test
    void cancelWorkOrder_newStatus_shouldSucceed() {
        // 准备模拟数据
        WorkOrder wo = new WorkOrder();
        wo.setId(1L);
        wo.setStatus(WorkOrderConstants.STATUS_NEW);
        when(workOrderMapper.selectById(1L)).thenReturn(wo);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(workOrderRecordMapper.insert(any(WorkOrderRecord.class))).thenReturn(1);

        // 执行测试
        workOrderService.cancelWorkOrder(1L, "客户取消", 100L, "SYS_ADMIN");

        // 使用ArgumentCaptor验证状态更新
        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderMapper).updateById(captor.capture());
        assertEquals(WorkOrderConstants.STATUS_CANCELLED, captor.getValue().getStatus(), "状态应为CANCELLED");
    }

    /**
     * 测试取消工单 - COMPLETED状态不允许取消
     */
    @Test
    void cancelWorkOrder_completedStatus_shouldThrow() {
        // 准备模拟数据
        WorkOrder wo = new WorkOrder();
        wo.setId(1L);
        wo.setStatus(WorkOrderConstants.STATUS_COMPLETED);
        when(workOrderMapper.selectById(1L)).thenReturn(wo);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> workOrderService.cancelWorkOrder(1L, "尝试取消", 100L, "SYS_ADMIN"),
                "已完成工单取消应抛出BusinessException");
    }

    /**
     * 测试获取待处理工单数量
     * 验证返回正确的待处理数量
     */
    @Test
    void getPendingCount_shouldReturnCorrectCount() {
        // 准备模拟数据
        when(workOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // 执行测试
        Long count = workOrderService.getPendingCount(null, null);

        // 验证结果
        assertEquals(5L, count, "待处理工单数量应为5");
    }

    /**
     * 测试获取工单详情 - 工单不存在
     * 验证抛出BusinessException
     */
    @Test
    void getWorkOrderDetail_notExist_shouldThrow() {
        // 准备模拟数据
        when(workOrderMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> workOrderService.getWorkOrderDetail(999L),
                "不存在的工单应抛出BusinessException");
    }
}
