package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.annotation.DecodeId;
import com.delta.common.annotation.PermAuth;
import com.delta.common.dto.FaqItemDTO;
import com.delta.common.service.CacheService;
import com.delta.common.service.FaqItemService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.vo.FaqItemVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "FAQ知识库管理", description = "FAQ知识库管理接口")
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/faq-items")
@RequiredArgsConstructor
@PermAuth("faq_item:view")
public class FaqItemController {

    private final FaqItemService faqItemService;

    private final CacheService cacheService;

    @Operation(summary = "获取FAQ列表")
    @GetMapping
    @PermAuth("faq_item:edit")
    public Result<Page<FaqItemVO>> getFaqItems(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "category", required = false) String category) {
        return Result.success(faqItemService.getFaqItems(page, size, category));
    }

    @Operation(summary = "新增FAQ")
    @PostMapping
    @PermAuth("faq_item:edit")
    public Result<String> addFaqItem(@Valid @RequestBody FaqItemDTO dto) {
        faqItemService.addFaqItem(dto);
        cacheService.reloadFaqItems();
        return Result.success("添加成功");
    }

    @Operation(summary = "更新FAQ")
    @PutMapping
    @PermAuth("faq_item:edit")
    public Result<String> updateFaqItem(@Valid @RequestBody FaqItemDTO dto) {
        faqItemService.updateFaqItem(dto);
        cacheService.reloadFaqItems();
        return Result.success("更新成功");
    }

    @Operation(summary = "删除FAQ")
    @DeleteMapping("/{id}")
    @PermAuth("faq_item:edit")
    public Result<String> deleteFaqItem(@PathVariable("id") @DecodeId Long id) {
        faqItemService.deleteFaqItem(id);
        cacheService.reloadFaqItems();
        return Result.success("删除成功");
    }

    @Operation(summary = "导出FAQ知识库Excel")
    @GetMapping("/export")
    @PermAuth("faq_item:edit")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(name = "category", required = false) String category) throws IOException {
        Page<FaqItemVO> page = faqItemService.getFaqItems(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, category);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "ID");
        headers.put("category", "分类");
        headers.put("question", "问题");
        headers.put("answer", "答案");
        headers.put("sortOrder", "排序");
        headers.put("enabled", "是否启用");
        ExcelUtils.export(response, "FAQ知识库", headers, page.getRecords(), item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("category", item.getCategory());
            map.put("question", item.getQuestion());
            map.put("answer", item.getAnswer());
            map.put("sortOrder", item.getSortOrder());
            map.put("enabled", item.getEnabled() != null && Integer.valueOf(BusinessStatusConstants.ENABLED_INT).equals(item.getEnabled()) ? "启用" : "禁用");
            return map;
        });
    }

    @Operation(summary = "导入FAQ知识库Excel")
    @PostMapping("/import")
    @PermAuth("faq_item:import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Map<String, String>> rows = ExcelUtils.importExcel(file.getInputStream());
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                FaqItemDTO dto = new FaqItemDTO();
                dto.setCategory(row.getOrDefault("分类", row.getOrDefault("category", "")));
                dto.setQuestion(row.getOrDefault("问题", row.getOrDefault("question", "")));
                dto.setAnswer(row.getOrDefault("答案", row.getOrDefault("answer", "")));
                dto.setSortOrder(Integer.parseInt(row.getOrDefault("排序", row.getOrDefault("sortOrder", "0"))));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabledInt(enabledStr));
                faqItemService.addFaqItem(dto);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        cacheService.reloadFaqItems();
        Map<String, Object> result = Map.of("success", success, "fail", fail, "total", rows.size());
        return Result.success(result);
    }
}
