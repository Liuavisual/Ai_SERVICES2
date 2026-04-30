package com.delta.common.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.dto.ChatTestSendDTO;
import com.delta.common.entity.Message;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.User;
import com.delta.common.mapper.*;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.OrderService;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.service.ReplyService;
import com.delta.common.service.CustomerProfileService;
import com.delta.common.service.matcher.KeywordMatcherService;
import com.delta.common.vo.ChatTestReplyVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("对话测试服务单元测试")
public class ChatTestServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private PendingMessageMapper pendingMessageMapper;

    @Mock
    private PendingMessageService pendingMessageService;

    @Mock
    private DeepSeekService deepSeekService;

    @Mock
    private KeywordMatcherService keywordMatcherService;

    @Mock
    private ReplyService replyService;

    @Mock
    private RedisService redisService;

    @Mock
    private CustomerProfileService customerProfileService;

    @Mock
    private ClubConfigMapper clubConfigMapper;

    @Mock
    private ServiceItemMapper serviceItemMapper;

    @Mock
    private ActivityPackageMapper activityPackageMapper;

    @Mock
    private CompanionLevelMapper companionLevelMapper;

    @Mock
    private ServicePriceRuleMapper servicePriceRuleMapper;

    @Mock
    private CompanionMapper companionMapper;

    @Mock
    private OrderService orderService;

    private ChatTestServiceImpl chatTestService;

    private User testUser;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        chatTestService = new ChatTestServiceImpl(
                messageMapper, pendingMessageMapper, pendingMessageService,
                deepSeekService, keywordMatcherService, replyService, redisService,
                customerProfileService, clubConfigMapper, serviceItemMapper,
                activityPackageMapper, companionLevelMapper, servicePriceRuleMapper,
                companionMapper, orderService, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setPlatform("wechat");
        testUser.setPlatformUserId("test_wechat_测试用户");
        testUser.setNickname("测试用户");
        testUser.setAiEnabled(true);

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);
        Page<Message> emptyPage = new Page<>(1, 20, 0);
        emptyPage.setRecords(Collections.emptyList());
        when(messageMapper.selectPage(any(Page.class), any())).thenReturn(emptyPage);

        Page<PendingMessage> emptyPendingPage = new Page<>(1, 1, 0);
        emptyPendingPage.setRecords(Collections.emptyList());
        when(pendingMessageMapper.selectPage(any(Page.class), any())).thenReturn(emptyPendingPage);

        when(redisService.get(anyString())).thenReturn(null);
        when(redisService.increment(anyString())).thenReturn(1L);
    }

    @Test
    @DisplayName("DIRECT_REPLY_KEYWORDS中的关键词应走直接回复路径")
    void testDirectReplyForPrice() {
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(List.of("你好"));
        when(replyService.getKeywordReply("你好")).thenReturn("嗨~欢迎来到三角洲行动陪玩俱乐部！");

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("你好");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertNotNull(result.getReplyContent());
        assertTrue(result.getReplyContent().contains("欢迎"));
        assertFalse(result.getAiReply());
        assertEquals("KEYWORD_DIRECT", result.getResponseSource());
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("客户明确要求人工时应转接并通知客服（不调用AI）")
    void testHumanExplicitDetection() {
        when(deepSeekService.isEnabled()).thenReturn(true);

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("我要找人工");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertNotNull(result.getReplyContent());
        assertTrue(result.getReplyContent().contains("转接"));
        assertFalse(result.getAiReply());
        assertEquals("HANDOFF_REPLY", result.getResponseSource());
        verify(pendingMessageService).createPendingMessage(any(), eq(1L), any(), eq("我要找人工"), any(), any());
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("检测到预约意图时应转人工并通知客服（不调用AI）")
    void testOrderIntentDetection() {
        when(deepSeekService.isEnabled()).thenReturn(true);

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("我要预约");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertNotNull(result.getReplyContent());
        assertTrue(result.getReplyContent().contains("人工"));
        assertEquals("HANDOFF_REPLY", result.getResponseSource());
        verify(pendingMessageService).createPendingMessage(any(), eq(1L), any(), eq("我要预约"), any(), any());
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("检测到具体时间+预约意图应转人工")
    void testTimeBasedOrderIntent() {
        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("就今天玩儿，给我预约好");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_REPLY", result.getResponseSource());
        verify(pendingMessageService).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("询问排班/谁在线应转人工")
    void testScheduleInquiryShouldHandoff() {
        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("勇者者行动有人接吗");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_REPLY", result.getResponseSource());
        verify(pendingMessageService).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("指定陪玩师应转人工")
    void testSpecificCompanionRequest() {
        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("我要找红狼陪我打");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_REPLY", result.getResponseSource());
    }

    @Test
    @DisplayName("用户处于等待客服接手状态时，AI应回复等待提示")
    void testWaitingWhenUserInHandoffMode() {
        PendingMessage pendingMsg = new PendingMessage();
        pendingMsg.setId(100L);
        pendingMsg.setStatus("pending");
        Page<PendingMessage> handoffPage = new Page<>(1, 1, 1);
        handoffPage.setRecords(List.of(pendingMsg));
        when(pendingMessageMapper.selectPage(any(Page.class), any())).thenReturn(handoffPage);

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("那就今天晚上八点");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_WAITING", result.getResponseSource());
        assertTrue(result.getReplyContent().contains("安排客服"));
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
        verify(pendingMessageService, never()).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("用户处于客服服务中状态时，AI应完全静默")
    void testSilentWhenUserInService() {
        PendingMessage processingMsg = new PendingMessage();
        processingMsg.setId(101L);
        processingMsg.setStatus("processing");
        Page<PendingMessage> handoffPage = new Page<>(1, 1, 1);
        handoffPage.setRecords(List.of(processingMsg));
        when(pendingMessageMapper.selectPage(any(Page.class), any())).thenReturn(handoffPage);

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("那就今天晚上八点");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_IN_SERVICE", result.getResponseSource());
        assertTrue(result.getReplyContent().contains("客服正在为您服务中"));
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
        verify(pendingMessageService, never()).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("检测到负面情绪时应自动转人工")
    void testNegativeEmotionTriggersHandoff() {
        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("什么破服务，太垃圾了");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_REPLY", result.getResponseSource());
        assertTrue(result.getReplyContent().contains("不满") || result.getReplyContent().contains("转接"));
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
        verify(pendingMessageService).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("AI连续回复3次后应自动触发转人工")
    void testAiConsecutiveFailureTriggersHandoff() {
        when(redisService.get(contains("consecutive"))).thenReturn(3);

        when(keywordMatcherService.matchKeywords("你们有什么活动嘛")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(true);

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("你们有什么活动嘛");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertEquals("HANDOFF_REPLY", result.getResponseSource());
        assertTrue(result.getReplyContent().contains("复杂") || result.getReplyContent().contains("转接"));
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
        verify(pendingMessageService).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("普通游戏问题应调用AI获取回复")
    void testAiReplyForGameQuestion() {
        when(keywordMatcherService.matchKeywords("烽火地带怎么玩")).thenReturn(List.of("烽火"));
        when(replyService.getKeywordReply("烽火")).thenReturn(null);
        when(deepSeekService.isEnabled()).thenReturn(true);
        when(deepSeekService.getChatReplyWithHistory(any(), any())).thenReturn("烽火地带就是搜打撤~ 3人小队进图搜物资打敌人撤离");

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("烽火地带怎么玩");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertNotNull(result.getReplyContent());
        assertTrue(result.getAiReply());
        assertEquals("AI_REPLY", result.getResponseSource());
    }

    @Test
    @DisplayName("无关键词匹配时应调用AI获取回复")
    void testAiReplyForNoKeyword() {
        when(keywordMatcherService.matchKeywords("你们有什么活动嘛")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(true);
        when(deepSeekService.getChatReplyWithHistory(any(), any())).thenReturn("经常有优惠活动哦~");

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("你们有什么活动嘛");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertTrue(result.getAiReply());
        verify(deepSeekService).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("AI不可用时应返回友好兜底回复")
    void testFallbackWhenAiDisabled() {
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(false);

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("你好");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertNotNull(result.getReplyContent());
        assertFalse(result.getAiReply());
        assertEquals("DEFAULT_FALLBACK", result.getResponseSource());
    }

    @Test
    @DisplayName("异常处理应返回友好兜底回复")
    void testExceptionHandling() {
        when(userMapper.selectOne(any())).thenThrow(new RuntimeException("DB error"));

        ChatTestSendDTO dto = new ChatTestSendDTO();
        dto.setContent("你好");
        dto.setPlatform("wechat");
        dto.setCustomerNickname("测试用户");

        ChatTestReplyVO result = chatTestService.sendMessage(dto);

        assertNotNull(result.getReplyContent());
        assertFalse(result.getAiReply());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("验证DIRECT_REPLY_KEYWORDS中的关键词走直接回复路径")
    void testDirectReplyKeywordsNeverCallAi() {
        for (String keyword : AiCustomerServiceConstants.DIRECT_REPLY_KEYWORDS) {
            reset(deepSeekService, replyService, pendingMessageMapper, redisService);
            Page<PendingMessage> emptyP = new Page<>(1, 1, 0);
            emptyP.setRecords(Collections.emptyList());
            when(pendingMessageMapper.selectPage(any(Page.class), any())).thenReturn(emptyP);
            when(redisService.get(anyString())).thenReturn(null);

            when(keywordMatcherService.matchKeywords(keyword)).thenReturn(List.of(keyword));
            when(replyService.getKeywordReply(keyword)).thenReturn("预设：" + keyword);

            ChatTestSendDTO dto = new ChatTestSendDTO();
            dto.setContent(keyword);
            dto.setPlatform("wechat");
            dto.setCustomerNickname("测试用户");

            ChatTestReplyVO result = chatTestService.sendMessage(dto);

            assertEquals("KEYWORD_DIRECT", result.getResponseSource(),
                    "关键词[" + keyword + "]应走直接回复路径");
            verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
        }
    }
}
