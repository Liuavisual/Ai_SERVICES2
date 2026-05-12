package com.delta.admin.controller;

import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.CustomerLifecycleService;
import com.delta.common.vo.CustomerVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户生命周期管理Controller
 * <p>
 * 提供客户流失风险预警、已流失客户查询和生命周期标签更新等接口。</p>
 *
 * @author 刘建国
 */
@Tag(name = "客户生命周期管理", description = "客户生命周期管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/customer-lifecycle")
@RequiredArgsConstructor
@PermAuth("customer_lifecycle:view")
public class CustomerLifecycleController {

    /** 客户生命周期服务 */
    private final CustomerLifecycleService lifecycleService;

    /**
     * 获取流失风险客户列表
     *
     * @return 流失风险客户VO列表
     */
    @Operation(summary = "获取流失风险客户列表")
    @GetMapping("/at-risk")
    public Result<List<CustomerVO>> getAtRiskCustomers() {
        return Result.success(lifecycleService.getAtRiskCustomers());
    }

    /**
     * 获取已流失客户列表
     *
     * @return 已流失客户VO列表
     */
    @Operation(summary = "获取已流失客户列表")
    @GetMapping("/churned")
    public Result<List<CustomerVO>> getChurnedCustomers() {
        return Result.success(lifecycleService.getChurnedCustomers());
    }

    /**
     * 判断客户生命周期阶段
     *
     * @param userId 混淆后的客户用户ID
     * @return 生命周期阶段标识
     */
    @Operation(summary = "判断客户生命周期阶段")
    @GetMapping("/stage/{userId}")
    public Result<String> getLifecycleStage(@PathVariable @DecodeId Long userId) {
        return Result.success(lifecycleService.determineLifecycleStage(userId));
    }

    /**
     * 手动触发更新客户生命周期标签
     *
     * @return 操作结果
     */
    @Operation(summary = "手动触发更新客户生命周期标签")
    @PostMapping("/update-tags")
    @PermAuth("customer_lifecycle:edit")
    public Result<Void> updateLifecycleTags() {
        lifecycleService.updateCustomerLifecycleTags();
        return Result.success();
    }
}
