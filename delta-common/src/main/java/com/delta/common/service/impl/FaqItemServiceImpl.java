package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.FaqItemDTO;
import com.delta.common.entity.FaqItem;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.FaqItemMapper;
import com.delta.common.service.FaqItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaqItemServiceImpl implements FaqItemService {

    @Autowired
    private FaqItemMapper faqItemMapper;

    @Override
    public Page<FaqItem> getFaqItems(int page, int size, String category) {
        Page<FaqItem> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<FaqItem> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(FaqItem::getCategory, category);
        }
        wrapper.orderByAsc(FaqItem::getSortOrder);
        return faqItemMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public List<FaqItem> getEnabledFaqItems() {
        LambdaQueryWrapper<FaqItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FaqItem::getEnabled, BusinessStatusConstants.ENABLED_INT);
        wrapper.orderByAsc(FaqItem::getSortOrder);
        return faqItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFaqItem(FaqItemDTO dto) {
        if (dto == null) {
            throw new BusinessException("FAQ条目参数不能为空");
        }
        FaqItem item = new FaqItem();
        BeanUtils.copyProperties(dto, item);
        faqItemMapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFaqItem(FaqItemDTO dto) {
        if (dto == null || dto.getId() == null || faqItemMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("FAQ条目不存在");
        }
        FaqItem item = new FaqItem();
        BeanUtils.copyProperties(dto, item);
        faqItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFaqItem(Long id) {
        if (faqItemMapper.selectById(id) == null) {
            throw new BusinessException("FAQ条目不存在");
        }
        faqItemMapper.deleteById(id);
    }
}
