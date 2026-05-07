package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.CompanionSettlementVO;

/**
 * 陪玩师结算服务接口
 *
 * @author 刘建国
 */
public interface CompanionSettlementService {

    Page<CompanionSettlementVO> getPage(Integer page, Integer size, Long companionId, String settlementStatus, String confirmStatus);

    CompanionSettlementVO getById(Long id);

    void confirm(Long id, Long companionId);

    void dispute(Long id, Long companionId, String disputeContent);

    void settle(Long id);
}
