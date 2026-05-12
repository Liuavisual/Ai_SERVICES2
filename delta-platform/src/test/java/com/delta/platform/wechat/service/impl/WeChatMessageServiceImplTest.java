package com.delta.platform.wechat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.constant.PlatformConstants;
import com.delta.common.entity.Message;
import com.delta.common.entity.PendingMessage;
import com.delta.common.entity.User;
import com.delta.common.mapper.ActivityPackageMapper;
import com.delta.common.mapper.ClubConfigMapper;
import com.delta.common.mapper.CompanionLevelMapper;
import com.delta.common.mapper.CompanionMapper;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.ServiceItemMapper;
import com.delta.common.mapper.ServicePriceRuleMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.ContentSafetyService;
import com.delta.common.service.CustomerProfileService;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.OrderService;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.service.ReplyService;
import com.delta.common.service.matcher.KeywordMatcherService;
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

/**
 * 微信消息服务单元测试
 *
 * @author 刘建国
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("微信消息服务单元测试")
class WeChatMessageServiceImplTest {

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

    @Mock
    private ContentSafetyService contentSafetyService;

    @Mock
    private UserMapper userMapper;

    private WeChatMessageServiceImpl weChatMessageService;

    private User testUser;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        weChatMessageService = new WeChatMessageServiceImpl(
                messageMapper, pendingMessageMapper, pendingMessageService,
                deepSeekService, keywordMatcherService, replyService, redisService,
                customerProfileService, clubConfigMapper, serviceItemMapper,
                activityPackageMapper, companionLevelMapper, servicePriceRuleMapper,
                companionMapper, orderService, contentSafetyService, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setPlatform(PlatformConstants.WECHAT);
        testUser.setPlatformUserId("test_wechat_user");
        testUser.setNickname("测试微信用户");
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

        when(contentSafetyService.checkContent(anyString(), anyString()))
                .thenReturn(new ContentSafetyService.ContentSafetyResult(
                        true, null, Collections.emptyList(),
                        ContentSafetyService.SafetyLevel.SAFE, "安全"));
    }

    @Test
    @DisplayName("基本实例化测试")
    void shouldInstantiate() {
        assertNotNull(weChatMessageService);
    }

    @Test
    @DisplayName("处理文本消息 - 已存在用户应返回回复")
    void processTextMessage_existingUser_shouldReturnReply() {
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(List.of("你好"));
        when(replyService.getKeywordReply("你好")).thenReturn("嗨~欢迎来到三角洲行动陪玩俱乐部！");

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你好");

        assertNotNull(result);
        assertTrue(result.contains("欢迎"));
        verify(userMapper).selectOne(any());
        verify(messageMapper, atLeastOnce()).insert(any(Message.class));
    }

    @Test
    @DisplayName("处理文本消息 - 新用户应自动创建")
    void processTextMessage_newUser_shouldCreateUser() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(false);

        String result = weChatMessageService.processTextMessage("new_wechat_user", "你好");

        assertNotNull(result);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("处理文本消息 - 新用户昵称应包含微信用户前缀")
    void processTextMessage_newUser_shouldHaveWechatNicknamePrefix() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertTrue(user.getNickname().startsWith("微信用户"),
                    "新用户昵称应以'微信用户'开头");
            assertEquals(PlatformConstants.WECHAT, user.getPlatform());
            assertTrue(user.getAiEnabled());
            return 1;
        });
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(false);

        weChatMessageService.processTextMessage("new_wechat_user_123456", "你好");
    }

    @Test
    @DisplayName("处理文本消息 - 用户处于客服服务中状态应返回服务中提示")
    void processTextMessage_inServiceState_shouldReturnInServiceReply() {
        PendingMessage processingMsg = new PendingMessage();
        processingMsg.setId(101L);
        processingMsg.setStatus("processing");
        Page<PendingMessage> handoffPage = new Page<>(1, 1, 1);
        handoffPage.setRecords(List.of(processingMsg));
        when(pendingMessageMapper.selectPage(any(Page.class), any())).thenReturn(handoffPage);

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你好");

        assertEquals(AiCustomerServiceConstants.IN_SERVICE_REPLY, result);
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("处理文本消息 - 用户处于等待客服状态应返回等待提示")
    void processTextMessage_waitingState_shouldReturnWaitingReply() {
        PendingMessage pendingMsg = new PendingMessage();
        pendingMsg.setId(100L);
        pendingMsg.setStatus("pending");
        Page<PendingMessage> handoffPage = new Page<>(1, 1, 1);
        handoffPage.setRecords(List.of(pendingMsg));
        when(pendingMessageMapper.selectPage(any(Page.class), any())).thenReturn(handoffPage);

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你好");

        assertTrue(result.contains("安排客服"));
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("处理文本消息 - 客户明确要求人工应转接")
    void processTextMessage_humanExplicit_shouldHandoff() {
        when(keywordMatcherService.matchKeywords("我要找人工")).thenReturn(Collections.emptyList());
        when(keywordMatcherService.matchFirst(eq("我要找人工"), anyList())).thenReturn("人工");
        when(deepSeekService.isEnabled()).thenReturn(true);

        String result = weChatMessageService.processTextMessage("test_wechat_user", "我要找人工");

        assertNotNull(result);
        verify(pendingMessageService).createPendingMessage(
                any(), eq(1L), any(), any(), eq(PlatformConstants.WECHAT), any());
    }

    @Test
    @DisplayName("处理文本消息 - 检测到预约意图应转人工")
    void processTextMessage_orderIntent_shouldHandoff() {
        when(keywordMatcherService.matchKeywords("我要预约")).thenReturn(Collections.emptyList());
        when(keywordMatcherService.matchFirst(eq("我要预约"), anyList())).thenReturn("我要预约");
        when(deepSeekService.isEnabled()).thenReturn(true);

        String result = weChatMessageService.processTextMessage("test_wechat_user", "我要预约");

        assertNotNull(result);
        verify(pendingMessageService).createPendingMessage(
                any(), eq(1L), any(), any(), eq(PlatformConstants.WECHAT), any());
    }

    @Test
    @DisplayName("处理文本消息 - 价格咨询应走AI通道")
    void processTextMessage_priceInquiry_shouldUseAiChannel() {
        when(keywordMatcherService.matchKeywords("多少钱一小时")).thenReturn(List.of("多少钱"));
        when(keywordMatcherService.matchFirst(eq("多少钱一小时"), anyList())).thenAnswer(invocation -> {
            List<?> keywords = invocation.getArgument(1);
            if (keywords == AiCustomerServiceConstants.PRICE_INQUIRY_KEYWORDS) {
                return "多少钱";
            }
            return null;
        });
        when(deepSeekService.isEnabled()).thenReturn(true);
        when(deepSeekService.getChatReplyWithHistory(any(), any())).thenReturn("我们的价格是XX元/小时~");

        String result = weChatMessageService.processTextMessage("test_wechat_user", "多少钱一小时");

        assertNotNull(result);
        verify(deepSeekService).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("处理文本消息 - 服务咨询应走AI通道")
    void processTextMessage_serviceInquiry_shouldUseAiChannel() {
        when(keywordMatcherService.matchKeywords("有什么服务")).thenReturn(List.of("服务"));
        when(keywordMatcherService.matchFirst(eq("有什么服务"), anyList())).thenAnswer(invocation -> {
            List<?> keywords = invocation.getArgument(1);
            if (keywords == AiCustomerServiceConstants.SERVICE_INQUIRY_KEYWORDS) {
                return "服务";
            }
            return null;
        });
        when(deepSeekService.isEnabled()).thenReturn(true);
        when(deepSeekService.getChatReplyWithHistory(any(), any())).thenReturn("我们提供多种陪玩服务~");

        String result = weChatMessageService.processTextMessage("test_wechat_user", "有什么服务");

        assertNotNull(result);
        verify(deepSeekService).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("处理文本消息 - 排班咨询应走AI通道")
    void processTextMessage_scheduleInquiry_shouldUseAiChannel() {
        when(keywordMatcherService.matchKeywords("几点有空")).thenReturn(List.of("几点"));
        when(keywordMatcherService.matchFirst(eq("几点有空"), anyList())).thenAnswer(invocation -> {
            List<?> keywords = invocation.getArgument(1);
            if (keywords == AiCustomerServiceConstants.SCHEDULE_INQUIRY_KEYWORDS) {
                return "几点有空";
            }
            return null;
        });
        when(deepSeekService.isEnabled()).thenReturn(true);
        when(deepSeekService.getChatReplyWithHistory(any(), any())).thenReturn("今天下午2点开始有空哦~");

        String result = weChatMessageService.processTextMessage("test_wechat_user", "几点有空");

        assertNotNull(result);
        verify(deepSeekService).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("处理文本消息 - AI不可用时应返回兜底回复")
    void processTextMessage_aiDisabled_shouldReturnFallback() {
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(false);

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你好");

        assertEquals(AiCustomerServiceConstants.DEFAULT_FALLBACK_REPLY, result);
    }

    @Test
    @DisplayName("处理文本消息 - 异常时应返回错误回复")
    void processTextMessage_exception_shouldReturnErrorReply() {
        when(userMapper.selectOne(any())).thenThrow(new RuntimeException("DB连接异常"));

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你好");

        assertEquals(AiCustomerServiceConstants.WECHAT_ERROR_REPLY, result);
    }

    @Test
    @DisplayName("处理关注事件 - 应返回欢迎回复")
    void processSubscribeEvent_shouldReturnWelcomeReply() {
        when(replyService.getWelcomeReply()).thenReturn("自定义欢迎语");

        String result = weChatMessageService.processSubscribeEvent("test_wechat_user");

        assertEquals("自定义欢迎语", result);
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    @DisplayName("处理关注事件 - 欢迎语为空时应使用默认欢迎语")
    void processSubscribeEvent_nullWelcome_shouldUseDefault() {
        when(replyService.getWelcomeReply()).thenReturn(null);

        String result = weChatMessageService.processSubscribeEvent("test_wechat_user");

        assertEquals(AiCustomerServiceConstants.WECHAT_WELCOME_REPLY, result);
    }

    @Test
    @DisplayName("处理关注事件 - 异常时应返回兜底欢迎语")
    void processSubscribeEvent_exception_shouldReturnFallbackReply() {
        when(userMapper.selectOne(any())).thenThrow(new RuntimeException("DB连接异常"));

        String result = weChatMessageService.processSubscribeEvent("test_wechat_user");

        assertEquals(AiCustomerServiceConstants.WECHAT_WELCOME_FALLBACK_REPLY, result);
    }

    @Test
    @DisplayName("处理文本消息 - 关键词直接回复不应调用AI")
    void processTextMessage_keywordDirectReply_shouldNotCallAi() {
        when(keywordMatcherService.matchKeywords("你好")).thenReturn(List.of("你好"));
        when(replyService.getKeywordReply("你好")).thenReturn("嗨~欢迎来到三角洲行动陪玩俱乐部！");

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你好");

        assertNotNull(result);
        assertTrue(result.contains("欢迎"));
        verify(deepSeekService, never()).getChatReplyWithHistory(any(), any());
    }

    @Test
    @DisplayName("处理文本消息 - AI连续失败应触发转人工")
    @SuppressWarnings("null")
    void processTextMessage_aiConsecutiveFailure_shouldHandoff() {
        when(redisService.get(contains("consecutive"))).thenReturn(3);
        when(keywordMatcherService.matchKeywords("你们有什么活动嘛")).thenReturn(Collections.emptyList());
        when(deepSeekService.isEnabled()).thenReturn(true);

        String result = weChatMessageService.processTextMessage("test_wechat_user", "你们有什么活动嘛");

        assertNotNull(result);
        verify(pendingMessageService).createPendingMessage(
                any(), eq(1L), any(), any(), eq(PlatformConstants.WECHAT), any());
    }
}
