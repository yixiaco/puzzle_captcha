package com.hexm.puzzle.captcha.util;

import cn.hutool.core.util.NumberUtil;
import com.hexm.puzzle.captcha.core.CaptchaResult;
import com.hexm.puzzle.captcha.core.CaptchaVo;
import com.hexm.puzzle.captcha.core.PuzzleCaptcha;
import com.hexm.puzzle.captcha.core.PuzzleGifCaptcha;
import com.hexm.puzzle.captcha.redis.Cache;
import com.hexm.puzzle.captcha.redis.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证码工具类
 *
 * @author hexm
 * @date 2020/10/23 16:29
 */
@Slf4j
@Component
public class CaptchaUtil {

    @Autowired
    private Cache<Object> cache;

    private static final int X_OFFSET = 8;

    private static final int SPEED = 500;

    @Autowired
    private HttpServletRequest request;

    /**
     * 获取验证码
     *
     * @return
     */
    public CaptchaVo captcha(PuzzleCaptcha puzzleCaptcha) {
        String id = request.getRequestedSessionId();
        // 删除上次验证结果
        cache.remove(CacheConstant.CAPTCHA_RESULT + id);

        Map<String, Object> cacheMap = new HashMap<>();
        CaptchaVo captchaVo = new CaptchaVo();
        captchaVo.setImage1(ImageConvertUtil.toDataUri(puzzleCaptcha.getArtwork(), "png"));
        captchaVo.setImage2(ImageConvertUtil.toDataUri(puzzleCaptcha.getVacancy(), "png"));

        // 偏移量
        cacheMap.put("x", puzzleCaptcha.getX());
        cacheMap.put("time", System.currentTimeMillis());
        cacheMap.put("width", puzzleCaptcha.getWidth());
        cache.put(CacheConstant.CAPTCHA + id, cacheMap, 5 * 60);
        return captchaVo;
    }

    /**
     * 获取验证码
     *
     * @return
     */
    public CaptchaVo captcha(PuzzleGifCaptcha puzzleCaptcha) {
        String id = request.getRequestedSessionId();
        // 删除上次验证结果
        cache.remove(CacheConstant.CAPTCHA_RESULT + id);

        Map<String, Object> cacheMap = new HashMap<>();
        CaptchaVo captchaVo = new CaptchaVo();
        captchaVo.setImage1(ImageConvertUtil.toDataUri(puzzleCaptcha.getArtwork().toByteArray(), "png"));
        captchaVo.setImage2(ImageConvertUtil.toDataUri(puzzleCaptcha.getVacancy().toByteArray(), "png"));

        // 偏移量
        cacheMap.put("x", puzzleCaptcha.getX());
        cacheMap.put("time", System.currentTimeMillis());
        cacheMap.put("width", puzzleCaptcha.getWidth());
        cache.put(CacheConstant.CAPTCHA + id, cacheMap, 5 * 60);
        return captchaVo;
    }

    /**
     * 验证码验证
     *
     * @param map
     * @return
     */
    public CaptchaResult verify(Map<String, Object> map) {
        String id = request.getRequestedSessionId();
        CaptchaResult result = new CaptchaResult();
        result.setSuccess(false);

        String key = CacheConstant.CAPTCHA + id;
        // 偏移量
        Integer vx = StrUtil.toInt(map.get("x"));
        // 宽度
        Integer width = StrUtil.toInt(map.get("width"), 1);

        //缓存
        Map<String, Object> cacheMap = cache.get(key);
        if (cacheMap == null) {
            return result;
        }
        Integer x = StrUtil.toInt(cacheMap.get("x"));
        Integer realWidth = StrUtil.toInt(cacheMap.get("width"));
        Long time = StrUtil.toLong(cacheMap.get("time"));
        long s = System.currentTimeMillis() - time;

        // 查看前端的缩放比例
        double ratio = NumberUtil.div(realWidth, width).doubleValue();

        if (x == null || vx == null) {
            cache.remove(key);
            return result;
        } else if (Math.abs(x - (vx * ratio)) > X_OFFSET * ratio || s < SPEED) {
            cache.remove(key);
            return result;
        }
        result.setSuccess(true);
        cache.remove(key);
        cache.put(CacheConstant.CAPTCHA_RESULT + id, result, 5 * 60);
        return result;
    }
}
