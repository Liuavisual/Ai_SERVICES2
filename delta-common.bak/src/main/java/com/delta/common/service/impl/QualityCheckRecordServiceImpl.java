package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Companion;
import com.delta.common.entity.QualityCheckRecord;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.QualityCheckRecordMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.QualityCheckRecordService;
import com.delta.common.vo.QualityCheckRecordVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 质检记录服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class QualityCheckRecordServiceImpl implements QualityCheckRecordService {

    private static final Logger log = LoggerFactory.getLogger(QualityCheckRecordServiceImpl.class);

    private final QualityCheckRecordMapper qualityCheckRecordMapper;
    private final CompanionMapper companionMapper;
    private final UserMapper userMapper;

    @Override
    public Page<QualityCheckRecordVO> getPage(Integer page, Integer size, Long companionId, String riskLevel, String handleStatus) {
        Page<QualityCheckRecord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<QualityCheckRecord> wrapper = new LambdaQueryWrapper<>();

        if (companionId != null) {
            wrapper.eq(QualityCheckRecord::getCompanionId, companionId);
        }
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            wrapper.eq(QualityCheckRecord::getRiskLevel, riskLevel);
        }
        if (handleStatus != null && !handleStatus.trim().isEmpty()) {
            wrapper.eq(QualityCheckRecord::getHandleStatus, handleStatus);
        }
        wrapper.orderByDesc(QualityCheckRecord::getCheckTime);

        Page<QualityCheckRecord> result = qualityCheckRecordMapper.selectPage(pageObj, wrapper);
        Page<QualityCheckRecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(record -> {
            QualityCheckRecordVO vo = BeanUtil.copyProperties(record, QualityCheckRecordVO.class);
            if (record.getCompanionId() != null) {
                Companion companion = companionMapper.selectById(record.getCompanionId());
                if (companion != null) {
                    vo.setCompanionNickname(companion.getNickname());
                }
            }
            if (record.getUserId() != null) {
                User user = userMapper.selectById(record.getUserId());
                if (user != null) {
                    vo.setUserName(user.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public QualityCheckRecordVO getById(Long id) {
        QualityCheckRecord record = qualityCheckRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("质检记录不存在");
        }
        return buildVO(record);
    }

    @Override
    public void handleCheck(Long id, String handleStatus, String handleRemark, Long handlerId) {
        QualityCheckRecord record = qualityCheckRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("质检记录不存在");
        }
        record.setHandleStatus(handleStatus);
        record.setHandleRemark(handleRemark);
        record.setHandlerId(handlerId);
        qualityCheckRecordMapper.updateById(record);
        log.info("处理质检记录成功: id={}, handleStatus={}", id, handleStatus);
    }

    private QualityCheckRecordVO buildVO(QualityCheckRecord record) {
        QualityCheckRecordVO vo = BeanUtil.copyProperties(record, QualityCheckRecordVO.class);
        if (record.getCompanionId() != null) {
            Companion companion = companionMapper.selectById(record.getCompanionId());
            if (companion != null) {
                vo.setCompanionNickname(companion.getNickname());
            }
        }
        if (record.getUserId() != null) {
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickname());
            }
        }
        return vo;
    }
}
