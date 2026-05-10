package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CustomerSatisfactionDTO;
import com.delta.common.vo.CustomerSatisfactionVO;

/**
 * 客户满意度评价服务接口
 *
 * @author 刘建国
 */
public interface CustomerSatisfactionService {

    /**
     * 提交满意度评价
     *
     * @param userId 客户ID
     * @param dto    评价数据
     * @return 评价视图对象
     */
    CustomerSatisfactionVO submitSatisfaction(Long userId, CustomerSatisfactionDTO dto);

    /**
     * 分页查询满意度评价
     *
     * @param page        页码
     * @param size        每页大小
     * @param companionId 陪玩师ID（可选筛选）
     * @param minRating   最低评分（可选筛选）
     * @param maxRating   最高评分（可选筛选）
     * @return 分页评价视图对象
     */
    Page<CustomerSatisfactionVO> getSatisfactions(int page, int size, Long companionId, Integer minRating, Integer maxRating);

    /**
     * 获取陪玩师平均评分
     *
     * @param companionId 陪玩师ID
     * @return 平均评分
     */
    Double getAverageRating(Long companionId);

    /**
     * 获取陪玩师评价数量
     *
     * @param companionId 陪玩师ID
     * @return 评价数量
     */
    Long getRatingCount(Long companionId);
}
