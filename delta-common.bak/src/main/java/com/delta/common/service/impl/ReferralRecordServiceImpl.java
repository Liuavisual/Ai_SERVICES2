package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Campaign;
import com.delta.common.entity.ReferralRecord;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CampaignMapper;
import com.delta.common.mapper.ReferralRecordMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.ReferralRecordService;
import com.delta.common.vo.ReferralRecordVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 裂变推荐服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class ReferralRecordServiceImpl implements ReferralRecordService {

    private static final Logger log = LoggerFactory.getLogger(ReferralRecordServiceImpl.class);

    private final ReferralRecordMapper referralRecordMapper;
    private final CampaignMapper campaignMapper;
    private final UserMapper userMapper;

    @Override
    public Page<ReferralRecordVO> getPage(Integer page, Integer size, Long campaignId, Long referrerUserId, String conversionStatus, String rewardStatus) {
        Page<ReferralRecord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ReferralRecord> wrapper = new LambdaQueryWrapper<>();

        if (campaignId != null) {
            wrapper.eq(ReferralRecord::getCampaignId, campaignId);
        }
        if (referrerUserId != null) {
            wrapper.eq(ReferralRecord::getReferrerUserId, referrerUserId);
        }
        if (conversionStatus != null && !conversionStatus.trim().isEmpty()) {
            wrapper.eq(ReferralRecord::getConversionStatus, conversionStatus);
        }
        if (rewardStatus != null && !rewardStatus.trim().isEmpty()) {
            wrapper.eq(ReferralRecord::getRewardStatus, rewardStatus);
        }
        wrapper.orderByDesc(ReferralRecord::getCreatedAt);

        Page<ReferralRecord> result = referralRecordMapper.selectPage(pageObj, wrapper);
        Page<ReferralRecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(record -> {
            ReferralRecordVO vo = BeanUtil.copyProperties(record, ReferralRecordVO.class);
            if (record.getCampaignId() != null) {
                Campaign campaign = campaignMapper.selectById(record.getCampaignId());
                if (campaign != null) {
                    vo.setCampaignName(campaign.getCampaignName());
                }
            }
            if (record.getReferrerUserId() != null) {
                User user = userMapper.selectById(record.getReferrerUserId());
                if (user != null) {
                    vo.setReferrerUserName(user.getNickname());
                }
            }
            if (record.getRefereeUserId() != null) {
                User user = userMapper.selectById(record.getRefereeUserId());
                if (user != null) {
                    vo.setRefereeUserName(user.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public ReferralRecordVO getById(Long id) {
        ReferralRecord record = referralRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("推荐记录不存在");
        }
        return buildVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueReward(Long id) {
        ReferralRecord record = referralRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("推荐记录不存在");
        }
        if (!"PENDING".equals(record.getRewardStatus())) {
            throw new BusinessException("奖励已发放或已取消");
        }
        record.setRewardStatus("ISSUED");
        record.setRewardIssuedAt(LocalDateTime.now());
        referralRecordMapper.updateById(record);
        log.info("发放推荐奖励成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReward(Long id) {
        ReferralRecord record = referralRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("推荐记录不存在");
        }
        record.setRewardStatus("CANCELLED");
        referralRecordMapper.updateById(record);
        log.info("取消推荐奖励成功: id={}", id);
    }

    private ReferralRecordVO buildVO(ReferralRecord record) {
        ReferralRecordVO vo = BeanUtil.copyProperties(record, ReferralRecordVO.class);
        if (record.getCampaignId() != null) {
            Campaign campaign = campaignMapper.selectById(record.getCampaignId());
            if (campaign != null) {
                vo.setCampaignName(campaign.getCampaignName());
            }
        }
        if (record.getReferrerUserId() != null) {
            User user = userMapper.selectById(record.getReferrerUserId());
            if (user != null) {
                vo.setReferrerUserName(user.getNickname());
            }
        }
        if (record.getRefereeUserId() != null) {
            User user = userMapper.selectById(record.getRefereeUserId());
            if (user != null) {
                vo.setRefereeUserName(user.getNickname());
            }
        }
        return vo;
    }
}
