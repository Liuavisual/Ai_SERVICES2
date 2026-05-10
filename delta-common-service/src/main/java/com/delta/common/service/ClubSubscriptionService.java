package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.ClubSubscriptionVO;

/**
 * 俱乐部订阅服务接口
 *
 * @author 刘建国
 */
public interface ClubSubscriptionService {

    Page<ClubSubscriptionVO> getPage(Integer page, Integer size, Long clubConfigId, String status);

    ClubSubscriptionVO getById(Long id);

    ClubSubscriptionVO getByClubConfigId(Long clubConfigId);

    void subscribe(Long clubConfigId, Long planId);

    void cancelSubscription(Long id);

    void renewSubscription(Long id, Integer months);

    void trial(Long clubConfigId);
}
