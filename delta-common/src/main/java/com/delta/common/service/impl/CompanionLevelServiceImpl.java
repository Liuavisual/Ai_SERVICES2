package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.service.CompanionLevelService;
import com.delta.common.util.ExcelUtils;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CompanionLevelVO;
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
import java.util.stream.Collectors;

/**
 * 陪玩师等级服务实现，管理等级体系
 *
 * @author delta
 */
@Service
@RequiredArgsConstructor
public class CompanionLevelServiceImpl implements CompanionLevelService {

    private static final Logger log = LoggerFactory.getLogger(CompanionLevelServiceImpl.class);

    private final CompanionLevelMapper companionLevelMapper;

    @Override
    public Page<CompanionLevelVO> getPage(Integer page, Integer size, String levelName) {
        Page<CompanionLevel> levelPage = new Page<>(page, size);
        LambdaQueryWrapper<CompanionLevel> wrapper = new LambdaQueryWrapper<>();

        if (levelName != null && !levelName.trim().isEmpty()) {
            wrapper.like(CompanionLevel::getLevelName, levelName);
        }

        wrapper.orderByAsc(CompanionLevel::getSortOrder)
               .orderByDesc(CompanionLevel::getCreatedAt);

        Page<CompanionLevel> levelPageResult = companionLevelMapper.selectPage(levelPage, wrapper);

        Page<CompanionLevelVO> resultPage = new Page<>(levelPageResult.getCurrent(), levelPageResult.getSize(), levelPageResult.getTotal());
        resultPage.setRecords(BeanUtil.copyToList(levelPageResult.getRecords(), CompanionLevelVO.class));
        VoUtils.setRowNumbers(resultPage);

        return resultPage;
    }

    @Override
    public List<CompanionLevelVO> getAllEnabled() {
        LambdaQueryWrapper<CompanionLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionLevel::getEnabled, BusinessStatusConstants.ENABLED_INT);
        wrapper.orderByAsc(CompanionLevel::getSortOrder);

        List<CompanionLevel> levels = companionLevelMapper.selectList(wrapper);
        return levels.stream()
                .map(level -> BeanUtil.copyProperties(level, CompanionLevelVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CompanionLevelVO getById(Long id) {
        CompanionLevel level = companionLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException("陪玩师等级不存在");
        }
        return BeanUtil.copyProperties(level, CompanionLevelVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CompanionLevelDTO dto) {
        LambdaQueryWrapper<CompanionLevel> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(CompanionLevel::getLevelCode, dto.getLevelCode());
        if (companionLevelMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException("等级编码已存在");
        }

        CompanionLevel level = BeanUtil.copyProperties(dto, CompanionLevel.class);
        companionLevelMapper.insert(level);
        log.info("创建陪玩师等级成功: {}", level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CompanionLevelDTO dto) {
        CompanionLevel level = companionLevelMapper.selectById(dto.getId());
        if (level == null) {
            throw new BusinessException("陪玩师等级不存在");
        }

        LambdaQueryWrapper<CompanionLevel> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(CompanionLevel::getLevelCode, dto.getLevelCode())
                  .ne(CompanionLevel::getId, dto.getId());
        if (companionLevelMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException("等级编码已存在");
        }

        BeanUtil.copyProperties(dto, level, "id", "createdAt");
        companionLevelMapper.updateById(level);
        log.info("更新陪玩师等级成功: {}", level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CompanionLevel level = companionLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException("陪玩师等级不存在");
        }

        companionLevelMapper.deleteById(id);
        log.info("删除陪玩师等级成功: id={}", id);
    }

    /**
     * 导出陪玩师等级Excel
     *
     * @param response  HTTP响应
     * @param levelName 等级名称
     */
    @Override
    public void exportCompanionLevels(HttpServletResponse response, String levelName) {
        try {
            Page<CompanionLevelVO> pageResult = getPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, levelName);
            LinkedHashMap<String, String> headers = new LinkedHashMap<>();
            headers.put("id", "ID");
            headers.put("levelName", "等级名称");
            headers.put("levelCode", "等级编码");
            headers.put("sortOrder", "排序");
            headers.put("basePrice", "基础价格");
            headers.put("description", "描述");
            headers.put("enabled", "是否启用");
            ExcelUtils.export(response, "陪玩师等级", headers, pageResult.getRecords(), item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", item.getId());
                map.put("levelName", item.getLevelName());
                map.put("levelCode", item.getLevelCode());
                map.put("sortOrder", item.getSortOrder());
                map.put("basePrice", item.getBasePrice());
                map.put("description", item.getDescription());
                map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
                return map;
            });
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    /**
     * 导入陪玩师等级Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importCompanionLevels(MultipartFile file) {
        List<Map<String, String>> rows;
        try {
            rows = ExcelUtils.importExcel(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                CompanionLevelDTO dto = new CompanionLevelDTO();
                dto.setLevelName(row.getOrDefault("等级名称", row.getOrDefault("levelName", "")));
                dto.setLevelCode(row.getOrDefault("等级编码", row.getOrDefault("levelCode", "")));
                dto.setSortOrder(Integer.parseInt(row.getOrDefault("排序", row.getOrDefault("sortOrder", "0"))));
                dto.setBasePrice(new java.math.BigDecimal(row.getOrDefault("基础价格", row.getOrDefault("basePrice", "0"))));
                dto.setDescription(row.getOrDefault("描述", row.getOrDefault("description", "")));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabled(enabledStr));
                create(dto);
                success++;
            } catch (Exception e) {
                log.warn("导入陪玩师等级行失败: {}", e.getMessage());
                fail++;
            }
        }
        return new ImportResultDTO(success, fail);
    }
}
