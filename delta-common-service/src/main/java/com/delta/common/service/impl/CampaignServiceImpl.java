package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Campaign;
import com.delta.common.entity.ClubConfig;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CampaignMapper;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.service.CampaignService;
import com.delta.common.vo.CampaignVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 营销活动服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignServiceImpl.class);

    private final CampaignMapper campaignMapper;
    private final ClubConfigMapper clubConfigMapper;

    @Override
    public Page<CampaignVO> getPage(Integer page, Integer size, Long clubConfigId, String campaignType, String status) {
        Page<Campaign> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<>();

        if (clubConfigId != null) {
            wrapper.eq(Campaign::getClubConfigId, clubConfigId);
        }
        if (campaignType != null && !campaignType.trim().isEmpty()) {
            wrapper.eq(Campaign::getCampaignType, campaignType);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Campaign::getStatus, status);
        }
        wrapper.orderByDesc(Campaign::getCreatedAt);

        Page<Campaign> result = campaignMapper.selectPage(pageObj, wrapper);
        Page<CampaignVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(campaign -> {
            CampaignVO vo = BeanUtil.copyProperties(campaign, CampaignVO.class);
            ClubConfig config = clubConfigMapper.selectById(campaign.getClubConfigId());
            if (config != null) {
                vo.setClubName(config.getClubName());
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public CampaignVO getById(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException("营销活动不存在");
        }
        CampaignVO vo = BeanUtil.copyProperties(campaign, CampaignVO.class);
        ClubConfig config = clubConfigMapper.selectById(campaign.getClubConfigId());
        if (config != null) {
            vo.setClubName(config.getClubName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CampaignVO vo) {
        Campaign campaign = BeanUtil.copyProperties(vo, Campaign.class);
        campaign.setStatus("DRAFT");
        campaignMapper.insert(campaign);
        log.info("创建营销活动成功: id={}, campaignName={}", campaign.getId(), campaign.getCampaignName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CampaignVO vo) {
        Campaign campaign = campaignMapper.selectById(vo.getId());
        if (campaign == null) {
            throw new BusinessException("营销活动不存在");
        }
        BeanUtil.copyProperties(vo, campaign, "id", "createdAt", "status");
        campaignMapper.updateById(campaign);
        log.info("更新营销活动成功: id={}", campaign.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startCampaign(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException("营销活动不存在");
        }
        campaign.setStatus("ACTIVE");
        campaignMapper.updateById(campaign);
        log.info("启动营销活动成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseCampaign(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException("营销活动不存在");
        }
        campaign.setStatus("PAUSED");
        campaignMapper.updateById(campaign);
        log.info("暂停营销活动成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endCampaign(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException("营销活动不存在");
        }
        campaign.setStatus("ENDED");
        campaignMapper.updateById(campaign);
        log.info("结束营销活动成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException("营销活动不存在");
        }
        campaignMapper.deleteById(id);
        log.info("删除营销活动成功: id={}", id);
    }
}
