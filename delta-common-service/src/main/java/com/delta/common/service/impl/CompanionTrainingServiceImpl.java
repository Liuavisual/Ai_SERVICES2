package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionTraining;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionTrainingMapper;
import com.delta.common.service.CompanionTrainingService;
import com.delta.common.vo.CompanionTrainingVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 陪玩师培训服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CompanionTrainingServiceImpl implements CompanionTrainingService {

    private static final Logger log = LoggerFactory.getLogger(CompanionTrainingServiceImpl.class);

    private final CompanionTrainingMapper companionTrainingMapper;
    private final CompanionMapper companionMapper;

    @Override
    public Page<CompanionTrainingVO> getPage(Integer page, Integer size, Long companionId, String trainingStatus) {
        Page<CompanionTraining> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<CompanionTraining> wrapper = new LambdaQueryWrapper<>();

        if (companionId != null) {
            wrapper.eq(CompanionTraining::getCompanionId, companionId);
        }
        if (trainingStatus != null && !trainingStatus.trim().isEmpty()) {
            wrapper.eq(CompanionTraining::getTrainingStatus, trainingStatus);
        }
        wrapper.orderByDesc(CompanionTraining::getCreatedAt);

        Page<CompanionTraining> result = companionTrainingMapper.selectPage(pageObj, wrapper);
        Page<CompanionTrainingVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(training -> {
            CompanionTrainingVO vo = BeanUtil.copyProperties(training, CompanionTrainingVO.class);
            if (training.getCompanionId() != null) {
                Companion companion = companionMapper.selectById(training.getCompanionId());
                if (companion != null) {
                    vo.setCompanionNickname(companion.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public CompanionTrainingVO getById(Long id) {
        CompanionTraining training = companionTrainingMapper.selectById(id);
        if (training == null) {
            throw new BusinessException("培训记录不存在");
        }
        return buildVO(training);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CompanionTrainingVO vo) {
        CompanionTraining training = BeanUtil.copyProperties(vo, CompanionTraining.class);
        training.setTrainingStatus("NOT_STARTED");
        companionTrainingMapper.insert(training);
        log.info("创建培训课程成功: id={}, courseName={}", training.getId(), training.getCourseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CompanionTrainingVO vo) {
        CompanionTraining training = companionTrainingMapper.selectById(vo.getId());
        if (training == null) {
            throw new BusinessException("培训记录不存在");
        }
        BeanUtil.copyProperties(vo, training, "id", "createdAt", "trainingStatus", "startedAt", "completedAt", "examScore");
        companionTrainingMapper.updateById(training);
        log.info("更新培训课程成功: id={}", training.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTraining(Long id) {
        CompanionTraining training = companionTrainingMapper.selectById(id);
        if (training == null) {
            throw new BusinessException("培训记录不存在");
        }
        training.setTrainingStatus("IN_PROGRESS");
        training.setStartedAt(LocalDateTime.now());
        companionTrainingMapper.updateById(training);
        log.info("开始培训学习: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTraining(Long id, Integer examScore) {
        CompanionTraining training = companionTrainingMapper.selectById(id);
        if (training == null) {
            throw new BusinessException("培训记录不存在");
        }
        training.setTrainingStatus("COMPLETED");
        training.setCompletedAt(LocalDateTime.now());
        training.setExamScore(examScore);
        companionTrainingMapper.updateById(training);
        log.info("完成培训学习: id={}, examScore={}", id, examScore);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CompanionTraining training = companionTrainingMapper.selectById(id);
        if (training == null) {
            throw new BusinessException("培训记录不存在");
        }
        companionTrainingMapper.deleteById(id);
        log.info("删除培训课程成功: id={}", id);
    }

    private CompanionTrainingVO buildVO(CompanionTraining training) {
        CompanionTrainingVO vo = BeanUtil.copyProperties(training, CompanionTrainingVO.class);
        if (training.getCompanionId() != null) {
            Companion companion = companionMapper.selectById(training.getCompanionId());
            if (companion != null) {
                vo.setCompanionNickname(companion.getNickname());
            }
        }
        return vo;
    }
}
