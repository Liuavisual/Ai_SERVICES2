package com.delta.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delta.common.entity.CompanionRatingSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 陪玩师综合评分汇总Mapper
 *
 * @author 刘建国
 */
@Mapper
public interface CompanionRatingSummaryMapper extends BaseMapper<CompanionRatingSummary> {

    /**
     * 根据陪玩师ID查询评分汇总
     *
     * @param companionId 陪玩师ID
     * @return 评分汇总
     */
    @Select("SELECT * FROM companion_rating_summary WHERE companion_id = #{companionId}")
    CompanionRatingSummary selectByCompanionId(@Param("companionId") Long companionId);

    /**
     * 查询所有陪玩师评分汇总(按平均评分降序)
     *
     * @return 评分汇总列表
     */
    @Select("SELECT * FROM companion_rating_summary ORDER BY avg_rating DESC, total_reviews DESC")
    List<CompanionRatingSummary> selectAllOrderByRating();

    /**
     * 根据新评价更新陪玩师评分汇总
     *
     * @param companionId 陪玩师ID
     * @param rating      新评分(1-5)
     * @param now         评价时间
     * @return 影响行数
     */
    @Update("UPDATE companion_rating_summary SET " +
            "total_reviews = total_reviews + 1, " +
            "avg_rating = ROUND((avg_rating * total_reviews + #{rating}) / (total_reviews + 1), 2), " +
            "rating_1_count = rating_1_count + CASE WHEN #{rating} = 1 THEN 1 ELSE 0 END, " +
            "rating_2_count = rating_2_count + CASE WHEN #{rating} = 2 THEN 1 ELSE 0 END, " +
            "rating_3_count = rating_3_count + CASE WHEN #{rating} = 3 THEN 1 ELSE 0 END, " +
            "rating_4_count = rating_4_count + CASE WHEN #{rating} = 4 THEN 1 ELSE 0 END, " +
            "rating_5_count = rating_5_count + CASE WHEN #{rating} = 5 THEN 1 ELSE 0 END, " +
            "last_review_at = #{now}, " +
            "updated_at = #{now} " +
            "WHERE companion_id = #{companionId}")
    int updateRatingByCompanionId(@Param("companionId") Long companionId,
                                   @Param("rating") Integer rating,
                                   @Param("now") LocalDateTime now);
}