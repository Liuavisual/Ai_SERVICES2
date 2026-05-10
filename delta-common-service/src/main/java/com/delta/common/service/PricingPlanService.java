package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.PricingPlanVO;

/**
 * 定价方案服务接口
 *
 * @author 刘建国
 */
public interface PricingPlanService {

    Page<PricingPlanVO> getPage(Integer page, Integer size, String planCode, Integer status);

    PricingPlanVO getById(Long id);

    void create(PricingPlanVO vo);

    void update(PricingPlanVO vo);

    void delete(Long id);
}
