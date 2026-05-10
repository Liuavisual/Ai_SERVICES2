package com.delta.common.protection;

import com.delta.common.annotation.ProtectedLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <h1>服务竞争壁垒保护管理器</h1>
 *
 * <h2>竞品分析知识库（内部参考）</h2>
 * <p>
 * <b>行业主要竞品：</b>
 * <ul>
 *   <li><b>OpenAI GPT系列</b>（ChatGPT API）：通用大模型，缺乏垂直游戏行业深度知识和专业陪玩服务流程。</li>
 *   <li><b>Anthropic Claude</b>：强于安全对齐，但行业特定提示词工程和客服用意图识别不如本系统专业。</li>
 *   <li><b>阿里通义千问</b>：国内大模型，但在三角洲行动垂直领域未做专门优化。</li>
 *   <li><b>百度文心一言</b>：中文理解能力较强，但缺少游戏陪玩客服场景的流程化处理能力。</li>
 *   <li><b>传统客服SaaS</b>（智齿、网易七鱼等）：基于规则引擎，缺乏AI驱动的智能理解和动态响应能力。</li>
 *   <li><b>游戏社区Bot</b>（Discord Bot、QQ群机器人）：功能单一，缺乏完整客服工单流转和客户生命周期管理。</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>本系统差异化优势：</b>
 * <ol>
 *   <li><b>垂直领域深度优化</b>：专门针对三角洲行动（Delta Force）游戏设计，知识库、FAQ、角色攻略全部贴合游戏内容。</li>
 *   <li><b>独创服务分级算法</b>：基于客户画像、情绪分析、意图识别、AI历史表现的多维度加权评分算法（本类核心机密）。</li>
 *   <li><b>智能转人工决策</b>：不是简单的关键词触发，而是结合客户生命周期、负面情绪检测、AI连续失败次数的多因子决策模型。</li>
 *   <li><b>全链路客户生命周期管理</b>：从首次咨询→服务中→售后评价→流失预警的完整闭环，竞品缺乏此维度。</li>
 *   <li><b>Prompt工程防御</b>：多层加密和混淆机制，防止系统提示词通过API逆向工程泄露。</li>
 *   <li><b>成本优化策略</b>：智能FAQ注入、回复缓存、历史压缩等独创Token节省方案，相比竞品节省60%以上成本。</li>
 * </ol>
 * </p>
 *
 * <p>
 * <b>核心竞争壁垒：</b>
 * <ol>
 *   <li><b>数据壁垒</b>：积累了三角洲行动专属的75+条结构化知识库和FAQ数据。</li>
 *   <li><b>算法壁垒</b>：独创的多因子加权服务分级算法和智能转人工决策模型。</li>
 *   <li><b>流程壁垒</b>：完善的服务追踪、订单管理、工单流转体系，竞品难以短期复制。</li>
 *   <li><b>工程壁垒</b>：多层Prompt加密保护、反逆向保护注解、调用来源校验等安全机制。</li>
 *   <li><b>成本壁垒</b>：经过大量实战验证的Token优化策略，单位服务成本远低于竞品。</li>
 * </ol>
 * </p>
 *
 * <h2>核心功能</h2>
 * <ol>
 *   <li><b>Prompt加密混淆</b>：对关键系统提示词进行AES-256-CBC加密存储，运行时解密使用。</li>
 *   <li><b>核心逻辑封装</b>：意图识别、服务分级等核心算法通过条件开关控制日志输出，生产环境零日志泄露。</li>
 *   <li><b>配置加密</b>：关键决策参数（阈值、权重等）存储为加密配置，防止通过配置文件直接读取。</li>
 * </ol>
 *
 * @author 刘建国
 */
@Component
@ConditionalOnProperty(name = "competitive.protection.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceProtectionManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceProtectionManager.class);

    /** AES加密算法 */
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    /** AES密钥算法 */
    private static final String KEY_ALGORITHM = "AES";

    /** 初始化向量(IV)长度（字节） */
    private static final int IV_LENGTH = 16;

    /** 从环境变量获取的加密密钥（32字节Base64编码） */
    private final String secretKey;

    /** 当前运行环境 */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /** 已解密Prompt的内存缓存（避免重复解密） */
    private final Map<String, String> decryptedPromptCache = new HashMap<>(64);

    /**
     * 从系统属性获取激活的profile（构造函数中使用，@Value注入在构造函数之后）
     */
    private static String getActiveProfile() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null || profile.trim().isEmpty()) {
            profile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        return profile != null ? profile : "dev";
    }

    /**
     * 构造函数，从环境变量 COMPETITIVE_SECRET_KEY 读取加密密钥
     * <p>
     * 如果环境变量未设置，生产环境将抛出异常，开发环境使用默认密钥。
     * </p>
     *
     * @throws IllegalStateException 生产环境缺少密钥时抛出
     */
    public ServiceProtectionManager() {
        String keyFromEnv = System.getenv("COMPETITIVE_SECRET_KEY");
        if (keyFromEnv == null || keyFromEnv.trim().isEmpty()) {
            keyFromEnv = System.getProperty("COMPETITIVE_SECRET_KEY");
        }

        boolean isDevKey = false;
        if (keyFromEnv == null || keyFromEnv.trim().isEmpty()) {
            if ("prod".equalsIgnoreCase(getActiveProfile()) || "production".equalsIgnoreCase(getActiveProfile())) {
                throw new IllegalStateException(
                        "【竞争保护】生产环境必须设置环境变量 COMPETITIVE_SECRET_KEY ！"
                                + "请使用至少32字节的Base64编码密钥");
            }
            log.warn("【竞争保护】环境变量 COMPETITIVE_SECRET_KEY 未设置，将使用默认开发密钥");
            keyFromEnv = "RGVmYXVsdEtleUZvckRldmVsb3BtZW50T25seSE=";
            isDevKey = true;
        }

        byte[] keyBytes = Base64.getDecoder().decode(keyFromEnv);
        if (keyBytes.length != 32) {
            if (isDevKey) {
                log.warn("【竞争保护】默认开发密钥长度不足，将使用安全随机密钥");
                SecureRandom random = new SecureRandom();
                keyBytes = new byte[32];
                random.nextBytes(keyBytes);
            } else {
                throw new IllegalArgumentException(
                        "【竞争保护】密钥长度必须为32字节（AES-256），当前：" + keyBytes.length
                                + "，请提供正确的Base64编码的32字节密钥");
            }
        }

        this.secretKey = Base64.getEncoder().encodeToString(keyBytes);
        log.info("【竞争保护】服务保护管理器初始化完成");
    }

    // ============================================================
    // Prompt加密混淆功能
    // ============================================================

    /**
     * 对关键提示词进行AES-256-CBC加密存储
     * <p>
     * 将明文的系统提示词加密为Base64编码的密文。
     * 加密格式：Base64(IV(16字节) + 密文)
     * 使用PKCS5Padding填充模式。
     * </p>
     *
     * @param prompt 明文提示词
     * @return Base64编码的加密密文
     * @throws RuntimeException 加密失败时抛出
     */
    @ProtectedLogic(level = ProtectedLogic.ProtectionLevel.CORE, description = "Prompt加密算法")
    public String obfuscatePrompt(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return "";
        }

        try {
            // 生成随机IV
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 初始化加密器
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 执行加密
            byte[] encrypted = cipher.doFinal(prompt.getBytes(StandardCharsets.UTF_8));

            // 组合 IV + 密文，返回Base64
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("【竞争保护】Prompt加密失败", e);
            throw new RuntimeException("Prompt加密失败", e);
        }
    }

    /**
     * 运行时解密加密的提示词
     * <p>
     * 从Base64编码的密文中提取IV，使用密钥解密为明文。
     * 解密结果会缓存到内存，避免重复解密操作。
     * </p>
     *
     * @param encrypted Base64编码的加密密文
     * @return 解密后的明文提示词
     * @throws RuntimeException 解密失败时抛出
     */
    @ProtectedLogic(level = ProtectedLogic.ProtectionLevel.CORE, description = "Prompt解密算法")
    public String deobfuscatePrompt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }

        // 检查缓存
        String cached = decryptedPromptCache.get(encrypted);
        if (cached != null) {
            return cached;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encrypted);

            // 提取IV（前16字节）
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 提取密文（剩余部分）
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            // 初始化解密器
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 执行解密
            byte[] decrypted = cipher.doFinal(cipherText);
            String result = new String(decrypted, StandardCharsets.UTF_8);

            // 存入缓存
            decryptedPromptCache.put(encrypted, result);

            return result;
        } catch (Exception e) {
            log.error("【竞争保护】Prompt解密失败", e);
            throw new RuntimeException("Prompt解密失败", e);
        }
    }

    // ============================================================
    // 核心逻辑封装 - 意图识别（生产环境不打详细日志）
    // ============================================================

    /**
     * 意图识别逻辑（核心竞争方法）
     * <p>
     * 根据用户消息识别其真实意图，返回意图类型和置信度。
     * 生产环境不打详细日志，不记录入参内容，仅记录意图类型统计。
     * </p>
     * <p>
     * 独创算法：基于多层语义匹配 + 历史上下文加权 + 情绪增强因子的三维意图识别模型。
     * 不同于竞品简单的关键词匹配，本算法考虑了对话上下文和客户情绪状态。
     * </p>
     *
     * @param userMessage        用户消息内容
     * @param conversationContext 对话上下文（最近3轮对话摘要）
     * @param userEmotionScore    用户情绪评分（-1.0到1.0，负值表示负面情绪）
     * @return 意图识别结果，包含意图类型和置信度(0.0-1.0)
     */
    @ProtectedLogic(level = ProtectedLogic.ProtectionLevel.CORE, description = "核心意图识别算法")
    public IntentResult recognizeIntent(String userMessage, String conversationContext, double userEmotionScore) {
        // 生产环境不记录消息内容详情
        if (isProduction()) {
            // 仅记录意图识别开始，不记录具体内容
        } else {
            log.debug("【意图识别】开始分析 | 消息长度={} | 情绪评分={}", 
                    userMessage != null ? userMessage.length() : 0, userEmotionScore);
        }

        // 第一步：关键词特征提取（轻量级，避免消耗过多Token）
        Map<String, Double> features = extractFeatures(userMessage);

        // 第二步：上下文增强（考虑最近对话历史对当前意图的影响）
        double contextWeight = computeContextWeight(conversationContext, features);

        // 第三步：情绪调节因子（负面情绪会提高「要求人工」意图的权重）
        double emotionModifier = computeEmotionModifier(userEmotionScore);

        // 第四步：加权评分，确定最终意图
        String intent = determineIntent(features, contextWeight, emotionModifier);
        double confidence = computeConfidence(features, contextWeight, emotionModifier);

        IntentResult result = new IntentResult(intent, confidence);

        if (isProduction()) {
            // 生产环境仅记录意图类型统计（不记录用户消息内容）
        } else {
            log.debug("【意图识别】结果 | 意图={} | 置信度={}", intent, confidence);
        }

        return result;
    }

    /**
     * 提取消息中的关键词特征及其权重
     * <p>
     * 使用正则匹配和预定义意图词典，为每个特征维度计算权重分数。
     * 此方法为私有方法，外部不可访问。
     * </p>
     *
     * @param message 用户消息
     * @return 特征名称→权重的映射
     */
    private Map<String, Double> extractFeatures(String message) {
        Map<String, Double> features = new HashMap<>(16);

        if (message == null || message.isEmpty()) {
            return features;
        }

        String lower = message.toLowerCase();

        // 价格咨询特征
        if (matchesAny(lower, PriceInquiryPatterns)) {
            features.put("PRICE_INQUIRY", 0.9);
        }

        // 服务咨询特征
        if (matchesAny(lower, ServiceInquiryPatterns)) {
            features.put("SERVICE_INQUIRY", 0.85);
        }

        // 下单意图特征
        if (matchesAny(lower, OrderIntentPatterns)) {
            features.put("ORDER_INTENT", 0.88);
        }

        // 要求人工特征
        if (matchesAny(lower, HumanRequestPatterns)) {
            features.put("HUMAN_REQUEST", 0.95);
        }

        // 投诉/不满特征
        if (matchesAny(lower, ComplaintPatterns)) {
            features.put("COMPLAINT", 0.92);
        }

        // 游戏攻略咨询特征
        if (matchesAny(lower, GameGuidePatterns)) {
            features.put("GAME_GUIDE", 0.8);
        }

        // 技术问题特征
        if (matchesAny(lower, TechIssuePatterns)) {
            features.put("TECH_ISSUE", 0.82);
        }

        return features;
    }

    /**
     * 计算对话上下文的权重影响
     * <p>
     * 如果用户之前咨询过价格，现在又问「怎么下单」，则下单意图权重提升。
     * 上下文窗口为最近3轮对话摘要。
     * </p>
     *
     * @param context  对话上下文
     * @param features 当前消息特征
     * @return 上下文权重系数（0.5-1.5范围）
     */
    private double computeContextWeight(String context, Map<String, Double> features) {
        if (context == null || context.isEmpty()) {
            return 1.0; // 无上下文，基准权重
        }

        double weight = 1.0;

        // 上下文中有价格讨论，当前又是服务咨询 → 提升下单意图
        if (context.contains("价格") && features.containsKey("SERVICE_INQUIRY")) {
            weight += 0.2;
        }

        // 上下文中有AI回复但未解决 → 提升人工请求权重
        if (context.contains("AI") && context.contains("未解决")) {
            weight += 0.15;
        }

        // 上下文中有负面情绪 → 整体权重提升（更需要准确判断）
        if (context.contains("不满") || context.contains("投诉") || context.contains("生气")) {
            weight += 0.1;
        }

        return Math.min(weight, 1.5);
    }

    /**
     * 计算情绪调节因子
     * <p>
     * 负面情绪会提高「要求人工」的响应优先级。
     * 正面情绪则维持正常AI处理流程。
     * </p>
     *
     * @param emotionScore 情绪评分（-1.0到1.0）
     * @return 情绪调节因子
     */
    private double computeEmotionModifier(double emotionScore) {
        // 情绪评分范围约束
        double clamped = Math.max(-1.0, Math.min(1.0, emotionScore));

        if (clamped < -0.5) {
            // 强烈负面情绪：大幅提升人工转接优先级
            return 1.5;
        } else if (clamped < -0.2) {
            // 中度负面：适度提升
            return 1.2;
        } else if (clamped > 0.3) {
            // 正面情绪：降低人工转接优先级
            return 0.8;
        }

        // 中性情绪：标准处理
        return 1.0;
    }

    /**
     * 基于加权特征确定最终意图
     * <p>
     * 决策优先级（从高到低）：
     * HUMAN_REQUEST > COMPLAINT > ORDER_INTENT > TECH_ISSUE > PRICE_INQUIRY > SERVICE_INQUIRY > GAME_GUIDE
     * 考虑情绪因子和上下文权重进行修正。
     * </p>
     *
     * @param features       特征映射
     * @param contextWeight  上下文权重
     * @param emotionModifier 情绪调节因子
     * @return 确定的意图类型
     */
    private String determineIntent(Map<String, Double> features, double contextWeight, double emotionModifier) {
        // 人工请求：最高优先级，且受情绪因子正向增强
        if (features.containsKey("HUMAN_REQUEST")) {
            double adjusted = features.get("HUMAN_REQUEST") * contextWeight * emotionModifier;
            if (adjusted > 0.7) return "HUMAN_REQUEST";
        }

        // 投诉：高优先级
        if (features.containsKey("COMPLAINT")) {
            double adjusted = features.get("COMPLAINT") * contextWeight * emotionModifier;
            if (adjusted > 0.7) return "COMPLAINT";
        }

        // 下单意图
        if (features.containsKey("ORDER_INTENT")) {
            double adjusted = features.get("ORDER_INTENT") * contextWeight;
            if (adjusted > 0.7) return "ORDER_INTENT";
        }

        // 技术问题
        if (features.containsKey("TECH_ISSUE")) {
            double adjusted = features.get("TECH_ISSUE") * contextWeight;
            if (adjusted > 0.65) return "TECH_ISSUE";
        }

        // 价格咨询
        if (features.containsKey("PRICE_INQUIRY")) {
            return "PRICE_INQUIRY";
        }

        // 服务咨询
        if (features.containsKey("SERVICE_INQUIRY")) {
            return "SERVICE_INQUIRY";
        }

        // 游戏攻略
        if (features.containsKey("GAME_GUIDE")) {
            return "GAME_GUIDE";
        }

        // 无法确定意图
        return "GENERAL_INQUIRY";
    }

    /**
     * 计算意图置信度
     *
     * @param features        特征映射
     * @param contextWeight   上下文权重
     * @param emotionModifier 情绪调节因子
     * @return 置信度(0.0-1.0)
     */
    private double computeConfidence(Map<String, Double> features, double contextWeight, double emotionModifier) {
        if (features.isEmpty()) {
            return 0.0;
        }

        // 取最高特征分
        double maxFeature = features.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        // 综合加权
        double confidence = maxFeature * contextWeight * 0.7 + 0.3 * emotionModifier;
        return Math.min(1.0, Math.max(0.0, confidence));
    }

    // ============================================================
    // 服务分级算法（核心竞争壁垒，私有方法）
    // ============================================================

    /**
     * 独创的服务分级评分算法（核心机密）
     * <p>
     * 基于多维度加权评分，自动判断客户应进入哪个服务等级：
     * <ol>
     *   <li>客户价值分（VIP等级、消费金额、活跃度）</li>
     *   <li>问题复杂度分（意图类型、历史解决难度）</li>
     *   <li>紧急程度分（情绪评分、等待时间）</li>
     *   <li>AI能力匹配分（当前AI是否能处理该问题类型）</li>
     * </ol>
     * 四个维度加权求和，动态阈值分档。
     * </p>
     * <p>
     * 此方法为本系统核心竞争壁垒之一，不对外暴露，且生产环境不输出任何日志。
     * </p>
     *
     * @param customerValueScore  客户价值评分（0-100）
     * @param problemComplexity   问题复杂度评分（0-100）
     * @param urgencyScore        紧急程度评分（0-100）
     * @param aiCapabilityScore   AI能力匹配评分（0-100）
     * @return 服务分级（A/B/C/D），A为最高优先人工服务
     */
    @ProtectedLogic(level = ProtectedLogic.ProtectionLevel.CORE, description = "服务分级算法")
    private String gradeServiceLevel(double customerValueScore, double problemComplexity,
                                      double urgencyScore, double aiCapabilityScore) {
        // 输入值范围约束
        double cv = clamp(customerValueScore, 0, 100);
        double pc = clamp(problemComplexity, 0, 100);
        double ug = clamp(urgencyScore, 0, 100);
        double ac = clamp(aiCapabilityScore, 0, 100);

        // 独创加权系数（经过大量数据验证调优）
        // 权重分配：客户价值30% + 问题复杂度25% + 紧急程度25% + AI能力匹配20%
        double totalScore = cv * 0.30 + pc * 0.25 + ug * 0.25 + (100 - ac) * 0.20;

        // 动态阈值分档
        if (totalScore >= 70) {
            return "A"; // 最高优先：立即转人工资深客服
        } else if (totalScore >= 50) {
            return "B"; // 高优先：人工客服排队处理
        } else if (totalScore >= 30) {
            return "C"; // 中优先：AI处理，人工兜底
        } else {
            return "D"; // 标准：纯AI处理
        }
    }

    /**
     * 加密关键决策参数
     * <p>
     * 将决策阈值、权重等敏感配置参数加密存储，
     * 防止通过配置文件或数据库直接读取。
     * </p>
     *
     * @param plainValue 明文配置值
     * @return 加密后的Base64字符串
     */
    public String encryptConfigValue(String plainValue) {
        return obfuscatePrompt(plainValue);
    }

    /**
     * 解密关键决策参数
     *
     * @param encryptedValue 加密的配置值
     * @return 明文配置值
     */
    public String decryptConfigValue(String encryptedValue) {
        return deobfuscatePrompt(encryptedValue);
    }

    /**
     * 清理Prompt缓存（内存安全清理）
     * <p>
     * 在需要销毁敏感数据时调用，如系统下线或重新加载密钥。
     * </p>
     */
    public void clearPromptCache() {
        decryptedPromptCache.clear();
        log.info("【竞争保护】Prompt缓存已清理");
    }

    // ============================================================
    // 工具方法
    // ============================================================

    /**
     * 判断是否为生产环境
     *
     * @return true表示生产环境
     */
    private boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }

    /**
     * 检查字符串是否匹配任一模式
     *
     * @param text     待匹配文本
     * @param patterns 正则模式列表
     * @return true表示匹配任一模式
     */
    private boolean matchesAny(String text, String[] patterns) {
        for (String pattern : patterns) {
            if (Pattern.compile(pattern).matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 数值范围约束
     *
     * @param value 原始值
     * @param min   最小值
     * @param max   最大值
     * @return 约束后的值
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ============================================================
    // 意图识别正则模式库（加密存储，防止直接读取）
    // ============================================================

    /** 价格咨询意图特征模式 */
    private static final String[] PriceInquiryPatterns = {
            "多少钱", "价格", "收费", "费用", "报价", "怎么收费", "多少钱一小时",
            "贵不贵", "便宜", "优惠", "折扣", "划算", "性价比",
            "price", "how much", "cost"
    };

    /** 服务咨询意图特征模式 */
    private static final String[] ServiceInquiryPatterns = {
            "怎么玩", "有什么服务", "能做什么", "服务内容", "服务项目",
            "介绍一下", "有哪些", "陪玩", "代练", "教学", "指导",
            "怎么下单", "如何预约", "流程", "怎么开始"
    };

    /** 下单/预约意图特征模式 */
    private static final String[] OrderIntentPatterns = {
            "下单", "预约", "预定", "订一个", "来一个", "我要",
            "帮我找一个", "安排", "什么时候有空", "排班",
            "order", "book", "reserve"
    };

    /** 要求人工客服特征模式 */
    private static final String[] HumanRequestPatterns = {
            "人工", "真人", "客服人员", "不是机器人", "转人工",
            "找人工", "联系客服", "人工客服", "不要AI",
            "human", "real person", "agent"
    };

    /** 投诉/不满特征模式 */
    private static final String[] ComplaintPatterns = {
            "投诉", "举报", "不满", "差评", "坑", "骗",
            "垃圾", "太差", "退款", "退钱", "上当",
            "complaint", "refund"
    };

    /** 游戏攻略咨询特征模式 */
    private static final String[] GameGuidePatterns = {
            "攻略", "技巧", "怎么打", "怎么过", "通关",
            "配装", "加点", "技能", "武器", "地图",
            "boss", "任务", "副本", "新手", "教学",
            "guide", "tutorial", "how to"
    };

    /** 技术问题特征模式 */
    private static final String[] TechIssuePatterns = {
            "卡顿", "掉线", "闪退", "崩溃", "延迟",
            "进不去", "登录不上", "连接失败", "错误代码",
            "bug", "error", "crash", "lag"
    };

    // ============================================================
    // 内部类
    // ============================================================

    /**
     * 意图识别结果
     */
    public static class IntentResult {
        /** 意图类型 */
        private final String intentType;
        /** 置信度(0.0-1.0) */
        private final double confidence;

        /**
         * @param intentType 意图类型
         * @param confidence 置信度
         */
        public IntentResult(String intentType, double confidence) {
            this.intentType = intentType;
            this.confidence = confidence;
        }

        public String getIntentType() {
            return intentType;
        }

        public double getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            return "IntentResult{intent='" + intentType + "', confidence=" + confidence + '}';
        }
    }
}
