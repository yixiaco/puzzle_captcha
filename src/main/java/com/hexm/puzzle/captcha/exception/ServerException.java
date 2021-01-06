package com.hexm.puzzle.captcha.exception;

import org.springframework.http.HttpStatus;

/**
 * 内部服务异常
 *
 * @author hexm
 * @date 2020/4/20
 */
public class ServerException extends RuntimeException {
    protected HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

    private final String code;
    /**
     * 要返回前端的数据
     */
    private Object data;

    public ServerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ServerException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public ServerException(String message) {
        super(message);
        this.code = "E0000";
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static ServerException of(String code, String message) {
        return new ServerException(code, message);
    }

    public static ServerException of(String code, String message, Object data) {
        return new ServerException(code, message, data);
    }

}
