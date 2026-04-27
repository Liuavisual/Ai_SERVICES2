package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.ClubConfigDTO;
import com.delta.common.dto.ClubLevelPriceDTO;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.ClubLevelPrice;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.mapper.ClubLevelPriceMapper;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.service.ClubConfigService;
import com.delta.common.vo.ClubConfigVO;
import com.delta.common.vo.ClubLevelPriceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 俱乐部配置服务实现，管理俱乐部信息和等级价格
 *
 * @author delta
 */
@Service
public class ClubConfigServiceImpl implements ClubConfigService {

    @Autowired
    private ClubConfigMapper clubConfigMapper;

    @Autowired
    private ClubLevelPriceMapper clubLevelPriceMapper;

    @Autowired
    private CompanionLevelMapper companionLevelMapper;

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
    }

}
