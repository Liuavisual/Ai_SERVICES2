package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.service.CacheService;
import com.delta.common.service.KeywordService;
import com.delta.common.vo.KeywordVO;
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
 * 关键词管理控制器
 *
 * @author delta
 */
@Tag(name = "关键词管理", description = "关键词管理接口")
@RestController
@RequestMapping("/v1/keywords")
@RequiredArgsConstructor
public class KeywordController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(KeywordController.class);

    private final KeywordService keywordService;

    private final CacheService cacheService;

    @Operation(summary = "分页查询关键词")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<KeywordVO>> getKeywordPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "keyword", required = false) String keyword) {
        Page<KeywordVO> pageResult = keywordService.getKeywordPage(page, size, keyword);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取关键词详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<KeywordVO> getKeywordById(@PathVariable("id") String id) {
        KeywordVO keywordVO = keywordService.getKeywordById(decodeId(id));
        return Result.success(keywordVO);
    }

    @Operation(summary = "创建关键词")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> createKeyword(@Valid @RequestBody KeywordDTO keywordDTO) {
        keywordService.createKeyword(keywordDTO);
        log.info("创建关键词，刷新关键词缓存");
        cacheService.reloadKeywords();
        return Result.success();
    }

    @Operation(summary = "更新关键词")
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> updateKeyword(@Valid @RequestBody KeywordDTO keywordDTO) {
        keywordService.updateKeyword(keywordDTO);
        log.info("更新关键词，刷新关键词缓存");
        cacheService.reloadKeywords();
        return Result.success();
    }

    @Operation(summary = "删除关键词")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> deleteKeyword(@PathVariable("id") String id) {
        keywordService.deleteKeyword(decodeId(id));
        log.info("删除关键词，刷新关键词缓存");
        cacheService.reloadKeywords();
        return Result.success();
    }

    @Operation(summary = "刷新关键词库")
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> refreshKeywordTrie() {
        keywordService.refreshKeywordTrie();
        log.info("手动刷新关键词缓存");
        cacheService.reloadKeywords();
        return Result.success();
    }

    @Operation(summary = "导出关键词Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "keyword", required = false) String keyword) {
        keywordService.exportKeywords(response, keyword);
    }

    @Operation(summary = "导入关键词Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = keywordService.importKeywords(file);
        cacheService.reloadKeywords();
        return Result.success(result.toMap());
    }
}
