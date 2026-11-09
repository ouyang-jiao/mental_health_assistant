package com.swu.aispringboot.common;

import com.swu.aispringboot.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //处理参数异常校验
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.warn("参数异常校验失败：{}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).
                collect(Collectors.joining(","));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), ResultCode.PARAM_ERROR.getMsg(), message);
    }

    //处理业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e){
        log.warn("业务异常：code={},msg={}", e.getCode(), e.getMessage());
        //如果异常携带额外的数据
        if (e.getData() != null) {
            return Result.error(e.getCode(), e.getMessage(), e.getData());
        }
        return Result.error(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleUnknownException(Exception e, HttpServletRequest request){
        if(e instanceof NoResourceFoundException){
            log.warn("请求路径不存在：{}", e.getMessage());
            return Result.error(ResultCode.NOT_FOUND.getCode(), ResultCode.NOT_FOUND.getMsg(), null);
        }
        log.error("未捕获异常, uri={}", request.getRequestURI(), e);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMsg(), null);
    }
}
