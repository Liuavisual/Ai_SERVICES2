package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.ActivityPackageDTO;
import com.delta.common.entity.ActivityPackage;
import com.delta.common.entity.GameConfig;
import com.delta.common.entity.ServiceItem;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ActivityPackageMapper;
import com.delta.common.mapper.GameConfigMapper;
import com.delta.common.mapper.ServiceItemMapper;
import com.delta.common.service.ActivityPackageService;
import com.delta.common.vo.ActivityPackageVO;
import com.delta.common.util.IdObfuscateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityPackageServiceImpl implements ActivityPackageService {

    private final ActivityPackageMapper activityPackageMapper;

    private final GameConfigMapper gameConfigMapper;

    private final ServiceItemMapper serviceItemMapper;

    @Override
    public List<ActivityPackageVO> getByClubId(Long clubConfigId) {
        LambdaQueryWrapper<ActivityPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPackage::getClubConfigId, clubConfigId);
        wrapper.orderByAsc(ActivityPackage::getSortOrder);
        List<ActivityPackage> packages = activityPackageMapper.selectList(wrapper);
        if (packages.isEmpty()) return Collections.emptyList();

        // 批量查询GameConfig和ServiceItem，避免N+1查询
        Map<Long, GameConfig> gameConfigMap = batchQueryGameConfig(packages);
        Map<Long, ServiceItem> serviceItemMap = batchQueryServiceItem(packages);

        return packages.stream()
                .map(pkg -> convertToVO(pkg, gameConfigMap, serviceItemMap))
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityPackageVO> getActivePackages(Long clubConfigId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ActivityPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPackage::getClubConfigId, clubConfigId);
        wrapper.eq(ActivityPackage::getEnabled, BusinessStatusConstants.ENABLED_INT);
        wrapper.le(ActivityPackage::getStartTime, now);
        wrapper.ge(ActivityPackage::getEndTime, now);
        wrapper.orderByAsc(ActivityPackage::getSortOrder);
        List<ActivityPackage> packages = activityPackageMapper.selectList(wrapper);
        if (packages.isEmpty()) return Collections.emptyList();

        // 批量查询GameConfig和ServiceItem，避免N+1查询
        Map<Long, GameConfig> gameConfigMap = batchQueryGameConfig(packages);
        Map<Long, ServiceItem> serviceItemMap = batchQueryServiceItem(packages);

        return packages.stream()
                .map(pkg -> convertToVO(pkg, gameConfigMap, serviceItemMap))
                .collect(Collectors.toList());
    }

    @Override
    public ActivityPackageVO getById(Long id) {
        ActivityPackage pkg = activityPackageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException("活动套餐不存在");
        }
        // 单条记录也使用批量查询方式，保持一致性
        Map<Long, GameConfig> gameConfigMap = batchQueryGameConfig(List.of(pkg));
        Map<Long, ServiceItem> serviceItemMap = batchQueryServiceItem(List.of(pkg));
        return convertToVO(pkg, gameConfigMap, serviceItemMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ActivityPackageDTO dto) {
        ActivityPackage pkg = new ActivityPackage();
        BeanUtil.copyProperties(dto, pkg);
        if (pkg.getEnabled() == null) pkg.setEnabled(BusinessStatusConstants.ENABLED_INT);
        if (pkg.getSortOrder() == null) pkg.setSortOrder(BusinessStatusConstants.DISABLED_INT);
        activityPackageMapper.insert(pkg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActivityPackageDTO dto) {
        if (dto.getId() == null || activityPackageMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("活动套餐不存在");
        }
        ActivityPackage pkg = new ActivityPackage();
        BeanUtil.copyProperties(dto, pkg);
        activityPackageMapper.updateById(pkg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (activityPackageMapper.selectById(id) == null) {
            throw new BusinessException("活动套餐不存在");
        }
        activityPackageMapper.deleteById(id);
    }

    /**
     * 批量查询GameConfig，返回ID到实体的映射
     *
     * @param packages 活动套餐列表
     * @return GameConfig ID到实体的映射
     */
    private Map<Long, GameConfig> batchQueryGameConfig(List<ActivityPackage> packages) {
        Set<Long> gameConfigIds = packages.stream()
                .map(ActivityPackage::getGameConfigId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (gameConfigIds.isEmpty()) return Collections.emptyMap();
        return gameConfigMapper.selectByIds(gameConfigIds).stream()
                .collect(Collectors.toMap(GameConfig::getId, Function.identity()));
    }

    /**
     * 批量查询ServiceItem，返回ID到实体的映射
     *
     * @param packages 活动套餐列表
     * @return ServiceItem ID到实体的映射
     */
    private Map<Long, ServiceItem> batchQueryServiceItem(List<ActivityPackage> packages) {
        Set<Long> serviceItemIds = packages.stream()
                .map(ActivityPackage::getServiceItemIds)
                .filter(Objects::nonNull)
                .flatMap(ids -> Arrays.stream(ids.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(IdObfuscateUtils::decode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (serviceItemIds.isEmpty()) return Collections.emptyMap();
        return serviceItemMapper.selectByIds(serviceItemIds).stream()
                .collect(Collectors.toMap(ServiceItem::getId, Function.identity()));
    }

    /**
     * 将ActivityPackage实体转换为VO（使用预查询的Map避免N+1查询）
     *
     * @param pkg             活动套餐实体
     * @param gameConfigMap   GameConfig ID到实体的映射
     * @param serviceItemMap  ServiceItem ID到实体的映射
     * @return 活动套餐VO
     */
    private ActivityPackageVO convertToVO(ActivityPackage pkg, Map<Long, GameConfig> gameConfigMap, Map<Long, ServiceItem> serviceItemMap) {
        ActivityPackageVO vo = new ActivityPackageVO();
        BeanUtil.copyProperties(pkg, vo);

        // 从预查询Map中获取GameConfig，替代循环内selectById
        if (pkg.getGameConfigId() != null) {
            GameConfig game = gameConfigMap.get(pkg.getGameConfigId());
            if (game != null) {
                vo.setGameName(game.getGameName());
            }
        }

        // 从预查询Map中获取ServiceItem，替代循环内selectById
        if (pkg.getServiceItemIds() != null && !pkg.getServiceItemIds().isEmpty()) {
            List<String> names = Arrays.stream(pkg.getServiceItemIds().split(","))
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::trim)
                    .map(IdObfuscateUtils::decode)
                    .filter(sid -> sid != null)
                    .map(sid -> {
                        ServiceItem item = serviceItemMap.get(sid);
                        return item != null ? item.getServiceName() : "";
                    })
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            vo.setServiceItemNames(String.join("、", names));
        }

        LocalDateTime now = LocalDateTime.now();
        if (pkg.getEnabled() != null && Integer.valueOf(BusinessStatusConstants.ENABLED_INT).equals(pkg.getEnabled())) {
            if (pkg.getStartTime() != null && pkg.getEndTime() != null) {
                if (now.isAfter(pkg.getStartTime()) && now.isBefore(pkg.getEndTime())) {
                    vo.setStatus("进行中");
                } else if (now.isBefore(pkg.getStartTime())) {
                    vo.setStatus("未开始");
                } else {
                    vo.setStatus("已结束");
                }
            } else {
                vo.setStatus("永久有效");
            }
        } else {
            vo.setStatus("已禁用");
        }

        return vo;
    }
}
