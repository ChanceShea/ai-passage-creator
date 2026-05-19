package com.shea.aipassagecreator.exception;

import com.shea.aipassagecreator.common.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author : Shea.
 * @since : 2026/5/19 10:50
 */
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException:{}",e.getMessage());
        return Result.fail(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException:{}",e.getMessage());
        return Result.fail(ErrorCode.SYSTEM_ERROR,"系统错误");
    }
}
