package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.QualityCheckRecordVO;

/**
 * 质检记录服务接口
 *
 * @author 刘建国
 */
public interface QualityCheckRecordService {

    Page<QualityCheckRecordVO> getPage(Integer page, Integer size, Long companionId, String riskLevel, String handleStatus);

    QualityCheckRecordVO getById(Long id);

    void handleCheck(Long id, String handleStatus, String handleRemark, Long handlerId);
}
