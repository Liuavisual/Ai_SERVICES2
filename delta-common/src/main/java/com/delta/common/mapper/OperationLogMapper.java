package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
