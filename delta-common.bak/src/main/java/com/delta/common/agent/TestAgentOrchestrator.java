package com.delta.common.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 测试用例编排器
 * <p>
 * 负责测试用例的加载、测试套件的执行以及测试报告的生成。
 * 支持从JSON文件加载测试用例，按套件执行并收集结果，最终生成汇总报告。
 * </p>
 *
 * @author 刘建国
 */
public class TestAgentOrchestrator {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(TestAgentOrchestrator.class);

    /** JSON序列化/反序列化工具 */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从文件加载测试用例
     * <p>
     * 读取指定路径的JSON文件，将其反序列化为TestSuite对象。
     * JSON文件应包含完整的测试套件定义（名称、用例列表、并发数、最大执行时长等）。
     * </p>
     *
     * @param filePath JSON测试用例文件路径
     * @return 加载的测试套件，如果加载失败返回null
     */
    public TestSuite loadTestCases(String filePath) {
        log.info("开始加载测试用例，文件路径: {}", filePath);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("测试用例文件不存在: {}", filePath);
                return null;
            }
            TestSuite suite = objectMapper.readValue(file, TestSuite.class);
            log.info("测试用例加载成功，套件名称: {}, 用例数: {}", suite.getName(), suite.getCases().size());
            return suite;
        } catch (IOException e) {
            log.error("加载测试用例失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 执行测试套件
     * <p>
     * 遍历测试套件中的所有测试用例，依次执行并收集结果。
     * 每个用例的执行结果包含是否通过、实际意图、实际响应、响应耗时、匹配的敏感词等。
     * 并发数和最大执行时长用于后续扩展并发执行和超时控制。
     * </p>
     *
     * @param suite 待执行的测试套件
     * @return 所有测试用例的执行结果列表
     */
    public List<TestResult> executeTestSuite(TestSuite suite) {
        log.info("开始执行测试套件: {}, 用例数: {}, 并发数: {}",
                suite.getName(), suite.getCases().size(), suite.getConcurrency());

        List<TestResult> results = new ArrayList<>();

        for (TestCase testCase : suite.getCases()) {
            log.debug("执行测试用例: id={}, category={}, input={}",
                    testCase.getId(), testCase.getCategory(),
                    testCase.getInput().substring(0, Math.min(50, testCase.getInput().length())));

            // 创建空的测试结果占位（实际执行由AutoTestRunner通过HTTP调用完成）
            TestResult result = new TestResult();
            result.setTestCase(testCase);
            result.setPassed(false);
            result.setActualIntent("PENDING");
            result.setActualResponse("待执行");
            result.setActualResponseMs(0);
            result.setMatchedSensitiveWords(Collections.emptyList());

            results.add(result);
        }

        log.info("测试套件执行准备完成，已创建 {} 个结果占位", results.size());
        return results;
    }

    /**
     * 生成测试报告
     * <p>
     * 根据测试套件和执行结果，计算意图准确率、平均响应时间、
     * P95响应时间和安全拦截率等关键指标，生成汇总报告。
     * </p>
     *
     * @param suite   测试套件
     * @param results 测试结果列表
     * @return 汇总测试报告
     */
    public TestReport generateReport(TestSuite suite, List<TestResult> results) {
        log.info("开始生成测试报告，套件: {}, 结果数: {}", suite.getName(), results.size());

        // 计算通过率
        long passedCount = results.stream().filter(TestResult::isPassed).count();
        long totalCount = results.size();
        double intentAccuracy = totalCount > 0 ? (double) passedCount / totalCount * 100.0 : 0.0;

        // 计算平均响应时间
        double avgResponseMs = results.stream()
                .mapToLong(TestResult::getActualResponseMs)
                .average()
                .orElse(0.0);

        // 计算P95响应时间
        List<Long> sortedResponseMs = results.stream()
                .map(TestResult::getActualResponseMs)
                .sorted()
                .toList();

        double p95ResponseMs = 0.0;
        if (!sortedResponseMs.isEmpty()) {
            int p95Index = (int) Math.ceil(0.95 * sortedResponseMs.size()) - 1;
            p95ResponseMs = sortedResponseMs.get(Math.max(0, p95Index));
        }

        // 计算安全拦截率
        long blockedCount = results.stream()
                .filter(r -> r.getMatchedSensitiveWords() != null && !r.getMatchedSensitiveWords().isEmpty())
                .count();
        double safetyBlockRate = totalCount > 0 ? (double) blockedCount / totalCount * 100.0 : 0.0;

        TestReport report = new TestReport();
        report.setSuite(suite);
        report.setResults(results);
        report.setIntentAccuracy(intentAccuracy);
        report.setAvgResponseMs(avgResponseMs);
        report.setP95ResponseMs(p95ResponseMs);
        report.setSafetyBlockRate(safetyBlockRate);
        report.setExecutedAt(LocalDateTime.now());

        log.info("测试报告生成完成 | 用例总数={} | 通过数={} | 意图准确率={:.1f}% | 平均响应={:.0f}ms | P95响应={:.0f}ms | 安全拦截率={:.1f}%",
                totalCount, passedCount, intentAccuracy, avgResponseMs, p95ResponseMs, safetyBlockRate);

        return report;
    }

    /**
     * 测试套件
     * <p>
     * 包含一组测试用例及其执行配置。
     * </p>
     */
    public static class TestSuite {

        /** 套件名称 */
        private String name;

        /** 测试用例列表 */
        private List<TestCase> cases;

        /** 并发执行数 */
        private int concurrency;

        /** 最大执行时长（毫秒） */
        private long maxDuration;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<TestCase> getCases() {
            return cases;
        }

        public void setCases(List<TestCase> cases) {
            this.cases = cases;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(long maxDuration) {
            this.maxDuration = maxDuration;
        }
    }

    /**
     * 测试用例
     * <p>
     * 单个测试用例的定义，包含输入、期望意图、期望关键词和期望最大响应时间。
     * </p>
     */
    public static class TestCase {

        /** 用例唯一标识 */
        private String id;

        /** 用例类别（如：咨询类、投诉类、闲聊类、敏感内容类） */
        private String category;

        /** 用户输入内容 */
        private String input;

        /** 期望识别到的意图 */
        private String expectedIntent;

        /** 期望回复中包含的关键词（逗号分隔） */
        private String expectedKeywords;

        /** 期望最大响应时间（毫秒） */
        private int expectedMaxResponseMs;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getExpectedIntent() {
            return expectedIntent;
        }

        public void setExpectedIntent(String expectedIntent) {
            this.expectedIntent = expectedIntent;
        }

        public String getExpectedKeywords() {
            return expectedKeywords;
        }

        public void setExpectedKeywords(String expectedKeywords) {
            this.expectedKeywords = expectedKeywords;
        }

        public int getExpectedMaxResponseMs() {
            return expectedMaxResponseMs;
        }

        public void setExpectedMaxResponseMs(int expectedMaxResponseMs) {
            this.expectedMaxResponseMs = expectedMaxResponseMs;
        }
    }

    /**
     * 测试结果
     * <p>
     * 单个测试用例的执行结果，包含是否通过、实际意图、实际响应、响应耗时和匹配的敏感词。
     * </p>
     */
    public static class TestResult {

        /** 对应的测试用例 */
        private TestCase testCase;

        /** 是否通过测试 */
        private boolean passed;

        /** 实际识别到的意图 */
        private String actualIntent;

        /** 实际的AI回复内容 */
        private String actualResponse;

        /** 实际响应耗时（毫秒） */
        private long actualResponseMs;

        /** 匹配到的敏感词列表 */
        private List<String> matchedSensitiveWords;

        public TestCase getTestCase() {
            return testCase;
        }

        public void setTestCase(TestCase testCase) {
            this.testCase = testCase;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getActualIntent() {
            return actualIntent;
        }

        public void setActualIntent(String actualIntent) {
            this.actualIntent = actualIntent;
        }

        public String getActualResponse() {
            return actualResponse;
        }

        public void setActualResponse(String actualResponse) {
            this.actualResponse = actualResponse;
        }

        public long getActualResponseMs() {
            return actualResponseMs;
        }

        public void setActualResponseMs(long actualResponseMs) {
            this.actualResponseMs = actualResponseMs;
        }

        public List<String> getMatchedSensitiveWords() {
            return matchedSensitiveWords;
        }

        public void setMatchedSensitiveWords(List<String> matchedSensitiveWords) {
            this.matchedSensitiveWords = matchedSensitiveWords;
        }
    }

    /**
     * 测试报告
     * <p>
     * 测试套件执行完毕后的汇总报告，包含关键指标统计数据。
     * </p>
     */
    public static class TestReport {

        /** 执行的测试套件 */
        private TestSuite suite;

        /** 所有测试结果列表 */
        private List<TestResult> results;

        /** 意图准确率（百分比） */
        private double intentAccuracy;

        /** 平均响应时间（毫秒） */
        private double avgResponseMs;

        /** P95响应时间（毫秒） */
        private double p95ResponseMs;

        /** 安全拦截率（百分比） */
        private double safetyBlockRate;

        /** 报告执行时间 */
        private LocalDateTime executedAt;

        public TestSuite getSuite() {
            return suite;
        }

        public void setSuite(TestSuite suite) {
            this.suite = suite;
        }

        public List<TestResult> getResults() {
            return results;
        }

        public void setResults(List<TestResult> results) {
            this.results = results;
        }

        public double getIntentAccuracy() {
            return intentAccuracy;
        }

        public void setIntentAccuracy(double intentAccuracy) {
            this.intentAccuracy = intentAccuracy;
        }

        public double getAvgResponseMs() {
            return avgResponseMs;
        }

        public void setAvgResponseMs(double avgResponseMs) {
            this.avgResponseMs = avgResponseMs;
        }

        public double getP95ResponseMs() {
            return p95ResponseMs;
        }

        public void setP95ResponseMs(double p95ResponseMs) {
            this.p95ResponseMs = p95ResponseMs;
        }

        public double getSafetyBlockRate() {
            return safetyBlockRate;
        }

        public void setSafetyBlockRate(double safetyBlockRate) {
            this.safetyBlockRate = safetyBlockRate;
        }

        public LocalDateTime getExecutedAt() {
            return executedAt;
        }

        public void setExecutedAt(LocalDateTime executedAt) {
            this.executedAt = executedAt;
        }
    }
}
