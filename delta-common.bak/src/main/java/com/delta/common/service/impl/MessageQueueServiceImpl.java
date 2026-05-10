package com.delta.common.service.impl;

import com.delta.common.service.MessageQueueService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamCreateGroupArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.api.stream.StreamTrimArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis Stream消息队列服务实现
 * 基于Redisson客户端实现轻量级消息队列
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedissonClient.class)
public class MessageQueueServiceImpl implements MessageQueueService {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueServiceImpl.class);

    /** Redisson客户端 */
    private final RedissonClient redissonClient;

    @Override
    public String send(String streamKey, Map<String, String> message) {
        RStream<String, String> stream = redissonClient.getStream(streamKey);
        StreamMessageId messageId = stream.add(StreamAddArgs.entries(message));
        log.debug("【消息队列】发送消息到流: {}, ID: {}", streamKey, messageId);
        return messageId.toString();
    }

    @Override
    public void sendDelayed(String streamKey, Map<String, String> message, long delay, TimeUnit timeUnit) {
        String delayedKey = "delayed:" + streamKey + ":" + System.nanoTime();
        redissonClient.getMapCache(delayedKey).putAll(message, delay, timeUnit);
        log.debug("【消息队列】发送延迟消息: {}, 延迟: {}{}", streamKey, delay, timeUnit);
    }

    @Override
    public Map<String, Map<String, String>> consume(String streamKey, String consumerGroup, String consumerName, int batchSize) {
        RStream<String, String> stream = redissonClient.getStream(streamKey);
        try {
            stream.createGroup(StreamCreateGroupArgs.name(consumerGroup).makeStream());
        } catch (Exception ignored) {
        }

        Map<StreamMessageId, Map<String, String>> readResult = stream.readGroup(
                consumerGroup,
                consumerName,
                StreamReadGroupArgs.greaterThan(StreamMessageId.NEVER_DELIVERED).count(batchSize)
        );

        if (readResult == null || readResult.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        readResult.forEach((id, data) -> result.put(id.toString(), new HashMap<>(data)));
        log.debug("【消息队列】消费消息: {}, 数量: {}", streamKey, result.size());
        return result;
    }

    @Override
    public void acknowledge(String streamKey, String consumerGroup, String... messageIds) {
        RStream<String, String> stream = redissonClient.getStream(streamKey);
        StreamMessageId[] ids = new StreamMessageId[messageIds.length];
        for (int i = 0; i < messageIds.length; i++) {
            ids[i] = parseStreamMessageId(messageIds[i]);
        }
        stream.ack(consumerGroup, ids);
        log.debug("【消息队列】确认消息: {}, 数量: {}", streamKey, messageIds.length);
    }

    @Override
    public long size(String streamKey) {
        RStream<String, String> stream = redissonClient.getStream(streamKey);
        return stream.size();
    }

    @Override
    public void trim(String streamKey, String minId) {
        RStream<String, String> stream = redissonClient.getStream(streamKey);
        stream.trim(StreamTrimArgs.minId(parseStreamMessageId(minId)).noLimit());
        log.debug("【消息队列】清理流: {}, minId: {}", streamKey, minId);
    }

    /**
     * 将字符串ID解析为StreamMessageId对象
     * Redis Stream ID格式为 "id0-id1"，如 "1746412345678-0"
     *
     * @param id 格式为 "id0-id1" 的字符串ID
     * @return StreamMessageId对象
     */
    private StreamMessageId parseStreamMessageId(String id) {
        String[] parts = id.split("-");
        long id0 = Long.parseLong(parts[0]);
        long id1 = Long.parseLong(parts[1]);
        return new StreamMessageId(id0, id1);
    }
}