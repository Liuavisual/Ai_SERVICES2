package com.delta.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdObfuscateUtilsTest {

    @Test
    void encode_shouldReturnObfuscatedString() {
        String encoded = IdObfuscateUtils.encode(1L);
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("d_"));
        assertNotEquals("d_1", encoded);
    }

    @Test
    void decode_shouldReturnOriginalId() {
        Long originalId = 12345L;
        String encoded = IdObfuscateUtils.encode(originalId);
        Long decoded = IdObfuscateUtils.decode(encoded);
        assertEquals(originalId, decoded);
    }

    @Test
    void encodeDecode_shouldBeReversible_forVariousIds() {
        long[] testIds = {1L, 100L, 999999L, Long.MAX_VALUE / 2, 201L, 1301L, 2701L};
        for (long id : testIds) {
            String encoded = IdObfuscateUtils.encode(id);
            Long decoded = IdObfuscateUtils.decode(encoded);
            assertEquals(id, decoded, "Failed for id: " + id);
        }
    }

    @Test
    void encode_shouldProduceDifferentOutputs_forDifferentIds() {
        String encoded1 = IdObfuscateUtils.encode(1L);
        String encoded2 = IdObfuscateUtils.encode(2L);
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void encode_shouldProduceSameOutput_forSameId() {
        String encoded1 = IdObfuscateUtils.encode(42L);
        String encoded2 = IdObfuscateUtils.encode(42L);
        assertEquals(encoded1, encoded2);
    }

    @Test
    void decode_shouldReturnNull_forNullInput() {
        assertNull(IdObfuscateUtils.decode(null));
    }

    @Test
    void decode_shouldReturnNull_forEmptyInput() {
        assertNull(IdObfuscateUtils.decode(""));
    }

    @Test
    void decode_shouldFallbackToLong_forPlainNumber() {
        Long result = IdObfuscateUtils.decode("123");
        assertEquals(123L, result);
    }

    @Test
    void decode_shouldReturnNull_forInvalidString() {
        assertNull(IdObfuscateUtils.decode("d_invalid!!!"));
    }

    @Test
    void decodeRequired_shouldThrow_forInvalidInput() {
        assertThrows(com.delta.common.exception.BusinessException.class,
                () -> IdObfuscateUtils.decodeRequired("d_invalid!!!"));
    }

    @Test
    void encode_shouldHandleNullId() {
        assertNull(IdObfuscateUtils.encode(null));
    }

    @Test
    void encodedId_shouldNotRevealOriginalId() {
        Long id = 1L;
        String encoded = IdObfuscateUtils.encode(id);
        assertFalse(encoded.contains("1"), "Encoded ID should not contain the original ID value");
        assertFalse(encoded.matches("\\d+"), "Encoded ID should not be a plain number");
    }
}
