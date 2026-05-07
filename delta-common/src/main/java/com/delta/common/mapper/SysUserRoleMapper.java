package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 用户-角色关联 Mapper 接口
 *
 * @author 刘建国
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 删除指定用户的所有角色关联
     *
     * @param userId 用户ID
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}
