package com.counseling.system.exception;

import com.counseling.system.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ApiResponse.error(400, "操作失败：数据重复或违反约束条件");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("java.time.LocalDate")) {
             return ApiResponse.error(400, "日期格式错误，请输入 yyyy-MM-dd 格式");
        }
        return ApiResponse.error(400, "请求格式不正确");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.error(400, "验证错误：" + errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<String> handleAccessDeniedException(AccessDeniedException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "无权限访问";
        return ApiResponse.error(403, message);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleAuthenticationException(AuthenticationException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "未认证";
        return ApiResponse.error(401, message);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("not found") || message.contains("exists"))) {
            log.warn("Business exception: {}", message);
            return ApiResponse.error(400, message);
        }
        log.error("Unhandled runtime exception", ex);
        return ApiResponse.error(500, message != null ? message : "系统运行时错误");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
