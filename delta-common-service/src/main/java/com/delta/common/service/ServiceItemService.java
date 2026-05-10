package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ServiceItemDTO;
import com.delta.common.dto.ServicePriceRuleDTO;
import com.delta.common.vo.ServiceItemVO;
import com.delta.common.vo.ServicePriceRuleVO;

import java.util.List;

public interface ServiceItemService {

    List<ServiceItemVO> getByClubId(Long clubConfigId);

    List<ServiceItemVO> getByGameId(Long gameConfigId);

    ServiceItemVO getById(Long id);

    void create(ServiceItemDTO dto);

    void update(ServiceItemDTO dto);

    void delete(Long id);

    List<ServicePriceRuleVO> getPriceRules(Long serviceItemId);

    void savePriceRule(ServicePriceRuleDTO dto);

    void deletePriceRule(Long id);

    Page<ServiceItemVO> getPage(Integer page, Integer size, Long clubConfigId, Long gameConfigId);
}
