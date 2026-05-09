package com.delta.platform.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付回调数据传输对象，统一微信支付和支付宝的回调数据结构
 *
 * @author 刘建国
 */
public class PaymentCallbackDTO {

    /** 商户订单号 */
    private String outTradeNo;

    /** 第三方交易流水号 */
    private String transactionId;

    /** 支付金额（元） */
    private BigDecimal totalAmount;

    /** 支付完成时间 */
    private LocalDateTime payTime;

    /** 支付渠道（WECHAT / ALIPAY） */
    private String channel;

    /** 支付状态（SUCCESS / FAIL / REFUND） */
    private String tradeStatus;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }
}