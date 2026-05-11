package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.OrderStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单状态变更历史Mapper
 *
 * @author 刘建国
 */
@Mapper
public interface OrderStatusHistoryMapper extends BaseMapper<OrderStatusHistory> {

    /**
     * 根据订单ID查询状态变更历史
     *
     * @param orderId 订单ID
     * @return 状态变更历史列表(按时间正序)
     */
    @Select("SELECT * FROM order_status_history WHERE order_id = #{orderId} ORDER BY created_at ASC")
    List<OrderStatusHistory> selectByOrderId(@Param("orderId") Long orderId);
}