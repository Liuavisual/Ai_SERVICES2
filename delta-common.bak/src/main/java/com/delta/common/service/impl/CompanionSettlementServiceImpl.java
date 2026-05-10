package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CompanionSettlement;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CompanionSettlementMapper;
import com.delta.common.service.CompanionSettlementService;
import com.delta.common.vo.CompanionSettlementVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 陪玩师结算服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CompanionSettlementServiceImpl implements CompanionSettlementService {

    private static final Logger log = LoggerFactory.getLogger(CompanionSettlementServiceImpl.class);

    private final CompanionSettlementMapper companionSettlementMapper;
    private final CompanionMapper companionMapper;

    @Override
    public Page<CompanionSettlementVO> getPage(Integer page, Integer size, Long companionId, String settlementStatus, String confirmStatus) {
        Page<CompanionSettlement> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<CompanionSettlement> wrapper = new LambdaQueryWrapper<>();

        if (companionId != null) {
            wrapper.eq(CompanionSettlement::getCompanionId, companionId);
        }
        if (settlementStatus != null && !settlementStatus.trim().isEmpty()) {
            wrapper.eq(CompanionSettlement::getSettlementStatus, settlementStatus);
        }
        if (confirmStatus != null && !confirmStatus.trim().isEmpty()) {
            wrapper.eq(CompanionSettlement::getConfirmStatus, confirmStatus);
        }
        wrapper.orderByDesc(CompanionSettlement::getCreatedAt);

        Page<CompanionSettlement> result = companionSettlementMapper.selectPage(pageObj, wrapper);
        Page<CompanionSettlementVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(settlement -> {
            CompanionSettlementVO vo = BeanUtil.copyProperties(settlement, CompanionSettlementVO.class);
            if (settlement.getCompanionId() != null) {
                Companion companion = companionMapper.selectById(settlement.getCompanionId());
                if (companion != null) {
                    vo.setCompanionNickname(companion.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public CompanionSettlementVO getById(Long id) {
        CompanionSettlement settlement = companionSettlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException("结算记录不存在");
        }
        return buildVO(settlement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id, Long companionId) {
        CompanionSettlement settlement = companionSettlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException("结算记录不存在");
        }
        if (!settlement.getCompanionId().equals(companionId)) {
            throw new BusinessException("无权确认他人结算记录");
        }
        if (!"UNCONFIRMED".equals(settlement.getConfirmStatus())) {
            throw new BusinessException("该结算记录已确认或已申诉");
        }
        settlement.setConfirmStatus("CONFIRMED");
        companionSettlementMapper.updateById(settlement);
        log.info("确认结算成功: id={}, companionId={}", id, companionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispute(Long id, Long companionId, String disputeContent) {
        CompanionSettlement settlement = companionSettlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException("结算记录不存在");
        }
        if (!settlement.getCompanionId().equals(companionId)) {
            throw new BusinessException("无权申诉他人结算记录");
        }
        settlement.setConfirmStatus("DISPUTED");
        settlement.setDisputeContent(disputeContent);
        companionSettlementMapper.updateById(settlement);
        log.info("结算申诉提交成功: id={}, companionId={}", id, companionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long id) {
        CompanionSettlement settlement = companionSettlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException("结算记录不存在");
        }
        if (!"CONFIRMED".equals(settlement.getConfirmStatus())) {
            throw new BusinessException("陪玩师未确认结算，无法执行结算");
        }
        settlement.setSettlementStatus("COMPLETED");
        settlement.setSettledAt(LocalDateTime.now());
        companionSettlementMapper.updateById(settlement);
        log.info("执行结算成功: id={}", id);
    }

    private CompanionSettlementVO buildVO(CompanionSettlement settlement) {
        CompanionSettlementVO vo = BeanUtil.copyProperties(settlement, CompanionSettlementVO.class);
        if (settlement.getCompanionId() != null) {
            Companion companion = companionMapper.selectById(settlement.getCompanionId());
            if (companion != null) {
                vo.setCompanionNickname(companion.getNickname());
            }
        }
        return vo;
    }
}
