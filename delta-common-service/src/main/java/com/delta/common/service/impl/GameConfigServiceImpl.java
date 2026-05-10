package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.GameConfigDTO;
import com.delta.common.entity.GameConfig;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.GameConfigMapper;
import com.delta.common.service.GameConfigService;
import com.delta.common.vo.GameConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameConfigServiceImpl implements GameConfigService {

    private final GameConfigMapper gameConfigMapper;

    @Override
    public List<GameConfigVO> getByClubId(Long clubConfigId) {
        LambdaQueryWrapper<GameConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameConfig::getClubConfigId, clubConfigId);
        wrapper.orderByAsc(GameConfig::getSortOrder);
        return gameConfigMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public GameConfigVO getById(Long id) {
        GameConfig config = gameConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("游戏配置不存在");
        }
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(GameConfigDTO dto) {
        GameConfig config = new GameConfig();
        BeanUtil.copyProperties(dto, config);
        if (config.getEnabled() == null) config.setEnabled(BusinessStatusConstants.ENABLED_INT);
        if (config.getSortOrder() == null) config.setSortOrder(BusinessStatusConstants.DISABLED_INT);
        if (config.getGameType() == null) config.setGameType(BusinessStatusConstants.GAME_TYPE_FPS);
        gameConfigMapper.insert(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GameConfigDTO dto) {
        if (dto.getId() == null || gameConfigMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("游戏配置不存在");
        }
        GameConfig config = new GameConfig();
        BeanUtil.copyProperties(dto, config);
        gameConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (gameConfigMapper.selectById(id) == null) {
            throw new BusinessException("游戏配置不存在");
        }
        gameConfigMapper.deleteById(id);
    }

    @Override
    public List<GameConfigVO> getAllEnabled() {
        LambdaQueryWrapper<GameConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameConfig::getEnabled, BusinessStatusConstants.ENABLED_INT);
        wrapper.orderByAsc(GameConfig::getSortOrder);
        return gameConfigMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<GameConfigVO> getPage(Integer page, Integer size, Long clubConfigId) {
        LambdaQueryWrapper<GameConfig> wrapper = new LambdaQueryWrapper<>();
        if (clubConfigId != null) {
            wrapper.eq(GameConfig::getClubConfigId, clubConfigId);
        }
        wrapper.orderByAsc(GameConfig::getSortOrder);

        Page<GameConfig> entityPage = gameConfigMapper.selectPage(new Page<>(page, size), wrapper);
        List<GameConfigVO> records = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        Page<GameConfigVO> voPage = new Page<>(page, size, entityPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    private GameConfigVO convertToVO(GameConfig config) {
        GameConfigVO vo = new GameConfigVO();
        BeanUtil.copyProperties(config, vo);
        return vo;
    }
}
