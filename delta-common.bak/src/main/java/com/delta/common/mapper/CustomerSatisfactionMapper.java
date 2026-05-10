package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.CustomerSatisfaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户满意度评价Mapper接口
 *
 * @author 刘建国
 */
@Mapper
public interface CustomerSatisfactionMapper extends BaseMapper<CustomerSatisfaction> {
}
