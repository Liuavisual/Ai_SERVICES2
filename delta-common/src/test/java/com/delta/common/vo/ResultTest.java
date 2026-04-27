package com.delta.common.vo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    @Test
    public void testSuccessWithoutData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testSuccessWithData() {
        String testData = "test data";
        Result<String> result = Result.success(testData);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(testData, result.getData());
    }

    @Test
    public void testSuccessWithMessageAndData() {
        String testMessage = "operation successful";
        String testData = "test data";
        Result<String> result = Result.success(testMessage, testData);
        assertEquals(200, result.getCode());
        assertEquals(testMessage, result.getMessage());
        assertEquals(testData, result.getData());
    }

    @Test
    public void testErrorWithMessage() {
        String errorMessage = "something went wrong";
        Result<Void> result = Result.error(errorMessage);
        assertEquals(500, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testErrorWithCodeAndMessage() {
        Integer errorCode = 400;
        String errorMessage = "bad request";
        Result<Void> result = Result.error(errorCode, errorMessage);
        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testResultAllArgsConstructor() {
        Integer code = 404;
        String message = "not found";
        String data = "test data";
        Result<String> result = new Result<>(code, message, data);
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    public void testResultNoArgsConstructor() {
        Result<String> result = new Result<>();
        assertNull(result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }
}
