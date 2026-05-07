package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.CampaignVO;

/**
 * 营销活动服务接口
 *
 * @author 刘建国
 */
public interface CampaignService {

    Page<CampaignVO> getPage(Integer page, Integer size, Long clubConfigId, String campaignType, String status);

    CampaignVO getById(Long id);

    void create(CampaignVO vo);

    void update(CampaignVO vo);

    void startCampaign(Long id);

    void pauseCampaign(Long id);

    void endCampaign(Long id);

    void delete(Long id);
}
