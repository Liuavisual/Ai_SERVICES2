package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.FaqItemDTO;
import com.delta.common.entity.FaqItem;

import java.util.List;

public interface FaqItemService {
    Page<FaqItem> getFaqItems(int page, int size, String category);
    List<FaqItem> getEnabledFaqItems();
    void addFaqItem(FaqItemDTO dto);
    void updateFaqItem(FaqItemDTO dto);
    void deleteFaqItem(Long id);
}
