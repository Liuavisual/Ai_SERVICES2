package com.delta.admin.controller;

import com.delta.common.dto.ChatTestSendDTO;
import com.delta.common.service.ChatTestService;
import com.delta.common.vo.ChatTestReplyVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天测试控制器，提供AI对话测试接口
 *
 * @author delta
 */
@Tag(name = "对话测试", description = "对话测试接口")
@RestController
@RequestMapping("/v1/chat-test")
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
public class ChatTestController {

    @Autowired
    private ChatTestService chatTestService;

    @Operation(summary = "发送测试消息")
    @PostMapping("/send")
    public Result<ChatTestReplyVO> sendMessage(@Valid @RequestBody ChatTestSendDTO sendDTO) {
        ChatTestReplyVO reply = chatTestService.sendMessage(sendDTO);
        return Result.success(reply);
    }
}
