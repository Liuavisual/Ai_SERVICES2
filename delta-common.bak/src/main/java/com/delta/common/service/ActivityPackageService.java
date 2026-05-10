package com.delta.common.service;

import com.delta.common.dto.ActivityPackageDTO;
import com.delta.common.vo.ActivityPackageVO;

import java.util.List;

public interface ActivityPackageService {

    List<ActivityPackageVO> getByClubId(Long clubConfigId);

    List<ActivityPackageVO> getActivePackages(Long clubConfigId);

    ActivityPackageVO getById(Long id);

    void create(ActivityPackageDTO dto);

    void update(ActivityPackageDTO dto);

    void delete(Long id);
}
