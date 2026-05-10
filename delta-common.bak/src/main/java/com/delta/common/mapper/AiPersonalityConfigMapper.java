package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.AiPersonalityConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI人格配置 Mapper 接口
 * <p>
 * 继承 MyBatis-Plus BaseMapper，自动获得 CRUD 能力。
 * 通过 @Mapper 注解注册为 MyBatis Mapper。
 * </p>
 *
 * @author 刘建国
 */
@Mapper
public interface AiPersonalityConfigMapper extends BaseMapper<AiPersonalityConfig> {
}
