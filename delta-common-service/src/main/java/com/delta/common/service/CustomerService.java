package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.vo.CustomerVO;
import jakarta.servlet.http.HttpServletResponse;

public interface CustomerService {

    Page<CustomerVO> getCustomerPage(Integer page, Integer size, String platform, Boolean aiEnabled, Long csUserId, String keyword);

    CustomerVO getCustomerById(Long id);

    CustomerVO getCustomerById(Long id, Long currentUserId, String currentUserRole);

    void toggleAiEnabled(Long id, Boolean aiEnabled);

    void assignCustomer(Long id, Long csUserId, String assignType, String remark);

    /**
     * 同步客户分配关系，以CsUserCustomer为权威数据源
     *
     * @param userId 客户用户ID
     */
    void syncCustomerAssignments(Long userId);

    boolean isCustomerAssignedToCsStaff(Long customerId, Long csUserId);

    /**
     * 导出客户Excel
     *
     * @param response  HTTP响应
     * @param platform  平台
     * @param aiEnabled AI启用状态
     * @param csUserId  客服用户ID
     * @param keyword   关键词
     */
    void exportCustomers(HttpServletResponse response, String platform, Boolean aiEnabled, Long csUserId, String keyword);
}
