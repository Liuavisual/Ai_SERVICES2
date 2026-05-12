package com.delta.admin.controller;

import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.service.MessageService;
import com.delta.common.vo.MessageVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息管理", description = "消息管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/messages")
@PermAuth("message:view")
@RequiredArgsConstructor
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Operation(summary = "分页查询消息记录")
    @GetMapping("/page")
    public Result<Page<MessageVO>> getMessagePage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "userId", required = false) @DecodeId(required = false) Long userId,
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "isAi", required = false) Boolean isAi,
            @RequestParam(name = "keywordTriggered", required = false) Boolean keywordTriggered,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
            userId = getCurrentUserId(request);
        }
        Page<MessageVO> pageResult = messageService.getMessagePage(page, size, userId, platform, direction, isAi, keywordTriggered, keyword);
        return Result.success(pageResult);
    }
}
