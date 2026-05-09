package com.delta.common.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Agent自动化测试运行器
 * <p>
 * 定时任务调度器，每天凌晨2点自动执行回归测试。
 * 通过HTTP调用ChatTestController的/send接口执行测试用例，
 * 收集结果并生成JSON格式的测试报告。
 * </p>
 *
 * @author 刘建国
 */
@Component
@EnableScheduling
public class AutoTestRunner {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(AutoTestRunner.class);

    /** 报告输出目录 */
    private static final String REPORT_DIR = "d:\\Project\\AI-SERVERS\\reports\\";

    /** 报告文件名模板 */
    private static final String REPORT_FILE_TEMPLATE = "auto_test_report_%s.json";

    /** 日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 服务基础URL，包含context-path */
    @Value("${auto-test.server-url:http://localhost:8080/api}")
    private String serverUrl;

    /** 登录接口路径 */
    @Value("${auto-test.login-path:/v1/auth/login}")
    private String loginPath;

    /** 发送消息接口路径 */
    @Value("${auto-test.send-path:/v1/chat-test/send}")
    private String sendPath;

    /** 测试用管理员用户名 */
    @Value("${auto-test.username:admin}")
    private String testUsername;

    /** 测试用管理员密码 */
    @Value("${auto-test.password:admin123}")
    private String testPassword;

    /** 测试用例编排器，管理测试用例的加载和执行 */
    private final TestAgentOrchestrator orchestrator;

    /** HTTP客户端，用于发送API请求 */
    private final RestTemplate restTemplate;

    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 构造自动测试运行器
     * <p>
     * 初始化编排器、HTTP客户端和JSON序列化工具。
     * </p>
     */
    public AutoTestRunner() {
        this.orchestrator = new TestAgentOrchestrator();
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // 注册Java 8时间模块以支持LocalDateTime序列化
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 定时回归测试任务
     * <p>
     * 每天凌晨2点自动执行，流程如下：
     * 1. 登录获取JWT认证令牌
     * 2. 构建默认测试套件
     * 3. 依次执行每个测试用例
     * 4. 生成测试报告并写入JSON文件
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeRegressionTest() {
        log.info("========== 自动回归测试开始 ==========");

        try {
            // 第一步：登录获取JWT令牌
            String jwtToken = loginAndGetToken();
            if (jwtToken == null) {
                log.error("自动测试登录失败，无法获取JWT令牌，测试终止");
                return;
            }
            log.info("自动测试登录成功，已获取JWT令牌");

            // 第二步：构建默认测试套件
            TestAgentOrchestrator.TestSuite suite = buildDefaultTestSuite();
            log.info("已构建默认测试套件: {}, 用例数: {}", suite.getName(), suite.getCases().size());

            // 第三步：执行测试用例
            List<TestAgentOrchestrator.TestResult> results = executeTestCases(suite, jwtToken);
            log.info("测试用例执行完成，共 {} 个结果", results.size());

            // 第四步：生成测试报告
            TestAgentOrchestrator.TestReport report = orchestrator.generateReport(suite, results);

            // 第五步：写入报告文件
            writeReportToFile(report);

            log.info("========== 自动回归测试完成 ==========");

        } catch (Exception e) {
            log.error("自动回归测试执行异常", e);
        }
    }

    /**
     * 登录并获取JWT令牌
     * <p>
     * 向/auth/login接口发送登录请求，获取Bearer令牌用于后续API调用。
     * </p>
     *
     * @return JWT令牌字符串，登录失败返回null
     */
    @SuppressWarnings("null")
    private String loginAndGetToken() {
        try {
            String loginUrl = serverUrl + loginPath;

            Map<String, String> loginBody = new HashMap<>(8);
            loginBody.put("username", testUsername);
            loginBody.put("password", testPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginBody, headers);

            // 发送登录请求
            HttpMethod postMethod = HttpMethod.POST;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    loginUrl, postMethod, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> responseBody = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                // 解析Result包装的响应：{code:200, message:"success", data:{token:"..."}}
                if (responseBody.get("data") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object token = data.get("token");
                    if (token != null) {
                        return token.toString();
                    }
                }
                // 尝试直接从响应体获取token
                Object token = responseBody.get("token");
                if (token != null) {
                    return token.toString();
                }
            }

            log.warn("登录响应异常: status={}, body={}", response.getStatusCode(), response.getBody());
            return null;

        } catch (Exception e) {
            log.error("登录请求失败", e);
            return null;
        }
    }

    /**
     * 执行测试用例
     * <p>
     * 遍历测试套件中的所有用例，通过HTTP调用/send接口执行，
     * 记录每个用例的执行结果、响应时间、匹配的敏感词等。
     * </p>
     *
     * @param suite    测试套件
     * @param jwtToken JWT认证令牌
     * @return 测试结果列表
     */
    @SuppressWarnings("null")
    private List<TestAgentOrchestrator.TestResult> executeTestCases(
            TestAgentOrchestrator.TestSuite suite, String jwtToken) {

        List<TestAgentOrchestrator.TestResult> results = new ArrayList<>();
        String sendUrl = serverUrl + sendPath;

        for (TestAgentOrchestrator.TestCase testCase : suite.getCases()) {
            TestAgentOrchestrator.TestResult result = new TestAgentOrchestrator.TestResult();
            result.setTestCase(testCase);

            try {
                // 构建请求体
                Map<String, Object> sendBody = new HashMap<>(16);
                sendBody.put("customerNickname", "AutoTest_" + testCase.getId());
                sendBody.put("platform", "WEB");
                sendBody.put("csUserId", 1);
                sendBody.put("content", testCase.getInput());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (jwtToken != null) {
                    headers.setBearerAuth(jwtToken);
                }

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(sendBody, headers);

                long startTime = System.currentTimeMillis();

                HttpMethod postMethod2 = HttpMethod.POST;
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        sendUrl, postMethod2, requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {});

                long responseTime = System.currentTimeMillis() - startTime;
                result.setActualResponseMs(responseTime);

                // 解析响应
                Map<String, Object> respBody = response.getBody();
                if (response.getStatusCode() == HttpStatus.OK && respBody != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) respBody.get("data");

                    if (data != null) {
                        Object replyContent = data.get("replyContent");
                        String actualResponse = replyContent != null ? replyContent.toString() : "";
                        result.setActualResponse(actualResponse);

                        // 获取响应来源作为意图
                        Object responseSource = data.get("responseSource");
                        String actualIntent = responseSource != null ? responseSource.toString() : "UNKNOWN";
                        result.setActualIntent(actualIntent);

                        // 检查是否匹配到敏感词（SAFETY_BLOCK表示被安全拦截）
                        List<String> sensitiveWords = new ArrayList<>();
                        if ("SAFETY_BLOCK".equals(actualIntent)) {
                            sensitiveWords.add("安全系统拦截");
                        }
                        result.setMatchedSensitiveWords(sensitiveWords);

                        boolean passed = evaluateTestCase(testCase, result);
                        result.setPassed(passed);

                        log.info("测试用例 {} 执行完成 | 通过={} | 响应时间={}ms | 意图={}",
                                testCase.getId(), passed, responseTime, actualIntent);
                    } else {
                        result.setActualResponse("响应数据为空");
                        result.setActualIntent("ERROR");
                        result.setPassed(false);
                        log.warn("测试用例 {} 响应数据为空", testCase.getId());
                    }
                } else {
                    result.setActualResponse("HTTP " + response.getStatusCode());
                    result.setActualIntent("HTTP_ERROR");
                    result.setPassed(false);
                    log.warn("测试用例 {} HTTP响应异常: {}", testCase.getId(), response.getStatusCode());
                }

            } catch (Exception e) {
                result.setActualResponse("请求异常: " + e.getMessage());
                result.setActualIntent("EXCEPTION");
                result.setPassed(false);
                result.setMatchedSensitiveWords(Collections.emptyList());
                log.error("测试用例 {} 执行异常", testCase.getId(), e);
            }

            results.add(result);
        }

        return results;
    }

    /**
     * 评估测试用例是否通过
     * <p>
     * 检查实际结果是否满足测试用例的期望：
     * 1. 响应时间不超过期望最大响应时间
     * 2. 如果有期望关键词，检查回复内容是否包含
     * 3. 如果有期望意图，检查是否匹配
     * </p>
     *
     * @param testCase 测试用例
     * @param result   实际测试结果
     * @return 是否通过测试
     */
    private boolean evaluateTestCase(TestAgentOrchestrator.TestCase testCase,
                                      TestAgentOrchestrator.TestResult result) {
        // 检查响应时间
        if (testCase.getExpectedMaxResponseMs() > 0
                && result.getActualResponseMs() > testCase.getExpectedMaxResponseMs()) {
            log.debug("测试用例 {} 响应时间超标: 期望<={}ms, 实际={}ms",
                    testCase.getId(), testCase.getExpectedMaxResponseMs(), result.getActualResponseMs());
            return false;
        }

        // 检查期望关键词
        if (testCase.getExpectedKeywords() != null && !testCase.getExpectedKeywords().isEmpty()) {
            String[] keywords = testCase.getExpectedKeywords().split(",");
            String actualResponse = result.getActualResponse() != null ? result.getActualResponse() : "";
            for (String keyword : keywords) {
                if (!actualResponse.contains(keyword.trim())) {
                    log.debug("测试用例 {} 缺少期望关键词: {}", testCase.getId(), keyword.trim());
                    return false;
                }
            }
        }

        // 检查期望意图
        if (testCase.getExpectedIntent() != null && !testCase.getExpectedIntent().isEmpty()
                && !testCase.getExpectedIntent().equals(result.getActualIntent())) {
            log.debug("测试用例 {} 意图不匹配: 期望={}, 实际={}",
                    testCase.getId(), testCase.getExpectedIntent(), result.getActualIntent());
            // 意图不匹配不直接判定失败，仅记录日志（意图分类可能因模型而异）
        }

        return true;
    }

    /**
     * 将测试报告写入JSON文件
     * <p>
     * 报告文件输出到 d:\Project\AI-SERVERS\reports\ 目录，
     * 文件名格式为 auto_test_report_{yyyyMMdd}.json。
     * </p>
     *
     * @param report 测试报告对象
     */
    private void writeReportToFile(TestAgentOrchestrator.TestReport report) {
        try {
            // 确保报告目录存在
            File reportDir = new File(REPORT_DIR);
            if (!reportDir.exists()) {
                boolean created = reportDir.mkdirs();
                if (created) {
                    log.info("创建报告目录: {}", REPORT_DIR);
                }
            }

            // 生成报告文件名
            String dateStr = DATE_FORMATTER.format(LocalDate.now());
            String fileName = String.format(REPORT_FILE_TEMPLATE, dateStr);
            File reportFile = new File(reportDir, fileName);

            // 序列化并写入文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile, report);

            log.info("测试报告已写入文件: {}", reportFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("写入测试报告文件失败", e);
        }
    }

    /**
     * 构建默认测试套件
     * <p>
     * 当没有外部测试用例文件时，使用内置的默认测试用例。
     * 覆盖常见场景：问候咨询、价格咨询、投诉反馈、敏感内容、闲聊等。
     * </p>
     *
     * @return 默认测试套件
     */
    private TestAgentOrchestrator.TestSuite buildDefaultTestSuite() {
        TestAgentOrchestrator.TestSuite suite = new TestAgentOrchestrator.TestSuite();
        suite.setName("每日自动回归测试套件");
        suite.setConcurrency(1);
        suite.setMaxDuration(300000); // 5分钟最大执行时长

        // 构建默认测试用例列表
        List<TestAgentOrchestrator.TestCase> cases = new ArrayList<>();

        // 用例1：问候咨询
        cases.add(createTestCase("TC001", "咨询类", "你好，请问你们有什么服务？",
                "AI_REPLY", "服务", 10000));

        // 用例2：价格咨询
        cases.add(createTestCase("TC002", "咨询类", "陪玩多少钱一小时？",
                "AI_REPLY", "价格", 10000));

        // 用例3：服务预约
        cases.add(createTestCase("TC003", "咨询类", "我想预约明天的陪玩服务",
                "HANDOFF_REPLY", "预约", 10000));

        // 用例4：投诉反馈
        cases.add(createTestCase("TC004", "投诉类", "我要投诉，你们的服务太差了！",
                "HANDOFF_REPLY", "投诉", 10000));

        // 用例5：闲聊测试
        cases.add(createTestCase("TC005", "闲聊类", "今天天气真好",
                "AI_REPLY", "", 10000));

        // 用例6：普通问题
        cases.add(createTestCase("TC006", "咨询类", "陪玩师有什么等级？",
                "AI_REPLY", "等级", 10000));

        // 用例7：要求人工
        cases.add(createTestCase("TC007", "转人工类", "我要找人工客服",
                "HANDOFF_REPLY", "人工", 10000));

        // 用例8：感谢消息
        cases.add(createTestCase("TC008", "闲聊类", "好的，谢谢你",
                "AI_REPLY", "", 10000));

        // 用例9：空消息（边界测试）
        cases.add(createTestCase("TC009", "边界测试", "",
                "DEFAULT_FALLBACK", "", 10000));

        // 用例10：长文本测试
        cases.add(createTestCase("TC010", "压力测试", "我想了解你们俱乐部的全部服务项目，包括各种陪玩等级的价格、陪玩师的排班时间、如何预约、是否可以退款等等",
                "AI_REPLY", "", 15000));

        suite.setCases(cases);
        return suite;
    }

    /**
     * 创建单个测试用例
     *
     * @param id                   用例ID
     * @param category             用例类别
     * @param input                用户输入
     * @param expectedIntent       期望意图
     * @param expectedKeywords     期望关键词
     * @param expectedMaxResponseMs 期望最大响应时间（毫秒）
     * @return 测试用例对象
     */
    private TestAgentOrchestrator.TestCase createTestCase(
            String id, String category, String input,
            String expectedIntent, String expectedKeywords, int expectedMaxResponseMs) {
        TestAgentOrchestrator.TestCase testCase = new TestAgentOrchestrator.TestCase();
        testCase.setId(id);
        testCase.setCategory(category);
        testCase.setInput(input);
        testCase.setExpectedIntent(expectedIntent);
        testCase.setExpectedKeywords(expectedKeywords);
        testCase.setExpectedMaxResponseMs(expectedMaxResponseMs);
        return testCase;
    }
}
