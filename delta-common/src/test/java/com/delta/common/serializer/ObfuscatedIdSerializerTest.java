package com.delta.common.serializer;

import com.delta.common.util.IdObfuscateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObfuscatedIdSerializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Data
    static class TestVO {
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = ObfuscatedIdSerializer.class)
        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = ObfuscatedIdDeserializer.class)
        private Long id;

        private String name;
    }

    @Test
    void serialize_shouldObfuscateId() throws JsonProcessingException {
        TestVO vo = new TestVO();
        vo.setId(123L);
        vo.setName("test");

        String json = objectMapper.writeValueAsString(vo);

        assertFalse(json.contains("\"id\":123"), "Raw ID should not appear in JSON");
        String expectedEncoded = IdObfuscateUtils.encode(123L);
        assertTrue(json.contains("\"id\":\"" + expectedEncoded + "\""), "Obfuscated ID should appear in JSON");
        assertTrue(json.contains("\"name\":\"test\""), "Other fields should serialize normally");
    }

    @Test
    void serialize_shouldHandleNullId() throws JsonProcessingException {
        TestVO vo = new TestVO();
        vo.setId(null);
        vo.setName("test");

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"id\":null"), "Null ID should serialize as null");
    }

    @Test
    void deserialize_shouldDecodeObfuscatedId() throws JsonProcessingException {
        String encodedId = IdObfuscateUtils.encode(456L);
        String json = "{\"id\":\"" + encodedId + "\",\"name\":\"test\"}";

        TestVO vo = objectMapper.readValue(json, TestVO.class);

        assertEquals(456L, vo.getId(), "Deserialized ID should match original");
        assertEquals("test", vo.getName());
    }

    @Test
    void roundTrip_shouldPreserveId() throws JsonProcessingException {
        TestVO original = new TestVO();
        original.setId(789L);
        original.setName("roundtrip");

        String json = objectMapper.writeValueAsString(original);
        TestVO deserialized = objectMapper.readValue(json, TestVO.class);

        assertEquals(original.getId(), deserialized.getId(), "Round-trip should preserve ID");
        assertEquals(original.getName(), deserialized.getName());
    }

    @Test
    void serialize_idShouldNotBeGuessable() throws JsonProcessingException {
        TestVO vo1 = new TestVO();
        vo1.setId(1L);
        vo1.setName("a");

        TestVO vo2 = new TestVO();
        vo2.setId(2L);
        vo2.setName("b");

        String json1 = objectMapper.writeValueAsString(vo1);
        String json2 = objectMapper.writeValueAsString(vo2);

        assertNotEquals(
                objectMapper.readTree(json1).get("id").asText(),
                objectMapper.readTree(json2).get("id").asText(),
                "Different IDs should produce different obfuscated values"
        );
    }
}
