package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CsUserCustomerDTO;
import com.delta.common.vo.CsUserCustomerVO;

/**
 * 客服-客户分配服务接口，管理客服与客户的绑定关系
 *
 * @author delta
 */
public interface CsUserCustomerService {
    
    Page<CsUserCustomerVO> getPage(Integer pageNum, Integer pageSize, Long csUserId, Long customerUserId, String status);
    
    CsUserCustomerVO getById(Long id);
    
    void create(CsUserCustomerDTO dto);
    
    void update(CsUserCustomerDTO dto);
    
    void delete(Long id);
}
