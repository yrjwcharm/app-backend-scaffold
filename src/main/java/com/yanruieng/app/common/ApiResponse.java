package com.yanruieng.app.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码：0 成功，非 0 失败
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    /**
     * 响应时间
     */
    private final Instant timestamp;

    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> failed(String message) {
        return new ApiResponse<>(ResponseCode.FAIL.getCode(), message, null);
    }

    public static <T> ApiResponse<T> failed(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(ResponseCode.UNAUTHORIZED.getCode(), ResponseCode.UNAUTHORIZED.getMessage(), null);
    }
}