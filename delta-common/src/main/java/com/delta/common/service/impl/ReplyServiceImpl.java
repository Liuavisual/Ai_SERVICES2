package com.delta.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.ReplyDTO;
import com.delta.common.entity.Reply;
import com.delta.common.enums.ReplyTriggerTypeEnum;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.ReplyMapper;
import com.delta.common.service.ReplyService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.ReplyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自动回复服务实现，处理关键词回复和欢迎语回复
 *
 * @author delta
 */
@Service
public class ReplyServiceImpl implements ReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReplyServiceImpl.class);

    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private RedisService redisService;

    private static final String REPLIES_WELCOME_KEY = "delta:replies:welcome";
    private static final String REPLIES_DEFAULT_KEY = "delta:replies:default";
    private static final String KEYWORD_REPLY_PREFIX = "delta:keyword:reply:";

    @Override
    public Page<ReplyVO> getReplyPage(Integer pageNum, Integer pageSize, String triggerType) {
        Page<Reply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<>();

        if (triggerType != null && !triggerType.trim().isEmpty()) {
            wrapper.eq(Reply::getTriggerType, triggerType);
        }

        wrapper.orderByDesc(Reply::getCreatedAt);

        Page<Reply> replyPage = replyMapper.selectPage(page, wrapper);

        Page<ReplyVO> resultPage = new Page<>(replyPage.getCurrent(), replyPage.getSize(), replyPage.getTotal());
        resultPage.setRecords(BeanUtil.copyToList(replyPage.getRecords(), ReplyVO.class));

        return resultPage;
    }

    @Override
    public ReplyVO getReplyById(Long id) {
        Reply reply = replyMapper.selectById(id);
        if (reply == null) {
            throw new BusinessException("回复话术不存在");
        }
        return BeanUtil.copyProperties(reply, ReplyVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReply(ReplyDTO replyDTO) {
        LambdaQueryWrapper<Reply> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Reply::getTriggerType, replyDTO.getTriggerType())
                  .eq(Reply::getTriggerKey, replyDTO.getTriggerKey())
                  .eq(Reply::getEnabled, true);
        if (replyMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException("相同触发类型和触发键的回复话术已存在");
        }

        Reply reply = BeanUtil.copyProperties(replyDTO, Reply.class);
        replyMapper.insert(reply);
        log.info("创建回复话术成功: {}", reply);
        clearReplyCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReply(ReplyDTO replyDTO) {
        Reply reply = replyMapper.selectById(replyDTO.getId());
        if (reply == null) {
            throw new BusinessException("回复话术不存在");
        }

        LambdaQueryWrapper<Reply> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Reply::getTriggerType, replyDTO.getTriggerType())
                  .eq(Reply::getTriggerKey, replyDTO.getTriggerKey())
                  .ne(Reply::getId, replyDTO.getId())
                  .eq(Reply::getEnabled, true);
        if (replyMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException("相同触发类型和触发键的回复话术已存在");
        }

        BeanUtil.copyProperties(replyDTO, reply, "id", "createdAt");
        replyMapper.updateById(reply);
        log.info("更新回复话术成功: {}", reply);
        clearReplyCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(Long id) {
        Reply reply = replyMapper.selectById(id);
        if (reply == null) {
            throw new BusinessException("回复话术不存在");
        }

        replyMapper.deleteById(id);
        log.info("删除回复话术成功: id={}", id);
        clearReplyCache();
    }

    @Override
    public String getWelcomeReply() {
        Object cached = redisService.get(REPLIES_WELCOME_KEY);
        if (cached != null) {
            return cached.toString();
        }

        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reply::getTriggerType, ReplyTriggerTypeEnum.WELCOME.getCode())
               .eq(Reply::getEnabled, true);

        Page<Reply> page = replyMapper.selectPage(new Page<>(1, 1), wrapper);
        Reply reply = page.getRecords().isEmpty() ? null : page.getRecords().get(0);
        String content = reply != null ? reply.getContent() : "您好！欢迎咨询！";
        
        redisService.set(REPLIES_WELCOME_KEY, content);
        return content;
    }

    @Override
    public String getDefaultReply() {
        Object cached = redisService.get(REPLIES_DEFAULT_KEY);
        if (cached != null) {
            return cached.toString();
        }

        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reply::getTriggerType, ReplyTriggerTypeEnum.DEFAULT.getCode())
               .eq(Reply::getEnabled, true);

        Page<Reply> page = replyMapper.selectPage(new Page<>(1, 1), wrapper);
        Reply reply = page.getRecords().isEmpty() ? null : page.getRecords().get(0);
        String content = reply != null ? reply.getContent() : "感谢您的咨询！人工客服正在赶来的路上，请稍候...";
        
        redisService.set(REPLIES_DEFAULT_KEY, content);
        return content;
    }

    @Override
    public String getKeywordReply(String keyword) {
        String cacheKey = KEYWORD_REPLY_PREFIX + keyword;
        Object cached = redisService.get(cacheKey);
        if (cached != null) {
            return cached.toString();
        }

        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reply::getTriggerType, ReplyTriggerTypeEnum.KEYWORD.getCode())
               .eq(Reply::getTriggerKey, keyword)
               .eq(Reply::getEnabled, true);

        Page<Reply> page = replyMapper.selectPage(new Page<>(1, 1), wrapper);
        Reply reply = page.getRecords().isEmpty() ? null : page.getRecords().get(0);
        String content = reply != null ? reply.getContent() : null;
        
        if (content != null) {
            redisService.set(cacheKey, content);
        }
        return content;
    }

    private void clearReplyCache() {
        redisService.delete(REPLIES_WELCOME_KEY);
        redisService.delete(REPLIES_DEFAULT_KEY);
        redisService.deleteByPattern(KEYWORD_REPLY_PREFIX + "*");
    }
}
