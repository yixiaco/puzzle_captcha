package com.hexm.puzzle.captcha.exception;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 异常处理类
 *
 * @author hexm
 * @date 2020/4/20
 */
@RestControllerAdvice
public class AppExceptionHandler {

    /**
     * 处理单个参数校验
     * 需要在类或方法上面加入@Validated
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationException(ConstraintViolationException e) {
        for (ConstraintViolation<?> s : e.getConstraintViolations()) {
            return new ErrorMessage("E0001", s.getMessage());
        }
        return new ErrorMessage("E0001", "错误的请求参数");
    }

    /**
     * 处理参数异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationBodyException(MethodArgumentNotValidException e) {
        for (ObjectError s : e.getBindingResult().getAllErrors()) {
            return new ErrorMessage("E0001", s.getDefaultMessage());
        }
        return new ErrorMessage("E0001", "错误的请求参数");
    }

    /**
     * 处理实体类校验,在实体类前面添加@Valid注解
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationBeanException(BindException e) {
        for (ObjectError s : e.getAllErrors()) {
            return new ErrorMessage("E0001", s.getDefaultMessage());
        }
        return new ErrorMessage("E0001", "错误的请求参数");
    }

    /**
     * 处理ServerException：业务类抛出来的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ServerException.class)
    public ErrorMessage handleServiceException(ServerException e, HttpServletResponse response) {
        response.setStatus(e.getStatusCode().value());
        Object data = e.getData();
        if (data == null) {
            return new ErrorMessage(e.getCode(), e.getMessage());
        } else {
            return new ErrorMessage(e.getCode(), e.getMessage(), data);
        }
    }

    /**
     * 处理参数传递异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorMessage handleUnProcessableServiceException(IllegalArgumentException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ErrorMessage("E0001", e.getMessage());
    }

    /**
     * 处理数据转换传递异常
     */
    @ExceptionHandler(ConversionFailedException.class)
    public ErrorMessage parseException(ConversionFailedException e, HttpServletResponse response) {
        String message = e.getMessage();
        if (e.getCause() instanceof IllegalArgumentException) {
            message = e.getCause().getMessage();
        }
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ErrorMessage("E0001", message);
    }
}
