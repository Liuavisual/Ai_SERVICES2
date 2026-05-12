package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.annotation.AuditLog;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.CustomerSatisfactionDTO;
import com.delta.common.service.CustomerSatisfactionService;
import com.delta.common.vo.CustomerSatisfactionVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 客户满意度评价Controller
 *
 * @author 刘建国
 */
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/satisfaction")
@RequiredArgsConstructor
@Tag(name = "客户满意度评价")
public class CustomerSatisfactionController {

    /** 满意度评价服务 */
    private final CustomerSatisfactionService satisfactionService;

    /**
     * 提交满意度评价
     *
     * @param dto 评价数据
     * @return 评价视图对象
     */
    @Operation(summary = "提交满意度评价")
    @PostMapping
    @AuditLog(module = "满意度评价", action = "提交评价")
    public Result<CustomerSatisfactionVO> submitSatisfaction(@Valid @RequestBody CustomerSatisfactionDTO dto) {
        // 从安全上下文获取当前登录用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (auth != null && auth.getPrincipal() instanceof Long) {
            userId = (Long) auth.getPrincipal();
        }
        return Result.success(satisfactionService.submitSatisfaction(userId, dto));
    }

    /**
     * 获取满意度评价列表（分页）
     *
     * @param page        页码
     * @param size        每页大小
     * @param companionId 陪玩师ID（混淆后的ID，可选）
     * @param minRating   最低评分（可选）
     * @param maxRating   最高评分（可选）
     * @return 分页评价视图对象
     */
    @Operation(summary = "获取满意度评价列表")
    @GetMapping("/page")
    public Result<Page<CustomerSatisfactionVO>> getSatisfactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DecodeId(required = false) Long companionId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating) {
        return Result.success(satisfactionService.getSatisfactions(page, size, companionId, minRating, maxRating));
    }

    /**
     * 获取陪玩师平均评分
     *
     * @param companionId 陪玩师ID（混淆后的ID）
     * @return 平均评分
     */
    @Operation(summary = "获取陪玩师平均评分")
    @GetMapping("/average/{companionId}")
    public Result<Double> getAverageRating(@PathVariable @DecodeId Long companionId) {
        return Result.success(satisfactionService.getAverageRating(companionId));
    }

    @Operation(summary = "通过订单提交评价(含陪玩师评分联动)")
    @PostMapping("/order-review")
    @PermAuth("customer_satisfaction:edit")
    @AuditLog(module = "满意度评价", action = "订单评价")
    public Result<CustomerSatisfactionVO> submitOrderReview(
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam Long companionId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false, defaultValue = "0") Integer isAnonymous) {
        return Result.success(satisfactionService.submitOrderReview(userId, orderId, companionId, rating, feedback, tags, isAnonymous));
    }
}
