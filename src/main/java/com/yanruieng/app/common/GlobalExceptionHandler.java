package com.yanruieng.app.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义业务异常
     */
    @ExceptionHandler(CustomException.class)
    public ApiResponse<Void> handleCustomException(CustomException e) {
        log.warn("业务异常：{}", e.getMessage());
        return ApiResponse.failed(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常：@RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("参数校验失败");

        return ApiResponse.failed(ResponseCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 参数校验异常：表单参数
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("参数校验失败");

        return ApiResponse.failed(ResponseCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 参数校验异常：@RequestParam / @PathVariable
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        return ApiResponse.failed(ResponseCode.BAD_REQUEST.getCode(), "参数校验失败");
    }

    /**
     * 登录失败
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException e) {
        return ApiResponse.failed(ResponseCode.UNAUTHORIZED.getCode(), "账号或密码错误");
    }

    /**
     * 唯一索引冲突，例如手机号、用户名重复
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一索引冲突：{}", e.getMessage());
        return ApiResponse.failed(ResponseCode.BAD_REQUEST.getCode(), "数据已存在");
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return ApiResponse.failed(ResponseCode.FAIL.getCode(), "系统异常，请稍后再试");
    }
}
