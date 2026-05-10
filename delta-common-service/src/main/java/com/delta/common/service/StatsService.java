package com.delta.common.service;

import com.delta.common.vo.StatsVO;

/**
 * 统计服务接口，提供运营数据统计和报表
 *
 * @author 刘建国
 */
public interface StatsService {

    StatsVO getPersonalStats(Long csUserId, String period, String date);

    StatsVO getTeamStats(String period, String date);

    StatsVO getGlobalStats(String period, String date);
}
