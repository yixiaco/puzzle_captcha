package com.hexm.puzzle.captcha.core;

import lombok.Data;

/**
 * 验证结果
 *
 * @author hexm
 * @date 2020/10/27 11:00
 */
@Data
public class CaptchaResult {

    private boolean success;

    private String message;
}
