package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.dto.ReplyDTO;
import com.delta.common.service.CacheService;
import com.delta.common.service.ReplyService;
import com.delta.common.vo.ReplyVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 自动回复管理控制器
 *
 * @author delta
 */
@Tag(name = "回复话术管理", description = "回复话术管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/replies")
@RequiredArgsConstructor
public class ReplyController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ReplyController.class);

    private final ReplyService replyService;

    private final CacheService cacheService;

    @Operation(summary = "分页查询回复话术")
    @GetMapping("/page")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Page<ReplyVO>> getReplyPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "triggerType", required = false) String triggerType) {
        Page<ReplyVO> pageResult = replyService.getReplyPage(page, size, triggerType);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取回复话术详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<ReplyVO> getReplyById(@PathVariable("id") String id) {
        ReplyVO replyVO = replyService.getReplyById(decodeId(id));
        return Result.success(replyVO);
    }

    @Operation(summary = "创建回复话术")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> createReply(@Valid @RequestBody ReplyDTO replyDTO) {
        replyService.createReply(replyDTO);
        log.info("创建回复话术，刷新回复话术缓存");
        cacheService.reloadReplies();
        return Result.success();
    }

    @Operation(summary = "更新回复话术")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> updateReply(@Valid @RequestBody ReplyDTO replyDTO) {
        replyService.updateReply(replyDTO);
        log.info("更新回复话术，刷新回复话术缓存");
        cacheService.reloadReplies();
        return Result.success();
    }

    @Operation(summary = "删除回复话术")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> deleteReply(@PathVariable("id") String id) {
        replyService.deleteReply(decodeId(id));
        log.info("删除回复话术，刷新回复话术缓存");
        cacheService.reloadReplies();
        return Result.success();
    }

    @Operation(summary = "导出回复话术Excel")
    @GetMapping("/export")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "triggerType", required = false) String triggerType) {
        replyService.exportReplies(response, triggerType);
    }

    @Operation(summary = "导入回复话术Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = replyService.importReplies(file);
        cacheService.reloadReplies();
        return Result.success(result.toMap());
    }
}
