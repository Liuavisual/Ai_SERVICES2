package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.dto.PlatformConfigDTO;
import com.delta.common.entity.PlatformConfig;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.PlatformConfigMapper;
import com.delta.common.service.PlatformConfigService;
import com.delta.common.vo.PlatformConfigVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 平台配置服务实现，管理平台接入参数
 *
 * @author delta
 */
@Service
public class PlatformConfigServiceImpl implements PlatformConfigService {

    private static final Logger log = LoggerFactory.getLogger(PlatformConfigServiceImpl.class);

    @Autowired
    private PlatformConfigMapper platformConfigMapper;

    @Override
    public List<PlatformConfigVO> getAllPlatformConfigs() {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(PlatformConfig::getId);

        List<PlatformConfig> configs = platformConfigMapper.selectList(wrapper);
        return BeanUtil.copyToList(configs, PlatformConfigVO.class);
    }

    @Override
    public PlatformConfigVO getPlatformConfigByPlatform(String platform) {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getPlatform, platform);

        PlatformConfig config = platformConfigMapper.selectOne(wrapper);
        return config != null ? BeanUtil.copyProperties(config, PlatformConfigVO.class) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlatformConfig(PlatformConfigDTO configDTO) {
        PlatformConfig config = platformConfigMapper.selectById(configDTO.getId());
        if (config == null) {
            throw new BusinessException("平台配置不存在");
        }

        BeanUtil.copyProperties(configDTO, config, "id", "createdAt");
        platformConfigMapper.updateById(config);
        log.info("更新平台配置成功: platform={}", config.getPlatform());
    }

    @Override
    public boolean isPlatformEnabled(String platform) {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getPlatform, platform)
               .eq(PlatformConfig::getEnabled, true);

        return platformConfigMapper.selectCount(wrapper) > 0;
    }
}
