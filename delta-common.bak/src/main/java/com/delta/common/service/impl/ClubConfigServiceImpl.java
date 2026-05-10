package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.ClubConfigDTO;
import com.delta.common.dto.ClubLevelPriceDTO;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.ClubLevelPrice;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.entity.OperationLog;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.mapper.ClubLevelPriceMapper;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.OperationLogMapper;
import com.delta.common.service.ClubConfigService;
import com.delta.common.vo.ClubConfigVO;
import com.delta.common.vo.ClubLevelPriceVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 俱乐部配置服务实现，管理俱乐部信息和等级价格
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class ClubConfigServiceImpl implements ClubConfigService {

    private static final Logger log = LoggerFactory.getLogger(ClubConfigServiceImpl.class);

    private final ClubConfigMapper clubConfigMapper;

    private final ClubLevelPriceMapper clubLevelPriceMapper;

    private final CompanionLevelMapper companionLevelMapper;

    /** 操作日志Mapper，用于记录配置变更审计 */
    private final OperationLogMapper operationLogMapper;

    @Override
    public ClubConfig getClubConfig() {
        LambdaQueryWrapper<ClubConfig> wrapper = new LambdaQueryWrapper<>();
        Page<ClubConfig> page = clubConfigMapper.selectPage(new Page<>(1, 1), wrapper);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    @Override
    public ClubConfigVO getClubConfigVO() {
        ClubConfig config = getClubConfig();
        if (config == null) {
            return null;
        }

        ClubConfigVO vo = new ClubConfigVO();
        BeanUtils.copyProperties(config, vo);

        List<CompanionLevel> levels = companionLevelMapper.selectList(
                new LambdaQueryWrapper<CompanionLevel>()
                        .eq(CompanionLevel::getEnabled, BusinessStatusConstants.ENABLED_INT)
                        .orderByAsc(CompanionLevel::getSortOrder)
        );

        List<ClubLevelPrice> prices = clubLevelPriceMapper.selectList(
                new LambdaQueryWrapper<ClubLevelPrice>()
                        .eq(ClubLevelPrice::getClubConfigId, config.getId())
        );

        Map<Long, ClubLevelPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(ClubLevelPrice::getLevelId, p -> p));

        List<ClubLevelPriceVO> levelPriceVOs = new ArrayList<>();
        for (CompanionLevel level : levels) {
            ClubLevelPriceVO priceVO = new ClubLevelPriceVO();
            priceVO.setLevelId(level.getId());
            priceVO.setLevelName(level.getLevelName());
            priceVO.setLevelCode(level.getLevelCode());
            priceVO.setSortOrder(level.getSortOrder());

            ClubLevelPrice price = priceMap.get(level.getId());
            if (price != null) {
                priceVO.setId(price.getId());
                priceVO.setPrice(price.getPrice());
            } else {
                priceVO.setPrice(level.getBasePrice());
            }

            levelPriceVOs.add(priceVO);
        }

        vo.setLevelPrices(levelPriceVOs);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClubConfig(ClubConfigDTO configDTO) {
        if (configDTO == null) {
            throw new BusinessException("俱乐部配置参数不能为空");
        }
        ClubConfig existing = getClubConfig();
        ClubConfig config = new ClubConfig();
        BeanUtils.copyProperties(configDTO, config);

        if (existing != null) {
            config.setId(existing.getId());
            clubConfigMapper.updateById(config);
        } else {
            clubConfigMapper.insert(config);
        }

        if (configDTO.getLevelPrices() != null && !configDTO.getLevelPrices().isEmpty()) {
            clubLevelPriceMapper.delete(
                    new LambdaQueryWrapper<ClubLevelPrice>()
                            .eq(ClubLevelPrice::getClubConfigId, config.getId())
            );

            for (ClubLevelPriceDTO priceDTO : configDTO.getLevelPrices()) {
                ClubLevelPrice price = new ClubLevelPrice();
                price.setClubConfigId(config.getId());
                price.setLevelId(priceDTO.getLevelId());
                price.setPrice(priceDTO.getPrice());
                clubLevelPriceMapper.insert(price);
            }
        }

        recordConfigAudit(configDTO);
    }

    /**
     * 记录俱乐部配置变更审计日志
     *
     * @param configDTO 配置变更DTO，用于提取变更摘要
     */
    private void recordConfigAudit(ClubConfigDTO configDTO) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationTime(LocalDateTime.now());
            operationLog.setOperator("系统管理员");
            operationLog.setOperationType("CONFIG_UPDATE");
            operationLog.setOperationTarget("俱乐部配置");
            operationLog.setOperationContent("俱乐部配置已更新");

            ClubConfig config = getClubConfig();
            if (config != null && configDTO != null) {
                StringBuilder detail = new StringBuilder("更新俱乐部信息：");
                detail.append("名称=").append(config.getClubName()).append("; ");
                detail.append("简介=").append(config.getServiceSlogan()).append("; ");
                operationLog.setOperationContent(detail.toString());
            }

            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.warn("【配置审计】记录配置变更日志失败，不影响核心业务流程", e);
        }
    }

}
