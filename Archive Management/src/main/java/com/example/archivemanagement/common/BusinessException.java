package com.example.archivemanagement.common;

/**
 * 业务异常 — 用于在 Service 层抛出可预期的业务错误
 * Controller 层无需 try-catch，由全局异常处理器统一处理
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 常用工厂方法
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
}
