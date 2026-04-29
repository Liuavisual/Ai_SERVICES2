package com.delta.common.service.impl;

import com.delta.common.exception.BusinessException;
import com.delta.common.util.IdObfuscateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdObfuscateUtils 安全性测试
 * 验证ID混淆工具的编码/解码安全性、一致性和边界条件
 *
 * @author 刘建国
 */
class IdObfuscateSecurityTest {

    /**
     * 测试编码后的ID不应暴露原始ID
     * 编码结果不应包含原始数字字符串
     */
    @Test
    void encode_shouldNotExposeOriginalId() {
        // 准备测试数据
        Long id = 12345L;

        // 执行编码
        String encoded = IdObfuscateUtils.encode(id);

        // 验证编码后不包含原始ID
        assertFalse(encoded.contains(id.toString()), "编码后的ID不应包含原始ID");
        assertFalse(encoded.matches("\\d+"), "编码后的ID不应是纯数字");
    }

    /**
     * 测试编码后的ID应有d_前缀
     * 验证混淆ID的格式规范
     */
    @Test
    void encode_shouldHavePrefix() {
        // 执行编码
        String encoded = IdObfuscateUtils.encode(1L);

        // 验证前缀
        assertTrue(encoded.startsWith("d_"), "编码后的ID应以d_开头");
    }

    /**
     * 测试编码-解码往返一致性
     * 编码后再解码应返回原始ID
     */
    @Test
    void encodeDecode_roundTrip_shouldReturnOriginal() {
        // 准备测试数据
        Long originalId = 99999L;

        // 执行编码-解码
        String encoded = IdObfuscateUtils.encode(originalId);
        Long decoded = IdObfuscateUtils.decode(encoded);

        // 验证一致性
        assertEquals(originalId, decoded, "编码-解码往返应返回原始ID");
    }

    /**
     * 测试解码无效输入应返回null
     * 包括null、空字符串和非法格式
     */
    @Test
    void decode_invalidInput_shouldReturnNull() {
        // 验证各种无效输入
        assertNull(IdObfuscateUtils.decode("invalid"), "非法字符串应返回null");
        assertNull(IdObfuscateUtils.decode(""), "空字符串应返回null");
        assertNull(IdObfuscateUtils.decode(null), "null应返回null");
    }

    /**
     * 测试篡改后的编码不应解码为原始ID
     * 验证编码的防篡改能力
     */
    @Test
    void decode_tamperedEncoded_shouldNotReturnValidId() {
        // 准备测试数据
        Long id = 12345L;
        String encoded = IdObfuscateUtils.encode(id);

        // 篡改编码
        String tampered = encoded + "x";
        Long decoded = IdObfuscateUtils.decode(tampered);

        // 验证篡改后不能解码为原始ID
        assertNotEquals(id, decoded, "篡改后的编码不应解码为原始ID");
    }

    /**
     * 测试同一ID的编码应一致
     * 重复编码100次验证结果一致
     */
    @RepeatedTest(100)
    void encode_shouldBeConsistentAcrossMultipleCalls() {
        // 准备测试数据
        Long id = 42L;

        // 多次编码
        String first = IdObfuscateUtils.encode(id);
        String second = IdObfuscateUtils.encode(id);

        // 验证一致性
        assertEquals(first, second, "同一ID的编码应一致");
    }

    /**
     * 测试不同ID应产生不同编码
     * 验证编码的唯一性
     */
    @Test
    void encode_differentIds_shouldProduceDifferentEncoded() {
        // 编码不同ID
        String encoded1 = IdObfuscateUtils.encode(1L);
        String encoded2 = IdObfuscateUtils.encode(2L);

        // 验证不同
        assertNotEquals(encoded1, encoded2, "不同ID应产生不同编码");
    }

    /**
     * 测试纯数字输入应作为fallback直接解析
     * 兼容前端直接传数字ID的场景
     */
    @Test
    void decode_plainNumber_shouldFallback() {
        // 解析纯数字
        Long result = IdObfuscateUtils.decode("12345");

        // 验证fallback解析
        assertEquals(12345L, result, "纯数字应作为fallback直接解析");
    }

    /**
     * 测试大ID值（Long.MAX_VALUE）的编码解码
     * 验证边界条件处理
     */
    @Test
    void encode_largeId_shouldWork() {
        // 准备测试数据
        Long largeId = Long.MAX_VALUE;

        // 执行编码-解码
        String encoded = IdObfuscateUtils.encode(largeId);
        Long decoded = IdObfuscateUtils.decode(encoded);

        // 验证一致性
        assertEquals(largeId, decoded, "Long.MAX_VALUE的编码解码应一致");
    }

    /**
     * 测试零值ID的编码解码
     * 验证边界条件处理
     */
    @Test
    void encode_zeroId_shouldWork() {
        // 执行编码-解码
        String encoded = IdObfuscateUtils.encode(0L);
        Long decoded = IdObfuscateUtils.decode(encoded);

        // 验证一致性
        assertEquals(0L, decoded, "0值ID的编码解码应一致");
    }

    /**
     * 测试decodeRequired对无效输入应抛出BusinessException
     * 验证强制解码的异常处理
     */
    @Test
    void decodeRequired_invalidInput_shouldThrow() {
        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> IdObfuscateUtils.decodeRequired("invalid"),
                "无效输入强制解码应抛出BusinessException");
    }

    /**
     * 测试null值的编码应返回null
     */
    @Test
    void encode_null_shouldReturnNull() {
        // 执行编码
        String result = IdObfuscateUtils.encode(null);

        // 验证结果
        assertNull(result, "null编码应返回null");
    }

    /**
     * 测试负数ID的编码解码
     * 验证边界条件处理
     */
    @Test
    void encode_negativeId_shouldWork() {
        // 准备测试数据
        Long negativeId = -1L;

        // 执行编码-解码
        String encoded = IdObfuscateUtils.encode(negativeId);
        Long decoded = IdObfuscateUtils.decode(encoded);

        // 验证一致性
        assertEquals(negativeId, decoded, "负数ID的编码解码应一致");
    }
}
