package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.entity.CompanionSchedule;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionScheduleMapper;
import com.delta.common.service.CompanionService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CompanionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 陪玩师服务实现，管理陪玩师信息
 *
 * @author delta
 */
@Service
public class CompanionServiceImpl implements CompanionService {

    private static final Logger log = LoggerFactory.getLogger(CompanionServiceImpl.class);

    @Autowired
    private CompanionMapper companionMapper;

    @Autowired
    private CompanionLevelMapper companionLevelMapper;

    @Autowired
    private CompanionScheduleMapper companionScheduleMapper;

    @Override
    public Page<CompanionVO> getPage(Integer pageNum, Integer pageSize, Long levelId, String nickname, Integer enabled) {
        Page<Companion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Companion> wrapper = new LambdaQueryWrapper<>();

        if (levelId != null) {
            wrapper.eq(Companion::getLevelId, levelId);
        }

        if (nickname != null && !nickname.trim().isEmpty()) {
            wrapper.like(Companion::getNickname, nickname);
        }

        if (enabled != null) {
            wrapper.eq(Companion::getEnabled, enabled);
        }

        wrapper.orderByDesc(Companion::getCreatedAt);

        Page<Companion> companionPage = companionMapper.selectPage(page, wrapper);

        List<Long> levelIds = companionPage.getRecords().stream()
                .map(Companion::getLevelId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, CompanionLevel> levelMap = levelIds.isEmpty() ? Map.of() :
                companionLevelMapper.selectBatchIds(levelIds).stream()
                        .collect(Collectors.toMap(CompanionLevel::getId, l -> l));

        Page<CompanionVO> resultPage = new Page<>(companionPage.getCurrent(), companionPage.getSize(), companionPage.getTotal());
        List<CompanionVO> voList = companionPage.getRecords().stream().map(c -> {
            CompanionVO vo = BeanUtil.copyProperties(c, CompanionVO.class);
            CompanionLevel level = levelMap.get(c.getLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
                vo.setLevelBasePrice(level.getBasePrice());
            }
            vo.setDisplayPrice(c.getPrice() != null ? c.getPrice() : (level != null ? level.getBasePrice() : null));
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        VoUtils.setRowNumbers(resultPage);
        return resultPage;
    }

    @Override
    public List<CompanionVO> getAllEnabled() {
        LambdaQueryWrapper<Companion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Companion::getEnabled, BusinessStatusConstants.ENABLED_INT);
        wrapper.orderByDesc(Companion::getCreatedAt);

        List<Companion> companions = companionMapper.selectList(wrapper);
        return companions.stream()
                .map(c -> BeanUtil.copyProperties(c, CompanionVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanionVO> getAvailableByDateAndLevel(LocalDate date, Long levelId) {
        LambdaQueryWrapper<Companion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Companion::getEnabled, BusinessStatusConstants.ENABLED_INT);
        if (levelId != null) {
            wrapper.eq(Companion::getLevelId, levelId);
        }
        wrapper.orderByDesc(Companion::getCreatedAt);

        List<Companion> companions = companionMapper.selectList(wrapper);
        List<Long> companionIds = companions.stream().map(Companion::getId).collect(Collectors.toList());

        LambdaQueryWrapper<CompanionSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CompanionSchedule::getScheduleDate, date);
        scheduleWrapper.eq(CompanionSchedule::getStatus, BusinessStatusConstants.SCHEDULE_STATUS_AVAILABLE);
        scheduleWrapper.in(CompanionSchedule::getCompanionId, companionIds);

        List<CompanionSchedule> schedules = companionScheduleMapper.selectList(scheduleWrapper);
        Set<Long> availableCompanionIds = schedules.stream()
                .map(CompanionSchedule::getCompanionId)
                .collect(Collectors.toSet());

        List<Long> levelIds = companions.stream()
                .map(Companion::getLevelId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, CompanionLevel> levelMap = levelIds.isEmpty() ? Map.of() :
                companionLevelMapper.selectBatchIds(levelIds).stream()
                        .collect(Collectors.toMap(CompanionLevel::getId, l -> l));

        return companions.stream()
                .filter(c -> availableCompanionIds.contains(c.getId()))
                .map(c -> {
                    CompanionVO vo = BeanUtil.copyProperties(c, CompanionVO.class);
                    CompanionLevel level = levelMap.get(c.getLevelId());
                    if (level != null) {
                        vo.setLevelName(level.getLevelName());
                        vo.setLevelBasePrice(level.getBasePrice());
                    }
                    vo.setDisplayPrice(c.getPrice() != null ? c.getPrice() : (level != null ? level.getBasePrice() : null));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompanionVO getById(Long id) {
        Companion companion = companionMapper.selectById(id);
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }

        CompanionVO vo = BeanUtil.copyProperties(companion, CompanionVO.class);

        if (companion.getLevelId() != null) {
            CompanionLevel level = companionLevelMapper.selectById(companion.getLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
                vo.setLevelBasePrice(level.getBasePrice());
            }
        }

        vo.setDisplayPrice(companion.getPrice() != null ? companion.getPrice() :
                (vo.getLevelBasePrice() != null ? vo.getLevelBasePrice() : null));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CompanionDTO dto) {
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            LambdaQueryWrapper<Companion> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(Companion::getPhone, dto.getPhone());
            if (companionMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException("手机号已被其他陪玩师使用");
            }
        }
        if (dto.getWechat() != null && !dto.getWechat().isEmpty()) {
            LambdaQueryWrapper<Companion> wechatWrapper = new LambdaQueryWrapper<>();
            wechatWrapper.eq(Companion::getWechat, dto.getWechat());
            if (companionMapper.selectCount(wechatWrapper) > 0) {
                throw new BusinessException("微信号已被其他陪玩师使用");
            }
        }

        Companion companion = BeanUtil.copyProperties(dto, Companion.class);
        companionMapper.insert(companion);
        log.info("创建陪玩师成功: id={}, nickname={}", companion.getId(), companion.getNickname());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CompanionDTO dto) {
        Companion companion = companionMapper.selectById(dto.getId());
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            LambdaQueryWrapper<Companion> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(Companion::getPhone, dto.getPhone());
            phoneWrapper.ne(Companion::getId, dto.getId());
            if (companionMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException("手机号已被其他陪玩师使用");
            }
        }
        if (dto.getWechat() != null && !dto.getWechat().isEmpty()) {
            LambdaQueryWrapper<Companion> wechatWrapper = new LambdaQueryWrapper<>();
            wechatWrapper.eq(Companion::getWechat, dto.getWechat());
            wechatWrapper.ne(Companion::getId, dto.getId());
            if (companionMapper.selectCount(wechatWrapper) > 0) {
                throw new BusinessException("微信号已被其他陪玩师使用");
            }
        }

        BeanUtil.copyProperties(dto, companion, "id", "createdAt");
        companionMapper.updateById(companion);
        log.info("更新陪玩师成功: id={}, nickname={}", companion.getId(), companion.getNickname());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Companion companion = companionMapper.selectById(id);
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }

        companionMapper.deleteById(id);
        log.info("删除陪玩师成功: id={}", id);
    }
}
