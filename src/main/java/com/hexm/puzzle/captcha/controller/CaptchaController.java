package com.hexm.puzzle.captcha.controller;

import cn.hutool.core.io.resource.ResourceUtil;
import com.hexm.puzzle.captcha.core.CaptchaResult;
import com.hexm.puzzle.captcha.core.CaptchaVo;
import com.hexm.puzzle.captcha.core.PuzzleCaptcha;
import com.hexm.puzzle.captcha.util.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.Map;

/**
 * 验证码
 *
 * @author hexm
 * @date 2020/10/27 10:58
 */
@RestController
@RequestMapping("/captcha")
@Validated
public class CaptchaController {

    @Autowired
    private CaptchaUtil captchaUtil;

    @GetMapping(value = "/")
    public CaptchaVo captcha() {
        PuzzleCaptcha puzzleCaptcha = new PuzzleCaptcha(ResourceUtil.getStream("images/captcha/default.jpg"));
        puzzleCaptcha.setImageQuality(Image.SCALE_AREA_AVERAGING);
        puzzleCaptcha.run();
        return captchaUtil.captcha(puzzleCaptcha);
    }

    @PostMapping(value = "/verify")
    public CaptchaResult verify(@RequestBody Map<String, Object> map) {
        return captchaUtil.verify(map);
    }
}
