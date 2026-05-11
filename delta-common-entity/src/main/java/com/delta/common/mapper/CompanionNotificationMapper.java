package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.CompanionNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 陪玩师通知消息Mapper
 *
 * @author 刘建国
 */
@Mapper
public interface CompanionNotificationMapper extends BaseMapper<CompanionNotification> {

    /**
     * 根据陪玩师ID查询未读通知列表
     *
     * @param companionId 陪玩师ID
     * @return 未读通知列表
     */
    @Select("SELECT * FROM companion_notifications WHERE companion_id = #{companionId} AND is_read = 0 ORDER BY created_at DESC")
    List<CompanionNotification> selectUnreadByCompanionId(@Param("companionId") Long companionId);

    /**
     * 根据陪玩师ID查询所有通知列表
     *
     * @param companionId 陪玩师ID
     * @return 通知列表(按时间降序)
     */
    @Select("SELECT * FROM companion_notifications WHERE companion_id = #{companionId} ORDER BY created_at DESC")
    List<CompanionNotification> selectByCompanionId(@Param("companionId") Long companionId);

    /**
     * 将某条通知标记为已读
     *
     * @param id 通知ID
     * @return 影响行数
     */
    @Update("UPDATE companion_notifications SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    /**
     * 将某陪玩师所有通知标记为已读
     *
     * @param companionId 陪玩师ID
     * @return 影响行数
     */
    @Update("UPDATE companion_notifications SET is_read = 1 WHERE companion_id = #{companionId} AND is_read = 0")
    int markAllAsRead(@Param("companionId") Long companionId);

    /**
     * 查询某陪玩师未读通知数量
     *
     * @param companionId 陪玩师ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM companion_notifications WHERE companion_id = #{companionId} AND is_read = 0")
    int countUnreadByCompanionId(@Param("companionId") Long companionId);
}