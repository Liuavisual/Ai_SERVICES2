package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.constant.ExportConstants;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.entity.CompanionRatingSummary;
import com.delta.common.entity.CompanionSchedule;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionRatingSummaryMapper;
import com.delta.common.mapper.CompanionScheduleMapper;
import com.delta.common.service.CompanionService;
import com.delta.common.util.DesensitizeUtils;
import com.delta.common.util.ExcelUtils;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CompanionVO;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 陪玩师服务实现，管理陪玩师信息
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CompanionServiceImpl implements CompanionService {

    private static final Logger log = LoggerFactory.getLogger(CompanionServiceImpl.class);

    private final CompanionMapper companionMapper;

    private final CompanionLevelMapper companionLevelMapper;

    private final CompanionScheduleMapper companionScheduleMapper;

    private final CompanionRatingSummaryMapper companionRatingSummaryMapper;

    @Override
    public Page<CompanionVO> getPage(Integer page, Integer size, Long levelId, String nickname, Integer enabled) {
        Page<Companion> pageObj = new Page<>(page, size);
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

        Page<Companion> companionPageResult = companionMapper.selectPage(pageObj, wrapper);

        List<Long> levelIds = companionPageResult.getRecords().stream()
                .map(Companion::getLevelId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, CompanionLevel> levelMap = levelIds.isEmpty() ? Map.of() :
                companionLevelMapper.selectByIds(levelIds).stream()
                        .collect(Collectors.toMap(CompanionLevel::getId, l -> l));

        Page<CompanionVO> resultPage = new Page<>(companionPageResult.getCurrent(), companionPageResult.getSize(), companionPageResult.getTotal());
        List<CompanionVO> voList = companionPageResult.getRecords().stream().map(c -> {
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
                companionLevelMapper.selectByIds(levelIds).stream()
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

    /**
     * 导出陪玩师Excel
     *
     * @param response HTTP响应
     * @param levelId  等级ID
     * @param nickname 昵称
     * @param enabled  启用状态
     */
    @Override
    public void exportCompanions(HttpServletResponse response, Long levelId, String nickname, Integer enabled) {
        try {
            Page<CompanionVO> pageResult = getPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, levelId, nickname, enabled);
            LinkedHashMap<String, String> headers = new LinkedHashMap<>();
            headers.put("id", "ID");
            headers.put("realName", "真实姓名");
            headers.put("nickname", "昵称");
            headers.put("phone", "手机号");
            headers.put("wechat", "微信");
            headers.put("levelName", "等级");
            headers.put("gameType", "游戏类型");
            headers.put("price", "价格");
            headers.put("enabled", "是否启用");
            ExcelUtils.export(response, "陪玩师列表", headers, pageResult.getRecords(), item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", item.getId());
                map.put("realName", item.getRealName());
                map.put("nickname", item.getNickname());
                map.put("phone", DesensitizeUtils.maskPhone(item.getPhone()));
                map.put("wechat", DesensitizeUtils.maskSecret(item.getWechat()));
                map.put("levelName", item.getLevelName());
                map.put("gameType", item.getGameType());
                map.put("price", item.getPrice());
                map.put("enabled", item.getEnabled() != null && item.getEnabled() ? "启用" : "禁用");
                return map;
            });
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    /**
     * 导入陪玩师Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importCompanions(MultipartFile file) {
        List<Map<String, String>> rows;
        try {
            rows = ExcelUtils.importExcel(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        int success = 0, fail = 0;
        for (Map<String, String> row : rows) {
            try {
                CompanionDTO dto = new CompanionDTO();
                dto.setRealName(row.getOrDefault("真实姓名", row.getOrDefault("realName", "")));
                dto.setNickname(row.getOrDefault("昵称", row.getOrDefault("nickname", "")));
                dto.setPhone(row.getOrDefault("手机号", row.getOrDefault("phone", "")));
                dto.setWechat(row.getOrDefault("微信", row.getOrDefault("wechat", "")));
                dto.setGameType(row.getOrDefault("游戏类型", row.getOrDefault("gameType", "")));
                String priceStr = row.getOrDefault("价格", row.getOrDefault("price", "0"));
                dto.setPrice(new java.math.BigDecimal(priceStr.isEmpty() ? "0" : priceStr));
                String enabledStr = row.getOrDefault("是否启用", row.getOrDefault("enabled", "启用"));
                dto.setEnabled(BusinessStatusConstants.parseExcelEnabled(enabledStr));
                create(dto);
                success++;
            } catch (Exception e) {
                log.warn("导入陪玩师行失败: {}", e.getMessage());
                fail++;
            }
        }
        return new ImportResultDTO(success, fail);
    }

    @Override
    public Map<String, Object> getRatingDashboard(Long companionId) {
        Companion companion = companionMapper.selectById(companionId);
        if (companion == null) {
            throw new BusinessException("陪玩师不存在");
        }
        CompanionRatingSummary summary = companionRatingSummaryMapper.selectByCompanionId(companionId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("companionId", companionId);
        result.put("companionName", companion.getRealName());
        result.put("companionNickname", companion.getNickname());
        if (summary != null) {
            result.put("totalReviews", summary.getTotalReviews());
            result.put("avgRating", summary.getAvgRating());
            result.put("rating1Count", summary.getRating1Count());
            result.put("rating2Count", summary.getRating2Count());
            result.put("rating3Count", summary.getRating3Count());
            result.put("rating4Count", summary.getRating4Count());
            result.put("rating5Count", summary.getRating5Count());
            result.put("lastReviewAt", summary.getLastReviewAt());
        } else {
            result.put("totalReviews", 0);
            result.put("avgRating", java.math.BigDecimal.ZERO);
            result.put("rating1Count", 0);
            result.put("rating2Count", 0);
            result.put("rating3Count", 0);
            result.put("rating4Count", 0);
            result.put("rating5Count", 0);
            result.put("lastReviewAt", null);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllCompanionRatings() {
        List<CompanionRatingSummary> summaries = companionRatingSummaryMapper.selectAllOrderByRating();
        return summaries.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("companionId", s.getCompanionId());
            Companion companion = companionMapper.selectById(s.getCompanionId());
            map.put("companionName", companion != null ? companion.getRealName() : "未知");
            map.put("companionNickname", companion != null ? companion.getNickname() : "未知");
            map.put("totalReviews", s.getTotalReviews());
            map.put("avgRating", s.getAvgRating());
            map.put("rating5Count", s.getRating5Count());
            map.put("rating4Count", s.getRating4Count());
            map.put("rating3Count", s.getRating3Count());
            map.put("rating2Count", s.getRating2Count());
            map.put("rating1Count", s.getRating1Count());
            map.put("lastReviewAt", s.getLastReviewAt());
            return map;
        }).collect(Collectors.toList());
    }
}
