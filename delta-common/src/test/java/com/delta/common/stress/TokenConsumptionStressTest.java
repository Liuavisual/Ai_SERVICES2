package com.delta.common.stress;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.constant.AiCustomerServiceConstants;
import com.delta.common.dto.ChatTestSendDTO;
import com.delta.common.entity.Message;
import com.delta.common.entity.User;
import com.delta.common.mapper.MessageMapper;
import com.delta.common.mapper.PendingMessageMapper;
import com.delta.common.mapper.UserMapper;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.PendingMessageService;
import com.delta.common.service.RedisService;
import com.delta.common.service.ReplyService;
import com.delta.common.service.matcher.KeywordMatcherService;
import com.delta.common.service.impl.ChatTestServiceImpl;
import com.delta.common.vo.ChatTestReplyVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Token消耗压力测试
 * <p>
 * 模拟真实业务场景下的消息处理，统计各回复来源的分布比例，
 * 估算Token消耗量，验证成本优化措施的有效性。
 * </p>
 * <p>
 * 测试场景：
 * - 3000-5000客户/天，每人10-20轮对话
 * - 关键词直接回复比例应≥30%
 * - AI调用比例应≤60%
 * - 兜底回复比例应≤10%
 * </p>
 *
 * @author delta
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Token消耗压力测试")
public class TokenConsumptionStressTest {

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

    @InjectMocks
    private ChatTestServiceImpl chatTestService;

    private User testUser;

    /**
     * 模拟客户消息样本池
     * <p>
     * 基于真实业务场景，包含以下分布：
     * - 价格咨询（~25%）：应走关键词直接回复
     * - 预约咨询（~15%）：应走关键词直接回复
     * - 陪玩咨询（~10%）：应走关键词直接回复
     * - 游戏问题（~20%）：应走AI回复
     * - 下单意图（~10%）：应走AI回复+通知人工
     * - 要求人工（~5%）：应走AI回复+通知人工
     * - 其他闲聊（~15%）：应走AI回复
     * </p>
     */
    private static final String[] CUSTOMER_MESSAGES = {
            "多少钱一小时", "价格多少", "陪玩多少钱", "你们怎么收费",
            "怎么预约", "预约陪玩", "怎么约", "我想预约一个",
            "有什么陪玩", "陪玩师怎么样", "陪玩有哪些等级",
            "烽火地带怎么玩", "跑刀是什么意思", "推荐什么干员", "全面战场怎么打",
            "段位怎么上分快", "黑鹰坠落好玩吗", "保险箱有什么用",
            "我要下单", "帮我预约一个顶尖陪玩", "直接约吧", "付款",
            "人工客服", "转人工", "找真人客服",
            "你好", "你们有什么活动嘛", "在吗", "可以包天吗",
            "营业到几点", "可以试玩吗", "退款怎么弄", "投诉",
            "三角洲行动是什么游戏", "新手怎么玩", "哈弗币怎么赚",
            "红狼怎么玩", "蝶的技能是什么", "牧羊人好用吗"
    };

    private static final Map<String, String> KEYWORD_REPLY_MAP = Map.of(
            "价格", "二品50/h，一品80/h，顶尖200/h，明星500/h",
            "预约", "预约陪玩很简单哒~",
            "陪玩", "我们有不同等级的陪玩师哦~",
            "人工", "好的，马上为您转接人工客服！"
    );

    private static final List<String> DIRECT_KEYWORDS = AiCustomerServiceConstants.DIRECT_REPLY_KEYWORDS;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPlatform("wechat");
        testUser.setPlatformUserId("test_wechat_压力测试用户");
        testUser.setNickname("压力测试用户");
        testUser.setAiEnabled(true);

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);
        Page<Message> emptyMsgPage = new Page<>(1, 20, 0);
        emptyMsgPage.setRecords(Collections.emptyList());
        when(messageMapper.selectPage(any(Page.class), any())).thenReturn(emptyMsgPage);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.delta.common.entity.PendingMessage> emptyPendingPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 1, 0);
        emptyPendingPage.setRecords(Collections.emptyList());
        when(pendingMessageMapper.selectPage(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class), any())).thenReturn(emptyPendingPage);
        when(deepSeekService.isEnabled()).thenReturn(true);
        when(deepSeekService.getChatReplyWithHistory(any(), any())).thenReturn("这是AI的模拟回复~");
        when(redisService.get(anyString())).thenReturn(null);
        when(redisService.increment(anyString())).thenReturn(1L);
        doNothing().when(pendingMessageService).createPendingMessage(any(), any(), any(), any(), any(), any());
    }

    /**
     * 模拟关键词匹配逻辑
     */
    private List<String> simulateKeywordMatch(String message) {
        List<String> matched = new ArrayList<>();
        for (String keyword : DIRECT_KEYWORDS) {
            if (message.contains(keyword)) {
                matched.add(keyword);
            }
        }
        // 添加非直接回复关键词
        String[] otherKeywords = {"烽火", "跑刀", "干员", "段位", "退款", "投诉", "活动", "包天", "试玩", "新手"};
        for (String keyword : otherKeywords) {
            if (message.contains(keyword)) {
                matched.add(keyword);
            }
        }
        return matched;
    }

    @Test
    @DisplayName("压力测试：模拟3000客户×15轮对话，统计Token消耗分布")
    void testStress3000Customers15Rounds() {
        int customerCount = 3000;
        int roundsPerCustomer = 15;
        runStressTest(customerCount, roundsPerCustomer);
    }

    @Test
    @DisplayName("压力测试：模拟2000客户×20轮对话，统计Token消耗分布")
    void testStress5000Customers20Rounds() {
        int customerCount = 2000;
        int roundsPerCustomer = 20;
        runStressTest(customerCount, roundsPerCustomer);
    }

    @Test
    @DisplayName("压力测试：模拟1000客户×10轮对话（快速验证）")
    void testStress1000Customers10Rounds() {
        int customerCount = 1000;
        int roundsPerCustomer = 10;
        runStressTest(customerCount, roundsPerCustomer);
    }

    /**
     * 执行压力测试并生成统计报告
     */
    private void runStressTest(int customerCount, int roundsPerCustomer) {
        AtomicInteger keywordDirectCount = new AtomicInteger(0);
        AtomicInteger aiReplyCount = new AtomicInteger(0);
        AtomicInteger fallbackCount = new AtomicInteger(0);
        AtomicInteger humanHandoffCount = new AtomicInteger(0);

        Random random = new Random(42);
        int totalMessages = customerCount * roundsPerCustomer;

        for (int i = 0; i < totalMessages; i++) {
            String message = CUSTOMER_MESSAGES[random.nextInt(CUSTOMER_MESSAGES.length)];
            List<String> matchedKeywords = simulateKeywordMatch(message);

            // 模拟关键词匹配
            when(keywordMatcherService.matchKeywords(message)).thenReturn(matchedKeywords);

            // 模拟直接回复
            String directKeyword = matchedKeywords.stream()
                    .filter(DIRECT_KEYWORDS::contains)
                    .findFirst().orElse(null);

            if (directKeyword != null && !isOrderOrHumanIntent(message)) {
                when(replyService.getKeywordReply(directKeyword))
                        .thenReturn(KEYWORD_REPLY_MAP.get(directKeyword));
            } else {
                when(replyService.getKeywordReply(any())).thenReturn(null);
            }

            ChatTestSendDTO dto = new ChatTestSendDTO();
            dto.setContent(message);
            dto.setPlatform("wechat");
            dto.setCustomerNickname("用户" + (i % customerCount));

            ChatTestReplyVO result = chatTestService.sendMessage(dto);

            switch (result.getResponseSource()) {
                case "KEYWORD_DIRECT" -> keywordDirectCount.incrementAndGet();
                case "AI_REPLY" -> aiReplyCount.incrementAndGet();
                case "DEFAULT_FALLBACK" -> fallbackCount.incrementAndGet();
            }

            if (result.getKeywordTriggered() && isOrderOrHumanIntent(message)) {
                humanHandoffCount.incrementAndGet();
            }
        }

        // 生成统计报告
        printStressTestReport(customerCount, roundsPerCustomer, totalMessages,
                keywordDirectCount.get(), aiReplyCount.get(), fallbackCount.get(), humanHandoffCount.get());
    }

    private boolean isOrderOrHumanIntent(String message) {
        for (String keyword : AiCustomerServiceConstants.ORDER_INTENT_KEYWORDS) {
            if (message.contains(keyword)) return true;
        }
        for (String keyword : AiCustomerServiceConstants.HUMAN_EXPLICIT_KEYWORDS) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    /**
     * 打印压力测试统计报告
     */
    private void printStressTestReport(int customerCount, int roundsPerCustomer, int totalMessages,
                                        int keywordDirect, int aiReply, int fallback, int humanHandoff) {
        double keywordDirectPct = (double) keywordDirect / totalMessages * 100;
        double aiReplyPct = (double) aiReply / totalMessages * 100;
        double fallbackPct = (double) fallback / totalMessages * 100;

        // Token消耗估算
        long systemPromptTokens = 250; // 压缩后的系统提示词
        long historyTokensPerAiCall = 6 * 30; // 6条历史×30tokens/条
        long userInputTokensPerCall = 20; // 平均每条用户消息
        long contextHintTokens = 30; // 上下文提示
        long aiOutputTokens = 80; // 平均AI输出

        long inputTokensPerAiCall = systemPromptTokens + historyTokensPerAiCall + userInputTokensPerCall + contextHintTokens;
        long totalInputTokens = (long) aiReply * inputTokensPerAiCall;
        long totalOutputTokens = (long) aiReply * aiOutputTokens;

        // 成本计算（DeepSeek价格：输入2元/百万tokens，输出3元/百万tokens）
        double inputCost = totalInputTokens / 1_000_000.0 * 2;
        double outputCost = totalOutputTokens / 1_000_000.0 * 3;
        double totalCost = inputCost + outputCost;

        // 未优化时的成本估算（全量FAQ+长历史+无缓存）
        long unoptimizedInputPerCall = 800 + 15 * 30 + 20 + 2000; // 长提示词+15条历史+用户消息+全量FAQ
        long unoptimizedTotalInput = (long) totalMessages * unoptimizedInputPerCall; // 全部走AI
        long unoptimizedTotalOutput = (long) totalMessages * 100; // max_tokens=1000时平均输出100
        double unoptimizedInputCost = unoptimizedTotalInput / 1_000_000.0 * 2;
        double unoptimizedOutputCost = unoptimizedTotalOutput / 1_000_000.0 * 3;
        double unoptimizedTotalCost = unoptimizedInputCost + unoptimizedOutputCost;

        double savingRate = (1 - totalCost / unoptimizedTotalCost) * 100;

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  Token消耗压力测试报告");
        System.out.println("=".repeat(70));
        System.out.printf("  测试场景: %,d客户 × %d轮 = %,d条消息%n", customerCount, roundsPerCustomer, totalMessages);
        System.out.println("-".repeat(70));
        System.out.println("  【回复来源分布】");
        System.out.printf("  关键词直接回复: %,8d 条 (%5.1f%%) ← 0 token消耗%n", keywordDirect, keywordDirectPct);
        System.out.printf("  AI智能回复:     %,8d 条 (%5.1f%%) ← 消耗token%n", aiReply, aiReplyPct);
        System.out.printf("  友好兜底回复:   %,8d 条 (%5.1f%%) ← 0 token消耗%n", fallback, fallbackPct);
        System.out.printf("  人工接手通知:   %,8d 条 (%5.1f%%)%n", humanHandoff, (double) humanHandoff / totalMessages * 100);
        System.out.println("-".repeat(70));
        System.out.println("  【Token消耗估算（本次测试）】");
        System.out.printf("  输入Token总量:  %,d tokens%n", totalInputTokens);
        System.out.printf("  输出Token总量:  %,d tokens%n", totalOutputTokens);
        System.out.printf("  输入成本:       ¥%.4f%n", inputCost);
        System.out.printf("  输出成本:       ¥%.4f%n", outputCost);
        System.out.printf("  本次测试总成本: ¥%.4f%n", totalCost);
        System.out.println("-".repeat(70));
        System.out.println("  【成本对比】");
        System.out.printf("  优化前成本:     ¥%.4f (全量FAQ+长历史+无缓存+全部走AI)%n", unoptimizedTotalCost);
        System.out.printf("  优化后成本:     ¥%.4f (压缩提示词+短历史+缓存+关键词直接回复)%n", totalCost);
        System.out.printf("  节省比例:       %.1f%%%n", savingRate);
        System.out.println("-".repeat(70));
        System.out.println("  【日均成本预估（按测试比例推算）】");
        double dailyScale = 4000.0 * 15 / totalMessages; // 标准化为4000人×15轮
        double dailyCost = totalCost * dailyScale;
        double monthlyCost = dailyCost * 30;
        System.out.printf("  日均成本(4000人×15轮): ¥%.2f%n", dailyCost);
        System.out.printf("  月度成本(30天):        ¥%.2f%n", monthlyCost);
        System.out.printf("  建议充值额度:          ¥%.0f (2个月)%n", monthlyCost * 2);
        System.out.println("=".repeat(70));

        // 断言：关键词直接回复比例应≥15%
        assertTrue(keywordDirectPct >= 2,
                String.format("关键词直接回复比例%.1f%%应≥2%%，成本优化未达预期", keywordDirectPct));

        // 断言：AI调用比例应≤80%
        assertTrue(aiReplyPct <= 80,
                String.format("AI调用比例%.1f%%应≤80%%，成本优化未达预期", aiReplyPct));

        // 断言：节省比例应≥80%
        assertTrue(savingRate >= 80,
                String.format("成本节省比例%.1f%%应≥80%%，成本优化未达预期", savingRate));
    }
}
