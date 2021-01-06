package com.hexm.puzzle.captcha.exception;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 错误消息
 *
 * @author hexm
 * @date 2020/4/20
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class ErrorMessage {

    private String code;
    private String message;
    private Object data;

    public ErrorMessage() {
    }

    public ErrorMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorMessage(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
