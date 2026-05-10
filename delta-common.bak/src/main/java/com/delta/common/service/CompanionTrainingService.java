package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.CompanionTrainingVO;

/**
 * 陪玩师培训服务接口
 *
 * @author 刘建国
 */
public interface CompanionTrainingService {

    Page<CompanionTrainingVO> getPage(Integer page, Integer size, Long companionId, String trainingStatus);

    CompanionTrainingVO getById(Long id);

    void create(CompanionTrainingVO vo);

    void update(CompanionTrainingVO vo);

    void startTraining(Long id);

    void completeTraining(Long id, Integer examScore);

    void delete(Long id);
}
