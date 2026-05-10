package com.delta.common.exception;

import lombok.Getter;

/**
 * 业务异常类，用于抛出可预期的业务逻辑错误
 *
 * @author 刘建国
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
