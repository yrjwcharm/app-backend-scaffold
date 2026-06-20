package com.yanruieng.app.common;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class CustomException extends RuntimeException {
    private final Integer code;

    public CustomException(String message) {
        this(ResponseCode.BAD_REQUEST, message);
    }

    public CustomException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
    }

}
