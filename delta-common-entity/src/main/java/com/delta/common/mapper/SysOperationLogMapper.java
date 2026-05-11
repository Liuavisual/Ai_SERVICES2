package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统操作日志 Mapper 接口
 * <p>
 * 提供操作日志的分页条件查询和按模块统计功能
 * </p>
 *
 * @author 刘建国
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    /**
     * 分页按条件查询操作日志
     * <p>
     * 支持按操作人名称模糊匹配、模块精确匹配、操作时间范围筛选，
     * 结果按操作时间倒序排列。
     * </p>
     *
     * @param page         分页参数
     * @param operatorName 操作人名称（模糊匹配）
     * @param module       操作模块（精确匹配）
     * @param startTime    操作开始时间
     * @param endTime      操作结束时间
     * @return 分页查询结果
     */
    @Select("<script>" +
            "SELECT * FROM sys_operation_log WHERE 1=1" +
            "<if test='operatorName != null and operatorName != \"\"'>" +
            "AND operator_name LIKE CONCAT('%', #{operatorName}, '%')" +
            "</if>" +
            "<if test='module != null and module != \"\"'>" +
            "AND module = #{module}" +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND operate_time &gt;= #{startTime}" +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND operate_time &lt;= #{endTime}" +
            "</if>" +
            "ORDER BY operate_time DESC" +
            "</script>")
    IPage<SysOperationLog> selectPageByCondition(
            Page<SysOperationLog> page,
            @Param("operatorName") String operatorName,
            @Param("module") String module,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按模块分组统计操作日志数量
     * <p>
     * 返回每个模块的操作日志总数，按数量降序排列。
     * Map中key为"module"（模块名）和"count"（数量）。
     * </p>
     *
     * @return 模块统计结果列表
     */
    @Select("SELECT module, COUNT(*) AS count FROM sys_operation_log " +
            "GROUP BY module ORDER BY count DESC")
    List<Map<String, Object>> countByModule();
}