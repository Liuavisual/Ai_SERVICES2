package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.service.MessageService;
import com.delta.common.vo.MessageVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息管理", description = "消息管理接口")
@RestController
@RequestMapping("/messages")
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;

    @Operation(summary = "分页查询消息记录")
    @GetMapping("/page")
    public Result<Page<MessageVO>> getMessagePage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "isAi", required = false) Boolean isAi,
            @RequestParam(name = "keywordTriggered", required = false) Boolean keywordTriggered,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        Long decodedUserId = userId != null ? decodeId(userId) : null;
        String role = getCurrentUserRole(request);
        if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
            decodedUserId = getCurrentUserId(request);
        }
        Page<MessageVO> page = messageService.getMessagePage(pageNum, pageSize, decodedUserId, platform, direction, isAi, keywordTriggered, keyword);
        return Result.success(page);
    }
}
