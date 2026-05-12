package com.delta.admin.controller;

import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.entity.OrderStatusHistory;
import com.delta.common.mapper.OrderStatusHistoryMapper;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单状态变更历史控制器
 *
 * @author 刘建国
 */
@RequiredArgsConstructor
@Tag(name = "订单状态历史")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/orders")
@PermAuth("order_status_history:view")
public class OrderStatusHistoryController {

    private final OrderStatusHistoryMapper orderStatusHistoryMapper;

    @Operation(summary = "查询订单状态变更历史")
    @GetMapping("/{id}/status-history")
    public Result<List<OrderStatusHistory>> getStatusHistory(@PathVariable @DecodeId Long id) {
        List<OrderStatusHistory> histories = orderStatusHistoryMapper.selectByOrderId(id);
        return Result.success(histories);
    }
}