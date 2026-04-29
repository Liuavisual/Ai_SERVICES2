package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.service.CacheService;
import com.delta.common.service.KeywordService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.KeywordVO;
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
 * 关键词管理控制器
 *
 * @author delta
 */
@Tag(name = "关键词管理", description = "关键词管理接口")
@RestController
@RequestMapping("/keywords")
public class KeywordController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(KeywordController.class);

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private CacheService cacheService;

    @Operation(summary = "分页查询关键词")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<KeywordVO>> getKeywordPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "keyword", required = false) String keyword) {
        Page<KeywordVO> page = keywordService.getKeywordPage(pageNum, pageSize, keyword);
        return Result.success(page);
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
                            @RequestParam(name = "keyword", required = false) String keyword) throws IOException {
        Page<KeywordVO> page = keywordService.getKeywordPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, keyword);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("keyword", "关键词");
        headers.put("priority", "优先级");
        headers.put("enabled", "是否启用");
        headers.put("createdAt", "创建时间");
        headers.put("updatedAt", "更新时间");
        ExcelUtils.export(response, "关键词列表", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("keyword", item.getKeyword());
            map.put("priority", item.getPriority());
            map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
            map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
            map.put("updatedAt", item.getUpdatedAt() != null ? item.getUpdatedAt().toString() : "");
            return map;
        });
    }

    @Operation(summary = "导入关键词Excel")
    @PostMapping("/import")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                KeywordDTO dto = new KeywordDTO();
                dto.setKeyword(row.getOrDefault("关键词", row.getOrDefault("keyword", "")));
                dto.setPriority(Integer.parseInt(row.getOrDefault("优先级", row.getOrDefault("priority", "0"))));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabled(enabledStr));
                keywordService.createKeyword(dto);
                success++;
            } catch (Exception e) {
                fail++;
                log.warn("导入关键词失败: {}", e.getMessage());
            }
        }
        cacheService.reloadKeywords();
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
