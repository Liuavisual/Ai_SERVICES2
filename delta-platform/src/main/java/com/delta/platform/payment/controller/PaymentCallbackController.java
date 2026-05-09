package com.delta.platform.payment.controller;

import com.delta.platform.payment.dto.PaymentCallbackDTO;
import com.delta.common.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付回调控制器，处理微信支付和支付宝的异步通知回调
 * <p>
 * 提供 REST API 接口接收第三方支付平台的支付结果通知，
 * 验证签名后进行订单状态更新。当前版本提供回调骨架，
 * 需接入具体的 SDK（微信支付SDK 或 支付宝SDK）补充签名验证逻辑。
 * </p>
 * <p>
 * 通过配置项 payment.callback.enabled 控制启用/禁用。
 * </p>
 *
 * @author 刘建国
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "支付回调", description = "接收第三方支付平台异步通知回调")
@ConditionalOnProperty(prefix = "payment.callback", name = "enabled", havingValue = "true", matchIfMissing = false)
public class PaymentCallbackController {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackController.class);

    /** 订单服务，用于更新订单支付状态 */
    private final OrderService orderService;

    /**
     * 微信支付异步回调接口
     * <p>
     * 微信支付平台在用户支付成功后，向此接口发送异步通知。
     * 需验证签名、解析通知数据后进行状态更新。
     * </p>
     *
     * @param requestBody 微信支付通知报文（JSON格式）
     * @return 处理结果（必须返回特定格式的JSON通知微信处理状态）
     */
    @PostMapping("/wechat/notify")
    @Operation(summary = "微信支付异步通知回调")
    public Map<String, String> wechatPaymentNotify(@RequestBody String requestBody) {
        log.info("【支付回调】收到微信支付通知");

        try {
            PaymentCallbackDTO callback = parseWeChatNotify(requestBody);
            if (callback == null) {
                return Map.of("code", "FAIL", "message", "解析通知失败");
            }

            if (!verifyWeChatSignature(callback)) {
                log.warn("【支付回调】微信支付签名验证失败 | orderNo={}", callback.getOutTradeNo());
                return Map.of("code", "FAIL", "message", "签名验证失败");
            }

            handlePaymentSuccess(callback);
            return Map.of("code", "SUCCESS", "message", "OK");

        } catch (Exception e) {
            log.error("【支付回调】微信支付处理异常", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }

    /**
     * 支付宝异步回调接口
     * <p>
     * 支付宝在用户支付成功后，向此接口发送异步通知。
     * </p>
     *
     * @param params 支付宝通知参数（form-urlencoded格式）
     * @return 支付宝期望的响应文本 "success" 或 "fail"
     */
    @PostMapping("/alipay/notify")
    @Operation(summary = "支付宝异步通知回调")
    public String alipayPaymentNotify(@RequestParam Map<String, String> params) {
        log.info("【支付回调】收到支付宝支付通知");

        try {
            PaymentCallbackDTO callback = parseAlipayNotify(params);
            if (callback == null) {
                return "fail";
            }

            if (!verifyAlipaySignature(params)) {
                log.warn("【支付回调】支付宝签名验证失败 | orderNo={}", callback.getOutTradeNo());
                return "fail";
            }

            handlePaymentSuccess(callback);
            return "success";

        } catch (Exception e) {
            log.error("【支付回调】支付宝处理异常", e);
            return "fail";
        }
    }

    /**
     * 处理支付成功回调，更新订单支付状态
     *
     * @param callback 支付回调数据
     */
    private void handlePaymentSuccess(PaymentCallbackDTO callback) {
        log.info("【支付回调】支付成功 | orderNo={} | transactionId={} | amount={} | payTime={}",
                callback.getOutTradeNo(), callback.getTransactionId(),
                callback.getTotalAmount(), callback.getPayTime());

        orderService.confirmPayment(callback.getOutTradeNo(), callback.getTransactionId(),
                callback.getTotalAmount(), callback.getPayTime());
    }

    /**
     * 解析微信支付通知报文
     * <p>
     * TODO: 接入微信支付SDK后使用官方解密方法替换
     * </p>
     *
     * @param requestBody 通知报文
     * @return 支付回调DTO，解析失败返回null
     */
    private PaymentCallbackDTO parseWeChatNotify(String requestBody) {
        log.debug("【支付回调】微信通知原文: {}", requestBody);
        return null;
    }

    /**
     * 验证微信支付签名
     * <p>
     * TODO: 接入微信支付SDK后使用官方便证方法替换
     * </p>
     *
     * @param callback 回调数据
     * @return 签名是否有效
     */
    private boolean verifyWeChatSignature(PaymentCallbackDTO callback) {
        return false;
    }

    /**
     * 解析支付宝通知参数
     * <p>
     * TODO: 接入支付宝SDK后使用官方解析方法替换
     * </p>
     *
     * @param params 通知参数
     * @return 支付回调DTO，解析失败返回null
     */
    private PaymentCallbackDTO parseAlipayNotify(Map<String, String> params) {
        log.debug("【支付回调】支付宝通知参数: {}", params.keySet());

        PaymentCallbackDTO dto = new PaymentCallbackDTO();
        dto.setOutTradeNo(params.get("out_trade_no"));
        dto.setTransactionId(params.get("trade_no"));

        String totalAmount = params.get("total_amount");
        if (totalAmount != null) {
            try {
                dto.setTotalAmount(new java.math.BigDecimal(totalAmount));
            } catch (NumberFormatException e) {
                log.warn("【支付回调】金额解析失败: {}", totalAmount);
            }
        }

        return dto;
    }

    /**
     * 验证支付宝签名
     * <p>
     * TODO: 接入支付宝SDK后使用官方验签方法替换
     * </p>
     *
     * @param params 通知参数
     * @return 签名是否有效
     */
    private boolean verifyAlipaySignature(Map<String, String> params) {
        return false;
    }
}