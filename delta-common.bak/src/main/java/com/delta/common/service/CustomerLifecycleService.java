package com.delta.common.service;

import com.delta.common.vo.CustomerVO;

import java.util.List;

/**
 * 客户生命周期服务接口
 * <p>
 * 提供客户生命周期阶段判断、流失风险预警和标签更新等功能。</p>
 *
 * @author 刘建国
 */
public interface CustomerLifecycleService {

    /**
     * 判断客户生命周期阶段
     *
     * @param userId 客户用户ID
     * @return 生命周期阶段标识（NEW/ACTIVE/LOYAL/AT_RISK/CHURNED）
     */
    String determineLifecycleStage(Long userId);

    /**
     * 获取流失风险客户列表
     * <p>
     * 返回超过 AT_RISK_DAYS_THRESHOLD 天未活跃但未达到 CHURNED_DAYS_THRESHOLD 的客户。</p>
     *
     * @return 流失风险客户VO列表
     */
    List<CustomerVO> getAtRiskCustomers();

    /**
     * 获取已流失客户列表
     * <p>
     * 返回超过 CHURNED_DAYS_THRESHOLD 天未活跃的客户。</p>
     *
     * @return 已流失客户VO列表
     */
    List<CustomerVO> getChurnedCustomers();

    /**
     * 更新客户生命周期标签
     * <p>
     * 遍历所有有消息记录的客户，根据生命周期阶段自动添加对应标签。</p>
     */
    void updateCustomerLifecycleTags();
}
