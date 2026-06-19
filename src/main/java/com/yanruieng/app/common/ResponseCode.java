package com.yanruieng.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    SUCCESS(0, "success"),
    FAIL(500, "服务器异常"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问");

    private final Integer code;
    private final String message;
}