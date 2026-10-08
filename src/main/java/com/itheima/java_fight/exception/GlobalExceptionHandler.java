package com.itheima.java_fight.exception;


import com.itheima.java_fight.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e){
        log.warn("业务异常:{}",e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String>handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult br = e.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for(FieldError fe:br.getFieldErrors()){
            sb.append(fe.getField()).append(":").append(fe.getDefaultMessage()).append(";");
        }
        String msg = sb.toString();
        log.warn("参数校验失败：{}",msg);
        return Result.error(msg);


    }

    @ExceptionHandler(Exception.class)
    public Result<String>handleException(Exception e){
        log.error("系统异常",e);
        return Result.error("系统繁忙，请稍后重试");
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("id 格式错误：{}", e.getValue());
        return Result.error("id 格式错误");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        log.warn("id 参数不合法：{}", e.getMessage());
        return Result.error("id 必须是大于 0 的整数");
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> handleForbiddenException(ForbiddenException e){
        log.warn("越权访问:{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorizedException(UnauthorizedException e){
        log.warn("未认证:{}", e.getMessage());
        return Result.error(e.getMessage());
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e){
        log.warn("请求体格式错误:{}", e.getMessage());
        return Result.error("请求体格式错误");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e){
        log.warn("唯一键冲突:{}", e.getMessage());
        return Result.error("用户名已存在");
    }
}

