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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceItemServiceImpl implements ServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private ServicePriceRuleMapper servicePriceRuleMapper;

    @Autowired
    private GameConfigMapper gameConfigMapper;

    @Autowired
    private CompanionLevelMapper companionLevelMapper;

    @Override
    public List<ServiceItemVO> getByClubId(Long clubConfigId) {
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceItem::getClubConfigId, clubConfigId);
        wrapper.orderByAsc(ServiceItem::getSortOrder);
        return serviceItemMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceItemVO> getByGameId(Long gameConfigId) {
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceItem::getGameConfigId, gameConfigId)
                .or()
                .isNull(ServiceItem::getGameConfigId);
        wrapper.orderByAsc(ServiceItem::getSortOrder);
        return serviceItemMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceItemVO getById(Long id) {
        ServiceItem item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("服务项目不存在");
        }
        ServiceItemVO vo = convertToVO(item);
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
        return servicePriceRuleMapper.selectList(wrapper).stream()
                .map(this::convertPriceRuleToVO)
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

    private ServiceItemVO convertToVO(ServiceItem item) {
        ServiceItemVO vo = new ServiceItemVO();
        BeanUtil.copyProperties(item, vo);
        if (item.getGameConfigId() != null) {
            GameConfig game = gameConfigMapper.selectById(item.getGameConfigId());
            if (game != null) {
                vo.setGameName(game.getGameName());
            }
        }
        return vo;
    }

    private ServicePriceRuleVO convertPriceRuleToVO(ServicePriceRule rule) {
        ServicePriceRuleVO vo = new ServicePriceRuleVO();
        BeanUtil.copyProperties(rule, vo);
        if (rule.getCompanionLevelId() != null) {
            CompanionLevel level = companionLevelMapper.selectById(rule.getCompanionLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
            }
        }
        return vo;
    }
}
