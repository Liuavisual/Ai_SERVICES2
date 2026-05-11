package com.delta.admin.controller;

import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.ChatTestSendDTO;
import com.delta.common.entity.GameKnowledge;
import com.delta.common.service.ChatTestService;
import com.delta.common.service.GameKnowledgeService;
import com.delta.common.vo.ChatTestReplyVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天测试与知识库控制器，提供AI对话测试和游戏知识库搜索接口
 *
 * @author 刘建国
 */
@RequiredArgsConstructor
@Tag(name = "对话测试与知识库", description = "AI对话测试接口及游戏知识库接口")
@RestController
@RequestMapping(ApiVersionConstants.V1)
@PermAuth("chat:view")
public class ChatTestController {

    private final ChatTestService chatTestService;

    /** 游戏知识库服务 */
    private final GameKnowledgeService gameKnowledgeService;

    @Operation(summary = "发送测试消息")
    @PostMapping("/chat-test/send")
    public Result<ChatTestReplyVO> sendMessage(@Valid @RequestBody ChatTestSendDTO sendDTO) {
        ChatTestReplyVO reply = chatTestService.sendMessage(sendDTO);
        return Result.success(reply);
    }

    /**
     * 知识库搜索端点
     * <p>
     * 根据查询关键词在游戏知识库中进行全文搜索，
     * 匹配标题、关键词和内容字段。返回已启用的知识条目。
     * </p>
     *
     * @param query 搜索关键词
     * @return 匹配的知识条目列表
     */
    @Operation(summary = "知识库搜索", description = "根据关键词搜索游戏知识库内容")
    @GetMapping("/knowledge/search")
    public Result<List<GameKnowledge>> searchKnowledge(@RequestParam String query) {
        List<GameKnowledge> results = gameKnowledgeService.searchKnowledge(query);
        return Result.success(results);
    }

    /**
     * 知识库分类端点
     * <p>
     * 返回知识库中所有可用的分类列表（去重）。
     * 包括游戏规则、角色攻略、操作指南、故障排除等。
     * </p>
     *
     * @return 分类名称列表
     */
    @Operation(summary = "知识库分类", description = "获取游戏知识库的所有分类列表")
    @GetMapping("/knowledge/categories")
    public Result<List<String>> getCategories() {
        List<String> categories = gameKnowledgeService.getAllCategories();
        return Result.success(categories);
    }
}
