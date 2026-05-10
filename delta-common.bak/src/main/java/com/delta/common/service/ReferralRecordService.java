package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.ReferralRecordVO;

/**
 * 裂变推荐服务接口
 *
 * @author 刘建国
 */
public interface ReferralRecordService {

    Page<ReferralRecordVO> getPage(Integer page, Integer size, Long campaignId, Long referrerUserId, String conversionStatus, String rewardStatus);

    ReferralRecordVO getById(Long id);

    void issueReward(Long id);

    void cancelReward(Long id);
}
