package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.PricingPlan;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.PricingPlanMapper;
import com.delta.common.service.PricingPlanService;
import com.delta.common.vo.PricingPlanVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 定价方案服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class PricingPlanServiceImpl implements PricingPlanService {

    private static final Logger log = LoggerFactory.getLogger(PricingPlanServiceImpl.class);

    private final PricingPlanMapper pricingPlanMapper;

    @Override
    public Page<PricingPlanVO> getPage(Integer page, Integer size, String planCode, Integer status) {
        Page<PricingPlan> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<PricingPlan> wrapper = new LambdaQueryWrapper<>();

        if (planCode != null && !planCode.trim().isEmpty()) {
            wrapper.eq(PricingPlan::getPlanCode, planCode);
        }
        if (status != null) {
            wrapper.eq(PricingPlan::getStatus, status);
        }
        wrapper.orderByAsc(PricingPlan::getSortOrder);

        Page<PricingPlan> result = pricingPlanMapper.selectPage(pageObj, wrapper);
        Page<PricingPlanVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(p -> BeanUtil.copyProperties(p, PricingPlanVO.class))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public PricingPlanVO getById(Long id) {
        PricingPlan plan = pricingPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException("定价方案不存在");
        }
        return BeanUtil.copyProperties(plan, PricingPlanVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(PricingPlanVO vo) {
        PricingPlan plan = BeanUtil.copyProperties(vo, PricingPlan.class);
        pricingPlanMapper.insert(plan);
        log.info("创建定价方案成功: id={}, planCode={}", plan.getId(), plan.getPlanCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PricingPlanVO vo) {
        PricingPlan plan = pricingPlanMapper.selectById(vo.getId());
        if (plan == null) {
            throw new BusinessException("定价方案不存在");
        }
        BeanUtil.copyProperties(vo, plan, "id", "createdAt");
        pricingPlanMapper.updateById(plan);
        log.info("更新定价方案成功: id={}", plan.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PricingPlan plan = pricingPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException("定价方案不存在");
        }
        pricingPlanMapper.deleteById(id);
        log.info("删除定价方案成功: id={}", id);
    }
}
