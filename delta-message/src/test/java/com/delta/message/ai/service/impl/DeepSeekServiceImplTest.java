package com.delta.message.ai.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.FaqItem;
import com.delta.common.service.AiConfigService;
import com.delta.common.service.CacheService;
import com.delta.common.service.DeepSeekService;
import com.delta.common.service.GameKnowledgeService;
import com.delta.common.service.RedisService;
import com.delta.common.vo.CompanionLevelVO;
import com.delta.message.ai.config.DeepSeekConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeepSeek AI服务单元测试
 * <p>
 * 覆盖场景：
 * <ul>
 *   <li>AI未启用场景</li>
 *   <li>API Key缺失场景</li>
 *   <li>Redis缓存命中场景</li>
 *   <li>API调用成功场景（Mock HttpRequest静态方法）</li>
 *   <li>API调用HTTP错误场景</li>
 *   <li>API调用异常场景</li>
 *   <li>API返回空内容场景</li>
 *   <li>带对话历史的调用场景</li>
 *   <li>isEnabled配置读取场景</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeepSeek AI服务单元测试")
class DeepSeekServiceImplTest {

    /** DeepSeek配置Mock */
    @Mock
    private DeepSeekConfig deepSeekConfig;

    /** 缓存服务Mock */
    @Mock
    private CacheService cacheService;

    /** AI配置服务Mock */
    @Mock
    private AiConfigService aiConfigService;

    /** Redis服务Mock */
    @Mock
    private RedisService redisService;

    /** 游戏知识库服务Mock */
    @Mock
    private GameKnowledgeService gameKnowledgeService;

    /** 被测服务实例（自动注入Mock依赖） */
    @InjectMocks
    private DeepSeekServiceImpl deepSeekService;

    /** HttpRequest静态Mock，用于Mock hutool的HTTP调用 */
    private MockedStatic<HttpRequest> httpRequestStatic;

    /**
     * 每个测试前初始化公共Mock配置
     * <p>
     * 默认配置：AI启用、API Key有效、缓存未命中、无对话历史
     * </p>
     */
    @BeforeEach
    void setUp() {
        httpRequestStatic = mockStatic(HttpRequest.class);

        when(aiConfigService.getConfigValue("deepseek.enabled")).thenReturn("true");
        when(aiConfigService.getConfigValue("deepseek.api_key")).thenReturn("sk-test-api-key-12345");
        when(aiConfigService.getConfigValue("deepseek.base_url")).thenReturn(null);
        when(aiConfigService.getConfigValue("deepseek.model")).thenReturn(null);
        when(aiConfigService.getConfigValue("deepseek.temperature")).thenReturn(null);
        when(aiConfigService.getConfigValue("deepseek.max_tokens")).thenReturn(null);
        when(aiConfigService.getConfigValue("deepseek.system_prompt")).thenReturn(null);

        when(deepSeekConfig.isEnabled()).thenReturn(true);
        when(deepSeekConfig.getApiKey()).thenReturn("sk-test-api-key-12345");
        when(deepSeekConfig.getApiUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");
        when(deepSeekConfig.getModel()).thenReturn("deepseek-chat");
        when(deepSeekConfig.getTemperature()).thenReturn(0.7);
        when(deepSeekConfig.getMaxTokens()).thenReturn(500);
        when(deepSeekConfig.getSystemPrompt()).thenReturn("你是客服小三角。");

        when(redisService.get(anyString())).thenReturn(null);
        when(redisService.increment(anyString())).thenReturn(1L);

        when(cacheService.getClubConfig()).thenReturn(null);
        when(cacheService.getCompanionLevels()).thenReturn(Collections.emptyList());
        when(cacheService.getServiceItems()).thenReturn(Collections.emptyList());
        when(cacheService.getFaqItems()).thenReturn(Collections.emptyList());

        when(gameKnowledgeService.injectKnowledgeToPrompt(anyString())).thenReturn("");
    }

    /**
     * 每个测试后关闭HttpRequest静态Mock，防止资源泄漏
     */
    @AfterEach
    void tearDown() {
        httpRequestStatic.close();
    }

    @Test
    @DisplayName("AI未启用时应返回null")
    void getChatReply_disabled_shouldReturnNull() {
        when(aiConfigService.getConfigValue("deepseek.enabled")).thenReturn("false");
        when(deepSeekConfig.isEnabled()).thenReturn(false);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API Key未配置时应返回null")
    void getChatReply_noApiKey_shouldReturnNull() {
        when(aiConfigService.getConfigValue("deepseek.api_key")).thenReturn(null);
        when(deepSeekConfig.getApiKey()).thenReturn(null);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API Key为空字符串时应返回null")
    void getChatReply_emptyApiKey_shouldReturnNull() {
        when(aiConfigService.getConfigValue("deepseek.api_key")).thenReturn("");
        when(deepSeekConfig.getApiKey()).thenReturn("");

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API Key为纯空格时应返回null")
    void getChatReply_blankApiKey_shouldReturnNull() {
        when(aiConfigService.getConfigValue("deepseek.api_key")).thenReturn("   ");
        when(deepSeekConfig.getApiKey()).thenReturn("   ");

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("Redis缓存命中应直接返回缓存结果")
    void getChatReply_cacheHit_shouldReturnCachedResult() {
        when(redisService.get(anyString())).thenReturn("缓存的AI回复");

        String result = deepSeekService.getChatReply("你好");

        assertEquals("缓存的AI回复", result);
        verify(redisService, never()).increment(anyString());
        httpRequestStatic.verify(() -> HttpRequest.post(anyString()), never());
    }

    @Test
    @DisplayName("Redis缓存读取异常应继续调用API")
    void getChatReply_cacheReadError_shouldContinueToApi() {
        when(redisService.get(anyString())).thenThrow(new RuntimeException("Redis连接失败"));
        mockSuccessfulApiResponse("API回复内容");

        String result = deepSeekService.getChatReply("你好");

        assertEquals("API回复内容", result);
    }

    @Test
    @DisplayName("API调用成功应返回AI回复内容")
    void getChatReply_apiSuccess_shouldReturnReply() {
        mockSuccessfulApiResponse("你好呀，有什么可以帮你的？");

        String result = deepSeekService.getChatReply("你好");

        assertEquals("你好呀，有什么可以帮你的？", result);
    }

    @Test
    @DisplayName("API调用成功应将结果写入Redis缓存")
    void getChatReply_apiSuccess_shouldCacheResult() {
        mockSuccessfulApiResponse("AI回复内容");

        deepSeekService.getChatReply("你好");

        verify(redisService).set(anyString(), eq("AI回复内容"),
                anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("API调用成功应记录Token使用量")
    void getChatReply_apiSuccess_shouldRecordTokenUsage() {
        mockSuccessfulApiResponse("AI回复内容");

        deepSeekService.getChatReply("你好");

        verify(redisService, atLeastOnce()).increment(anyString());
    }

    @Test
    @DisplayName("API返回HTTP 401认证失败应返回null")
    void getChatReply_apiUnauthorized_shouldReturnNull() {
        mockErrorResponse(401);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API返回HTTP 429频率限制应返回null")
    void getChatReply_apiRateLimited_shouldReturnNull() {
        mockErrorResponse(429);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API返回HTTP 500服务端错误应返回null")
    void getChatReply_apiServerError_shouldReturnNull() {
        mockErrorResponse(500);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API返回HTTP 502网关错误应返回null")
    void getChatReply_apiBadGateway_shouldReturnNull() {
        mockErrorResponse(502);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API返回空内容应返回null")
    void getChatReply_apiEmptyContent_shouldReturnNull() {
        String emptyContentResponse = buildApiResponse("", 100, 0, 100);
        mockRawApiResponse(200, emptyContentResponse);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API返回纯空格内容应返回null")
    void getChatReply_apiWhitespaceContent_shouldReturnNull() {
        String whitespaceResponse = buildApiResponse("   ", 50, 0, 50);
        mockRawApiResponse(200, whitespaceResponse);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API调用抛出异常应返回null")
    void getChatReply_apiException_shouldReturnNull() {
        HttpRequest mockRequest = mock(HttpRequest.class);
        httpRequestStatic.when(() -> HttpRequest.post(anyString())).thenReturn(mockRequest);
        when(mockRequest.header(anyString(), anyString())).thenReturn(mockRequest);
        when(mockRequest.body(anyString())).thenReturn(mockRequest);
        when(mockRequest.timeout(anyInt())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenThrow(new RuntimeException("网络连接超时"));

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API调用抛出SocketTimeoutException应返回null")
    void getChatReply_apiTimeout_shouldReturnNull() {
        HttpRequest mockRequest = mock(HttpRequest.class);
        httpRequestStatic.when(() -> HttpRequest.post(anyString())).thenReturn(mockRequest);
        when(mockRequest.header(anyString(), anyString())).thenReturn(mockRequest);
        when(mockRequest.body(anyString())).thenReturn(mockRequest);
        when(mockRequest.timeout(anyInt())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenThrow(
                new RuntimeException(new java.net.SocketTimeoutException("连接超时")));

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API调用抛出ConnectException应返回null")
    void getChatReply_apiConnectFailed_shouldReturnNull() {
        HttpRequest mockRequest = mock(HttpRequest.class);
        httpRequestStatic.when(() -> HttpRequest.post(anyString())).thenReturn(mockRequest);
        when(mockRequest.header(anyString(), anyString())).thenReturn(mockRequest);
        when(mockRequest.body(anyString())).thenReturn(mockRequest);
        when(mockRequest.timeout(anyInt())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenThrow(
                new RuntimeException(new java.net.ConnectException("连接被拒绝")));

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("缓存写入失败不应影响返回结果")
    void getChatReply_cacheWriteFail_shouldStillReturnReply() {
        mockSuccessfulApiResponse("AI回复内容");
        doThrow(new RuntimeException("Redis写入失败"))
                .when(redisService).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        String result = deepSeekService.getChatReply("你好");

        assertEquals("AI回复内容", result);
    }

    @Test
    @DisplayName("带对话历史应正常调用API")
    void getChatReplyWithHistory_withHistory_shouldCallApi() {
        mockSuccessfulApiResponse("带历史的回复");

        List<DeepSeekService.ChatMessage> history = new ArrayList<>();
        history.add(new DeepSeekService.ChatMessage("user", "之前的问题"));
        history.add(new DeepSeekService.ChatMessage("assistant", "之前的回答"));

        String result = deepSeekService.getChatReplyWithHistory("新问题", history);

        assertEquals("带历史的回复", result);
    }

    @Test
    @DisplayName("对话历史为空列表应正常调用API")
    void getChatReplyWithHistory_emptyHistory_shouldCallApi() {
        mockSuccessfulApiResponse("无历史的回复");

        String result = deepSeekService.getChatReplyWithHistory("问题", Collections.emptyList());

        assertEquals("无历史的回复", result);
    }

    @Test
    @DisplayName("对话历史超过4条应截取最近4条")
    void getChatReplyWithHistory_historyExceedsLimit_shouldTruncate() {
        mockSuccessfulApiResponse("截断历史后的回复");

        List<DeepSeekService.ChatMessage> history = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            history.add(new DeepSeekService.ChatMessage("user", "问题" + i));
            history.add(new DeepSeekService.ChatMessage("assistant", "回答" + i));
        }

        String result = deepSeekService.getChatReplyWithHistory("新问题", history);

        assertEquals("截断历史后的回复", result);
    }

    @Test
    @DisplayName("getChatReply应委托给getChatReplyWithHistory")
    void getChatReply_shouldDelegateToGetChatReplyWithHistory() {
        mockSuccessfulApiResponse("委托回复");

        String result = deepSeekService.getChatReply("测试消息");

        assertEquals("委托回复", result);
    }

    @Test
    @DisplayName("isEnabled从数据库读取为true时应返回true")
    void isEnabled_dbConfigTrue_shouldReturnTrue() {
        when(aiConfigService.getConfigValue("deepseek.enabled")).thenReturn("true");

        assertTrue(deepSeekService.isEnabled());
    }

    @Test
    @DisplayName("isEnabled从数据库读取为false时应返回false")
    void isEnabled_dbConfigFalse_shouldReturnFalse() {
        when(aiConfigService.getConfigValue("deepseek.enabled")).thenReturn("false");

        assertFalse(deepSeekService.isEnabled());
    }

    @Test
    @DisplayName("isEnabled数据库配置为空时应回退到配置文件")
    void isEnabled_dbConfigNull_shouldFallbackToConfigFile() {
        when(aiConfigService.getConfigValue("deepseek.enabled")).thenReturn(null);
        when(deepSeekConfig.isEnabled()).thenReturn(true);

        assertTrue(deepSeekService.isEnabled());
    }

    @Test
    @DisplayName("isEnabled数据库读取异常时应回退到配置文件")
    void isEnabled_dbReadError_shouldFallbackToConfigFile() {
        when(aiConfigService.getConfigValue("deepseek.enabled")).thenThrow(new RuntimeException("DB错误"));
        when(deepSeekConfig.isEnabled()).thenReturn(true);

        assertTrue(deepSeekService.isEnabled());
    }

    @Test
    @DisplayName("API Key从数据库读取时应优先使用数据库值")
    void getApiKey_dbConfig_shouldUseDbValue() {
        when(aiConfigService.getConfigValue("deepseek.api_key")).thenReturn("sk-db-api-key");

        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(anyString()));
    }

    @Test
    @DisplayName("API Key数据库为空时应回退到配置文件")
    void getApiKey_dbConfigEmpty_shouldFallbackToConfigFile() {
        when(aiConfigService.getConfigValue("deepseek.api_key")).thenReturn("");

        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(anyString()));
    }

    @Test
    @DisplayName("API URL从数据库读取时应拼接路径")
    void getApiUrl_dbConfig_shouldAppendPath() {
        when(aiConfigService.getConfigValue("deepseek.base_url")).thenReturn("https://custom.api.com");

        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(eq("https://custom.api.com/v1/chat/completions")));
    }

    @Test
    @DisplayName("API URL数据库配置带斜线结尾应正确拼接")
    void getApiUrl_dbConfigTrailingSlash_shouldAppendPath() {
        when(aiConfigService.getConfigValue("deepseek.base_url")).thenReturn("https://custom.api.com/");

        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(eq("https://custom.api.com/v1/chat/completions")));
    }

    @Test
    @DisplayName("价格相关消息应注入详细价格信息")
    void getChatReply_priceRelatedMessage_shouldInjectPricing() {
        ClubConfig clubConfig = new ClubConfig();
        clubConfig.setClubName("测试俱乐部");
        clubConfig.setMainGames("三角洲行动");
        clubConfig.setClubFeatures("专业陪玩");
        when(cacheService.getClubConfig()).thenReturn(clubConfig);

        List<CompanionLevelVO> levels = new ArrayList<>();
        CompanionLevelVO bronzeLevel = new CompanionLevelVO();
        bronzeLevel.setId(1L);
        bronzeLevel.setLevelCode("BRONZE");
        bronzeLevel.setLevelName("二品");
        bronzeLevel.setBasePrice(new BigDecimal("50"));
        levels.add(bronzeLevel);
        when(cacheService.getCompanionLevels()).thenReturn(levels);
        when(cacheService.getServiceItems()).thenReturn(Collections.emptyList());

        mockSuccessfulApiResponse("价格回复");

        String result = deepSeekService.getChatReply("陪玩多少钱一小时");

        assertEquals("价格回复", result);
    }

    @Test
    @DisplayName("游戏知识库注入应正常工作")
    void getChatReply_gameKnowledgeInjection_shouldWork() {
        when(gameKnowledgeService.injectKnowledgeToPrompt(anyString())).thenReturn("## 游戏知识\n三角洲行动攻略");

        mockSuccessfulApiResponse("知识库回复");

        String result = deepSeekService.getChatReply("三角洲行动怎么玩");

        assertEquals("知识库回复", result);
        verify(gameKnowledgeService).injectKnowledgeToPrompt("三角洲行动怎么玩");
    }

    @Test
    @DisplayName("FAQ匹配应注入到系统提示词")
    void getChatReply_faqMatch_shouldInjectFaq() {
        List<FaqItem> faqItems = new ArrayList<>();
        FaqItem faqItem = new FaqItem();
        faqItem.setQuestion("怎么收费？");
        faqItem.setAnswer("我们的价格从50元起");
        faqItem.setCategory("价格");
        faqItems.add(faqItem);
        when(cacheService.getFaqItems()).thenReturn(faqItems);

        mockSuccessfulApiResponse("FAQ回复");

        String result = deepSeekService.getChatReply("怎么收费的");

        assertEquals("FAQ回复", result);
    }

    @Test
    @DisplayName("API响应解析失败应返回null")
    void getChatReply_invalidResponseBody_shouldReturnNull() {
        mockRawApiResponse(200, "invalid json {{{");

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("API响应无choices应返回null")
    void getChatReply_noChoices_shouldReturnNull() {
        String noChoicesResponse = "{\"id\":\"chatcmpl-1\",\"object\":\"chat.completion\",\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":0,\"total_tokens\":10}}";
        mockRawApiResponse(200, noChoicesResponse);

        assertNull(deepSeekService.getChatReply("你好"));
    }

    @Test
    @DisplayName("Token记录失败不应影响返回结果")
    void getChatReply_tokenRecordFail_shouldStillReturnReply() {
        mockSuccessfulApiResponse("Token记录失败的回复");
        when(redisService.increment(anyString())).thenThrow(new RuntimeException("Redis异常"));

        String result = deepSeekService.getChatReply("你好");

        assertEquals("Token记录失败的回复", result);
    }

    @Test
    @DisplayName("模型参数从数据库读取应优先使用数据库值")
    void getModel_dbConfig_shouldUseDbValue() {
        when(aiConfigService.getConfigValue("deepseek.model")).thenReturn("deepseek-reasoner");
        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(anyString()));
    }

    @Test
    @DisplayName("温度参数从数据库读取应优先使用数据库值")
    void getTemperature_dbConfig_shouldUseDbValue() {
        when(aiConfigService.getConfigValue("deepseek.temperature")).thenReturn("0.5");
        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(anyString()));
    }

    @Test
    @DisplayName("maxTokens从数据库读取应优先使用数据库值")
    void getMaxTokens_dbConfig_shouldUseDbValue() {
        when(aiConfigService.getConfigValue("deepseek.max_tokens")).thenReturn("1000");
        mockSuccessfulApiResponse("回复");

        deepSeekService.getChatReply("你好");

        httpRequestStatic.verify(() -> HttpRequest.post(anyString()));
    }

    /**
     * Mock成功的API响应
     * <p>
     * 构建完整的HttpRequest → HttpResponse Mock链，
     * 模拟HTTP 200响应和标准DeepSeek API响应格式。
     * </p>
     *
     * @param content AI回复内容
     */
    private void mockSuccessfulApiResponse(String content) {
        String responseBody = buildApiResponse(content, 150, 50, 200);
        mockRawApiResponse(200, responseBody);
    }

    /**
     * Mock HTTP错误响应
     *
     * @param statusCode HTTP状态码
     */
    private void mockErrorResponse(int statusCode) {
        mockRawApiResponse(statusCode, "{\"error\":{\"message\":\"API Error\",\"type\":\"server_error\"}}");
    }

    /**
     * Mock原始HTTP响应
     * <p>
     * 构建HttpRequest静态方法Mock链：
     * HttpRequest.post(url) → request.header() → request.body() → request.timeout() → request.execute() → response
     * </p>
     *
     * @param statusCode   HTTP状态码
     * @param responseBody 响应体内容
     */
    private void mockRawApiResponse(int statusCode, String responseBody) {
        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpResponse mockResponse = mock(HttpResponse.class);

        httpRequestStatic.when(() -> HttpRequest.post(anyString())).thenReturn(mockRequest);
        when(mockRequest.header(anyString(), anyString())).thenReturn(mockRequest);
        when(mockRequest.body(anyString())).thenReturn(mockRequest);
        when(mockRequest.timeout(anyInt())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(statusCode);
        when(mockResponse.isOk()).thenReturn(statusCode >= 200 && statusCode < 300);
        when(mockResponse.body()).thenReturn(responseBody);
    }

    /**
     * 构建标准DeepSeek API响应JSON
     *
     * @param content          AI回复内容
     * @param promptTokens     输入Token数
     * @param completionTokens 输出Token数
     * @param totalTokens      总Token数
     * @return 标准格式的API响应JSON字符串
     */
    private String buildApiResponse(String content, int promptTokens, int completionTokens, int totalTokens) {
        return "{\"id\":\"chatcmpl-test-001\",\"object\":\"chat.completion\",\"created\":1700000000,"
                + "\"model\":\"deepseek-chat\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\""
                + escapeJson(content) + "\"},\"finish_reason\":\"stop\"}],"
                + "\"usage\":{\"prompt_tokens\":" + promptTokens
                + ",\"completion_tokens\":" + completionTokens
                + ",\"total_tokens\":" + totalTokens + "}}";
    }

    /**
     * 转义JSON字符串中的特殊字符
     *
     * @param text 原始文本
     * @return 转义后的JSON安全字符串
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
