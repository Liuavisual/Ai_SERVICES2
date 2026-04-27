package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.service.CompanionLevelService;
import com.delta.common.vo.CompanionLevelVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 陪玩师等级服务实现，管理等级体系
 *
 * @author delta
 */
@Service
public class CompanionLevelServiceImpl implements CompanionLevelService {

    private static final Logger log = LoggerFactory.getLogger(CompanionLevelServiceImpl.class);

    @Autowired
    private CompanionLevelMapper companionLevelMapper;

    @Override
    public Page<CompanionLevelVO> getPage(Integer pageNum, Integer pageSize, String levelName) {
        Page<CompanionLevel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CompanionLevel> wrapper = new LambdaQueryWrapper<>();

        if (levelName != null && !levelName.trim().isEmpty()) {
            wrapper.like(CompanionLevel::getLevelName, levelName);
        }

        wrapper.orderByAsc(CompanionLevel::getSortOrder)
               .orderByDesc(CompanionLevel::getCreatedAt);

        Page<CompanionLevel> levelPage = companionLevelMapper.selectPage(page, wrapper);

        Page<CompanionLevelVO> resultPage = new Page<>(levelPage.getCurrent(), levelPage.getSize(), levelPage.getTotal());
        resultPage.setRecords(BeanUtil.copyToList(levelPage.getRecords(), CompanionLevelVO.class));

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
}
