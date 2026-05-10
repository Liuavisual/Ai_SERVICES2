package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CustomerSatisfactionDTO;
import com.delta.common.entity.Companion;
import com.delta.common.entity.CustomerSatisfaction;
import com.delta.common.entity.ServiceTrack;
import com.delta.common.entity.User;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.CustomerSatisfactionMapper;
import com.delta.common.mapper.ServiceTrackMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.CustomerSatisfactionService;
import com.delta.common.util.IdObfuscateUtils;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.CustomerSatisfactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户满意度评价服务实现
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class CustomerSatisfactionServiceImpl implements CustomerSatisfactionService {

    /** 满意度评价Mapper */
    private final CustomerSatisfactionMapper satisfactionMapper;

    /** 服务追踪Mapper */
    private final ServiceTrackMapper serviceTrackMapper;

    /** 陪玩师Mapper */
    private final CompanionMapper companionMapper;

    /** 用户Mapper */
    private final UserMapper userMapper;

    /**
     * 提交满意度评价
     *
     * @param userId 客户ID
     * @param dto    评价数据
     * @return 评价视图对象
     * @throws BusinessException 服务追踪记录不存在或已提交过评价
     */
    @Override
    public CustomerSatisfactionVO submitSatisfaction(Long userId, CustomerSatisfactionDTO dto) {
        // 解码混淆后的服务追踪ID
        Long serviceTrackId = IdObfuscateUtils.decodeRequired(dto.getServiceTrackId());

        // 校验服务追踪记录是否存在
        ServiceTrack track = serviceTrackMapper.selectById(serviceTrackId);
        if (track == null) {
            throw new BusinessException("服务追踪记录不存在");
        }

        // 校验是否已提交过评价，防止重复提交
        LambdaQueryWrapper<CustomerSatisfaction> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(CustomerSatisfaction::getUserId, userId);
        existWrapper.eq(CustomerSatisfaction::getServiceTrackId, serviceTrackId);
        if (satisfactionMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("已提交过评价");
        }

        // 构建满意度评价实体
        CustomerSatisfaction satisfaction = new CustomerSatisfaction();
        satisfaction.setUserId(userId);
        satisfaction.setServiceTrackId(serviceTrackId);
        satisfaction.setCompanionId(track.getBookedCompanionId());
        satisfaction.setRating(dto.getRating());
        satisfaction.setFeedback(dto.getFeedback());
        satisfaction.setServiceType(dto.getServiceType());
        satisfaction.setTags(dto.getTags());
        satisfaction.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);

        // 插入数据库
        satisfactionMapper.insert(satisfaction);

        return convertToVO(satisfaction);
    }

    /**
     * 分页查询满意度评价
     *
     * @param page        页码
     * @param size        每页大小
     * @param companionId 陪玩师ID（可选筛选）
     * @param minRating   最低评分（可选筛选）
     * @param maxRating   最高评分（可选筛选）
     * @return 分页评价视图对象
     */
    @Override
    public Page<CustomerSatisfactionVO> getSatisfactions(int page, int size, Long companionId, Integer minRating, Integer maxRating) {
        // 构建查询条件
        LambdaQueryWrapper<CustomerSatisfaction> wrapper = new LambdaQueryWrapper<>();
        if (companionId != null) {
            wrapper.eq(CustomerSatisfaction::getCompanionId, companionId);
        }
        if (minRating != null) {
            wrapper.ge(CustomerSatisfaction::getRating, minRating);
        }
        if (maxRating != null) {
            wrapper.le(CustomerSatisfaction::getRating, maxRating);
        }
        wrapper.orderByDesc(CustomerSatisfaction::getCreatedAt);

        // 执行分页查询
        Page<CustomerSatisfaction> pageObj = new Page<>(page, size);
        Page<CustomerSatisfaction> resultPage = satisfactionMapper.selectPage(pageObj, wrapper);

        // 批量查询关联的陪玩师信息
        List<Long> companionIds = resultPage.getRecords().stream()
                .map(CustomerSatisfaction::getCompanionId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Companion> companionMap = companionIds.isEmpty() ? Map.of() :
                companionMapper.selectByIds(companionIds).stream()
                        .collect(Collectors.toMap(Companion::getId, c -> c));

        // 批量查询关联的客户信息
        List<Long> userIds = resultPage.getRecords().stream()
                .map(CustomerSatisfaction::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        // 转换为VO并填充关联信息
        Page<CustomerSatisfactionVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<CustomerSatisfactionVO> voList = resultPage.getRecords().stream().map(s -> {
            CustomerSatisfactionVO vo = convertToVO(s);
            // 填充陪玩师昵称
            if (s.getCompanionId() != null) {
                Companion companion = companionMap.get(s.getCompanionId());
                if (companion != null) {
                    vo.setCompanionName(companion.getNickname());
                }
            }
            // 填充客户昵称（匿名时显示"匿名用户"）
            if (s.getUserId() != null) {
                User user = userMap.get(s.getUserId());
                if (user != null) {
                    vo.setUserNickname(s.getIsAnonymous() == 1 ? "匿名用户" : user.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        VoUtils.setRowNumbers(voPage);
        return voPage;
    }

    /**
     * 获取陪玩师平均评分（SQL聚合替代全量加载）
     *
     * @param companionId 陪玩师ID
     * @return 平均评分，无评价时返回0.0
     */
    @Override
    public Double getAverageRating(Long companionId) {
        QueryWrapper<CustomerSatisfaction> wrapper = new QueryWrapper<>();
        wrapper.select("AVG(rating) as avg_rating");
        wrapper.eq("companion_id", companionId);
        List<Map<String, Object>> result = satisfactionMapper.selectMaps(wrapper);
        if (result.isEmpty() || result.get(0).get("avg_rating") == null) {
            return 0.0;
        }
        return ((Number) result.get(0).get("avg_rating")).doubleValue();
    }

    /**
     * 获取陪玩师评价数量
     *
     * @param companionId 陪玩师ID
     * @return 评价数量
     */
    @Override
    public Long getRatingCount(Long companionId) {
        LambdaQueryWrapper<CustomerSatisfaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerSatisfaction::getCompanionId, companionId);
        return satisfactionMapper.selectCount(wrapper);
    }

    /**
     * 将实体转换为视图对象
     *
     * @param s 满意度评价实体
     * @return 视图对象
     */
    private CustomerSatisfactionVO convertToVO(CustomerSatisfaction s) {
        CustomerSatisfactionVO vo = new CustomerSatisfactionVO();
        vo.setId(s.getId());
        vo.setUserId(s.getUserId());
        vo.setServiceTrackId(s.getServiceTrackId());
        vo.setCompanionId(s.getCompanionId());
        vo.setRating(s.getRating());
        vo.setFeedback(s.getFeedback());
        vo.setServiceType(s.getServiceType());
        vo.setTags(s.getTags());
        vo.setIsAnonymous(s.getIsAnonymous());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }
}
