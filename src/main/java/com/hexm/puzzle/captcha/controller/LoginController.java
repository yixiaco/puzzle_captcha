package com.hexm.puzzle.captcha.controller;

import com.hexm.puzzle.captcha.core.CaptchaResult;
import com.hexm.puzzle.captcha.exception.ServerException;
import com.hexm.puzzle.captcha.redis.Cache;
import com.hexm.puzzle.captcha.redis.CacheConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 登陆控制器
 *
 * @author hexm
 * @date 2020/4/20
 */
@RestController
@RequestMapping("/system")
@Validated
public class LoginController {
    @Autowired
    private Cache<Integer> cache;

    @PostMapping("/login")
    public void login(String username, String password, HttpServletRequest request) {
        CaptchaResult result = cache.get(CacheConstant.CAPTCHA_RESULT + request.getRequestedSessionId());
        if (result == null || !result.isSuccess()) {
            throw ServerException.of("E0000", "验证失败");
        }
        cache.remove(CacheConstant.CAPTCHA_RESULT + request.getRequestedSessionId());
        // 通过验证
    }
}
