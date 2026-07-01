package com.yanruieng.app.common;

/**
 * 基于ThreadLocal封装工具类，用于保存和获取当前登录用户id
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static Long getCurrentUserId() {
        return threadLocal.get();
    }

    public static void setCurrentUserId(Long id) {
        threadLocal.set(id);
    }

    public static void clear() {
        threadLocal.remove();
    }
}
