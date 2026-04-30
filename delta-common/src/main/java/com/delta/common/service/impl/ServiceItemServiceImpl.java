package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.dto.ServiceItemDTO;
import com.delta.common.dto.ServicePriceRuleDTO;
import com.delta.common.entity.CompanionLevel;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.entity.GameConfig;
import com.delta.common.entity.ServiceItem;
import com.delta.common.entity.ServicePriceRule;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.GameConfigMapper;
import com.delta.common.mapper.ServiceItemMapper;
import com.delta.common.mapper.ServicePriceRuleMapper;
import com.delta.common.service.ServiceItemService;
import com.delta.common.vo.ServiceItemVO;
import com.delta.common.vo.ServicePriceRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;

    private final ServicePriceRuleMapper servicePriceRuleMapper;

    private final GameConfigMapper gameConfigMapper;

    private final CompanionLevelMapper companionLevelMapper;

    @Override
    public List<ServiceItemVO> getByClubId(Long clubConfigId) {
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceItem::getClubConfigId, clubConfigId);
        wrapper.orderByAsc(ServiceItem::getSortOrder);
        List<ServiceItem> items = serviceItemMapper.selectList(wrapper);
        if (items.isEmpty()) return Collections.emptyList();

        Map<Long, GameConfig> gameConfigMap = batchQueryGameConfig(items);
        return items.stream()
                .map(item -> convertToVO(item, gameConfigMap))
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceItemVO> getByGameId(Long gameConfigId) {
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceItem::getGameConfigId, gameConfigId)
                .or()
                .isNull(ServiceItem::getGameConfigId);
        wrapper.orderByAsc(ServiceItem::getSortOrder);
        List<ServiceItem> items = serviceItemMapper.selectList(wrapper);
        if (items.isEmpty()) return Collections.emptyList();

        Map<Long, GameConfig> gameConfigMap = batchQueryGameConfig(items);
        return items.stream()
                .map(item -> convertToVO(item, gameConfigMap))
                .collect(Collectors.toList());
    }

    @Override
    public ServiceItemVO getById(Long id) {
        ServiceItem item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("服务项目不存在");
        }
        Map<Long, GameConfig> gameConfigMap = batchQueryGameConfig(List.of(item));
        ServiceItemVO vo = convertToVO(item, gameConfigMap);
        vo.setPriceRules(getPriceRules(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ServiceItemDTO dto) {
        ServiceItem item = new ServiceItem();
        BeanUtil.copyProperties(dto, item);
        if (item.getEnabled() == null) item.setEnabled(BusinessStatusConstants.ENABLED_INT);
        if (item.getSortOrder() == null) item.setSortOrder(BusinessStatusConstants.DISABLED_INT);
        if (item.getPriceUnit() == null) item.setPriceUnit(BusinessStatusConstants.PRICE_UNIT_HOUR);
        serviceItemMapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ServiceItemDTO dto) {
        if (dto.getId() == null || serviceItemMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("服务项目不存在");
        }
        ServiceItem item = new ServiceItem();
        BeanUtil.copyProperties(dto, item);
        serviceItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (serviceItemMapper.selectById(id) == null) {
            throw new BusinessException("服务项目不存在");
        }
        LambdaQueryWrapper<ServicePriceRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(ServicePriceRule::getServiceItemId, id);
        servicePriceRuleMapper.delete(ruleWrapper);
        serviceItemMapper.deleteById(id);
    }

    @Override
    public List<ServicePriceRuleVO> getPriceRules(Long serviceItemId) {
        LambdaQueryWrapper<ServicePriceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServicePriceRule::getServiceItemId, serviceItemId);
        List<ServicePriceRule> rules = servicePriceRuleMapper.selectList(wrapper);
        if (rules.isEmpty()) return Collections.emptyList();

        Map<Long, CompanionLevel> levelMap = batchQueryCompanionLevels(rules);
        return rules.stream()
                .map(rule -> convertPriceRuleToVO(rule, levelMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePriceRule(ServicePriceRuleDTO dto) {
        ServicePriceRule rule = new ServicePriceRule();
        BeanUtil.copyProperties(dto, rule);
        if (rule.getEnabled() == null) rule.setEnabled(BusinessStatusConstants.ENABLED_INT);
        if (rule.getPriceUnit() == null) rule.setPriceUnit(BusinessStatusConstants.PRICE_UNIT_HOUR);

        if (dto.getId() != null) {
            servicePriceRuleMapper.updateById(rule);
        } else {
            LambdaQueryWrapper<ServicePriceRule> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(ServicePriceRule::getServiceItemId, dto.getServiceItemId());
            if (dto.getCompanionLevelId() != null) {
                existWrapper.eq(ServicePriceRule::getCompanionLevelId, dto.getCompanionLevelId());
            } else {
                existWrapper.isNull(ServicePriceRule::getCompanionLevelId);
            }
            ServicePriceRule existing = servicePriceRuleMapper.selectOne(existWrapper);
            if (existing != null) {
                rule.setId(existing.getId());
                servicePriceRuleMapper.updateById(rule);
            } else {
                servicePriceRuleMapper.insert(rule);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePriceRule(Long id) {
        servicePriceRuleMapper.deleteById(id);
    }

    /**
     * 批量查询GameConfig，返回ID到实体的映射
     *
     * @param items 服务项目列表
     * @return GameConfig ID到实体的映射
     */
    private Map<Long, GameConfig> batchQueryGameConfig(List<ServiceItem> items) {
        Set<Long> gameConfigIds = items.stream()
                .map(ServiceItem::getGameConfigId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (gameConfigIds.isEmpty()) return Collections.emptyMap();
        return gameConfigMapper.selectBatchIds(gameConfigIds).stream()
                .collect(Collectors.toMap(GameConfig::getId, Function.identity()));
    }

    /**
     * 批量查询CompanionLevel，返回ID到实体的映射
     *
     * @param rules 价格规则列表
     * @return CompanionLevel ID到实体的映射
     */
    private Map<Long, CompanionLevel> batchQueryCompanionLevels(List<ServicePriceRule> rules) {
        Set<Long> levelIds = rules.stream()
                .map(ServicePriceRule::getCompanionLevelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (levelIds.isEmpty()) return Collections.emptyMap();
        return companionLevelMapper.selectBatchIds(levelIds).stream()
                .collect(Collectors.toMap(CompanionLevel::getId, Function.identity()));
    }

    /**
     * 将ServiceItem实体转换为VO（使用预查询Map避免N+1查询）
     *
     * @param item          服务项目实体
     * @param gameConfigMap GameConfig ID到实体的映射
     * @return 服务项目VO
     */
    private ServiceItemVO convertToVO(ServiceItem item, Map<Long, GameConfig> gameConfigMap) {
        ServiceItemVO vo = new ServiceItemVO();
        BeanUtil.copyProperties(item, vo);
        if (item.getGameConfigId() != null) {
            GameConfig game = gameConfigMap.get(item.getGameConfigId());
            if (game != null) {
                vo.setGameName(game.getGameName());
            }
        }
        return vo;
    }

    /**
     * 将ServicePriceRule实体转换为VO（使用预查询Map避免N+1查询）
     *
     * @param rule     价格规则实体
     * @param levelMap CompanionLevel ID到实体的映射
     * @return 价格规则VO
     */
    private ServicePriceRuleVO convertPriceRuleToVO(ServicePriceRule rule, Map<Long, CompanionLevel> levelMap) {
        ServicePriceRuleVO vo = new ServicePriceRuleVO();
        BeanUtil.copyProperties(rule, vo);
        if (rule.getCompanionLevelId() != null) {
            CompanionLevel level = levelMap.get(rule.getCompanionLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
            }
        }
        return vo;
    }
}
