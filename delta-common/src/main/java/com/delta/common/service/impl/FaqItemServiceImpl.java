package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.BusinessStatusConstants;
import com.delta.common.dto.FaqItemDTO;
import com.delta.common.entity.FaqItem;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.FaqItemMapper;
import com.delta.common.service.FaqItemService;
import com.delta.common.util.VoUtils;
import com.delta.common.vo.FaqItemVO;
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
    public Page<FaqItemVO> getFaqItems(int page, int size, String category) {
        Page<FaqItem> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<FaqItem> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(FaqItem::getCategory, category);
        }
        wrapper.orderByAsc(FaqItem::getSortOrder)
               .orderByDesc(FaqItem::getCreatedAt);
        Page<FaqItem> faqPage = faqItemMapper.selectPage(pageObj, wrapper);

        Page<FaqItemVO> resultPage = new Page<>(faqPage.getCurrent(), faqPage.getSize(), faqPage.getTotal());
        resultPage.setRecords(BeanUtil.copyToList(faqPage.getRecords(), FaqItemVO.class));
        VoUtils.setRowNumbers(resultPage);
        return resultPage;
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
        if (dto == null || dto.getId() == null) {
            throw new BusinessException("FAQ条目参数不能为空");
        }
        FaqItem existing = faqItemMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("FAQ条目不存在");
        }
        BeanUtils.copyProperties(dto, existing);
        faqItemMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFaqItem(Long id) {
        FaqItem existing = faqItemMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("FAQ条目不存在");
        }
        faqItemMapper.deleteById(id);
    }
}
