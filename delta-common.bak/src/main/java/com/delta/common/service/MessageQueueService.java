package com.delta.common.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列服务接口
 * 基于Redis Stream实现轻量级消息队列，替代定时任务轮询
 *
 * @author 刘建国
 */
public interface MessageQueueService {

    /**
     * 发送消息到指定流
     *
     * @param streamKey 流Key
     * @param message   消息内容（键值对）
     * @return 消息ID
     */
    String send(String streamKey, Map<String, String> message);

    /**
     * 发送延迟消息（通过Redis过期Key实现简化延迟队列）
     *
     * @param streamKey 流Key
     * @param message   消息内容
     * @param delay     延迟时间
     * @param timeUnit  时间单位
     */
    void sendDelayed(String streamKey, Map<String, String> message, long delay, TimeUnit timeUnit);

    /**
     * 消费消息
     *
     * @param streamKey     流Key
     * @param consumerGroup 消费组名
     * @param consumerName  消费者名
     * @param batchSize     批量消费数量
     * @return 消息列表（消息ID → 消息内容）
     */
    Map<String, Map<String, String>> consume(String streamKey, String consumerGroup, String consumerName, int batchSize);

    /**
     * 确认消息处理完成
     *
     * @param streamKey     流Key
     * @param consumerGroup 消费组名
     * @param messageId     消息ID列表
     */
    void acknowledge(String streamKey, String consumerGroup, String... messageId);

    /**
     * 获取流的消息数量
     *
     * @param streamKey 流Key
     * @return 消息数量
     */
    long size(String streamKey);

    /**
     * 清理流中已确认的消息（定期调用）
     *
     * @param streamKey 流Key
     * @param minId     最小保留的消息ID
     */
    void trim(String streamKey, String minId);
}