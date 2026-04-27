package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.CustomerVO;

public interface CustomerService {

    Page<CustomerVO> getCustomerPage(Integer pageNum, Integer pageSize, String platform, Boolean aiEnabled, Long csUserId, String keyword);

    CustomerVO getCustomerById(Long id);

    CustomerVO getCustomerById(Long id, Long currentUserId, String currentUserRole);

    void toggleAiEnabled(Long id, Boolean aiEnabled);

    void assignCustomer(Long id, Long csUserId, String assignType, String remark);

    boolean isCustomerAssignedToCsStaff(Long customerId, Long csUserId);
}
