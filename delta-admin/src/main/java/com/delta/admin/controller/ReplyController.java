package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.ReplyDTO;
import com.delta.common.service.CacheService;
import com.delta.common.service.ReplyService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.ReplyVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动回复管理控制器
 *
 * @author delta
 */
@Tag(name = "回复话术管理", description = "回复话术管理接口")
@RestController
@RequestMapping("/replies")
public class ReplyController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ReplyController.class);

    @Autowired
    private ReplyService replyService;

    @Autowired
    private CacheService cacheService;

    @Operation(summary = "分页查询回复话术")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<ReplyVO>> getReplyPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "triggerType", required = false) String triggerType) {
        Page<ReplyVO> page = replyService.getReplyPage(pageNum, pageSize, triggerType);
        return Result.success(page);
    }

    @Operation(summary = "获取回复话术详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
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
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "triggerType", required = false) String triggerType) throws IOException {
        Page<ReplyVO> page = replyService.getReplyPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, triggerType);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("triggerType", "触发类型");
        headers.put("triggerKey", "触发关键词");
        headers.put("content", "回复内容");
        headers.put("enabled", "是否启用");
        ExcelUtils.export(response, "回复话术", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("triggerType", item.getTriggerType());
            map.put("triggerKey", item.getTriggerKey());
            map.put("content", item.getContent());
            map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
            return map;
        });
    }

    @Operation(summary = "导入回复话术Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                ReplyDTO dto = new ReplyDTO();
                dto.setTriggerType(row.getOrDefault("触发类型", row.getOrDefault("triggerType", "")));
                dto.setTriggerKey(row.getOrDefault("触发关键词", row.getOrDefault("triggerKey", "")));
                dto.setContent(row.getOrDefault("回复内容", row.getOrDefault("content", "")));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabled(enabledStr));
                replyService.createReply(dto);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        cacheService.reloadReplies();
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
