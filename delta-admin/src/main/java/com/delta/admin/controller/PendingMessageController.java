package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.PendingMessageHandleDTO;
import com.delta.common.service.PendingMessageService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.PendingMessageVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 待处理消息管理控制器
 * <p>
 * 提供待处理消息的分页查询、处理和计数功能。
 * 隐私保护策略：
 * <ul>
 *   <li>CS_STAFF（普通客服）：只能查看分配给自己的客户的待处理消息</li>
 *   <li>CS_LEADER（客服负责人）：可以查看本团队所有待处理消息</li>
 *   <li>SYS_ADMIN（系统管理员）：可以查看所有待处理消息</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Tag(name = "待处理消息管理", description = "待处理消息管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/pending-messages")
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
@RequiredArgsConstructor
public class PendingMessageController extends BaseController {

    private final PendingMessageService pendingMessageService;

    /**
     * 分页查询待处理消息
     * <p>
     * 根据当前登录用户的角色自动过滤数据范围：
     * - 普通客服：仅返回自己负责的客户消息
     * - 客服负责人/管理员：返回所有消息
     * </p>
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   消息状态过滤
     * @param platform 平台过滤
     * @param keyword  关键词过滤
     * @param request  HTTP 请求（用于获取当前用户信息）
     * @return 分页结果
     */
    @Operation(summary = "分页查询待处理消息")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<PendingMessageVO>> getPendingMessagePage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);

        Page<PendingMessageVO> pageResult = pendingMessageService.getPendingMessagePage(
                page, size, status, platform, keyword, currentUserId, currentUserRole);
        return Result.success(pageResult);
    }

    /**
     * 处理待处理消息
     * <p>
     * 权限要求：CS_LEADER或SYS_ADMIN可处理任何工单，CS_STAFF仅能接手自己被分配的工单。
     * </p>
     */
    @Operation(summary = "处理待处理消息")
    @PostMapping({"/handle", "/process"})
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Void> handlePendingMessage(@Valid @RequestBody PendingMessageHandleDTO handleDTO,
                                              HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        handleDTO.setHandledBy(currentUserId);
        pendingMessageService.handlePendingMessage(handleDTO, currentUserId, currentUserRole);
        return Result.success();
    }

    /**
     * 查询待处理消息详情
     * <p>
     * 根据ID查询单条待处理消息的详细信息。
     * </p>
     */
    @Operation(summary = "查询待处理消息详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<PendingMessageVO> getPendingMessageById(@PathVariable Long id) {
        PendingMessageVO vo = pendingMessageService.getPendingMessageById(id);
        if (vo != null) {
            return Result.success(vo);
        }
        return Result.error("待处理消息不存在");
    }

    /**
     * 获取当前用户的待处理消息数量
     * <p>
     * 普通客服仅统计自己负责的客户消息数量，管理员统计全部。
     * </p>
     */
    @Operation(summary = "获取待处理消息数量")
    @GetMapping("/count")
    public Result<Long> getPendingCount(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);

        Long count = pendingMessageService.getPendingCount(currentUserId, currentUserRole);
        return Result.success(count);
    }

    /**
     * 从请求属性中获取当前用户ID
     * <p>
     * JwtAuthenticationFilter 会在认证通过后将 userId 和 role 存入请求属性。
     * </p>
     */
    @Operation(summary = "导出待办事项Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "status", required = false) String status,
                            @RequestParam(name = "platform", required = false) String platform,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            HttpServletRequest request) throws IOException {
        Long currentUserId = getCurrentUserId(request);
        String currentUserRole = getCurrentUserRole(request);
        Page<PendingMessageVO> page = pendingMessageService.getPendingMessagePage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, status, platform, keyword, currentUserId, currentUserRole);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "编号");
        headers.put("userNickname", "客户昵称");
        headers.put("platform", "平台");
        headers.put("interventionTypeDesc", "介入类型");
        headers.put("messageContent", "原始消息");
        headers.put("statusDesc", "状态");
        headers.put("keyword", "触发关键词");
        headers.put("assignedCsUserName", "指派客服");
        headers.put("handledByName", "处理人");
        headers.put("remark", "备注");
        headers.put("createdAt", "创建时间");
        headers.put("handledAt", "处理时间");
        ExcelUtils.export(response, "待办事项", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("userNickname", item.getUserNickname());
            map.put("platform", item.getPlatform());
            map.put("interventionTypeDesc", item.getInterventionTypeDesc() != null ? item.getInterventionTypeDesc() : item.getInterventionType());
            map.put("messageContent", item.getMessageContent());
            map.put("statusDesc", item.getStatusDesc() != null ? item.getStatusDesc() : item.getStatus());
            map.put("keyword", item.getKeyword());
            map.put("assignedCsUserName", item.getAssignedCsUserName());
            map.put("handledByName", item.getHandledByName());
            map.put("remark", item.getRemark());
            map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
            map.put("handledAt", item.getHandledAt() != null ? item.getHandledAt().toString() : "");
            return map;
        });
    }
}
