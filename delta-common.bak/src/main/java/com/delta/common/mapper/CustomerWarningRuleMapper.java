package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.CustomerWarningRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户预警规则Mapper接口
 *
 * @author 刘建国
 */
@Mapper
public interface CustomerWarningRuleMapper extends BaseMapper<CustomerWarningRule> {
}