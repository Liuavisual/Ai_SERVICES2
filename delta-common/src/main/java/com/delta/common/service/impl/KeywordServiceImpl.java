package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.dto.KeywordDTO;
import com.delta.common.entity.Keyword;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.KeywordMapper;
import com.delta.common.service.KeywordService;
import com.delta.common.service.matcher.KeywordMatcherService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.KeywordVO;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 关键词服务实现，管理关键词CRUD
 *
 * @author delta
 */
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private static final Logger log = LoggerFactory.getLogger(KeywordServiceImpl.class);

    private final KeywordMapper keywordMapper;

    private final KeywordMatcherService keywordMatcherService;

    @Override
    public Page<KeywordVO> getKeywordPage(Integer page, Integer size, String keyword) {
        Page<Keyword> keywordPage = new Page<>(page, size);
        LambdaQueryWrapper<Keyword> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Keyword::getKeyword, keyword);
        }

        wrapper.orderByDesc(Keyword::getPriority)
               .orderByDesc(Keyword::getCreatedAt);

        Page<Keyword> keywordPageResult = keywordMapper.selectPage(keywordPage, wrapper);

        Page<KeywordVO> resultPage = new Page<>(keywordPageResult.getCurrent(), keywordPageResult.getSize(), keywordPageResult.getTotal());
        resultPage.setRecords(BeanUtil.copyToList(keywordPageResult.getRecords(), KeywordVO.class));
        VoUtils.setRowNumbers(resultPage);

        return resultPage;
    }

    @Override
    public KeywordVO getKeywordById(Long id) {
        Keyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            throw new BusinessException("关键词不存在");
        }
        return BeanUtil.copyProperties(keyword, KeywordVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createKeyword(KeywordDTO keywordDTO) {
        LambdaQueryWrapper<Keyword> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Keyword::getKeyword, keywordDTO.getKeyword());
        if (keywordMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException("关键词已存在");
        }

        Keyword keyword = BeanUtil.copyProperties(keywordDTO, Keyword.class);
        keywordMapper.insert(keyword);
        keywordMatcherService.refreshKeywords();
        log.info("创建关键词成功: {}", keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKeyword(KeywordDTO keywordDTO) {
        Keyword keyword = keywordMapper.selectById(keywordDTO.getId());
        if (keyword == null) {
            throw new BusinessException("关键词不存在");
        }

        LambdaQueryWrapper<Keyword> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Keyword::getKeyword, keywordDTO.getKeyword())
                  .ne(Keyword::getId, keywordDTO.getId());
        if (keywordMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException("关键词已存在");
        }

        BeanUtil.copyProperties(keywordDTO, keyword, "id", "createdAt");
        keywordMapper.updateById(keyword);
        keywordMatcherService.refreshKeywords();
        log.info("更新关键词成功: {}", keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKeyword(Long id) {
        Keyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            throw new BusinessException("关键词不存在");
        }

        keywordMapper.deleteById(id);
        keywordMatcherService.refreshKeywords();
        log.info("删除关键词成功: id={}", id);
    }

    @Override
    public void refreshKeywordTrie() {
        keywordMatcherService.refreshKeywords();
    }

    /**
     * 导出关键词Excel
     *
     * @param response HTTP响应
     * @param keyword  关键词搜索条件
     */
    @Override
    public void exportKeywords(HttpServletResponse response, String keyword) {
        try {
            Page<KeywordVO> pageResult = getKeywordPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, keyword);
            LinkedHashMap<String, String> headers = new LinkedHashMap<>();
            headers.put("id", "ID");
            headers.put("keyword", "关键词");
            headers.put("priority", "优先级");
            headers.put("enabled", "是否启用");
            headers.put("createdAt", "创建时间");
            headers.put("updatedAt", "更新时间");
            ExcelUtils.export(response, "关键词列表", headers, pageResult.getRecords(), item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", item.getId());
                map.put("keyword", item.getKeyword());
                map.put("priority", item.getPriority());
                map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
                map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
                map.put("updatedAt", item.getUpdatedAt() != null ? item.getUpdatedAt().toString() : "");
                return map;
            });
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    /**
     * 导入关键词Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importKeywords(MultipartFile file) {
        List<Map<String, String>> rows;
        try {
            rows = ExcelUtils.importExcel(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                KeywordDTO dto = new KeywordDTO();
                dto.setKeyword(row.getOrDefault("关键词", row.getOrDefault("keyword", "")));
                dto.setPriority(Integer.parseInt(row.getOrDefault("优先级", row.getOrDefault("priority", "0"))));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabled(enabledStr));
                createKeyword(dto);
                success++;
            } catch (Exception e) {
                log.warn("导入关键词行失败: {}", e.getMessage());
                fail++;
            }
        }
        return new ImportResultDTO(success, fail);
    }
}
