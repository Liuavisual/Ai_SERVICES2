package com.delta.common.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AiCustomerServiceConstants {

    public static final List<String> ORDER_INTENT_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "我要预约", "我要下单", "我要点单", "帮我预约", "帮我下单",
            "我要订", "帮我订", "我要买", "帮我买",
            "现在可以约吗", "现在能约吗", "想约一个", "想约一下",
            "给我安排", "帮我安排", "直接约", "直接下单",
            "付款", "支付", "下单吧", "预约吧", "订一个",
            "就今天", "就今晚", "今天玩", "今晚玩", "明天玩",
            "今天晚上", "今天上午", "今天下午",
            "几点开始", "几点能", "什么时间", "什么时候",
            "晚上几点", "上午几点", "下午几点",
            "有人接吗", "谁在线", "哪个有空", "有没有人",
            "能不能接", "可以接吗", "在不在", "有空没",
            "我要找", "指定", "找某某", "要某某",
            "包夜", "通宵", "连坐", "连打",
            "退款", "投诉", "不满意", "退钱"
    ));

    public static final List<String> HUMAN_EXPLICIT_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "人工", "转人工", "找人工", "人工客服", "真人客服"
    ));

    public static final List<String> NEGATIVE_EMOTION_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "烦死", "太慢了", "什么破", "垃圾", "骗人", "无语",
            "太差了", "恶心", "受不了", "气死", "什么鬼", "坑人",
            "不行", "太烂了", "糊弄", "敷衍", "不靠谱", "浪费时间",
            "再也不来了", "差评", "举报", "拉黑", "骗子", "黑心"
    ));

    public static final List<String> SERVICE_COMPLETE_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "打完了", "结束了", "完事了", "结束服务", "打完了局",
            "游戏结束", "不玩了", "下线了", "收工了", "今天到这",
            "就到这里", "差不多了", "可以了", "够了", "好了不用了"
    ));

    public static final List<String> PRICE_INQUIRY_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "价格", "多少钱", "收费", "费用", "怎么收费", "收费标准",
            "报价", "价位", "多少钱一小时", "多少钱一单", "多少钱一个",
            "贵不贵", "便宜吗", "价格表", "价目表", "价格怎么样",
            "怎么算钱", "如何收费", "收费多少", "要多少钱", "花费多少"
    ));

    public static final List<String> SERVICE_INQUIRY_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "有什么服务", "都有什么", "服务项目", "能做什么", "提供什么",
            "陪玩项目", "有哪些服务", "服务内容", "玩什么", "能玩什么",
            "游戏类型", "有什么游戏", "能陪什么", "陪玩类型",
            "介绍一下", "介绍下", "说说你们", "你们家有什么", "有什么陪玩",
            "怎么玩", "玩法", "有什么好玩的"
    ));

    public static final List<String> SCHEDULE_INQUIRY_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "什么时候有空", "几点有空", "什么时候能约", "有空吗",
            "现在有人吗", "今天有人吗", "明天有人吗", "晚上有人吗",
            "谁在线", "哪个有空", "有没有人接", "能约吗",
            "排班", "时间表", "可约时间", "什么时候可以"
    ));

    public static final List<String> VIP_MEMBER_LEVELS = Collections.unmodifiableList(Arrays.asList(
            CustomerProfileConstants.MEMBER_LEVEL_SILVER,
            CustomerProfileConstants.MEMBER_LEVEL_GOLD,
            CustomerProfileConstants.MEMBER_LEVEL_PLATINUM,
            CustomerProfileConstants.MEMBER_LEVEL_DIAMOND
    ));

    public static final List<String> REVIEW_REQUEST_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "给好评", "给个好评", "好评", "评价一下", "打个分",
            "评分", "五星好评", "写个评价", "要评价", "想评价"
    ));

    public static final int AI_CONSECUTIVE_FAILURE_THRESHOLD = 3;

    public static final String AI_CONSECUTIVE_KEY_PREFIX = "delta:ai:consecutive:";

    public static final long AI_CONSECUTIVE_TTL_MINUTES = 30;

    public static final List<String> DIRECT_REPLY_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "价格"
    ));

    public static final int CONVERSATION_HISTORY_LIMIT = 6;

    public static final int AI_REPLY_CACHE_TTL_MINUTES = 10;

    public static final String AI_REPLY_CACHE_PREFIX = "delta:ai:reply:";

    public static final int AI_TIMEOUT_MS = 10000;

    public static final String DEFAULT_FALLBACK_REPLY = "你好呀~ 有什么可以帮你的？";
    public static final String WAITING_REPLY = "正在为您安排客服，请稍等片刻~ 🔔";
    public static final String IN_SERVICE_REPLY = "客服正在为您服务中，请直接描述您的需求~";
    public static final String EMOTION_HANDOFF_REPLY = "感受到您的不满了，我马上为您转接人工客服，请稍等~ 🫡";
    public static final String AI_FAILURE_HANDOFF_REPLY = "看来这个问题比较复杂，我为您转接人工客服来处理，请稍等~ 🫡";
    public static final String HUMAN_EXPLICIT_HANDOFF_REPLY = "好的，马上为您转接人工客服，请稍等一下哦~ 🫡";
    public static final String ORDER_INTENT_HANDOFF_REPLY = "收到！预约相关的事宜我来帮您安排人工客服处理，请稍等~ 人工客服会尽快联系您确认时间 🔔";
    public static final String VIP_HANDOFF_REPLY = "尊贵的VIP客户您好~ 为您安排专属人工客服，请稍等~ ✨";

    public static final String PRICE_INQUIRY_CONTEXT_HINT = "[客户咨询价格信息，请根据陪玩等级、服务项目价格表准确回答，语气热情专业]";
    public static final String PRICE_INQUIRY_CONVERSION_SUFFIX = "\n\n💡 是否需要立即预约？回复\"预约\"即可安排人工客服为您确认时间~";
    public static final String SERVICE_INQUIRY_CONTEXT_HINT = "[客户咨询服务项目，请根据服务项目列表、游戏配置详细介绍，突出特色和情绪价值]";
    public static final String SCHEDULE_INQUIRY_CONTEXT_HINT = "[客户咨询可约时间，请根据陪玩师排班信息回答，并提示如需预约需联系人工客服确认]";

    public static final String SATISFACTION_PROMPT = "您的问题已处理完毕~ 如需评价本次服务，请回复1-5分（1=非常不满意，5=非常满意）⭐";

    public static final String WECHAT_ERROR_REPLY = "不好意思呀，我这边出了点小问题 请稍后再试试~";
    public static final String CHAT_TEST_ERROR_REPLY = "不好意思呀，我这边出了点小问题 😅 请稍后再试试~";

    public static final String HANDOFF_REASON_AI_CONSECUTIVE = "AI连续未解决";

    public static final String WEWORK_WELCOME_REPLY = "嗨~ 欢迎添加！我是客服小助手 有啥想问的随时说~";
    public static final String WECHAT_WELCOME_REPLY = "嗨~ 欢迎关注！我是客服小助手 有啥想问的随时说~";
    public static final String WECHAT_WELCOME_FALLBACK_REPLY = "嗨~ 欢迎关注！有啥想问的随时说~";
    public static final String WEWORK_IMAGE_REPLY = "已收到您的图片，如需人工服务请回复\"人工\"";

    private AiCustomerServiceConstants() {
    }
}
