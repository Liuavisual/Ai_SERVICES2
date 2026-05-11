package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CustomerOrderRecordDTO;
import com.delta.common.dto.CustomerProfileUpdateDTO;
import com.delta.common.entity.Order;
import com.delta.common.vo.CustomerOrderRecordVO;
import com.delta.common.vo.CustomerProfileVO;

/**
 * 客户画像服务接口，基于RFM模型和消费行为分析管理客户画像
 * <p>
 * 数据来源：仅店内消费记录 + 客服/陪玩交互数据，不涉及客户隐私信息。
 * </p>
 *
 * @author 刘建国
 */
public interface CustomerProfileService {

    Page<CustomerProfileVO> getProfilePage(Integer page, Integer size, String memberLevel, String riskLevel, String lifecycleStage, String rfmSegment, String keyword);

    CustomerProfileVO getProfileByUserId(Long userId);

    void updateProfile(CustomerProfileUpdateDTO dto);

    void addOrderRecord(CustomerOrderRecordDTO dto);

    Page<CustomerOrderRecordVO> getOrderRecordPage(Integer page, Integer size, Long userId, String orderType, String status);

    void refreshProfile(Long userId);

    void initProfileIfNeeded(Long userId);

    void recordInteraction(Long userId, boolean isAi);

    void recordHandoffEvent(Long userId, String reason, boolean isEmotion, boolean isOrderIntent);

    void syncOrderRecord(Order order);
}
